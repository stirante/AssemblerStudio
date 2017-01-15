package com.stirante.asem.ui;

import com.stirante.asem.Constants;
import com.stirante.asem.syntax.code.FieldElement;
import com.stirante.asem.syntax.code.RoutineElement;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.TwoDimensional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by stirante
 */
public class AutocompletionPopup extends Popup {

    private final ListView<String> list;
    private final CodeView view;
    private final CodeArea codeArea;
    ObservableListWrapper<String> items = new ObservableListWrapper<>(new ArrayList<>());
    private Method m;
    private boolean autocomplete = false;
    private int autoIndex = 0;
    private boolean wasMnemonic = false;

    public AutocompletionPopup(CodeView view, CodeArea codeArea) {
        this.view = view;
        this.codeArea = codeArea;
        list = new ListView<>();
        try {
            m = StyledTextArea.class.getDeclaredMethod("getCaretBoundsOnScreen");
            m.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
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
                String item = list.getSelectionModel().getSelectedItem();
                boolean wasCall = false;
                if (item.equalsIgnoreCase("lcall\t\t")
                        || item.equalsIgnoreCase("sjmp\t\t")
                        || item.equalsIgnoreCase("ljmp\t\t")
                        || item.equalsIgnoreCase("acall\t\t")
                        || item.equalsIgnoreCase("jz\t\t"))
                    wasCall = true;
                view.insert(item.substring(autoIndex));
                hide();
                if (wasMnemonic && !item.startsWith("ret")) {
                    triggerAutocompletion(wasCall);
                    wasMnemonic = false;
                }
            } else if (event.getCode() == KeyCode.ESCAPE) hide();
        });
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) hide();
        });
        getContent().add(list);
    }

    @Override
    public void hide() {
        super.hide();
        autocomplete = false;
    }

    public boolean isAutocompletion() {
        return autocomplete;
    }

    public boolean triggerAutocompletion() {
        return triggerAutocompletion(false);
    }

    private boolean triggerAutocompletion(boolean wasCall) {
        TwoDimensional.Position caret = codeArea.offsetToPosition(codeArea.getCaretPosition(), TwoDimensional.Bias.Forward);
        TwoDimensional.Position lineStart = codeArea.position(caret.getMajor(), 0);
        String line = codeArea.getText().substring(lineStart.toOffset(), caret.toOffset());
        boolean isMnemonic = Constants.MNEMONIC.matcher(line).matches();
        String s = view.getWordAt(codeArea.getCaretPosition());
        ArrayList<String> suggestions = new ArrayList<>();
        if (isMnemonic) {
            wasMnemonic = true;
            boolean upperCase = Character.isUpperCase(s.charAt(0));
            String s1 = s.toUpperCase();
            for (String s2 : Constants.MNEMONIC_ARRAY) {
                if (s2.startsWith(s1)) {
                    String tabs = s2.toLowerCase().startsWith("ret") ? "" : "\t\t";
                    suggestions.add(upperCase ? s2 + tabs : s2.toLowerCase() + tabs);
                }
            }
        } else {
            if (!wasCall)
                suggestions.addAll(view.getSyntaxAnalysis().getFields().stream().filter(field -> field.getName().startsWith(s)).map(FieldElement::getName).collect(Collectors.toList()));
            suggestions.addAll(view.getSyntaxAnalysis().getRoutines().stream().filter(routine -> routine.getName().startsWith(s)).map(RoutineElement::getName).collect(Collectors.toList()));
        }
        if (suggestions.isEmpty()) return false;
        try {
            Optional invoke = (Optional) m.invoke(codeArea);
            if (invoke.isPresent()) {
                Bounds b = (Bounds) invoke.get();
                autoIndex = s.length();
                setSuggestions(suggestions);
                autocomplete = true;
                show(codeArea, b.getMinX(), b.getMaxY());
                return true;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setSuggestions(ArrayList<String> list) {
        Collections.sort(list);
        items.clear();
        items.addAll(list);
        this.list.getSelectionModel().select(0);
    }

    public void setIndex(int index) {
        this.autoIndex = index;
    }

    public void onChanges() {
        String s = view.getWordAt(codeArea.getCaretPosition());
        autoIndex = s.length();
        items.removeIf(str -> !str.toLowerCase().startsWith(s.toLowerCase()));
        if (items.isEmpty()) {
            hide();
        }
    }
}
