package com.stirante.asem;

import com.stirante.asem.utils.AsyncTask;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class SyntaxHighlighter {
    private static final String[] INSTRUCTIONS = {"PUSH", "POP", "MOV", "MOVX", "MOVC", "ADD", "ADDC", "SUBB", "INC", "DEC", "CLR", "CPL", "SETB", "SJMP", "AJMP", "LJMP", "JMP \\@A\\+DPTR", "ACALL", "LCALL", "RET", "RETI", "DJNZ", "CJNE", "JZ", "JNZ", "JC", "JNC", "JB", "JBC", "JNB", "ANL", "ORL", "XRL", "RL", "RLC", "RR", "RRC", "MUL", "DIV", "SWAP", "XCH", "XCHD", "DA", "NOP", "A", "AB", "B", "C", "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "\\@R0", "\\@R1", "\\@DPTR", "DPTR", "\\@A\\+DPTR", "@A\\+PC"};
    private static final String[] DIRECTIVES = {"DB", "HIGH", "LOW", "ORG", "USING", "EQU", "SET", "CALL", "JMP", "$", "END", "DATA", "BIT"};
    private static final String[] ALIASES = {"SP", "DPL", "DPH", "PCON", "TCON", "TMOD", "TL0", "TL1", "TH0", "TH1", "SCON", "SBUF", "PCON", "IE", "IP", "PSW", "ACC", "P0\\.0", "P0\\.1", "P0\\.2", "P0\\.3", "P0\\.4", "P0\\.5", "P0\\.6", "P0\\.7", "P0", "P1\\.0", "P1\\.1", "P1\\.2", "P1\\.3", "P1\\.4", "P1\\.5", "P1\\.6", "P1\\.7", "P1", "P2\\.0", "P2\\.1", "P2\\.2", "P2\\.3", "P2\\.4", "P2\\.5", "P2\\.6", "P2\\.7", "P2", "P3\\.0", "P3\\.1", "P3\\.2", "P3\\.3", "P3\\.4", "P3\\.5", "P3\\.6", "P3\\.7", "P3", "ACC\\.0", "ACC\\.1", "ACC\\.2", "ACC\\.3", "ACC\\.4", "ACC\\.5", "ACC\\.6", "ACC\\.7", "B\\.0", "B\\.1", "B\\.2", "B\\.3", "B\\.4", "B\\.5", "B\\.6", "B\\.7", "AR0", "AR1", "AR2", "AR3", "AR4", "AR5", "AR6", "AR7", "IT0", "IE0", "IT1", "IE1", "TR0", "TF0", "TR1", "TF1", "RI", "TI", "RB8", "TB8", "REN", "SM2", "SM1", "SM0", "EX0", "ET0", "EX1", "ET1", "ES", "EA", "PT0", "PX1", "PT1", "PS", "P", "OV", "RS0", "RS1", "F0", "AC", "CY"};

    //regex
    private static final String INSTRUCTION_PATTERN = "\\b(" + String.join("|", (CharSequence[]) INSTRUCTIONS) + ")\\b";
    private static final String DIRECTIVES_PATTERN = "\\b(" + String.join("|", (CharSequence[]) DIRECTIVES) + ")\\b";
    private static final String ALIASES_PATTERN = "\\b(" + String.join("|", (CharSequence[]) ALIASES) + ")\\b";
    private static final String NUMBER_PATTERN = "[\\s#\\-+][0-9][0-9ABCDEFH]*";
    private static final String COMMENT_PATTERN = ";.*";
    private static final String DOLLAR_PATTERN = "\\$.+";
    private static final Pattern PATTERN = Pattern.compile(
            "(?<INSTRUCTION>" + INSTRUCTION_PATTERN + ")"
                    + "|(?<DIRECTIVE>" + DIRECTIVES_PATTERN + ")"
                    + "|(?<ALIAS>" + ALIASES_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<DOLLAR>" + DOLLAR_PATTERN + ")"
    );


    public static void computeHighlighting(final CodeArea text) {
        final String str = text.getText();

        new AsyncTask<Void, Void, StyleSpans<Collection<String>>>() {
            @Override
            public StyleSpans<Collection<String>> doInBackground(Void[] params) {
                Matcher matcher = PATTERN.matcher(str.toUpperCase());
                int lastKwEnd = 0;
                StyleSpansBuilder<Collection<String>> spansBuilder
                        = new StyleSpansBuilder<>();
                while (matcher.find()) {
                    String styleClass =
                            matcher.group("INSTRUCTION") != null ? "instruction" :
                                    matcher.group("DIRECTIVE") != null ? "directive" :
                                            matcher.group("ALIAS") != null ? "alias" :
                                                    matcher.group("NUMBER") != null ? "number" :
                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                    matcher.group("DOLLAR") != null ? "dollar-thingy" :
                                                                            null; /* never happens */
                    assert styleClass != null;
                    spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                    spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                    lastKwEnd = matcher.end();
                }
                spansBuilder.add(Collections.emptyList(), str.length() - lastKwEnd);
                return spansBuilder.create();
            }

            @Override
            public void onPostExecute(StyleSpans<Collection<String>> result) {
                text.setStyleSpans(0, result);
            }
        }.execute();
    }
}
