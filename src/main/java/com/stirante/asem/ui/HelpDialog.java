package com.stirante.asem.ui;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;

/**
 * Created by stirante
 */
public class HelpDialog {

    private static final String CONTENT = "[CTRL + /]\t\t\tToggle line comment\n" +
            "[CTRL + LEFT MOUSE BUTTON]\tWhen clicked on recognized part of code, moves file to definition\n" +
            "[F9]\t\t\t\tCompile\n" +
            "[F10]\t\t\t\tRun\n" +
            "[CTRL + SPACE]\t\t\tSmart suggestions\n" +
            "[CTRL + F]\t\t\tFind\n" +
            "{CTRL + R]\t\t\tReplace";

    public static void show() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Keyboard help");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(SegmentCreator.class.getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Label l = new Label(CONTENT);
        l.setStyle("-fx-font-family: Consolas");//This font at least makes tabs align
        dialog.getDialogPane().setContent(l);
        dialog.showAndWait();
    }

}
