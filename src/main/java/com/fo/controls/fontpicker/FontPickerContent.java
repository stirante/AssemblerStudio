/**
 * Copyright (c) 2016, Farrukh Obaid
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of <organization>, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL FARRUKH OBAID BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.fo.controls.fontpicker;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

import java.util.List;

public class FontPickerContent extends GridPane {

    private Font newFont;
    private Label previewLabel;
    private ComboBox<String> fontComboBox;
    private Spinner<Integer> sizeSpinner;
    private CheckBox boldCheckBox;
    private CheckBox italicCheckBox;

    private FontPicker fontPicker;

    public FontPickerContent(final FontPicker fontPicker) {
        this.fontPicker = fontPicker;
        getStyleClass().add("font-picker-popup");
        initGui();
    }

    private void initGui() {
        setPrefSize(200, 230);
        getStylesheets().addAll(getClass().getResource("/style.css").toExternalForm(), getClass().getResource("/font-picker.css").toExternalForm());
        getColumnConstraints().addAll(new ColumnConstraints(40), new ColumnConstraints(160));
        getRowConstraints().addAll(
                new RowConstraints(20), new RowConstraints(35), new RowConstraints(35),
                new RowConstraints(35), new RowConstraints(30), new RowConstraints(30),
                new RowConstraints(30)
        );

        add(new Label("Preview:"), 0, 0, 2, 1);
        previewLabel = new Label("ABCDEF abcdef 0123456");
        previewLabel.setMaxWidth(Double.MAX_VALUE);
        previewLabel.setMaxHeight(Double.MAX_VALUE);
        previewLabel.getStyleClass().add("preview-label");
        add(previewLabel, 0, 1, 2, 2);

        add(new Label("Font:"), 0, 3, 1, 1);
        List<String> families = Font.getFamilies();
        fontComboBox = new ComboBox<>(FXCollections.observableList(families));
        fontComboBox.setMaxWidth(Double.MAX_VALUE);
        add(fontComboBox, 1, 3, 1, 1);

        add(new Label("Size:"), 0, 4, 1, 1);
        sizeSpinner = new Spinner<>(1, 100, 12);
        sizeSpinner.setEditable(true);
        sizeSpinner.setPrefWidth(70);
        add(sizeSpinner, 1, 4, 1, 1);

        add(new Label("Style:"), 0, 5, 1, 1);
        boldCheckBox = new CheckBox("Bold");
        italicCheckBox = new CheckBox("Italic");
        HBox styleHBox = new HBox(10);
        styleHBox.setAlignment(Pos.CENTER_LEFT);
        styleHBox.getChildren().addAll(boldCheckBox, italicCheckBox);
        add(styleHBox, 1, 5, 1, 1);

        Button okButton = new Button("OK");
        okButton.setPrefWidth(55);
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(55);
        HBox buttonsHBox = new HBox(5);
        buttonsHBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsHBox.getChildren().addAll(okButton, cancelButton);
        add(buttonsHBox, 1, 6, 1, 1);

        fontComboBox.getSelectionModel().select(0);
        fontComboBox.valueProperty().addListener(observable -> changeFont());
        fontComboBox.setCellFactory((ListView<String> listView) -> {
            final ListCell<String> cell = new ListCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item);
                        setFont(new Font(item, 12));
                    }
                }
            };
            cell.setPrefWidth(120);
            return cell;
        });

        StringConverter<Integer> spinnerFormatter = new StringConverter<Integer>() {
            @Override
            public Integer fromString(String val) {
                String oldVal = String.valueOf(sizeSpinner.getValue());
                try {
                    int result = Integer.parseInt(val);
                    if (result >= 1 && result <= 100) {
                        return result;
                    }
                    throw new Exception("");
                } catch (Exception e) {
                    sizeSpinner.getEditor().setText(oldVal);
                    return sizeSpinner.getValue();
                }
            }

            @Override
            public String toString(Integer val) {
                return val.toString();
            }
        };
        sizeSpinner.getValueFactory().setConverter(spinnerFormatter);
        sizeSpinner.valueProperty().addListener(observable -> changeFont());
        boldCheckBox.selectedProperty().addListener(observable -> changeFont());
        italicCheckBox.selectedProperty().addListener(observable -> changeFont());

        okButton.setOnAction(event -> {
            fontPicker.setValue(newFont);
            fontPicker.hide();
        });
        cancelButton.setOnAction(event -> fontPicker.hide());

        changeFont();
    }

    private void changeFont() {
        FontWeight weight = boldCheckBox.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = italicCheckBox.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;
        int size = sizeSpinner.getValue();
        String family = fontComboBox.getValue();
        Font font = Font.font(family, weight, posture, size);
        newFont = font;
        previewLabel.setFont(font);
    }
}