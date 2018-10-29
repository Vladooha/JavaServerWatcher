package com.karmazin.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class that provides information on one PID in a new stream with sleep pauses
 *
 * Stream for command tasklist /V /FO "CSV"
 *
 * Do not use at the moment
 */
public class ProcessWatcher implements Runnable {
    private int PID;
    private Thread t;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private int sleep;

    public ProcessWatcher(int PID) {
        this.PID = PID;
        t = new Thread(this);
        System.out.println("New watcher for " + PID + ": with sleep " + sleep + ": " + t);
        t.start();
    }

    public ProcessWatcher(int PID, int sleep) {
        this.PID = PID;
        this.sleep = sleep;
        t = new Thread(this);
        System.out.println("New watcher for " + PID + ": with sleep " + sleep + ": " + t);
        t.start();
    }

    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                Process p = Runtime.getRuntime().exec("tasklist /V /FI \"PID eq " + PID + "\" /FO \"CSV\"");

                try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    input.readLine();
                    System.out.println(new ProcessData(input.readLine()));
                }

                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.err.println(PID + "Interrupted");
                running.set(false);
            } catch (IOException e) {
                System.err.println("Ошибка потока записи/вывода!");
                running.set(false);
            } catch (NullPointerException e) {
                System.err.println(PID + " Не удалось обнаружить узел!");
                running.set(false);
            }
        }
    }

}
