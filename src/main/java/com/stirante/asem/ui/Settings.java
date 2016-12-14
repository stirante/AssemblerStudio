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
import javafx.scene.text.Font;

import java.util.HashMap;

/**
 * Created by stirante
 */
public class Settings {

    private static Settings instance;
    private final FontPicker font;
    private final CheckBox update;
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
        setCheckingUpdate(update.isSelected());
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

    public boolean isCheckingUpdate() {
        if (!map.containsKey("update_check")) return true;
        return (boolean) map.get("update_check");
    }

    public void setCheckingUpdate(boolean value) {
        map.put("update_check", value);
    }
}
