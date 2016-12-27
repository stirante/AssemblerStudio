package com.stirante.asem.syntax;

import com.stirante.asem.syntax.code.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by stirante
 */
public class SyntaxAnalyzer {
    private static final Pattern FIELD = Pattern.compile("(\\w+)\\s+(\\w+)\\s+([\\w.-]+)\\s*;*(.*)");//#1 name, #2 type, #3 address, #4 comment
    private static final Pattern ROUTINE = Pattern.compile("(\\w+):\\s*;*(.*)");//#1 name, #2 comment
    private static final Pattern OPERATION = Pattern.compile("\\s*([a-zA-Z]+)\\s*([@a-zA-Z0-9_,# /+*.-]*[a-zA-Z0-9._])\\s*;*.*");//#1 mnemonic, #2 args
    private static final HashMap<Integer, String> RESERVED_ADDRESSES = new HashMap<>();

    static {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(SyntaxAnalyzer.class.getResourceAsStream("/reserved_addresses.csv")));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] split = line.split(",");
                String name = split[0];
                Integer address = Integer.parseInt(split[1].replaceAll("h", ""), 16);
                RESERVED_ADDRESSES.put(address, name);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static AnalysisResult analyze(String source) {
        String[] lines = source.split("\n");
        AnalysisResult result = new AnalysisResult();
        int lineOffset = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher fieldMatcher = FIELD.matcher(line);
            if (fieldMatcher.matches()) {
                result.fields.add(new FieldElement(lineOffset + fieldMatcher.start(1), lineOffset + fieldMatcher.end(1), i + 1, fieldMatcher.group(1), fieldMatcher.group(2).toUpperCase(), fieldMatcher.group(3), fieldMatcher.group(4)));
                lineOffset += line.length() + 1;
                continue;
            }
            Matcher routineMatcher = ROUTINE.matcher(line);
            if (routineMatcher.matches()) {
                result.routines.add(new RoutineElement(lineOffset + routineMatcher.start(1), lineOffset + routineMatcher.end(1), i + 1, routineMatcher.group(1), routineMatcher.group(2)));
                lineOffset += line.length() + 1;
            }
        }
        lineOffset = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher operationMatcher = OPERATION.matcher(line);
            if (operationMatcher.matches()) {
                String mnemonic = operationMatcher.group(1).toUpperCase();
                String args = operationMatcher.group(2).replaceAll(" ", "");
                if (!ArgumentVerifier.verify(mnemonic, args, result.fields, result.routines)) {
                    CodeErrorElement e = new CodeErrorElement(lineOffset + operationMatcher.start(1), lineOffset + operationMatcher.end(2), i + 1, "Wrong arguments!");
                    result.errors.add(e);
                }
            }
            lineOffset += line.length() + 1;
        }
        ArrayList<CodeCollisionElement> c = new ArrayList<>();
        for (FieldElement field : result.fields) {
            if (field.getFieldType().equalsIgnoreCase("equ")) continue;
            int address = parseAddress(field.getValue());
            if (address == -1) continue;
            boolean found = false;
            for (CodeCollisionElement collision : c) {
                if (collision.getAddress() == address) {
                    collision.addCollision(field.getDefinitionStart(), field.getDefinitionEnd(), field.getDefinitionLine());
                    found = true;
                }
            }
            if (!found) {
                CodeCollisionElement e = new CodeCollisionElement(field.getDefinitionStart(), field.getDefinitionEnd(), field.getDefinitionLine(), address);
                c.add(e);
            }
            if (RESERVED_ADDRESSES.containsKey(address))
                c.add(new ReservedAddressCollisionElement(field.getDefinitionStart(), field.getDefinitionEnd(), field.getDefinitionLine(), address, RESERVED_ADDRESSES.get(address)));
        }
        result.collisions.addAll(c.stream().filter(collision -> collision.getRanges().size() > 1 || collision instanceof ReservedAddressCollisionElement).collect(Collectors.toList()));
        return result;
    }

    private static int parseAddress(String s) {
        String a = s.replace("#", "");
        if (a.contains("b")) {
            try {
                return Integer.parseInt(a.replaceAll("b", ""), 2);
            } catch (NumberFormatException e) {
                return -1;
            }
        } else if (a.contains("h")) {
            try {
                return Integer.parseInt(a.replaceAll("h", ""), 16);
            } catch (NumberFormatException e) {
                return -1;
            }
        } else {
            try {
                return Integer.parseInt(a.replaceAll("d", ""));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }


    public static class AnalysisResult {
        private ArrayList<FieldElement> fields = new ArrayList<>();
        private ArrayList<RoutineElement> routines = new ArrayList<>();
        private ArrayList<CodeCollisionElement> collisions = new ArrayList<>();
        private ArrayList<CodeErrorElement> errors = new ArrayList<>();

        public ArrayList<FieldElement> getFields() {
            return fields;
        }

        public ArrayList<RoutineElement> getRoutines() {
            return routines;
        }

        public ArrayList<CodeCollisionElement> getCollisions() {
            return collisions;
        }

        public ArrayList<CodeErrorElement> getErrors() {
            return errors;
        }
    }

}
