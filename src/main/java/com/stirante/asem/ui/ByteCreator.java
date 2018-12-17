package com.stirante.asem.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by stirante
 */
public class ByteCreator {

    @FXML
    public TextArea description;
    @FXML
    public TextField bit7;
    @FXML
    public TextField bit6;
    @FXML
    public TextField bit5;
    @FXML
    public TextField bit4;
    @FXML
    public TextField bit3;
    @FXML
    public TextField bit2;
    @FXML
    public TextField bit1;
    @FXML
    public TextField bit0;
    private List<String> descriptions;
    private TextField[] bits;
    private VBox node;

    public ByteCreator() {
        FXMLLoader loader = new FXMLLoader(ByteCreator.class.getResource("/ByteCreator.fxml"));
        loader.setController(this);
        try {
            node = loader.load();
            bits = new TextField[]{bit0, bit1, bit2, bit3, bit4, bit5, bit6, bit7};
            reset();
            for (int i = 0; i < 8; i++) {
                setupField(bits[i], i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupField(final TextField f, final int index) {
        f.setOnMouseClicked(event -> {
            f.selectAll();
            description.setText(descriptions.get(index));
        });
        f.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) f.setText(oldValue);
            if (newValue.length() == 1 && (newValue.charAt(0) != '0' && newValue.charAt(0) != '1')) f.setText(oldValue);
        });
    }

    public void reset() {
        description.setText("");
        for (TextField bit : bits) {
            bit.setText("0");
        }
    }

    public String create(String name, List<String> descriptions) {
        this.descriptions = descriptions;
        reset();
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Byte creator - " + name);
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        dialog.getDialogPane().setContent(node);
        Platform.runLater(() -> {
            bit7.requestFocus();
            description.setText(descriptions.get(7));
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return constructByte();
            }
            return "";
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }

    private String constructByte() {
        StringBuilder sb = new StringBuilder("#");
        for (int i = 7; i >= 0; i--) {
            TextField bit = bits[i];
            if (bit.getText().isEmpty()) {
                sb.append("0");
            } else {
                sb.append(bit.getText());
            }
        }
        sb.append("b");
        return sb.toString();
    }

}
