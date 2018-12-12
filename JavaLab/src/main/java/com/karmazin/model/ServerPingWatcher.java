package com.karmazin.model;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * The class provides information on pinging the server with IP in a new stream with pauses "sleep"
 *
 * Stream for command ping -n 1 {IP}
 */
public class ServerPingWatcher implements Runnable  {
    private static final LoggerAPI logger = new LoggerAPI(ServerPingWatcher.class.getName());

    private static int timeoutLimit;
    private static int emailTimeLimit;

    private String IP;
    private Thread t;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private int sleep;
    private int byteSize;
    private int awaitTime;

    private boolean itsAlive;
    private int time;
    private int timeoutCounter;
    private long lastEmailTime;

    static {
        // TODO Config timeout value
        timeoutLimit = 20;
        emailTimeLimit = 1200000;
    }

    public ServerPingWatcher(String IP, boolean startNow) {
        this(IP, 1000, 32, 50000, false);
    }

    public ServerPingWatcher(String IP) {
        this(IP, 1000, 32);
    }

    public ServerPingWatcher(String IP, int sleep, int byteSize) {
        this(IP, sleep, byteSize, 50000, true);
    }

    public ServerPingWatcher(String IP, int sleep, int byteSize, int awaitTime, boolean startNow) {
        this.IP = IP;
        this.sleep = sleep;
        this.byteSize = byteSize;
        this.awaitTime = awaitTime;

        this.itsAlive = true;
        this.timeoutCounter = 0;
        this.lastEmailTime = 0;
        this.t = new Thread(this);

        if (startNow) {
            logger.log(Level.INFO,"New watcher for " + IP + ": Sleep " + sleep
                    + ", byteSize " + byteSize + " in Thread: " + t);
            this.t.start();
        }
    }

    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                time = new PingAPI(IP, byteSize).ping();

                timeoutCounter = 0;
                itsAlive = true;

                pause(sleep);
                LoggerAPI.pingLog(IP, time);
            } catch (IOException e) {
                logger.log(Level.SEVERE,"Input/output stream error!");
                shutdown();
            } catch (NullPointerException e) {
                logger.log(Level.SEVERE,IP + " can't find an adress!");
                shutdown();
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.log(Level.SEVERE, IP + " unknown error");
            } catch (TimeoutException e) {
                time = 0;
                LoggerAPI.pingLog(IP, time);
                if (timeoutCounter++ > timeoutLimit) {
                    logger.log(Level.SEVERE, IP + " timeout!");
                    timeoutCounter = 0;

                    if (itsAlive) {
                        if (System.currentTimeMillis() - lastEmailTime > emailTimeLimit) {
                            try {
                                SendHTMLEmail email = new SendHTMLEmail("javaexamplesas", "Qwerty1337");
                                email.serverFailureMessage(ConfigAPI.getEmail(),
                                        "Сервер '" + IP + "' упал",
                                        IP);

                                lastEmailTime = System.currentTimeMillis();
                            } catch (Exception e1) {
                                logger.log(Level.SEVERE, "Email error: ", e1);
                                e1.printStackTrace();
                            }
                        }
                    }
                    itsAlive = false;

                    pause(awaitTime);
                }
            }
        }
    }

    /**
     * The method needed to obtain information about the ping in the main thread
     * @return response time
     */
    public synchronized boolean isRunning(){
        return running.get();
    }

    public synchronized int getTime() {
        return time;
    }

    public synchronized void shutdown() {
        running.set(false);
    }

    private void pause(int millisToSleep) {
        try {
            Thread.sleep(millisToSleep);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE,IP + " is interrupted!");
            shutdown();
        }
    }
}

