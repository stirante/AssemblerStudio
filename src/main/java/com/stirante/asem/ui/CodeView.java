package com.stirante.asem.ui;

import com.stirante.asem.Constants;
import com.stirante.asem.Main;
import com.stirante.asem.syntax.SyntaxAnalyzer;
import com.stirante.asem.syntax.SyntaxHighlighter;
import com.stirante.asem.syntax.code.FieldElement;
import com.stirante.asem.syntax.code.RoutineElement;
import com.stirante.asem.ui.tooltip.TooltipPopup;
import com.stirante.asem.utils.AsyncTask;
import com.stirante.asem.utils.DelayedTask;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.fxmisc.richtext.model.TwoDimensional;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * Created by stirante
 */
public class CodeView extends Tab {
    private static int newCounter = 1;
    private final TooltipPopup tooltipPopup;
    @FXML
    public StackPane content;
    private ContextMenu context;
    private MenuItem copyItem;
    private File file;
    private CodeArea codeArea;
    private boolean changed = false;
    private String original = "";
    private SyntaxAnalyzer.AnalysisResult syntaxAnalysis;
    private AutocompletionPopup autocompletionPopup;
    private SyntaxHighlighter highlighter;
    private DelayedTask elementHighlightTask;
    private DelayedTask syntaxHighlightTask;

