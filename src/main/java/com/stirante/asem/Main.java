package com.stirante.asem;

import com.stirante.asem.utils.AsyncTask;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Created by stirante
 */
public class Main extends Application {

    public static final String VERSION = "1.0";

    //Elements from layout
    @FXML
    public TabPane tabs;
    @FXML
    public TextArea result;
    @FXML
    public MenuItem saveMenuItem;

    private Stage stage;


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
        primaryStage.setTitle("AssemblerView51");
        //catch close request and check for unsaved files
        primaryStage.setOnCloseRequest(event -> tabs.getTabs().forEach(tab -> ((CodeView) tab).onClose()));
        //disable save menu item if there is no active tab
        tabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> saveMenuItem.setDisable(newValue == null));
        primaryStage.show();
        stage = primaryStage;
        //load file from startup parameters
        List<String> args = getParameters().getUnnamed();
        if (!args.isEmpty()) openFile(new File(args.get(0)));
    }

    //actually open tab for file, not file itself
    private void openFile(File f) {
        Tab tab = new CodeView(f);
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
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ASM file", "*.asm"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File f = fileChooser.showOpenDialog(stage);
        if (f != null)
            openFile(f);
    }

    public void onCloseClicked() {
        stage.close();
    }

    public void onAboutClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("AssemblerEditor51 v." + VERSION);
        alert.setContentText("Author: Piotr Brzozowski");
        alert.showAndWait();
    }

    public void onCompileClicked() {
        final CodeView selectedItem = (CodeView) tabs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedItem.save();
            new AsyncTask<Void, Void, String>() {
                @Override
                public String doInBackground(Void[] params) {
                    return selectedItem.compile();
                }

                @Override
                public void onPostExecute(String compileResult) {
                    result.setText(compileResult);
                }
            }.execute();
        }
    }

    public void onSaveClicked() {
        final CodeView selectedItem = (CodeView) tabs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedItem.save();
        }
    }

    public void undo() {
        final CodeView selectedItem = (CodeView) tabs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedItem.undo();
        }
    }

    public void redo() {
        final CodeView selectedItem = (CodeView) tabs.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedItem.redo();
        }
    }
}
