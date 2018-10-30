package com.karmazin.model;

import javafx.util.Pair;

import java.io.File;

final public class ConfigAPI {
    private static LoggerAPI logger = new LoggerAPI(ConfigAPI.class.getName());

    private static ConfigMaster config;

    // .ini file data
    private final static String configAdress = "src/main/resources/config/config.ini";

    private final static String userSettingsSection = "UserSettings";
    private final static String loginKey = "lastLogin";
    private final static String passKey = "lastPass";
    private final static String debugKey = "debugMode";
    private final static String selfTestKey = "selfTestMode";

    private final static String windowSettingsSection = "WindowSettings";
    private final static String heightKey = "height";
    private final static String widthKey = "width";

    // Initialize fields with empty values.
    static {
        config = new ConfigMaster(configAdress);
    }

    public static void createConfigFile() {
        File configFile = new File(configAdress);
        if (!configFile.exists()) {
            config.create();

            // Writing 'userSettings' sector
            config.write(userSettingsSection, loginKey, "");
            config.write(userSettingsSection, passKey, "");
            config.write(userSettingsSection, debugKey, "false");
            config.write(userSettingsSection, selfTestKey, "false");

            // Writing 'windowsSettings' sector
            config.write(windowSettingsSection, heightKey, 600);
            config.write(windowSettingsSection, widthKey, 800);
        }
    }

    /// User settings sector

    // debug field control
    public static void setDebug(boolean option) {
        config.write(userSettingsSection, debugKey, option);
    }

    public static boolean getDebug() {
        return Boolean.parseBoolean(config.read(userSettingsSection, debugKey));
    }

    // selfTest field control
    public static void setSelfTest(boolean option) {
        config.write(userSettingsSection, selfTestKey, option);
    }

    public static boolean getSelfTest() {
        return Boolean.parseBoolean(config.read(userSettingsSection, selfTestKey));
    }

    // Login data fields control
    public static String getLogin() {
        return config.read(userSettingsSection, loginKey);
    }

    public static String getPassword() {
        return config.read(userSettingsSection, passKey);
    }

    public static void setLoginData(String login, String password) {
        config.write(userSettingsSection, loginKey, login);
        config.write(userSettingsSection, passKey, password);
    }

    public static void unlogin() {
        config.write(userSettingsSection, loginKey, "");
        config.write(userSettingsSection, passKey, "");
    }

    /// WindowSettings sector

    // Resolution control
    public static void setResolution(int height, int width) {
        config.write(windowSettingsSection, heightKey, height);
        config.write(windowSettingsSection, widthKey, width);
    }

    public static Pair<Integer, Integer> getResolution() {
        int val1 = Integer.parseInt(config.read(windowSettingsSection, heightKey));
        int val2 = Integer.parseInt(config.read(windowSettingsSection, widthKey));
        return new Pair<>(val1, val2);
    }

    ///--------------------------------------------ShouldBeRemoved--------------------------------------------

//    public static boolean closeServerTab() {
//        // Check user access level
//        if (sessionStatus().equals(UserAPI.UserType.Unauthorized)) {
//            // UserAPI has not rights to execute the command
//            return false;
//        } else {
//            // UserAPI has rights to execute a command
//            if (executor != null) {
//                executor.shutdownNow();
//                executor = null;
//            }
//
//            return true;
//        }
//    }

    public static GeolocationAPIData getGeoData(String IP) {
        if (UserAPI.getStatus().equals(UserAPI.UserType.Unauthorized)) {
            // UserAPI has not rights to execute the command
            return null;
        } else {
            // UserAPI has rights to execute a command
            return new GeolocationAPI().sendRequest(IP);
        }
    }

    // When exiting the program, temporary maps are deleted.
    // -------------------------------------- ExitProcedure --------------------------------------
}
