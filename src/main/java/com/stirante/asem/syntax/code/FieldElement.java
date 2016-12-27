package com.stirante.asem.syntax.code;

import com.stirante.asem.ui.tooltip.TooltipContent;

/**
 * Created by stirante
 */
public class FieldElement implements CodeElement, TooltipContent {

    private int start;
    private int end;
    private int line;
    private String name;
    private String type;
    private String value;
    private String comment;

    public FieldElement(int start, int end, int line, String name, String type, String value, String comment) {
        this.start = start;
        this.end = end;
        this.line = line;
        this.name = name;
        this.type = type;
        this.value = value;
        this.comment = comment;
    }

    public String getValue() {
        return value;
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

    public String getFieldType() {
        return type;
    }

    @Override
    public CodeElementType getType() {
        return CodeElementType.FIELD;
    }

    @Override
    public boolean matches(String word, int index, int line) {
        return word.equals(getName());
    }

    @Override
    public String getTooltipText(String word, int index, int line) {
        return "Name: " + getName() + "\n" +
                "Type: " + getFieldType() + "\n" +
                "Value: " + getValue() +
                (getComment() != null && !getComment().isEmpty() ? ("\n" + getComment()) : "");
    }
}
