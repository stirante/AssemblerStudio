package com.stirante.asem.ui;

import com.stirante.asem.syntax.SyntaxAnalyzer;
import com.stirante.asem.utils.Tooltips;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.model.TwoDimensional;

/**
 * Created by stirante
 */
public class TooltipPopup extends Popup {

    private final CodeView view;
    private final CodeArea codeArea;
    private final Label popupMsg;

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
        String s1 = Tooltips.get(s);
        if (!s1.isEmpty()) {
            popupMsg.setText(s + ": " + s1);
            show(codeArea, pos.getX() + 15, pos.getY() + 15);
        } else {
            for (SyntaxAnalyzer.Collision collision : view.getSyntaxAnalysis().collisions) {
                if (collision.lines.contains(codeArea.offsetToPosition(chIdx, TwoDimensional.Bias.Forward).getMajor() + 1)) {
                    popupMsg.setText("Collision address! (" + collision.address + "d, " + Integer.toHexString(collision.address) + "h)");
                    show(codeArea, pos.getX() + 15, pos.getY() + 15);
                    return;
                }
            }
            for (SyntaxAnalyzer.Field field : view.getSyntaxAnalysis().fields) {
                if (field.name.equals(s) || s.equals("#" + field.name)) {
                    popupMsg.setText("Type: " + field.type +
                            "\nValue: " + field.address +
                            (field.comment.isEmpty() ? "" : "\n" + field.comment));
                    show(codeArea, pos.getX() + 15, pos.getY() + 15);
                    return;
                }
            }
            for (SyntaxAnalyzer.Routine routine : view.getSyntaxAnalysis().routines) {
                if (routine.name.equals(s)) {
                    popupMsg.setText("Name: " + routine.name +
                            (routine.comment.isEmpty() ? "" : "\n" + routine.comment));
                    show(codeArea, pos.getX() + 15, pos.getY() + 15);
                    return;
                }
            }
        }
    }

}
