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
public class FindDialog {

    @FXML
    public TextField findField;
    @FXML
    public Label status;
    private VBox node;
    private Main app;

    public FindDialog(Main app) {
        this.app = app;
        FXMLLoader loader = new FXMLLoader(FindDialog.class.getResource("/FindDialog.fxml"));
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
        dialog.setTitle("Find");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(SegmentCreator.class.getResource("/style.css").toExternalForm());
        ButtonType findButton = new ButtonType("Find", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(findButton, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(findButton).addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            onAction();

        });
        dialog.getDialogPane().setContent(node);
        findField.requestFocus();

        dialog.show();
    }

    public void onAction() {
        if (!app.hasOpenTab()) return;
        if (findField.getText().isEmpty()) return;
        status.setText(app.getOpenTab().find(findField.getText()));
    }
}
