package com.stirante.asem;

import com.stirante.asem.syntax.SyntaxAnalyzer;
import com.stirante.asem.syntax.SyntaxHighlighter;
import com.stirante.asem.utils.AsyncTask;
import com.stirante.asem.utils.Tooltips;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.MouseOverTextEvent;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class CodeView extends Tab {
    private static final Pattern WORD = Pattern.compile("[\\w.]+");
    private static int newCounter = 1;
    @FXML
    public StackPane content;
    private ContextMenu context;
    private MenuItem copyItem;
    private File file;
    private CodeArea codeArea;
    private boolean changed = false;
    private String original = "";
    private SyntaxAnalyzer.AnalysisResult syntaxAnalysis;

    public CodeView(File f) {
        this.file = f;
        //handle tab close
        setOnCloseRequest(event -> onClose());

        codeArea = new CodeArea();

        initClicks();
        initTooltips();

        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    SyntaxHighlighter.computeHighlighting(codeArea);
                    checkChanges();
                });
        loadFile();
        //load tab layout
        FXMLLoader loader = new FXMLLoader(CodeView.class.getResource("/Tab.fxml"));
        loader.setController(this);
        try {
            AnchorPane pane = loader.load();
            VirtualizedScrollPane e = new VirtualizedScrollPane<>(codeArea);
            content.getChildren().add(e);
            setContent(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file != null) {
            setText(file.getName());
        } else {
            setText("New file " + newCounter + "*");
            newCounter++;
        }
    }

    private static String getTime() {
        return SimpleDateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()));
    }

    private void loadFile() {
        //just loading file async for smoother experience
        if (file != null) {
            new AsyncTask<Void, Void, String>() {
                @Override
                public String doInBackground(Void[] params) {
                    try {
                        return new String(Files.readAllBytes(file.toPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "";
                        //TODO:Umm, handle it?
                    }
                }

                @Override
                public void onPostExecute(String result) {
                    original = result;
                    codeArea.replaceText(0, 0, result);
                    codeArea.moveTo(0);
                }
            }.execute();
        } else changed = true;
    }

    private void initClicks() {
        context = new ContextMenu();
        //copy item
        copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(codeArea.getSelectedText());
            clipboard.setContent(content);
        });
        context.getItems().addAll(copyItem);

        codeArea.setOnContextMenuRequested(event -> {
            context.show(codeArea, event.getScreenX(), event.getScreenY());
            String selectedText = codeArea.getSelectedText();
            copyItem.setDisable(selectedText == null || selectedText.isEmpty());
        });

        //hide context menu on click
        codeArea.setOnMouseClicked(event -> {
            context.hide();
            if (event.isControlDown()) {
                CharacterHit hit = codeArea.hit(event.getX(), event.getY());
                int index = hit.getInsertionIndex();
                String s = getWordAt(index);
                for (SyntaxAnalyzer.Field field : syntaxAnalysis.fields) {
                    if (field.name.equals(s)) {
                        codeArea.moveTo(codeArea.position(field.line, 0).toOffset());
                        return;
                    }
                }
                for (SyntaxAnalyzer.Routine routine : syntaxAnalysis.routines) {
                    if (routine.name.equals(s)) {
                        codeArea.moveTo(codeArea.position(routine.line, 0).toOffset());
                        return;
                    }
                }
            }
        });
    }

    private String getWordAt(int index) {
        int start = index;
        int end = index;
        Matcher matcher = WORD.matcher(codeArea.getText());
        while (matcher.find()) {
            if (matcher.start() <= index && matcher.end() >= index) {
                start = matcher.start();
                end = matcher.end();
            }
        }
        return codeArea.getText().substring(start, end);
    }

    private void initTooltips() {
        Popup popup = new Popup();
        Label popupMsg = new Label();
        popupMsg.setStyle(
                "-fx-background-color: #2e2e2e;" +
                        "-fx-text-fill: #8a8a8a;" +
                        "-fx-border-color: white;" +
                        "-fx-padding: 5;");
        popupMsg.setWrapText(true);
        popupMsg.setMaxWidth(400);
        popup.getContent().add(popupMsg);
        codeArea.setMouseOverTextDelay(Duration.ofSeconds(1));
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
            int chIdx = e.getCharacterIndex();
            Point2D pos = e.getScreenPosition();
            String s = getWordAt(chIdx);
            String s1 = Tooltips.get(s);
            if (!s1.isEmpty()) {
                popupMsg.setText(s + ": " + s1);
                popup.show(codeArea, pos.getX() + 15, pos.getY() + 15);
            } else {
                for (SyntaxAnalyzer.Field field : syntaxAnalysis.fields) {
                    if (field.name.equals(s) || s.equals("#" + field.name)) {
                        popupMsg.setText("Type: " + field.type +
                                "\nValue: " + field.address +
                                (field.comment.isEmpty() ? "" : "\n" + field.comment));
                        popup.show(codeArea, pos.getX() + 15, pos.getY() + 15);
                        return;
                    }
                }
                for (SyntaxAnalyzer.Routine routine : syntaxAnalysis.routines) {
                    if (routine.name.equals(s)) {
                        popupMsg.setText("Name: " + routine.name +
                                (routine.comment.isEmpty() ? "" : "\n" + routine.comment));
                        popup.show(codeArea, pos.getX() + 15, pos.getY() + 15);
                        return;
                    }
                }
            }
        });
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> popup.hide());
    }

    //checks changes between original code and the one inside editor and depending on the result changes tab title
    private void checkChanges() {
        syntaxAnalysis = SyntaxAnalyzer.analyze(codeArea.getText());
        if (file == null) return;
        boolean old = changed;
        changed = !codeArea.getText().replaceAll("\n", "\r\n").equals(original);
        if (changed && !old) setText(file.getName() + "*");
        else if (!changed && old) setText(file.getName());
    }

    public boolean save() {
        if (file == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("ASM file", "*.asm"),
                    new FileChooser.ExtensionFilter("All files", "*.*")
            );
            File file = fileChooser.showSaveDialog(Main.getStage());
            if (file != null) {
                this.file = file;
            } else {
                return false;
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            String text = codeArea.getText().replaceAll("\n", "\r\n");
            fos.write(text.getBytes());
            fos.flush();
            fos.close();
            original = text;
            changed = false;
            setText(file.getName());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onClose() {
        if (changed) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved changes");
            alert.setHeaderText("You have unsaved changes in tab " + getText().substring(0, getText().length() - 1));
            alert.setContentText("Do you want to save it?");
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yes, no);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == yes) {
                save();
            } else {
                //Well, nothing
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodeView codeView = (CodeView) o;
        if (file == null) return false;
        return file.equals(codeView.file);

    }

    @Override
    public int hashCode() {
        if (file == null) return super.hashCode();
        return file.hashCode();
    }

    public String compile() {
        //process with absolute path to compiler and absolute path to asm file
        ProcessBuilder pb = new ProcessBuilder(new File("bin/asemw.exe").getAbsolutePath(), file.getAbsolutePath());
        //set working directory to the one containing asm file (fixes MCU files missing)
        pb.directory(file.getParentFile());
        try {
            //start process and wait for it't end
            Process process = pb.start();
            int code = process.waitFor();

            //grab output and return it
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(getTime()).append("] Compiling ").append(file.getAbsolutePath()).append('\n');
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            sb.append("\n[").append(getTime()).append("] Compiler terminated with code ").append(code);
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to run compiler!\n" + e.getMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "Failed to run compiler!\n" + e.getMessage();
        }
    }

    public void undo() {
        codeArea.undo();
    }

    public void redo() {
        codeArea.redo();
    }

    public void insert(String str) {
        codeArea.insertText(codeArea.getCaretPosition(), str);
    }

    public String run() {
        //find hex file
        File hex = new File(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf('.')) + ".hex");
        if (!hex.exists()) return "You need to compile file first!";
        //process with absolute path to compiler and absolute path to asm file
        ProcessBuilder pb = new ProcessBuilder(new File("bin/DSM-51_Any_CPU.exe").getAbsolutePath(), hex.getAbsolutePath());
        //set working directory to the one containing asm file (fixes MCU files missing)
        pb.directory(file.getParentFile());
        try {
            //start process
            pb.start();
            return "Simulator started with file " + hex.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to run emulator!\n" + e.getMessage();
        }
    }
}
