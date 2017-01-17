package com.stirante.asem.utils;

/**
 * Created by stirante
 */
public class TextRange {
    private int start;
    private int end;
    private int line;

    public TextRange(int line) {
        this.line = line;
    }

    public TextRange(int start, int end, int line) {

        this.start = start;
        this.end = end;
        this.line = line;
    }

    public TextRange(int start, int end) {

        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLine() {
        return line;
    }

    public String getString(String text) {
        return text.substring(getStart(), getEnd());
    }

    public boolean contains(int index) {
        return index >= start && index < end;
    }
}
