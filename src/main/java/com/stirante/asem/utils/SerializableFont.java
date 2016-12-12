package com.stirante.asem.utils;

import javafx.scene.text.Font;

import java.io.Serializable;

/**
 * Created by stirante
 */
public class SerializableFont implements Serializable {

    private String name;
    private double size;

    public SerializableFont(Font original) {
        size = original.getSize();
        name = original.getName();
    }

    public Font getFont() {
        return new Font(name, size);
    }

}
