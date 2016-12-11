package com.stirante.asem.syntax;

/**
 * Created by stirante
 */
public class SyntaxFormatter {

    private static String SAMPLE_CODE = ";************************************************\n" +
            ";LEKCJA   1\t- LINIE WEJŚĆ/WYJŚĆ MIKROKONTROLERA\n" +
            ";PRZYKŁAD 7\t- STEROWANIE NAPRZEMIENNE\n" +
            ";\t\t- DIODY I BRZĘCZYKA\n" +
            ";************************************************\n" +
            "\n" +
            "LED\tEQU\tP1.7\t;Dioda TEST podłączona do P1.7\n" +
            "BUZZER EQU\tP1.5\t;Brzęczyk podłączony do P1.5\n" +
            "\n" +
            "\tLJMP\tSTART\n" +
            "\tORG\t100H\n" +
            "START:\n" +
            "\n" +
            "\tCLR\tLED\t\t;zapal diodę TEST !!!\n" +
            "\n" +
            "LOOP:\t\t\t\t;Pętla sterowania diody\n" +
            "\t\t\t\t;i brzęczyka\n" +
            "\tCPL\tLED\t\t;zapal/zgaś diodę TEST\n" +
            "\tCPL\tBUZZER\t\t;włącz/wyłącz brzęczyk\n" +
            "\n" +
            "\tMOV\tA,#10\t\t;czekaj czas 10*100ms=1s\n" +
            "\tLCALL\tDELAY_100MS\t;podprogram z EPROMu\n" +
            "\n" +
            "\tLJMP\tLOOP\t\t;powtórz\n" +
            "\n";

    public static void main(String[] args) {
        //Just a test
        System.out.println(SAMPLE_CODE);
        System.out.println(reformat(SAMPLE_CODE));
    }

    public static String reformat(String str) {
        String[] lines = str.split("\n");
        boolean afterColon = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            lines[i] = line.replaceAll("[^\\S\\n]+", " ");
            if (lines[i].startsWith(" ")) lines[i] = lines[i].substring(1);
            line = lines[i];
            if (!line.contains(":")) {
                if (!line.contains(" ")) continue;
                String mnemonic = line.substring(0, line.indexOf(' '));
                String params;
                String comment;
                if (line.contains(";")) {
                    comment = line.substring(line.indexOf(';'));
                    params = line.substring(line.indexOf(' ') + 1, line.indexOf(';'));
                    lines[i] = (afterColon ? "\t" : "") + mnemonic + "\t" + params + "\t\t" + comment;
                } else {
                    params = line.substring(line.indexOf(' ') + 1, line.length());
                    lines[i] = (afterColon ? "\t" : "") + mnemonic + "\t" + params;
                }
            } else {
                afterColon = true;
                line = line.replaceAll(" ", "");
                if (line.contains(";")) {
                    lines[i] = line.substring(0, line.indexOf(';')) + "\t\t\t\t" + line.substring(line.indexOf(';'));
                } else {
                    lines[i] = line;
                }
            }
        }
        return String.join("\n", (CharSequence[]) lines);
    }


}
