package com.stirante.asem.syntax.code;

import com.stirante.asem.ui.tooltip.TooltipContent;

/**
 * Created by stirante
 */
public class CodeErrorElement implements CodeElement, TooltipContent {

    private int start;
    private int end;
    private int line;
    private String description;

    public CodeErrorElement(int start, int end, int line, String description) {
        this.start = start;
        this.end = end;
        this.line = line;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    @Override
    public CodeElementType getType() {
        return CodeElementType.ERROR;
    }

    @Override
    public boolean matches(String word, int index, int line) {
        return index >= getDefinitionStart() && index < getDefinitionEnd();
    }

    @Override
    public String getTooltipText(String word, int index, int line) {
        return getDescription();
    }
}
