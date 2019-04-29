package com.karmazin.model;

import javafx.util.Pair;

import java.io.File;

final public class ConfigWrapper {
    private static ConfigAPI config;

    // .ini file data
    private final static String CONFIG_ADRESS = "./config/config.ini";

    // INI CONSTS
    private final static String USER_SETTINGS_SECTION = "UserSettings";
    private final static String LOGIN_KEY = "lastLogin";
    private final static String PASS_KEY = "lastPass";
    private final static String EMAIL_KEY = "lastEmail";
    private final static String DEBUG_KEY = "debugMode";
    private final static String SELF_TEST_KEY = "selfTestMode";

    private final static String WINDOW_SETTINGS_SECTION = "WindowSettings";
    private final static String HEIGHT_KEY = "height";
    private final static String WIDTH_KEY = "width";

    private final static String ETHERNET_SETTINGS_SECTION = "EthernetSettings";
    private final static String PING_DELAY_KEY = "pingDelay";
    private final static String PING_FAULTS_KEY = "pingFaults";
    private final static String PING_EMAIL_KEY = "pingEmail";
    private final static String HTTP_DELAY_KEY = "httpDelay";

    private final static String PROGRAM_SETTINGS_SECTION = "ProgramSettings";
    private final static String PROCESS_DELAY_KEY = "processDelay";

    // Initialize fields with empty values.
    static {
        config = new ConfigAPI(CONFIG_ADRESS);
    }

    public static void createConfigFile() {
        File configFile = new File(CONFIG_ADRESS);
        configFile.getParentFile().mkdirs();
        if (!configFile.exists()) {
            config.create();
        }

        config.writeIfNotExists(USER_SETTINGS_SECTION, LOGIN_KEY, "");
        config.writeIfNotExists(USER_SETTINGS_SECTION, PASS_KEY, "");
        config.writeIfNotExists(USER_SETTINGS_SECTION, EMAIL_KEY, "");
        config.writeIfNotExists(USER_SETTINGS_SECTION, DEBUG_KEY, "false");
        config.writeIfNotExists(USER_SETTINGS_SECTION, SELF_TEST_KEY, "false");

        config.writeIfNotExists(WINDOW_SETTINGS_SECTION, WIDTH_KEY, 800);
        config.writeIfNotExists(WINDOW_SETTINGS_SECTION, HEIGHT_KEY, 600);

        config.writeIfNotExists(ETHERNET_SETTINGS_SECTION, PING_DELAY_KEY, 1000);
        config.writeIfNotExists(ETHERNET_SETTINGS_SECTION, PING_FAULTS_KEY, 30);
        config.writeIfNotExists(ETHERNET_SETTINGS_SECTION, PING_EMAIL_KEY, 30);
        config.writeIfNotExists(ETHERNET_SETTINGS_SECTION, HTTP_DELAY_KEY, 3000);

        config.writeIfNotExists(PROGRAM_SETTINGS_SECTION, PROCESS_DELAY_KEY, 5000);
    }

    /// * User settings sector *

    // debug field control
    public static void setDebug(boolean option) {
        config.write(USER_SETTINGS_SECTION, DEBUG_KEY, option);
    }

    public static boolean getDebug() {
        return Boolean.parseBoolean(config.read(USER_SETTINGS_SECTION, DEBUG_KEY));
    }

    // selfTest field control
    public static void setSelfTest(boolean option) {
        config.write(USER_SETTINGS_SECTION, SELF_TEST_KEY, option);
    }

    public static boolean getSelfTest() {
        return Boolean.parseBoolean(config.read(USER_SETTINGS_SECTION, SELF_TEST_KEY));
    }

    // Login data fields control
    public static String getLogin() {
        return config.read(USER_SETTINGS_SECTION, LOGIN_KEY);
    }

    public static String getPassword() {
        return config.read(USER_SETTINGS_SECTION, PASS_KEY);
    }

    public static void setLoginData(String login, String password) {
        config.write(USER_SETTINGS_SECTION, LOGIN_KEY, login);
        config.write(USER_SETTINGS_SECTION, PASS_KEY, password);
    }

    public static void unlogin() {
        config.write(USER_SETTINGS_SECTION, LOGIN_KEY, "");
        config.write(USER_SETTINGS_SECTION, PASS_KEY, "");
    }

    // Email data fields control
    public static void setEmail(String option) {
        config.write(USER_SETTINGS_SECTION, EMAIL_KEY, option);
    }

    public static String getEmail() {
        return config.read(USER_SETTINGS_SECTION, EMAIL_KEY);
    }

    /// * WindowSettings sector *

    // Resolution control
    public static void setResolution(int height, int width) {
        config.write(WINDOW_SETTINGS_SECTION, HEIGHT_KEY, height);
        config.write(WINDOW_SETTINGS_SECTION, WIDTH_KEY, width);
    }

    public static Pair<Integer, Integer> getResolution() {
        int val1 = Integer.parseInt(config.read(WINDOW_SETTINGS_SECTION, HEIGHT_KEY));
        int val2 = Integer.parseInt(config.read(WINDOW_SETTINGS_SECTION, WIDTH_KEY));
        return new Pair<>(val1, val2);
    }

    /// * EthernetSettings sector *

    // Ping delay fields control
    public static int getPingDelay() {
        return Integer.parseInt(config.read(ETHERNET_SETTINGS_SECTION, PING_DELAY_KEY));
    }

    public static void setPingDelay(int pingDelay) {
        config.write(ETHERNET_SETTINGS_SECTION, PING_DELAY_KEY, pingDelay);
    }

    // Ping fault count fields control
    public static int getPingFaults() {
        return Integer.parseInt(config.read(ETHERNET_SETTINGS_SECTION, PING_FAULTS_KEY));
    }

    public static void setPingFaults(int pingFaults) {
        config.write(ETHERNET_SETTINGS_SECTION, PING_FAULTS_KEY, pingFaults);
    }

    // Email delay fields control
    public static int getPingEmail() {
        return Integer.parseInt(config.read(ETHERNET_SETTINGS_SECTION, PING_EMAIL_KEY));
    }

    public static void setPingEmail(int pingEmail) {
        config.write(ETHERNET_SETTINGS_SECTION, PING_EMAIL_KEY, pingEmail);
    }

    // Http delay fields control
    public static int getHttpDelay() {
        return Integer.parseInt(config.read(ETHERNET_SETTINGS_SECTION, HTTP_DELAY_KEY));
    }

    public static void setHttpDelay(int httpDelay) {
        config.write(ETHERNET_SETTINGS_SECTION, HTTP_DELAY_KEY, httpDelay);
    }

    /// * ProgramSettings sector *

    // Process watcher delay fields control
    public static int getProcessDelay() {
        return Integer.parseInt(config.read(PROGRAM_SETTINGS_SECTION, PROCESS_DELAY_KEY));
    }

    public static void setProcessDelay(int pingDelay) {
        config.write(PROGRAM_SETTINGS_SECTION, PROCESS_DELAY_KEY, pingDelay);
    }
}
