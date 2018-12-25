package com.karmazin.model;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that provides information on one PID in a new stream with sleep pauses
 *
 * Stream for command tasklist /V /FO "CSV"
 *
 * Do not use at the moment
 */
public class ProcessWatcher implements Runnable {
    private static final LoggerAPI logger = new LoggerAPI(ProcessWatcher.class.getName());

    private static final int cpuOverload = 90;
    private static final double memPercLack = 0.05;

    private static ProcessWatcher processWatcher;
    private static Thread t;
    private static File cfg;

    private static AtomicBoolean running;
    private static int sleep;
    private static int cpuLoad;
    private static long totalMem;
    private static long freeMem;
    private static int loadedSession;
    private static long time;

    static {
        processWatcher = null;

        cpuLoad = 0;
        totalMem = 1;
        freeMem = 1;
        loadedSession = 0;
        time = 0;
    }

    public static ProcessWatcher getProcessWatcher() {
        if (processWatcher == null) {
            processWatcher = new ProcessWatcher();
        }

        return processWatcher;
    }

    private ProcessWatcher() {
        sleep = ConfigAPI.getProcessDelay();
        running = new AtomicBoolean();
        cfg = new File("./logs/sysload.cfg");

        t = new Thread(this);
        logger.log(Level.INFO, "New watcher for with sleep " + sleep + ": " + t);
        t.start();
    }
    /*
    public ProcessWatcher(int PID) {
        this.PID = PID;
        t = new Thread(this);
        logger.log(Level.INFO, "New watcher for " + PID + ": with sleep " + sleep + ": " + t);
        t.start();
    }
    public ProcessWatcher(int PID, int sleep) {
        this.PID = PID;
        this.sleep = sleep;
        t = new Thread(this);
        logger.log(Level.INFO, "New watcher for " + PID + ": with sleep " + sleep + ": " + t);
        t.start();
    }
    */
    public void run() {
        //synchronized (processWatcher) {
            running.set(true);

            cfg.delete();

            while (running.get()) {
                try {
                /*
                Process p = Runtime.getRuntime().exec("tasklist /V /FI \"PID eq " + PID + "\" /FO \"CSV\"");
                try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    input.readLine();
                    System.out.println(new ProcessData(input.readLine()));
                }
                */

                    Process pCpu = Runtime.getRuntime().exec("wmic cpu get LoadPercentage /Value");
                    Process pMem = Runtime.getRuntime().exec(
                            "wmic OS get FreePhysicalMemory,TotalVisibleMemorySize /Value");
                    Thread.sleep(100);

                    try (BufferedReader inputCpu = new BufferedReader(new InputStreamReader(pCpu.getInputStream()));
                         BufferedReader inputMem = new BufferedReader(new InputStreamReader(pMem.getInputStream()))) {
                        String inputBuffer = "";

                        for (int i = 0; i < 5; ++i) {
                            inputBuffer += inputCpu.readLine();
                        }
                        for (int i = 0; i < 7; ++i) {
                            inputBuffer += inputMem.readLine();
                        }

                        Pattern cpuLoadRegex = Pattern.compile("LoadPercentage=[0-9]*");
                        Pattern totalMemRegex = Pattern.compile("TotalVisibleMemorySize=[0-9]*");
                        Pattern freeMemRegex = Pattern.compile("FreePhysicalMemory=[0-9]*");

                        Matcher match = cpuLoadRegex.matcher(inputBuffer);
                        if (match.find()) {
                            cpuLoad = Integer.parseInt(match.group().substring("LoadPercentage=".length()));
                        } else {
                            cpuLoad = -1;
                        }

                        match = totalMemRegex.matcher(inputBuffer);
                        if (match.find()) {
                            totalMem = Long.parseLong(match.group().substring("TotalVisibleMemorySize=".length()));
                        } else {
                            totalMem = -1;
                        }

                        match = freeMemRegex.matcher(inputBuffer);
                        if (match.find()) {
                            freeMem = Long.parseLong(match.group().substring("FreePhysicalMemory=".length()));
                        } else {
                            freeMem = -1;
                        }

                        writeToConf(cpuLoad, totalMem, freeMem);
                        //System.err.println("CPU load: " + cpuLoad);

                        if (isOverloaded()) {
                            logger.log(Level.SEVERE, "CPU critically loaded or not enough memory");
                            if (loadedSession > 10) {
                                loadedSession = 0;

                                //time = System.currentTimeMillis();

                                //System.err.println("Gonna send email");

                                if (System.currentTimeMillis() - time > 1200000) {

                                    time = System.currentTimeMillis();

                                    //System.err.println("Sending email...");
                                    try {
                                        SendHTMLEmail email = new SendHTMLEmail("javaexamplesas", "Qwerty1337");
                                        email.localOverloadMessage(ConfigAPI.getEmail(),
                                                "Ваш сервер сильно загружен!");
                                    } catch (Exception e) {
                                        logger.log(Level.SEVERE, "Email error: ", e);
                                    }
                                }
                            } else {
                                //System.err.println("loaded: " + loadedSession);
                                //time = System.currentTimeMillis();
                                loadedSession++;
                            }
                        } else {
                            //time = System.currentTimeMillis();
                            loadedSession = 0;
                        }
                    }

                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE,"Interrupted", e);
                    running.set(false);
                } catch (IOException e) {
                    logger.log(Level.SEVERE,"I/O stream error!", e);
                    running.set(false);
                } catch (NullPointerException e) {
                    logger.log(Level.SEVERE,"Can't resolve node!", e);
                    running.set(false);
                } catch (Exception e) { }
            }
        //}
    }

    private static boolean writeToConf(int cpuLoad, long totalMem, long freeMem) {
        try {
            if (!cfg.exists()) {
                cfg.createNewFile();
            }

            Scanner fileScan = new Scanner(cfg);
            List<String> fileStrArr = new LinkedList<>();
            while (fileScan.hasNext()) {
                fileStrArr.add(fileScan.nextLine());
            }
            while (fileStrArr.size() > 100) {
                fileStrArr.remove(0);
            }
            fileScan.close();

            fileStrArr.add(cpuLoad + ";" +
                    totalMem + ";" +
                    freeMem + ";" +
                    String.valueOf((double)freeMem / totalMem * 100.0).substring(0, 5));


            FileWriter fileWriter = new FileWriter(cfg, false);
            fileWriter.write("");
            for (String str : fileStrArr) {
                fileWriter.write(str + "\r\n");
            }
            fileWriter.flush();
            fileWriter.close();

            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't get access to sysload file", e);
            return false;
        }
    }

    public static boolean isOverloaded() {
        return cpuLoad > cpuOverload || (double)freeMem / totalMem < memPercLack;
    }

    public static boolean isCpuOverloaded() {
        return cpuLoad > cpuOverload;
    }

    public static boolean isMemLack() {
        return (double)freeMem / totalMem < memPercLack;
    }

    public static int getCpuLoad() {
        return cpuLoad;
    }
    public static double getFreeMemPerc() {
        return (double)freeMem / totalMem;
    }
}
