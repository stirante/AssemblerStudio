package com.stirante.asem.ui;

import com.fo.controls.fontpicker.FontPicker;
import com.stirante.asem.utils.ConfigManager;
import com.stirante.asem.utils.SerializableFont;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.HashMap;

/**
 * Created by stirante
 */
public class Settings {

    private static Settings instance;
    private final FontPicker font;
    private VBox node;
    private HashMap<String, Object> map;
    private ObjectProperty<Font> fontProperty;

    public Settings() {
        ConfigManager.load();
        map = ConfigManager.getMap();
        node = new VBox();
        Font font = getFont();
        fontProperty = new SimpleObjectProperty<>(font);
        System.out.println(font);
        this.font = new FontPicker();
        this.font.setValue(font);
        node.getChildren().add(this.font);
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

        dialog.getDialogPane().setContent(node);
        dialog.showAndWait();
        setFont(font.getValue());
        ConfigManager.save();
    }

    public Font getFont() {
        SerializableFont font = (SerializableFont) map.get("font");
        return font != null ? font.getFont() : Font.getDefault();
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
}
