package com.stirante.asem.utils;

import java.io.*;
import java.util.HashMap;

/**
 * Created by stirante
 */
public class ConfigManager {

    private static HashMap<String, Object> cfg = new HashMap<>();
    private static File f;

    static {
        f = new File("config.dat");
    }

    public static void load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            cfg = (HashMap<String, Object>) ois.readObject();
            if (cfg == null) cfg = new HashMap<>();//probably unreachable
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            //e.printStackTrace();
        }
    }

    public static void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(cfg);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, Object> getMap() {
        return cfg;
    }

}
