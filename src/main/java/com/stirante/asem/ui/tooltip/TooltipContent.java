package com.stirante.asem.ui.tooltip;

/**
 * Created by stirante
 */
public interface TooltipContent {

    boolean matches(String word, int index, int line);

    String getTooltipText(String word, int index, int line);

}
