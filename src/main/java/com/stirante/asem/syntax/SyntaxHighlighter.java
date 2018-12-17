package com.stirante.asem.syntax;

import com.stirante.asem.syntax.code.*;
import com.stirante.asem.ui.CodeView;
import com.stirante.asem.ui.Settings;
import com.stirante.asem.utils.AsyncTask;
import com.stirante.asem.utils.BetterSpanBuilder;
import com.stirante.asem.utils.TextRange;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class SyntaxHighlighter {
    private static final String[] INSTRUCTIONS = {"PUSH", "POP", "MOV", "MOVX", "MOVC", "ADD", "ADDC", "SUBB", "INC", "DEC", "CLR", "CPL", "SETB", "SJMP", "AJMP", "LJMP", "ACALL", "LCALL", "RET", "RETI", "DJNZ", "CJNE", "JZ", "JNZ", "JC", "JNC", "JB", "JBC", "JNB", "ANL", "ORL", "XRL", "RL", "RLC", "RR", "RRC", "MUL", "DIV", "SWAP", "XCH", "XCHD", "DA", "NOP"};
    private static final String[] DIRECTIVES = {"DB", "HIGH", "LOW", "ORG", "USING", "EQU", "SET", "CALL", "JMP", "END", "DATA", "BIT"};
    private static final String[] ALIASES = {"SP", "DPL", "DPH", "PCON", "TCON", "TMOD", "TL0", "TL1", "TH0", "TH1", "SCON", "SBUF", "PCON", "IE", "IP", "PSW", "ACC", "P0\\.0", "P0\\.1", "P0\\.2", "P0\\.3", "P0\\.4", "P0\\.5", "P0\\.6", "P0\\.7", "P0", "P1\\.0", "P1\\.1", "P1\\.2", "P1\\.3", "P1\\.4", "P1\\.5", "P1\\.6", "P1\\.7", "P1", "P2\\.0", "P2\\.1", "P2\\.2", "P2\\.3", "P2\\.4", "P2\\.5", "P2\\.6", "P2\\.7", "P2", "P3\\.0", "P3\\.1", "P3\\.2", "P3\\.3", "P3\\.4", "P3\\.5", "P3\\.6", "P3\\.7", "P3", "ACC\\.0", "ACC\\.1", "ACC\\.2", "ACC\\.3", "ACC\\.4", "ACC\\.5", "ACC\\.6", "ACC\\.7", "B\\.0", "B\\.1", "B\\.2", "B\\.3", "B\\.4", "B\\.5", "B\\.6", "B\\.7", "AR0", "AR1", "AR2", "AR3", "AR4", "AR5", "AR6", "AR7", "IT0", "IE0", "IT1", "IE1", "TR0", "TF0", "TR1", "TF1", "RI", "TI", "RB8", "TB8", "REN", "SM2", "SM1", "SM0", "EX0", "ET0", "EX1", "ET1", "ES", "EA", "PT0", "PX1", "PT1", "PS", "P", "OV", "RS0", "RS1", "F0", "AC", "CY", "A", "AB", "B", "C", "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "@R0", "@R1", "@DPTR", "DPTR", "@A\\+DPTR", "@A\\+PC"};

    //regex
    private static final String INSTRUCTION_PATTERN = "\\b(" + String.join("|", (CharSequence[]) INSTRUCTIONS) + ")\\b";
    private static final String DIRECTIVES_PATTERN = "\\s(" + String.join("|", (CharSequence[]) DIRECTIVES) + ")\\b";
    private static final String ALIASES_PATTERN = "\\b(" + String.join("|", (CharSequence[]) ALIASES) + ")\\b";
    private static final String NUMBER_PATTERN = "\\W#?([01]+B|[0-9ABCDEF]+H|[0-9]+D?)\\b";
    private static final String COMMENT_PATTERN = ";.*";
    private static final String DOLLAR_PATTERN = "\\$.+";
    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<INSTRUCTION>" + INSTRUCTION_PATTERN + ")"
                    + "|(?<DIRECTIVE>" + DIRECTIVES_PATTERN + ")"
                    + "|(?<ALIAS>" + ALIASES_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<DOLLAR>" + DOLLAR_PATTERN + ")"
    );
    private final CodeView codeView;
    private final CodeArea text;
    private AsyncTask<Void, Void, Object> task;
    private AtomicBoolean pause = new AtomicBoolean(false);
    private AtomicBoolean showClickables = new AtomicBoolean(false);
    private String highlightWord = "";

    public SyntaxHighlighter(CodeView codeView, CodeArea codeArea) {
        this.codeView = codeView;
        this.text = codeArea;
    }

    public String getHighlightWord() {
        return highlightWord;
    }

    public void setHighlightWord(String highlightWord) {
        this.highlightWord = highlightWord;
    }

    public void computeHighlighting() {
        final String str = text.getText();
        final double estimatedScrollY = text.getEstimatedScrollY();
        if (pause.get()) return;
        if (task != null) task.cancel();
        task = new AsyncTask<Void, Void, Object>() {
            @Override
            public Object doInBackground(Void[] params) {
                Matcher matcher = PATTERN.matcher(str.toUpperCase());
                BetterSpanBuilder builder = new BetterSpanBuilder();
                while (matcher.find()) {
                    if (isCancelled()) return null;
                    String styleClass =
                            matcher.group("INSTRUCTION") != null ? "instruction" :
                                    matcher.group("DIRECTIVE") != null ? "directive" :
                                            matcher.group("ALIAS") != null ? "alias" :
                                                    matcher.group("NUMBER") != null ? "number" :
                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                    matcher.group("DOLLAR") != null ? "dollar-thingy" :
                                                                            null; /* never happens */
                    assert styleClass != null;
                    int offset = styleClass.equalsIgnoreCase("number") ? 1 : 0;
                    builder.addStyle(styleClass, matcher.start() + offset, matcher.end());
                }
                for (CodeCollisionElement collision : codeView.getSyntaxAnalysis().getCollisions()) {
                    if (isCancelled()) return null;
                    if (collision instanceof ReservedAddressCollisionElement) {
                        builder.addStyle("warning", collision.getDefinitionStart(), collision.getDefinitionEnd());
                    } else {
                        for (TextRange range : collision.getRanges()) {
                            builder.addStyle("warning", range.getStart(), range.getEnd());
                        }
                    }
                }
                for (CodeErrorElement error : codeView.getSyntaxAnalysis().getErrors()) {
                    if (isCancelled()) return null;
                    builder.addStyle("error", error.getDefinitionStart(), error.getDefinitionEnd());
                }
                if (Settings.getInstance().isExperimental()) {
                    if (!highlightWord.isEmpty()) {
                        if (isCancelled()) return null;
                        String regex = "\\b" + highlightWord + "\\b";
                        Matcher clickables = Pattern.compile(regex).matcher(str);
                        while (clickables.find()) {
                            builder.addStyle("highlight", clickables.start(), clickables.end());
                        }
                    }
                    if (showClickables.get()) {
                        final List<CharSequence> list = new ArrayList<>();
                        Collections.addAll(list, INSTRUCTIONS);
                        for (FieldElement fieldElement : codeView.getSyntaxAnalysis().getFields()) {
                            list.add(fieldElement.getName());
                        }
                        for (RoutineElement routineElement : codeView.getSyntaxAnalysis().getRoutines()) {
                            list.add(routineElement.getName());
                        }
                        CharSequence[] arr = list.toArray(new CharSequence[]{});
                        String regex = "\\b" + String.join("|", arr) + "\\b";
                        Matcher clickables = Pattern.compile(regex).matcher(str);
                        while (clickables.find()) {
                            if (isCancelled()) return null;
                            builder.addStyle("clickable", clickables.start(), clickables.end());
                        }
                    }
                    return builder.createStyleSpans(str);
                } else {
                    return builder.createStyleSpans(str);
                }
            }

            @Override
            public void onPostExecute(Object result) {
                if (isCancelled() || result == null) {
                    return;
                }
                try {
                    if (result instanceof ArrayList) {
                        //noinspection unchecked
                        for (BetterSpanBuilder.StylizedRange stylizedRange : (List<BetterSpanBuilder.StylizedRange>) result) {
                            text.setStyle(stylizedRange.getStart(), stylizedRange.getEnd(), stylizedRange.getStyles());
                        }
                    } else if (result instanceof StyleSpans) {
                        //noinspection unchecked
                        text.setStyleSpans(0, (StyleSpans<Collection<String>>) result);
                    }
                } catch (Exception e) {
                    //Usually means that text is changing too fast. Not really a bug so shhh
                }
            }

        };
        task.execute();
    }


    public void setPause(boolean value) {
        pause.set(value);
    }

    public void setShowClickables(boolean value) {
        showClickables.set(value);
    }

}