    public CodeView(Main app, File f) {
        this.file = f;
        //handle tab close
        setOnCloseRequest(event -> onClose());

        codeArea = new CodeArea();

        highlighter = new SyntaxHighlighter(this, codeArea);
        autocompletionPopup = new AutocompletionPopup(this, codeArea);
        tooltipPopup = new TooltipPopup(this, codeArea);
        elementHighlightTask = new DelayedTask(500L);
        syntaxHighlightTask = new DelayedTask(300L);

        checkChanges();
        initMouse();
        initKeyboard();

        codeArea.setStyle("-fx-font-family: " + Settings.getInstance().getFont().getFamily() + ";-fx-font-size: " + Settings.getInstance().getFont().getSize() + ";");
        Settings.getInstance().fontProperty().addListener((observable, oldValue, newValue) -> codeArea.setStyle("-fx-font-family: " + newValue.getFamily() + ";-fx-font-size: " + newValue.getSize() + ";"));
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            checkChanges();
            computeHighlighting();
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

    private void computeHighlighting() {
        syntaxHighlightTask.start(() -> highlighter.computeHighlighting());
    }

    private void initKeyboard() {
        codeArea.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.SPACE) {
                autocompletionPopup.triggerAutocompletion();
                event.consume();
            } else if (event.isControlDown() && event.getCode() == KeyCode.SLASH) {
                triggerComment();
                event.consume();
            } else if (event.getCode() == KeyCode.CONTROL && Settings.getInstance().isExperimental()) {
                highlighter.setShowClickables(true);
                computeHighlighting();
            } else if (event.getCode() == KeyCode.ENTER) {
                int l = codeArea.offsetToPosition(codeArea.getCaretPosition(), TwoDimensional.Bias.Forward).getMajor() - 1;
                int start = codeArea.position(l, 0).toOffset();
                int end;
                int diff = 0;
                try {
                    end = codeArea.position(l + 1, 0).toOffset() - 1;
                } catch (Exception e) {
                    end = codeArea.getLength();
                }
                String line = codeArea.getText().substring(start, end);
                Matcher matcher = Constants.WHITESPACE.matcher(line);
                if (matcher.matches()) {
                    String str = matcher.group(1);
                    codeArea.insertText(codeArea.getCaretPosition(), str);
                }
            }
        });
        codeArea.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.CONTROL && Settings.getInstance().isExperimental()) {
                highlighter.setShowClickables(false);
                computeHighlighting();
            }
        });
    }

    private void triggerComment() {
        ArrayList<Integer> lines = new ArrayList<>();
        boolean wasSelected = false;
        int start;
        int end = 0;
        if (codeArea.selectedTextProperty().getValue().isEmpty()) {
            lines.add(codeArea.offsetToPosition(codeArea.getCaretPosition(), TwoDimensional.Bias.Forward).getMajor());
            start = codeArea.getCaretPosition();
        } else {
            wasSelected = true;
            start = codeArea.selectionProperty().getValue().getStart();
            end = codeArea.selectionProperty().getValue().getEnd();
            int startLine = codeArea.offsetToPosition(start, TwoDimensional.Bias.Forward).getMajor();
            int endLine = codeArea.offsetToPosition(end, TwoDimensional.Bias.Forward).getMajor();
            for (int i = startLine; i <= endLine; i++) {
                lines.add(i);
            }
        }
        highlighter.setPause(true);
        final int[] diff = {0};
        final int[] first = {0};
        lines.forEach(integer -> {
            int i = commentLine(integer);
            diff[0] += i;
            if (first[0] == 0) first[0] = i;
        });
        highlighter.setPause(false);
        checkChanges();
        computeHighlighting();
        if (wasSelected) {
            end += diff[0];
            codeArea.selectRange(start + first[0], end);
        } else {
            start += diff[0];
            codeArea.moveTo(start);
            codeArea.requestFollowCaret();
        }
    }

    private int commentLine(int l) {
        int start = codeArea.position(l, 0).toOffset();
        int end;
        int diff = 0;
        try {
            end = codeArea.position(l + 1, 0).toOffset() - 1;
        } catch (Exception e) {
            end = codeArea.getLength();
        }
        String line = codeArea.getText().substring(start, end);
        if (Constants.IS_COMMENTED.matcher(line).matches()) {
            line = line.replaceFirst(";", "");
            diff = -1;
        } else {
            Matcher matcher = Constants.TO_COMMENT.matcher(line);
            if (matcher.find()) {
                line = matcher.group(1) + ";" + matcher.group(2);
                diff = 1;
            }
        }
        codeArea.replaceText(start, end, line);
        return diff;
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
                    }
                }

                @Override
                public void onPostExecute(String result) {
                    original = result;
                    codeArea.replaceText(0, 0, result);
                    codeArea.moveTo(0);
                    codeArea.requestFollowCaret();
                    codeArea.getUndoManager().forgetHistory();
                    codeArea.getUndoManager().mark();
                    checkChanges();
                }
            }.execute();
        } else changed = true;
    }

    private void initMouse() {
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

        codeArea.setOnMouseClicked(event -> {
            context.hide();
            autocompletionPopup.hide();
            if (event.isControlDown()) {
                triggerGoTo(event);
            }
        });

        codeArea.setMouseOverTextDelay(Duration.ofMillis(700));
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, tooltipPopup::triggerTooltip);
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> tooltipPopup.hide());

        codeArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (Settings.getInstance().isExperimental())
                elementHighlightTask.start(() -> {
                    String wordAt = getWordAt(newValue);
                    String old = highlighter.getHighlightWord();
                    for (RoutineElement element : syntaxAnalysis.getRoutines()) {
                        if (element.getName().equals(wordAt)) {
                            highlighter.setHighlightWord(wordAt);
                            computeHighlighting();
                            return;
                        }
                    }
                    for (FieldElement element : syntaxAnalysis.getFields()) {
                        if (element.getName().equals(wordAt)) {
                            highlighter.setHighlightWord(wordAt);
                            computeHighlighting();
                            return;
                        }
                    }
                    highlighter.setHighlightWord("");
                    if (!old.isEmpty()) computeHighlighting();
                });
        });
    }

    private void triggerGoTo(MouseEvent event) {
        CharacterHit hit = codeArea.hit(event.getX(), event.getY());
        int index = hit.getInsertionIndex();
        String s = getWordAt(index);
        for (FieldElement field : syntaxAnalysis.getFields()) {
            if (field.matches(s, 0, 0)) {
                codeArea.moveTo(field.getDefinitionStart());
                codeArea.requestFollowCaret();
                return;
            }
        }
        for (RoutineElement routine : syntaxAnalysis.getRoutines()) {
            if (routine.matches(s, 0, 0)) {
                codeArea.moveTo(routine.getDefinitionStart());
                codeArea.requestFollowCaret();
                return;
            }
        }
    }

    public String getWordAt(int index) {
        int start = index;
        int end = index;
        Matcher matcher = Constants.WORD.matcher(codeArea.getText());
        while (matcher.find()) {
            if (matcher.start() <= index && matcher.end() >= index) {
                start = matcher.start();
                end = matcher.end();
            }
        }
        return codeArea.getText().substring(start, end);
    }

    //checks changes between original code and the one inside editor and depending on the result changes tab title
    private void checkChanges() {
        syntaxAnalysis = SyntaxAnalyzer.analyze(codeArea.getText());
        if (autocompletionPopup.isAutocompletion()) {
            autocompletionPopup.onChanges();
        }
        if (file == null) return;
        boolean old = changed;
        changed = !codeArea.getText().replaceAll("\n", "\r\n").equals(original);
        if (changed && !old) setText(file.getName() + "*");
        else if (!changed && old) setText(file.getName());
    }

    public boolean save() {
        if (file == null) {
            //it's a new file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save file");
            String path = Settings.getInstance().getLastPath();
            if (path != null && !path.isEmpty()) fileChooser.setInitialDirectory(new File(path));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("ASM file", "*.asm"),
                    new FileChooser.ExtensionFilter("All files", "*.*")
            );
            File file = fileChooser.showSaveDialog(Main.getStage());
            if (file != null) {
                Settings.getInstance().setLastPath(file.getParentFile().getAbsolutePath());
                this.file = file;
            } else {
                //user don't want to save so return no success
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
            alert.setTitle("Unsaved changes in tab " + getText().substring(0, getText().length() - 1));
            alert.setHeaderText(null);
            alert.setContentText("Do you want to save it?");
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yes, no);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == yes) {
                save();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodeView codeView = (CodeView) o;
        return file != null && file.equals(codeView.file);

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
            br.close();
            sb.append("\n[").append(getTime()).append("] Compiler terminated with code ").append(code);
            return sb.toString();
        } catch (IOException | InterruptedException e) {
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

    public String sendHex() {
        //find hex file
        File hex = new File(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf('.')) + ".hex");
        if (!hex.exists()) return "You need to compile file first!";
        String ini = "nastawy programu Hex_Sender\r\n" +
                hex.getParentFile().getAbsolutePath() + "\\\r\n" +
                "1\r\n" +
                hex.getName() + "\r\n";
        File iniFile = new File("bin/hex_sender.ini");
        try {
            iniFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(iniFile);
            fos.write(ini.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to set config file!\n" + e.getMessage();
        }
        File file = new File("bin/hex_sender.exe");
        ProcessBuilder pb = new ProcessBuilder(file.getAbsolutePath());
        pb.directory(file.getParentFile());
        try {
            //start process
            pb.start();
            return "Hex sender started with file " + hex.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to run hex sender!\n" + e.getMessage();
        }
    }

    void goToLine(int line) {
        codeArea.moveTo(codeArea.position(line - 1, 0).toOffset());
        codeArea.requestFollowCaret();
    }

    public String find(String text) {
        int start = codeArea.selectedTextProperty().getValue().isEmpty() ? codeArea.getCaretPosition() : codeArea.selectionProperty().getValue().getEnd();
        int i = codeArea.getText().toLowerCase().indexOf(text.toLowerCase(), start);
        if (i == -1) {
            i = codeArea.getText().toLowerCase().indexOf(text.toLowerCase());
            if (i == -1)
                return "Not found!";
            else {
                codeArea.selectRange(i, text.length() + i);
                codeArea.requestFollowCaret();
                return "";
            }
        } else {
            codeArea.selectRange(i, text.length() + i);
            codeArea.requestFollowCaret();
            return "";
        }
    }

    public String getSelectedText() {
        return codeArea.getSelectedText();
    }

    public String replace(String text, String replacement) {
        if (getSelectedText().isEmpty() || !getSelectedText().equalsIgnoreCase(text)) return "Not found!";
        codeArea.replaceText(codeArea.selectionProperty().getValue().getStart(), codeArea.selectionProperty().getValue().getEnd(), replacement);
        find(text);
        return "";
    }

    public SyntaxAnalyzer.AnalysisResult getSyntaxAnalysis() {
        return syntaxAnalysis;
    }
}
