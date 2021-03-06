package com.karmazin.model;

import org.ini4j.Ini;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigAPI {
    private static LoggerAPI logger = new LoggerAPI(ConfigAPI.class.getName());

    private String configAdress;

    public ConfigAPI(String configAdress) {
        this.configAdress = configAdress;
    }

    public boolean write(String section, String key, Object value) {
        if (configAdress == null || configAdress.equals("")) {
            return false;
        } else {
            try {
                Ini iniWrite;
                iniWrite = new Ini();
                File configFile = new File(configAdress);
                iniWrite.load(new FileInputStream(configFile));
                iniWrite.remove(section, key);
                iniWrite.put(section, key, value);
                iniWrite.store(new FileOutputStream(configFile));

                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public String read(String section, String key) {
        if (configAdress == null || configAdress.equals("")) {
            return null;
        } else {
            try {
                Ini iniRead;
                iniRead = new Ini();
                iniRead.load(new File(configAdress));
                String result = iniRead.get(section, key);

                return result;
            } catch (IOException e) {
                return null;
            }
        }
    }

    public boolean create() {
        if (configAdress == null || configAdress.equals("")) {
            return false;
        } else {
            File configFile = new File(configAdress);
            if (configFile.exists()) {

                return true;
            } else {
                try {
                    configFile.getParentFile().mkdirs();
                    configFile.createNewFile();

                    return true;
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Can't create config file!");

                    return false;
                }
            }
        }
    }

    public boolean writeIfNotExists(String section, String key, Object value) {
        if (read(section, key) == null) {
            return write(section, key, value);
        } else {
            return false;
        }
    }
}
