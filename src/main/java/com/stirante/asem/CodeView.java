package com.stirante.asem;

import com.stirante.asem.utils.AsyncTask;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Created by stirante
 */
public class CodeView extends Tab {
    private final ContextMenu context;
    private final MenuItem copyItem;
    private final File file;
    @FXML
    public StackPane content;
    private CodeArea codeArea;
    private boolean changed = false;
    private String original = "";

    public CodeView(File f) {
        if (f == null) throw new IllegalArgumentException("File cannot be null!");
        this.file = f;
        //handle tab close
        setOnCloseRequest(event -> onClose());
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
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    SyntaxHighlighter.computeHighlighting(codeArea);
                    checkChanges();
                });
        //just loading file async for smoother experience
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
        //handle context menu
        codeArea.setOnContextMenuRequested(event -> {
            context.show(codeArea, event.getScreenX(), event.getScreenY());
            String selectedText = codeArea.getSelectedText();
            copyItem.setDisable(selectedText == null || selectedText.isEmpty());
        });

        //hide context menu on click
        codeArea.setOnMouseClicked(event -> context.hide());
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
        setText(file.getName());
    }

    //checks changes between original code and the one inside editor and depending on the result changes tab title
    private void checkChanges() {
        boolean old = changed;
        changed = !codeArea.getText().replaceAll("\n", "\r\n").equals(original);
        if (changed && !old) setText(file.getName() + "*");
        else if (!changed && old) setText(file.getName());
    }

    public void save() {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            String text = codeArea.getText().replaceAll("\n", "\r\n");
            fos.write(text.getBytes());
            fos.flush();
            fos.close();
            original = text;
            changed = false;
            setText(file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClose() {
        if (changed) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved changes");
            alert.setHeaderText("You have unsaved changes in " + file.getName());
            alert.setContentText("Do you want to save it?");

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

        return file.equals(codeView.file);

    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    public String compile() {
        //process with absolute path to compiler and absolute path to asm file
        ProcessBuilder pb = new ProcessBuilder(new File("compiler/asemw.exe").getAbsolutePath(), file.getAbsolutePath());
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

    private static String getTime() {
        return SimpleDateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()));
    }

    public void undo() {
        codeArea.undo();
    }

    public void redo() {
        codeArea.redo();
    }
}
