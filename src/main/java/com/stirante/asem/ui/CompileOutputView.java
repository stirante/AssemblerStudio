package com.stirante.asem.ui;

import com.stirante.asem.Main;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class CompileOutputView extends StyledTextArea<CompileOutputView.ErrorLine, CompileOutputView.ErrorLine> {

    private static final Pattern PATTERN = Pattern.compile(".+\\(([0-9]+)\\).*");
    private Main app;

    public CompileOutputView(Main main) {
        super(ErrorLine.NO_ERROR, (text, style) -> style.applyToText(text), ErrorLine.NO_ERROR, (text, style) -> style.applyToText(text));
        app = main;
        setEditable(false);
    }

    public void setText(String text) {
        replaceText(0, getText().length(), text);
        highlightErrors();
        moveTo(0);
    }

    private void highlightErrors() {
        StyleSpansBuilder<ErrorLine> spansBuilder = new StyleSpansBuilder<>();
        String str = getText();
        int lastKwEnd = 0;
        Matcher matcher = PATTERN.matcher(str);
        while (matcher.find()) {
            int line = Integer.parseInt(matcher.group(1));
            ErrorLine errorLine = new ErrorLine(line, app);
            spansBuilder.add(ErrorLine.NO_ERROR, matcher.start() - lastKwEnd);
            spansBuilder.add(errorLine, matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(ErrorLine.NO_ERROR, str.length() - lastKwEnd);
        setStyleSpans(0, spansBuilder.create());
    }

    static class ErrorLine {
        static final ErrorLine NO_ERROR = new ErrorLine();

        private final int line;
        private Main app;

        private ErrorLine() {
            this(-1, null);
        }

        ErrorLine(int line, Main app) {
            this.line = line;
            this.app = app;
        }

        void applyToText(TextFlow text) {
        }

        public void applyToText(TextExt text) {
            if (line >= 0) {
                text.setCursor(Cursor.HAND);
                text.setUnderline(true);
                text.setFill(Color.RED);
                text.setBackgroundColor(Color.DARKRED);
                text.setOnMouseClicked(click -> ((CodeView) app.tabs.getSelectionModel().getSelectedItem()).goToLine(line));
            }
        }
    }
}
