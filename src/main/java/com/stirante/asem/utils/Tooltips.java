package com.stirante.asem.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by stirante
 */
public class Tooltips {

    private static ResourceBundle messages;

    static {
        messages = ResourceBundle.getBundle("tooltips", Locale.ROOT);
    }

    public static String get(String key) {
        if (messages.containsKey(key.toUpperCase()))
            return messages.getString(key.toUpperCase());
        return "";
    }
}
