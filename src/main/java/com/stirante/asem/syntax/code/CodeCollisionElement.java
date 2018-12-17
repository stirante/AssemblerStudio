package com.stirante.asem.syntax.code;

import com.stirante.asem.ui.tooltip.TooltipContent;
import com.stirante.asem.utils.TextRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by stirante
 */
public class CodeCollisionElement implements CodeElement, TooltipContent {
    private int address;
    private final List<TextRange> ranges = new ArrayList<>();

    public CodeCollisionElement(int start, int end, int line, int address) {
        this.address = address;
        ranges.add(new TextRange(start, end, line));
    }

    public void addCollision(int start, int end, int line) {
        ranges.add(new TextRange(start, end, line));
    }

    public Collection<TextRange> getRanges() {
        return Collections.unmodifiableCollection(ranges);
    }

    @Override
    public int getDefinitionStart() {
        return -1;
    }

    @Override
    public int getDefinitionEnd() {
        return -1;
    }

    @Override
    public int getDefinitionLine() {
        return -1;
    }

    @Override
    public CodeElementType getType() {
        return CodeElementType.WARNING;
    }

    @Override
    public boolean matches(String word, int index, int line) {
        for (TextRange range : ranges) {
            if (range.contains(index)) return true;
        }
        return false;
    }

    @Override
    public String getTooltipText(String word, int index, int line) {
        return "Collision address! (" + address + "d, " + Integer.toHexString(address) + "h)";
    }

    public int getAddress() {
        return address;
    }
}
