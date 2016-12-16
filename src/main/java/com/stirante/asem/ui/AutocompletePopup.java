package com.stirante.asem.ui;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by stirante
 */
public class AutocompletePopup extends Popup {

    private final ListView<String> list;
    ObservableListWrapper<String> items = new ObservableListWrapper<>(new ArrayList<>());

    public AutocompletePopup(CodeView view) {
        list = new ListView<>();
        list.setStyle(
                "-fx-background-color: #2e2e2e;" +
                        "-fx-text-fill: #8a8a8a;" +
                        "-fx-border-color: white;" +
                        "-fx-padding: 5;");
        list.setMaxWidth(600);
        list.setMaxHeight(300);
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.setItems(items);
        list.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                view.autocomplete(list.getSelectionModel().getSelectedItem());
                hide();
            } else if (event.getCode() == KeyCode.ESCAPE) hide();
        });
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) hide();
        });
        getContent().add(list);
    }

    public void setSuggestions(ArrayList<String> list) {
        Collections.sort(list);
        items.clear();
        items.addAll(list);
        this.list.getSelectionModel().select(0);
    }

}
