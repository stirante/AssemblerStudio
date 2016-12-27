package com.stirante.asem.ui.tooltip;

import com.stirante.asem.ui.CodeView;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.model.TwoDimensional;

import java.util.ArrayList;

/**
 * Created by stirante
 */
public class TooltipPopup extends Popup {

    private final CodeView view;
    private final CodeArea codeArea;
    private final Label popupMsg;
    private final ArrayList<TooltipContent> contents = new ArrayList<>();

    public TooltipPopup(CodeView view, CodeArea codeArea) {
        this.view = view;
        this.codeArea = codeArea;
        popupMsg = new Label();
        popupMsg.setStyle(
                "-fx-background-color: #2e2e2e;" +
                        "-fx-text-fill: #8a8a8a;" +
                        "-fx-border-color: white;" +
                        "-fx-padding: 5;");
        popupMsg.setWrapText(true);
        popupMsg.setMaxWidth(400);
        getContent().add(popupMsg);
    }

    public void triggerTooltip(MouseOverTextEvent e) {
        int chIdx = e.getCharacterIndex();
        Point2D pos = e.getScreenPosition();
        String s = view.getWordAt(chIdx);
        int line = codeArea.offsetToPosition(chIdx, TwoDimensional.Bias.Forward).getMajor() + 1;
        if (DefaultTooltips.getInstance().matches(s, chIdx, line)) {
            popupMsg.setText(DefaultTooltips.getInstance().getTooltipText(s, chIdx, line));
            show(codeArea, pos.getX() + 15, pos.getY() + 15);
            return;
        }
        for (TooltipContent content : getTooltips()) {
            if (content.matches(s, chIdx, line)) {
                popupMsg.setText(content.getTooltipText(s, chIdx, line));
                show(codeArea, pos.getX() + 15, pos.getY() + 15);
                return;
            }
        }
    }

    private ArrayList<TooltipContent> getTooltips() {
        ArrayList<TooltipContent> result = new ArrayList<>();
        result.add(DefaultTooltips.getInstance());
        result.addAll(view.getSyntaxAnalysis().getErrors());
        result.addAll(view.getSyntaxAnalysis().getCollisions());
        result.addAll(view.getSyntaxAnalysis().getRoutines());
        result.addAll(view.getSyntaxAnalysis().getFields());
        return result;
    }

}
