package com.karmazin.model;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class HttpWatcher {
    private static final LoggerAPI logger = new LoggerAPI(HttpWatcher.class.getName());

    public static final int WAITING = -1337;
    public static final int WRONG_URL = -1;
    public static final int TIMEOUT = -2;
    public static final int STREAM_CLOSED = -3;

    private Thread thread;

    private HttpURLConnection connection;
    private AtomicBoolean isWorking;
    private int code;

    public HttpWatcher() {
        thread = null;
        code = WAITING;
        isWorking = new AtomicBoolean(true);
    }

    public void startRecievingHttp(String query) {
        if (thread != null) {
            thread = new Thread(() -> {
                logger.log(Level.INFO, "New http url connection: " + query);

                while (isWorking.get()) {
                    int delay = ConfigAPI.getHttpDelay();
                    try {
                        connection = (HttpURLConnection) new URL("https://" + query).openConnection();
                        connection.setUseCaches(false);
                        connection.setConnectTimeout(delay);
                        connection.setReadTimeout(delay);

                        long startTime = System.currentTimeMillis();
                        connection.connect();
                        long endTime = System.currentTimeMillis();
                        int ping = (int) ((endTime - startTime));
                        LoggerAPI.httpLog(query, ping);

                        code = connection.getResponseCode();
                    } catch (MalformedURLException e) {
                        logger.log(Level.SEVERE, "Wrond URL adress: " + query, e);

                        code = WRONG_URL;
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "I/O problem occured by adress (timeout probably): " + query);

                        code = TIMEOUT;
                    } finally {
                        connection.disconnect();
                    }
                }

                code = STREAM_CLOSED;
            });

            thread.start();
        }
    }

    synchronized public String getHttpCode() {
        String httpsAnswer;
        if (code == HttpWatcher.WRONG_URL) {
            httpsAnswer = "Сервер не принимает https запросы";
        } else if (code == HttpWatcher.TIMEOUT) {
            httpsAnswer = "Превышено время ожидания запроса...";
        } else if (code == HttpWatcher.STREAM_CLOSED) {
            httpsAnswer = "Возникла непредвиденная ошибка";
        } else if (code == HttpWatcher.WAITING) {
            httpsAnswer = "Отправка запроса...";
        } else {
            httpsAnswer = code + "";
        }

        return httpsAnswer;
    }

    public boolean isEnabled() {
        return isWorking.get();
    }

    public void shutdown() {
        isWorking.set(false);
        //thread.interrupt();
    }
}
