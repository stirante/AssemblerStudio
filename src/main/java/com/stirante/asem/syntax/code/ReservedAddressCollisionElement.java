package com.stirante.asem.syntax.code;

import com.stirante.asem.utils.TextRange;

/**
 * Created by stirante
 */
public class ReservedAddressCollisionElement extends CodeCollisionElement {
    private String name;
    private TextRange range;
    public ReservedAddressCollisionElement(int start, int end, int line, int address, String name) {
        super(-1, -1, -1, address);
        this.name = name;
        range = new TextRange(start, end, line);
    }

    public String getName() {
        return name;
    }

    @Override
    public int getDefinitionStart() {
        return range.getStart();
    }

    @Override
    public int getDefinitionEnd() {
        return range.getEnd();
    }

    @Override
    public int getDefinitionLine() {
        return range.getLine();
    }

    @Override
    public CodeElementType getType() {
        return CodeElementType.WARNING;
    }

    @Override
    public boolean matches(String word, int index, int line) {
        return range.contains(index);
    }

    @Override
    public String getTooltipText(String word, int index, int line) {
        return "Collision address with " + name + "!";
    }

}
