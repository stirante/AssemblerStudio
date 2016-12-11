package com.stirante.asem.syntax;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class SyntaxAnalyzer {
    private static final Pattern FIELD = Pattern.compile("(\\w+)\\s+(\\w+)\\s+([\\w.-]+)\\s*;*(.*)");//#1 name, #2 type, #3 address, #4 comment
    private static final Pattern ROUTINE = Pattern.compile("(\\w+):\\s*;*(.*)");//#1 name, #2 comment


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
            }
        }
        return result;
    }


    public static class AnalysisResult {
        public ArrayList<Field> fields = new ArrayList<>();
        public ArrayList<Routine> routines = new ArrayList<>();

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

}
