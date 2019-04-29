package com.karmazin.model;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

public class HttpsAPI {
    private static final LoggerAPI logger = new LoggerAPI(HttpsAPI.class.getName());

    public static final int WAITING = -1337;
    public static final int WRONG_URL = -1;
    public static final int TIMEOUT = -2;
    public static final int STREAM_CLOSED = -3;

    private HttpURLConnection connection;

    private String IP;
    private int delay;
    private boolean isWorking;
    private int code;

    public HttpsAPI(String serverIP) {
        IP = serverIP;
        delay = ConfigWrapper.getHttpDelay();
        code = WAITING;
        isWorking = true;
    }

    public void startRecievingHttp() {
        if (isWorking) {
            try {
                connection = (HttpURLConnection) new URL("https://" + IP).openConnection();
                connection.setUseCaches(false);
                connection.setConnectTimeout(delay);
                connection.setReadTimeout(delay);

                long startTime = System.currentTimeMillis();
                connection.connect();
                long endTime = System.currentTimeMillis();
                int time = (int) ((endTime - startTime));
                time = (time == 0) ? 1 : time;
                LoggerAPI.httpLog(IP, time);
                //System.err.println(IP + ": " + ping + " ms");

                code = connection.getResponseCode();
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, "Wrond URL adress: " + IP, e);

                code = WRONG_URL;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "I/O problem occured by adress (timeout probably): " + IP);

                code = TIMEOUT;
            } finally {
                connection.disconnect();
            }
        } else {
            code = STREAM_CLOSED;
        }
    }

    synchronized public String getHttpCode() {
        String httpsAnswer;
        if (code == HttpsAPI.WRONG_URL) {
            httpsAnswer = "Сервер не принимает https запросы";
        } else if (code == HttpsAPI.TIMEOUT) {
            httpsAnswer = "Превышено время ожидания запроса...";
        } else if (code == HttpsAPI.STREAM_CLOSED) {
            httpsAnswer = "Возникла непредвиденная ошибка";
        } else if (code == HttpsAPI.WAITING) {
            httpsAnswer = "Отправка запроса...";
        } else {
            httpsAnswer = code + "";
        }

        return httpsAnswer;
    }

    public boolean isEnabled() {
        return isWorking;
    }

    public void shutdown() {
        isWorking = false;
    }

//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//        System.err.println(IP + " finalize httpsAPI");
//    }
}
