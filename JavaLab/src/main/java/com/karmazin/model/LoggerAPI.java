package com.karmazin.model;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class LoggerAPI {
    private static Handler fileHandler;
    private static boolean unaccessibleLog;
    private static boolean muted;

    static {
        // Global logger settings
        try {
            // Read logger config file
            LogManager.getLogManager().readConfiguration();

            // Log file format settings
            fileHandler = new FileHandler("src/main/resources/logs/log.txt");
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    Date time = new Date();
                    time.setTime(record.getMillis());
                    String result = String.format("[%02d:%02d:%02d]", time.getHours(), time.getMinutes(), time.getSeconds()) + " ";
                    result += "{" + record.getLevel() + "} ";
                    result += record.getSourceClassName() + "." + record.getSourceMethodName() + ": ";
                    result += record.getMessage() + "\r\n";

                    return result;
                }
            });

            // Log was configured succefully
            unaccessibleLog = false;
            muted = false;
        } catch (IOException e) {
            System.err.println("Handler init error!");
            e.printStackTrace();

            // Can't configure log file
            unaccessibleLog = true;
        }
    }

    private Logger logger;

    public LoggerAPI(String loggerName) {
        logger = Logger.getLogger(loggerName);

        // Apllying global settings to logger object
        if (!unaccessibleLog) {
            logger.addHandler(fileHandler);
        }
    }

    public LoggerAPI(String loggerName, String path) {
        logger = Logger.getLogger(loggerName);

        try {
            // Log file format settings
            Handler newFileHandler = new FileHandler(path);
            newFileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    Date time = new Date();
                    time.setTime(record.getMillis());
                    String result = String.format("[%02d:%02d:%02d]", time.getHours(), time.getMinutes(), time.getSeconds()) + " ";
                    result += "{" + record.getLevel() + "} ";
                    result += record.getSourceClassName() + "." + record.getSourceMethodName() + ": ";
                    result += record.getMessage() + "\r\n";

                    return result;
                }
            });

            logger.addHandler(newFileHandler);
        } catch (IOException e) {
            // TODO It's were fine to add log message here, but it's log constructor =(
            System.err.println("CAN'T CREATE CUSTOM LOG FILE!!!");

            // Apllying global settings to logger object
            if (!unaccessibleLog) {
                logger.addHandler(fileHandler);
            }
        }
    }

    public boolean log(Level level, String message, Exception e) {
        if (unaccessibleLog || muted) {
            // Error was occured till log configuration
            return false;
        }

        if (ConfigAPI.getDebug()) {
            logger.log(level, message, e);
            return true;
        } else {
            // Debug mode is off
            return false;
        }
    }

    public boolean log(Level level, String message) {
        if (unaccessibleLog || muted) {
            // Error was occured till log configuration
            return false;
        }

        if (ConfigAPI.getDebug()) {
            logger.log(level, message);
            return true;
        } else {
            // Debug mode is off
            return false;
        }
    }

    public void muteMode(boolean mute) {
        muted = mute;
    }
}
