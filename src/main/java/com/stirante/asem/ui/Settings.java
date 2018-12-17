package com.stirante.asem.ui;

import com.fo.controls.fontpicker.FontPicker;
import com.stirante.asem.utils.ConfigManager;
import com.stirante.asem.utils.SerializableFont;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by stirante
 */
@SuppressWarnings("unchecked")
public class Settings {

    private static Settings instance;
    private final FontPicker font;
    private final CheckBox update;
    private final CheckBox experiments;
    private final TextField assemblerFile;
    private final Button assemblerFileButton;
    private final TextField emulatorFile;
    private final Button emulatorFileButton;
    private final VBox monoErrorBox;
    private String monoPath;
    private VBox node;
    private HashMap<String, Object> map;
    private ObjectProperty<Font> fontProperty;

    public Settings() {
        ConfigManager.load();
        map = ConfigManager.getMap();
        node = new VBox(10);
        node.setPrefWidth(300);
        Font f = getFont();
        fontProperty = new SimpleObjectProperty<>(f);
        font = new FontPicker();
        font.setValue(f);
        HBox fontBox = new HBox();
        fontBox.setAlignment(Pos.CENTER);
        Label l = new Label("Font: ");
        fontBox.getChildren().addAll(l, font);
        node.getChildren().add(fontBox);

        update = new CheckBox();
        update.setSelected(isCheckingUpdate());
        HBox updateBox = new HBox();
        updateBox.setAlignment(Pos.CENTER);
        l = new Label("Check updates: ");
        updateBox.getChildren().addAll(l, update);
        node.getChildren().add(updateBox);

        experiments = new CheckBox();
        experiments.setSelected(isExperimental());
        HBox experimentsBox = new HBox();
        experimentsBox.setAlignment(Pos.CENTER);
        l = new Label("Enable experiments: ");
        experimentsBox.getChildren().addAll(l, experiments);
        node.getChildren().add(experimentsBox);

        if (!map.containsKey("assembler-file")) {
            map.put("assembler-file", "bin/asemw.exe");
        }

        assemblerFile = new TextField();
        assemblerFile.setText((String) map.get("assembler-file"));
        HBox assemblerFileBox = new HBox();
        assemblerFileBox.setAlignment(Pos.CENTER);
        l = new Label("Assembler path: ");
        assemblerFileButton = new Button();
        assemblerFileButton.setText("Browse");
        assemblerFileBox.getChildren().addAll(l, assemblerFile, assemblerFileButton);
        node.getChildren().add(assemblerFileBox);

        if (!map.containsKey("emulator-file")) {
            map.put("emulator-file", "bin/DSM-51_Any_CPU.exe");
        }

        emulatorFile = new TextField();
        emulatorFile.setText((String) map.get("emulator-file"));
        HBox emulatorFileBox = new HBox();
        emulatorFileBox.setAlignment(Pos.CENTER);
        l = new Label("Emulator path:");
        emulatorFileButton = new Button();
        emulatorFileButton.setText("Browse");
        emulatorFileBox.getChildren().addAll(l, emulatorFile, emulatorFileButton);
        node.getChildren().add(emulatorFileBox);

        CheckBox useMonoCheckBox = new CheckBox();
        l = new Label("Use mono: ");
        HBox useMonoBox = new HBox();
        useMonoBox.setAlignment(Pos.CENTER);
        useMonoBox.getChildren().addAll(l, useMonoCheckBox);
        node.getChildren().add(useMonoBox);
        monoPath = null;
        Label monoErrorLabel = new Label();
        monoErrorLabel.setTextFill(Color.RED);
        monoErrorLabel.setText("Could not find mono. Install it first.");
        monoErrorBox = new VBox();
        monoErrorBox.setAlignment(Pos.CENTER);
        monoErrorBox.getChildren().add(monoErrorLabel);

        if (map.containsKey("monopath") && !((String) map.get("monopath")).isEmpty()) {
            monoPath = (String) map.get("monopath");

            if (!new File(monoPath).exists()) {
                monoPath = null;
            } else {
                useMonoCheckBox.setSelected(true);
            }
        }

        useMonoCheckBox.setOnAction(e -> {
            node.getChildren().remove(monoErrorBox);

            if (useMonoCheckBox.isSelected()) {
                try {
                    Process p = new ProcessBuilder("which", "mono").start();
                    p.waitFor();

                    if (p.exitValue() == 0) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        monoPath = br.readLine();
                        map.put("monopath", monoPath);
                    } else {
                        monoPath = null;
                        node.getChildren().add(monoErrorBox);
                        useMonoCheckBox.setSelected(false);
                    }
                } catch (IOException | InterruptedException e1){
                    e1.printStackTrace();
                    monoPath = null;
                }
            }
        });
    }

    public static Settings getInstance() {
        if (instance == null) instance = new Settings();
        return instance;
    }

    public void show() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Settings");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton);

        assemblerFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName((String) map.get("assembler-file"));
            File file = fileChooser.showOpenDialog(dialog.getOwner());

            if (file != null) {
                map.put("assembler-file", file.getAbsolutePath());
                assemblerFile.setText(file.getAbsolutePath());
            }
        });

        emulatorFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName((String) map.get("emulator-file"));
            File file = fileChooser.showOpenDialog(dialog.getOwner());

            if (file != null) {
                map.put("emulator-file", file.getAbsolutePath());
                emulatorFile.setText(file.getAbsolutePath());
            }
        });

        dialog.getDialogPane().setContent(node);
        dialog.showAndWait();
        setFont(font.getValue());
        setCheckingUpdate(update.isSelected());
        setExperimental(experiments.isSelected());
        ConfigManager.save();
    }

    public Font getFont() {
        SerializableFont font = (SerializableFont) map.get("font");
        return font != null ? font.getFont() : Font.font("Consolas");
    }

    public void setFont(Font f) {
        fontProperty.setValue(f);
        map.put("font", new SerializableFont(f));
    }

    public ObjectProperty<Font> fontProperty() {
        return fontProperty;
    }

    public String getLastPath() {
        return (String) map.get("last_path");
    }

    public void setLastPath(String p) {
        map.put("last_path", p);
        ConfigManager.save();
    }

    public boolean isCheckingUpdate() {
        return !map.containsKey("update_check") || (boolean) map.get("update_check");
    }

    public void setCheckingUpdate(boolean value) {
        map.put("update_check", value);
    }

    public boolean isExperimental() {
        return map.containsKey("enable_experiments") && (boolean) map.get("enable_experiments");
    }

    public void setExperimental(boolean value) {
        map.put("enable_experiments", value);
    }

    public Optional<String> getMonoPath() {
        return Optional.ofNullable(monoPath);
    }

    public String getEmulatorPath() {
        return emulatorFile.getText();
    }
}
