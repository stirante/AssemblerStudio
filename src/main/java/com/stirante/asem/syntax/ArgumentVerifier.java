package com.stirante.asem.syntax;

import com.stirante.asem.syntax.code.FieldElement;
import com.stirante.asem.syntax.code.RoutineElement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class ArgumentVerifier {

    public static MatchType checkStatus(String mnemonic, String args, ArrayList<FieldElement> fields, ArrayList<RoutineElement> routines) {
        String[] split = args.split(",");
        Field[] fs = MnemonicArguments.class.getDeclaredFields();
        for (Field f : fs) {
            if (f.getName().equals(mnemonic) && f.getType() == Type[][].class) {
                try {
                    f.setAccessible(true);
                    Type[][] arr = (Type[][]) f.get(null);
                    for (Type[] types : arr) {
                        if (types.length == split.length) {
                            int match = 0;
                            for (int i = 0; i < types.length; i++) {
                                MatchType type = types[i].matches(split[i], fields, routines);
                                if (type == MatchType.MATCH) match++;
                                else if (type == MatchType.UNKNOWN_SYMBOL) return type;
                            }
                            if (match == types.length) return MatchType.MATCH;
                            return MatchType.NOT_MATCH;
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return MatchType.MATCH;
//        switch (mnemonic) {
//            case "MOV":
//                for (Type[] types : MOV) {
//                    if (types[0].matches(split[0], fields, routines) && types[1].matches(split[1], fields, routines)) return true;
//                }
//                return MatchType.MATCH;
//            case "JZ":
//                for (Type[] types : MnemonicArguments.JZ) {
//                    MatchType type = types[0].matches(args, fields, routines);
//                    if (type == MatchType.MATCH || type == MatchType.UNKNOWN_SYMBOL) return type;
//                }
//                return MatchType.NOT_MATCH;
//            default:
//                return MatchType.MATCH;
//        }
    }

    enum Type {
        A_B("[aA][bB]"),
        AT_R0("@[rR]0"),
        AT_R1("@[rR]1"),
        C("[cC]"),
        R0("[rR]0"),
        R1("[rR]1"),
        R2("[rR]2"),
        R3("[rR]3"),
        R4("[rR]4"),
        R5("[rR]5"),
        R6("[rR]6"),
        R7("[rR]7"),
        BIT_ADDR(".+"),//TODO
        IRAM_ADDR(".+"),//TODO
        CODE_ADDR("([a-zA-Z0-9_]+)"),
        DATA("#([0-9abcdefABCDEF]+[hH]|[0-9]+[dD]?|[01]+[bB])"),
        DATA_16("#([0-9abcdefABCDEF]+[hH]|[0-9]+[dD]?|[01]+[bB])"),
        DPTR("[dD][pP][tT][rR]"),
        A("[aA]");

        private final Pattern pattern;

        Type(String regex) {
            pattern = Pattern.compile("^" + regex + "$");
        }

        MatchType matches(String str, ArrayList<FieldElement> fields, ArrayList<RoutineElement> routines) {
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches() && matcher.groupCount() == 1) {
                String routine = matcher.group(1);
                for (RoutineElement routineElement : routines) {
                    if (routineElement.getName().equalsIgnoreCase(routine)) return MatchType.MATCH;
                }
                return MatchType.UNKNOWN_SYMBOL;
            } else if (matcher.matches()) {
                return MatchType.MATCH;
            }
            return MatchType.NOT_MATCH;
        }

    }

    enum MatchType {
        MATCH, NOT_MATCH, UNKNOWN_SYMBOL
    }

}