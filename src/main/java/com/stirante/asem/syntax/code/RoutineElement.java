package com.stirante.asem.syntax.code;

import com.stirante.asem.ui.tooltip.TooltipContent;

/**
 * Created by stirante
 */
public class RoutineElement implements CodeElement, TooltipContent {

    private int start;
    private int end;
    private int line;
    private String name;
    private String comment;

    public RoutineElement(int start, int end, int line, String name, String comment) {
        this.start = start;
        this.end = end;
        this.line = line;
        this.name = name;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public int getDefinitionStart() {
        return start;
    }

    @Override
    public int getDefinitionEnd() {
        return end;
    }

    @Override
    public int getDefinitionLine() {
        return line;
    }

    public String getName() {
        return name;
    }

    @Override
    public CodeElementType getType() {
        return CodeElementType.ROUTINE;
    }

    @Override
    public boolean matches(String word, int index, int line) {
        return word.equals(getName());
    }

    @Override
    public String getTooltipText(String word, int index, int line) {
        return "Name: " + getName() +
                (getComment() != null && !getComment().isEmpty() ? ("\n" + getComment()) : "");
    }
}
