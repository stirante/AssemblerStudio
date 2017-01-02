package com.stirante.asem.ui;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

/**
 * Created by stirante
 */
public class ChangelogDialog {

    private static final String CONTENT = "";

    public static void show() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Changelog");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(SegmentCreator.class.getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Label l = new Label(CONTENT);
        ScrollPane scroll = new ScrollPane(l);
        scroll.setPrefWidth(500);
        scroll.setPrefHeight(800);
        l.setPrefWidth(400);
        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
    }

}
