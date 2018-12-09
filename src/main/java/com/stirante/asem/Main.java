package com.stirante.asem;

import com.stirante.asem.ui.*;
import com.stirante.asem.utils.AsyncTask;
import com.stirante.asem.utils.UpdateUtil;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stirante
 */
public class Main extends Application {

    private static Stage stage;

    //Elements from layout
    @FXML
    public TabPane tabs;
    @FXML
    public BorderPane result;
    @FXML
    public MenuItem saveMenuItem;
    @FXML
    public Menu byteMenu;
    @FXML
    public MenuItem newMenuItem;
    @FXML
    public MenuItem openMenuItem;
    @FXML
    public MenuItem closeMenuItem;
    @FXML
    public MenuItem undoMenuItem;
    @FXML
    public MenuItem redoMenuItem;
    @FXML
    public MenuItem segmentCreatorItem;
    @FXML
    public MenuItem lcdCreatorItem;

    public CompileOutputView compileResult;
    private ByteCreator byteCreator;
    private FindDialog findDialog;
    private ReplaceDialog replaceDialog;

    public static Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //load layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        loader.setController(this);
        VBox root = loader.load();
        Scene scene = new Scene(root, 1280, 720);
        //set stylesheet
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Assembler Studio");
        //catch close request and check for unsaved files
        primaryStage.setOnCloseRequest(event -> tabs.getTabs().forEach(tab -> ((CodeView) tab).onClose()));
        //disable menu items if there is no active tab
        tabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            saveMenuItem.setDisable(newValue == null);
            byteMenu.setDisable(newValue == null);
            segmentCreatorItem.setDisable(newValue == null);
            lcdCreatorItem.setDisable(newValue == null);
            compileResult.setText("");
        });
        //add keyboard shortcuts
        saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        newMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        undoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        redoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        //segmentCreatorItem.setAccelerator(new KeyCodeCombination(KeyCode.INSERT, KeyCombination.CONTROL_DOWN));
        //lcdCreatorItem.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN));
        root.setOnKeyPressed(event -> {
            // disable menu focus on alt
            if (event.isAltDown()) {
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.F9) onCompileClicked();
            else if (event.getCode() == KeyCode.F10) onRunClicked();
            else if (event.isControlDown() && event.getCode() == KeyCode.F) {
                if (hasOpenTab()) {
                    String selectedText = getOpenTab().getSelectedText();
                    findDialog.show(selectedText.isEmpty() ? null : selectedText);
                }
            } else if (event.isControlDown() && event.getCode() == KeyCode.R) {
                if (hasOpenTab()) {
                    String selectedText = getOpenTab().getSelectedText();
                    replaceDialog.show(selectedText.isEmpty() ? null : selectedText);
                }
            }
        });
        primaryStage.show();
        stage = primaryStage;
        //Initialize dialogs
        findDialog = new FindDialog(this);
        replaceDialog = new ReplaceDialog(this);
        byteCreator = new ByteCreator();
        //handle result
        compileResult = new CompileOutputView(this);
        result.setCenter(new VirtualizedScrollPane<>(compileResult));
        //handle DnD
        root.setOnDragOver(event -> {
            if (event.getGestureSource() != root && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        root.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            System.out.println(db.getFiles());
            boolean success = false;
            if (db.hasFiles()) {
                db.getFiles().forEach(this::openFile);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
        //load file from startup parameters
        List<String> args = getParameters().getUnnamed();
        if (!args.isEmpty()) openFile(new File(args.get(0)));
        //check updates
        try {
            UpdateUtil.check(this);
        } catch (Exception e) {
            //don't want updates to ruin whole app
            e.printStackTrace();
        }
    }

    //actually open tab for file, not file itself
    private void openFile(File f) {
        Tab tab = new CodeView(this, f);
        if (!tabs.getTabs().contains(tab)) {
            tabs.getTabs().add(tab);
            tabs.getSelectionModel().select(tab);
        } else {
            tabs.getSelectionModel().select(tab);
        }
    }

    public void onOpenClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        String path = Settings.getInstance().getLastPath();
        if (path != null && !path.isEmpty()) {
            File value = new File(path);
            if (value.exists())
                fileChooser.setInitialDirectory(value);
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ASM file", "*.asm"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File f = fileChooser.showOpenDialog(stage);
        if (f != null) {
            Settings.getInstance().setLastPath(f.getParentFile().getAbsolutePath());
            openFile(f);
        }
    }

    public void onCloseClicked() {
        stage.close();
    }

    public void onAboutClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        alert.setTitle("About");
        alert.setHeaderText("Assembler Studio v." + Constants.VERSION);
        alert.setContentText("Author: Piotr \"stirante\" Brzozowski");
        alert.showAndWait();
    }

    public void onDonateClicked() {
        getHostServices().showDocument("https://paypal.me/stirante");
    }

    public void onCompileClicked() {
        if (hasOpenTab()) {
            if (!getOpenTab().save()) {
                compileResult.setText("You need to save file first!");
                return;
            }
            //compile async
            new AsyncTask<Void, Void, String>() {
                @Override
                public String doInBackground(Void[] params) {
                    return getOpenTab().compile();
                }

                @Override
                public void onPostExecute(String compileResult) {
                    Main.this.compileResult.setText(compileResult);
                }
            }.execute();
        }
    }

    public void onSendClicked() {
        if (hasOpenTab()) {
            if (!getOpenTab().save()) {
                compileResult.setText("You need to save and compile file first!");
                return;
            }
            compileResult.setText(getOpenTab().sendHex());
        }
    }

    public String onSetBiosClicked() {
        File file = new File("bin/set_bios.exe");
        ProcessBuilder pb = new ProcessBuilder(file.getAbsolutePath());
        pb.directory(file.getParentFile());
        try {
            //start process
            pb.start();
            return "Set bios started";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to run set bios!\n" + e.getMessage();
        }
    }

    public void onSaveClicked() {
        if (hasOpenTab()) {
            getOpenTab().save();
        }
    }

    public void undo() {
        if (hasOpenTab()) {
            getOpenTab().undo();
        }
    }

    public void redo() {
        if (hasOpenTab()) {
            getOpenTab().redo();
        }
    }

    public boolean hasOpenTab() {
        return tabs.getSelectionModel().getSelectedItem() != null;
    }

    public CodeView getOpenTab() {
        return (CodeView) tabs.getSelectionModel().getSelectedItem();
    }

    public void byteCreatorTmod() {
        if (hasOpenTab()) {
            ArrayList<String> descs = new ArrayList<>();
            descs.add(0, "Timer 0\n" +
                    "\n" +
                    "M0\n" +
                    "The last bits third and fourth are known as M1 and M0 respectively. These are used to select the timer mode.");
            descs.add(1, "Timer 0\n" +
                    "\n" +
                    "M1\n" +
                    "The last bits third and fourth are known as M1 and M0 respectively. These are used to select the timer mode.");
            descs.add(2, "Timer 0\n" +
                    "\n" +
                    "C/T\n" +
                    "C/T bit is used to decide whether a timer is used as a time delay generator or an event counter. If this bit is 0 then it is used as a timer and if it is 1 then it is used as a counter.");
            descs.add(3, "Timer 0\n" +
                    "\n" +
                    "GATE\n" +
                    "The hardware way of starting and stopping the timer by an external source is achieved by making GATE=1 in the TMOD register. And if we change to GATE=0 then we do no need external hardware to start and stop the timers.");
            descs.add(4, "Timer 1\n" +
                    "\n" +
                    "M0\n" +
                    "The last bits third and fourth are known as M1 and M0 respectively. These are used to select the timer mode.");
            descs.add(5, "Timer 1\n" +
                    "\n" +
                    "M1\n" +
                    "The last bits third and fourth are known as M1 and M0 respectively. These are used to select the timer mode.");
            descs.add(6, "Timer 1\n" +
                    "\n" +
                    "C/T\n" +
                    "C/T bit is used to decide whether a timer is used as a time delay generator or an event counter. If this bit is 0 then it is used as a timer and if it is 1 then it is used as a counter.");
            descs.add(7, "Timer 1\n" +
                    "\n" +
                    "GATE\n" +
                    "The hardware way of starting and stopping the timer by an external source is achieved by making GATE=1 in the TMOD register. And if we change to GATE=0 then we do no need external hardware to start and stop the timers.");
            String bits = byteCreator.create("TMOD", descs);
            getOpenTab().insert(bits);
        }
    }

    public void byteCreatorTcon() {
        if (hasOpenTab()) {
            ArrayList<String> descs = new ArrayList<>();
            descs.add(0, "IT0\n" +
                    "External interrupt 0 signal type control bit. Same as IT0.");
            descs.add(1, "IE0\n" +
                    "External interrupt 0 Edge flag. Not related to timer operations.");
            descs.add(2, "IT1\n" +
                    "External interrupt1 signal type control bit. Set to 1 by program to enable external interrupt 1 to be triggered by a falling edge signal. Set to 0 by program to enable a low level signal on external interrupt1 to generate an interrupt.");
            descs.add(3, "IE1\n" +
                    "External interrupt 1 Edge flag. Not related to timer operations.");
            descs.add(4, "TR0\n" +
                    "Timer 0 run control bit.  Same as TR1.");
            descs.add(5, "TF0\n" +
                    "Timer 0 over flow flag. Same as TF1.");
            descs.add(6, "TR1\n" +
                    "Timer 1 run control bit. Set to 1 by programmer to enable timer to count; Cleared to 0 by program to halt timer.");
            descs.add(7, "TF1\n" +
                    "Timer1 over flow flag. Set when timer rolls from all 1s to 0. Cleared when the processor vectors to execute interrupt service routine. Located at program address 001Bh.");
            String bits = byteCreator.create("TCON", descs);
            getOpenTab().insert(bits);
        }
    }

    public void byteCreatorIe() {
        if (hasOpenTab()) {
            ArrayList<String> descs = new ArrayList<>();
            descs.add(0, "EX0\n" +
                    "Enable External 0 Interrupt");
            descs.add(1, "ET0\n" +
                    "Enable Timer 0 Interrupt");
            descs.add(2, "EX1\n" +
                    "Enable External 1 Interrupt");
            descs.add(3, "ET1\n" +
                    "Enable Timer 1 Interrupt");
            descs.add(4, "ES\n" +
                    "Enable Serial Interrupt");
            descs.add(5, "Undefined");
            descs.add(6, "Undefined");
            descs.add(7, "EA\n" +
                    "Global Interrupt Enable/Disable");
            String bits = byteCreator.create("IE", descs);
            getOpenTab().insert(bits);
        }
    }

    public void onNewClicked() {
        openFile(null);
    }

    public void onRunClicked() {
        if (hasOpenTab()) {
            if (!getOpenTab().save()) {
                compileResult.setText("You need to save and compile file first!");
                return;
            }
            compileResult.setText(getOpenTab().run());
        }
    }

    public void segmentCreator() {
        if (hasOpenTab()) {
            String bits = SegmentCreator.create();
            getOpenTab().insert(bits);
        }
    }

    public void onSettingsClicked() {
        Settings.getInstance().show();
    }

    public void onKeyboardHelpClicked() {
        HelpDialog.show();
    }

    public void onForceUpdateClicked() {
        try {
            Runtime.getRuntime().exec("java -jar SimpleUpdater.jar check .");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void lcdCreator() {
        if (hasOpenTab()) {
            String bits = LcdCreator.create();
            getOpenTab().insert(bits);
        }
    }
}
