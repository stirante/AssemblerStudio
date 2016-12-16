package com.stirante.asem.ui;

import com.stirante.asem.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by stirante
 */
public class ReplaceDialog {

    @FXML
    public TextField findField;
    @FXML
    public TextField replaceField;
    @FXML
    public Label status;
    private VBox node;
    private Main app;

    public ReplaceDialog(Main app) {
        this.app = app;
        FXMLLoader loader = new FXMLLoader(ReplaceDialog.class.getResource("/ReplaceDialog.fxml"));
        loader.setController(this);
        try {
            node = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void show(String initialText) {
        if (initialText != null) findField.setText(initialText);

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Replace");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(SegmentCreator.class.getResource("/style.css").toExternalForm());
        ButtonType replaceButton = new ButtonType("Replace", ButtonBar.ButtonData.OTHER);
//        ButtonType replaceAllButton = new ButtonType("Replace All", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(replaceButton, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(replaceButton).addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            if (app.tabs.getSelectionModel().getSelectedItem() == null) return;
            if (findField.getText().isEmpty() || replaceField.getText().isEmpty()) return;
            status.setText(((CodeView) app.tabs.getSelectionModel().getSelectedItem()).replace(findField.getText(), replaceField.getText()));
        });
//        dialog.getDialogPane().lookupButton(replaceAllButton).addEventFilter(ActionEvent.ACTION, event -> {
//            event.consume();
//            if (app.tabs.getSelectionModel().getSelectedItem() == null) return;
//            if (findField.getText().isEmpty() || replaceField.getText().isEmpty()) return;
//            status.setText(((CodeView) app.tabs.getSelectionModel().getSelectedItem()).replaceAll(findField.getText(), replaceField.getText()));
//        });
        dialog.getDialogPane().setContent(node);

        dialog.show();
        if (initialText != null) return;
        if (app.tabs.getSelectionModel().getSelectedItem() == null) return;
        if (findField.getText().isEmpty()) return;
        status.setText(((CodeView) app.tabs.getSelectionModel().getSelectedItem()).find(findField.getText()));
    }

}
