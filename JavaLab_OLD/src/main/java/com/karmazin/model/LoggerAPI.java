package com.karmazin.model;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class LoggerAPI {
    private static Handler fileHandler;
    private static boolean unaccessibleLog;
    private static boolean muted;

    private static Logger pingLogger;
    private static Logger httpLogger;

    static {
        // Global logger settings
        try {
            // Read logger config file
            LogManager.getLogManager().readConfiguration();

            // Log file format settings
            new File("./logs").mkdirs();
            fileHandler = new FileHandler("./logs/log.txt");
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    Date time = new Date();
                    time.setTime(record.getMillis());
                    String result = String.format("[%02d:%02d:%02d]", time.getHours(), time.getMinutes(), time.getSeconds()) + " ";
                    result += "{" + record.getLevel() + "} ";
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

    public static void pingLog(String serverName, int ping) {
        File pingLogFile = getCustomLogFile("serv");
        if (!pingLogFile.exists() || pingLogger == null) {
            pingLogger = Logger.getLogger(
                    pingLogFile.getName());
            pingLogger.addHandler(
                    getCustomLogHandler(pingLogFile));
        }

        pingLogger.log(Level.INFO, "|" + serverName + "|: " + ping);
    }

    public static void httpLog(String serverName, int ping) {
        File httpLogFile = getCustomLogFile("http");
        if (!httpLogFile.exists() || httpLogger == null) {
            httpLogger = Logger.getLogger(
                    httpLogFile.getName());
            httpLogger.addHandler(
                    getCustomLogHandler(httpLogFile));
        }

        httpLogger.log(Level.INFO, "|" + serverName + "|: " + ping);
    }

    private static File getCustomLogFile(String mode) {
        Date loggerDate = new Date();
        String loggerName = mode + "Log_" +
                loggerDate.getDate() + "_" +
                (loggerDate.getMonth() + 1) + "_" +
                (loggerDate.getYear() + 1900);

        return new File("./logs/" + loggerName  + ".txt");
    }

    private static Handler getCustomLogHandler(File customLogFile) {
        try {
            new File("./logs").mkdirs();
            Handler newFileHandler = new FileHandler(customLogFile.getPath());
            newFileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    Date time = new Date();
                    time.setTime(record.getMillis());
                    String result = String.format("[%02d:%02d:%02d]", time.getHours(), time.getMinutes(), time.getSeconds()) + " ";
                    result += record.getMessage() + "\r\n";

                    return result;
                }
            });

            return newFileHandler;
        } catch (IOException e) {
            return null;
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
            logger.log(level, logger.getName() + ": " + message, e);
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
            logger.log(level, logger.getName() + ": " + message);
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
