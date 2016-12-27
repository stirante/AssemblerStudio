package com.stirante.asem.syntax;

import com.stirante.asem.syntax.code.FieldElement;
import com.stirante.asem.syntax.code.RoutineElement;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.stirante.asem.syntax.ArgumentVerifier.Type.*;

/**
 * Created by stirante
 */
public class ArgumentVerifier {
    private static Type[][] JZ = {
            {CODE_ADDR}
    };
    private static Type[][] MOV = {
            {AT_R0, DATA},
            {AT_R1, DATA},
            {AT_R0, A},
            {AT_R1, A},
            {AT_R0, IRAM_ADDR},
            {AT_R1, IRAM_ADDR},
            {A, DATA},
            {A, AT_R0},
            {A, AT_R1},
            {A, R0},
            {A, R1},
            {A, R2},
            {A, R3},
            {A, R4},
            {A, R5},
            {A, R6},
            {A, R7},
            {A, IRAM_ADDR},
            {DPTR, DATA_16},
            {R0, DATA},
            {R1, DATA},
            {R2, DATA},
            {R3, DATA},
            {R4, DATA},
            {R5, DATA},
            {R6, DATA},
            {R7, DATA},
            {R0, A},
            {R1, A},
            {R2, A},
            {R3, A},
            {R4, A},
            {R5, A},
            {R6, A},
            {R7, A},
            {R0, IRAM_ADDR},
            {R1, IRAM_ADDR},
            {R2, IRAM_ADDR},
            {R3, IRAM_ADDR},
            {R4, IRAM_ADDR},
            {R5, IRAM_ADDR},
            {R6, IRAM_ADDR},
            {R7, IRAM_ADDR},
            {BIT_ADDR, C},
            {IRAM_ADDR, DATA},
            {IRAM_ADDR, AT_R0},
            {IRAM_ADDR, AT_R1},
            {IRAM_ADDR, R0},
            {IRAM_ADDR, R1},
            {IRAM_ADDR, R2},
            {IRAM_ADDR, R3},
            {IRAM_ADDR, R4},
            {IRAM_ADDR, R5},
            {IRAM_ADDR, R6},
            {IRAM_ADDR, R7},
            {IRAM_ADDR, A},
            {IRAM_ADDR, IRAM_ADDR}
    };

    public static boolean verify(String mnemonic, String args, ArrayList<FieldElement> fields, ArrayList<RoutineElement> routines) {
        String[] split = args.split(",");
        switch (mnemonic) {
//            case "MOV":
//                for (Type[] types : MOV) {
//                    if (types[0].matches(split[0], fields, routines) && types[1].matches(split[1], fields, routines)) return true;
//                }
//                return false;
            case "JZ":
                for (Type[] types : JZ) {
                    if (types[0].matches(args, fields, routines)) return true;
                }
                return false;
            default:
                return true;
        }
    }

    enum Type {
        AT_R0("@r0"),
        AT_R1("@r1"),
        C("c"),
        R0("r0"),
        R1("r1"),
        R2("r2"),
        R3("r3"),
        R4("r4"),
        R5("r5"),
        R6("r6"),
        R7("r7"),
        BIT_ADDR(".+"),//TODO
        IRAM_ADDR(".+"),//TODO
        CODE_ADDR("([a-zA-Z0-9_]+)"),
        DATA("#([0-9abcdef]+h|[0-9]+d*|[01]+b)"),
        DATA_16("#([0-9abcdef]+h|[0-9]+d*|[01]+b)"),
        DPTR("dptr"),
        A("a");

        private final Pattern pattern;

        Type(String regex) {
            pattern = Pattern.compile("^" + regex + "$");
        }

        boolean matches(String str, ArrayList<FieldElement> fields, ArrayList<RoutineElement> routines) {
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches() && matcher.groupCount() == 1) {
                String routine = matcher.group(1);
                for (RoutineElement routineElement : routines) {
                    if (routineElement.getName().equals(routine)) return true;
                }
            }
            return false;
        }

    }

}