package com.karmazin.model;

import java.sql.Time;

/**
 * Here data is stored from the console response by process.
 *
 * Parser of string for command tasklist /V /FO "CSV"
 *
 * Do not use at the moment
 */
public class ProcessData {
    /*
    private enum status {
        RUNNING {
            public String toString() {
                return "Running";
            }
        },
        NOT_RESPONDING {
            public String toString() {
                return "Not responding";
            }
        },
        UNKNOWN {
            public String toString() {
                return "Unknown";
            }
        }
    }
    */

    /** Name of image */
    public String imageName;

    /** Value of PID */
    public int PID;

    /** Process status */
    public String status;

    // Name of session

    /** Number of session */
    public int session;

    /** Memory use  in kb */
    public int memUsage;

    // Status

    /** Name of user in form [<domain>\<user>] */
    public String username;

    /** Time of CPU in form hh:mm:ss */
    public Time cpuTime;

    /** Name of window */
    public String windowTitle;

    /** Name of service */
    //public String SERVICES; /SVC

    /** Name of DLL */
    //public String MODULES; /m

    public ProcessData(String line) {
        String[] data = line.replace("\"", "").split(",");

        imageName = data[0];
        PID = Integer.parseInt(data[1]);
        session = Integer.parseInt(data[3]);
        memUsage = Integer.parseInt(data[4].replaceAll("[^\\d.]", ""));
        status = data[5];
        username = data[6];
        cpuTime = Time.valueOf(data[7]);
        windowTitle = data[8];
        //SERVICES =
        //MODULES =
    }

    @Override
    public String toString() {
        return
            "Имя образа: " + imageName +
            ", PID: " + PID +
            ", № сеанса: " + session +
            ", Память: " + memUsage + " КБ" +
            ", Состояние: " + status +
            ", Пользователь: " + username +
            ", Время цп: " + cpuTime +
            ", Заголовок окна: " + windowTitle;
    }
}




























