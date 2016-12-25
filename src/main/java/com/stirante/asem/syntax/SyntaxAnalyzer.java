package com.stirante.asem.syntax;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by stirante
 */
public class SyntaxAnalyzer {
    private static final Pattern FIELD = Pattern.compile("(\\w+)\\s+(\\w+)\\s+([\\w.-]+)\\s*;*(.*)");//#1 name, #2 type, #3 address, #4 comment
    private static final Pattern ROUTINE = Pattern.compile("(\\w+):\\s*;*(.*)");//#1 name, #2 comment
    private static final Pattern OPERATION = Pattern.compile("\\s*([a-z]+)\\s*([@a-z0-9_,# /+*-]*[a-z0-9_])\\s*;*.*");//#1 mnemonic, #2 args


    public static AnalysisResult analyze(String source) {
        String[] lines = source.split("\n");
        AnalysisResult result = new AnalysisResult();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher fieldMatcher = FIELD.matcher(line);
            if (fieldMatcher.matches()) {
                Field f = new Field();
                f.name = fieldMatcher.group(1);
                f.type = fieldMatcher.group(2).toUpperCase();
                f.address = fieldMatcher.group(3);
                f.line = i;
                try {
                    f.comment = fieldMatcher.group(4);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.fields.add(f);
                continue;
            }
            Matcher routineMatcher = ROUTINE.matcher(line);
            if (routineMatcher.matches()) {
                Routine r = new Routine();
                r.line = i;
                r.name = routineMatcher.group(1);
                try {
                    r.comment = routineMatcher.group(2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.routines.add(r);
                continue;
            }
            Matcher operationMatcher = OPERATION.matcher(line.toLowerCase());
            if (operationMatcher.matches()) {
                String mnemonic = operationMatcher.group(1).toUpperCase();
                String args = operationMatcher.group(2).toLowerCase().replaceAll(" ", "");
                if (mnemonic.equalsIgnoreCase("mov") && args.contains("@dptr")) {
                    CodeError e = new CodeError();
                    e.description = "You probably meant to use MOVX?";
                    e.line = i + 1;
                    result.errors.add(e);
                }
            }
        }
        ArrayList<Collision> c = new ArrayList<>();
        for (Field field : result.fields) {
            if (field.type.equalsIgnoreCase("equ")) continue;
            int address;
            String a = field.address.replace("#", "");
            if (a.contains("b")) {
                try {
                    address = Integer.parseInt(a.replaceAll("b", ""), 2);
                } catch (NumberFormatException e) {
                    continue;
                }
            } else if (a.contains("h")) {
                try {
                    address = Integer.parseInt(a.replaceAll("h", ""), 16);
                } catch (NumberFormatException e) {
                    continue;
                }
            } else {
                try {
                    address = Integer.parseInt(a.replaceAll("d", ""));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            boolean found = false;
            for (Collision collision : c) {
                if (collision.address == address) {
                    collision.lines.add(field.line + 1);
                    found = true;
                }
            }
            if (!found) {
                Collision e = new Collision();
                e.lines.add(field.line + 1);
                e.address = address;
                c.add(e);
            }
        }
        result.collisions.addAll(c.stream().filter(collision -> collision.lines.size() > 1).collect(Collectors.toList()));
        return result;
    }


    public static class AnalysisResult {
        public ArrayList<Field> fields = new ArrayList<>();
        public ArrayList<Routine> routines = new ArrayList<>();
        public ArrayList<Collision> collisions = new ArrayList<>();
        public ArrayList<CodeError> errors = new ArrayList<>();

        @Override
        public String toString() {
            return "AnalysisResult{" +
                    "fields=" + fields +
                    ", routines=" + routines +
                    '}';
        }
    }

    public static class Field {
        public String name;
        public String type;
        public String address;
        public String comment = "";
        public int line;

        @Override
        public String toString() {
            return "Field{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", address='" + address + '\'' +
                    ", comment='" + comment + '\'' +
                    ", line=" + line +
                    '}';
        }
    }

    public static class Routine {
        public String name;
        public String comment = "";
        public int line;

        @Override
        public String toString() {
            return "Routine{" +
                    "name='" + name + '\'' +
                    ", comment='" + comment + '\'' +
                    ", line=" + line +
                    '}';
        }
    }

    public static class Collision {
        public int address;
        public ArrayList<Integer> lines = new ArrayList<>();

        @Override
        public String toString() {
            return "Collision{" +
                    "lines=" + lines +
                    '}';
        }
    }

    public static class CodeError {
        public int line;
        public String description = "";

        @Override
        public String toString() {
            return "CodeError{" +
                    "line=" + line +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

}
