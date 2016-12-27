package com.stirante.asem.ui.tooltip;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by stirante
 */
public class DefaultTooltips implements TooltipContent {
    private static ResourceBundle messages;
    private static TooltipContent instance;

    static {
        messages = ResourceBundle.getBundle("tooltips", Locale.ROOT);
    }

    private static String get(String key) {
        if (messages.containsKey(key.toUpperCase()))
            return messages.getString(key.toUpperCase());
        return "";
    }

    public static TooltipContent getInstance() {
        if (instance == null) instance = new DefaultTooltips();
        return instance;
    }

    @Override
    public boolean matches(String word, int index, int line) {
        String s = get(word);
        return !s.isEmpty();
    }

    @Override
    public String getTooltipText(String word, int index, int line) {
        return word + ": " + get(word);
    }

}
