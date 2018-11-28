package com.karmazin.model;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

public class HttpWatcher {
    private static final LoggerAPI logger = new LoggerAPI(HttpWatcher.class.getName());

    private static class Code {
        public static final int WRONG_URL = -1;
        public static final int TIMEOUT = -2;
    }

    private static class HttpParser {
        private static HttpURLConnection connection;

        synchronized public static int getHttpAnswerCode(String query, int delay) {
            logger.log(Level.INFO,"New http url connection: " + query);

            try {
                connection = (HttpURLConnection) new URL("https://" + query).openConnection();
                connection.setUseCaches(false);
                connection.setConnectTimeout(delay);
                connection.setReadTimeout(delay);

                connection.connect();

                return connection.getResponseCode();


//            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//
//            String line;
//            StringBuilder allPage = new StringBuilder();
//            while ((line = input.readLine()) != null) {
//                allPage.append(line + "\r\n");
//            }

            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE,"Wrond URL adress: " + query, e);

                return Code.WRONG_URL;
            } catch (IOException e) {
                logger.log(Level.SEVERE,"I/O problem occured by adress (timeout probably): " + query, e);

                return Code.TIMEOUT;
            } finally {
                connection.disconnect();
            }
        }
    }

    private boolean isWorking;

    //TODO Config await time
    public Label httpCodeLabel(String IP, int delay) {
        return httpCodeLabel(IP, delay, 500);
    }

    public Label httpCodeLabel(String IP, int delay, int awaitTime) {
        if (isWorking) {
            return null;
        } else {
            isWorking = true;

            Label resultLabel = new Label();

            logger.log(Level.INFO,"Http labler [" + IP + "] started!");

            Runnable httpThread = new Runnable() {
                @Override
                public void run() {
                    int code = HttpURLConnection.HTTP_ACCEPTED;

                    while (isWorking && code != Code.WRONG_URL) {
                        //logger.log(Level.INFO,"Updating label [" + IP + "]!");
                        try {
                            if (code == Code.TIMEOUT) {
                                Thread.sleep(awaitTime);
                            }

                            code = HttpParser.getHttpAnswerCode(IP, delay);

                            final int finalCode = code;
                            Platform.runLater(
                                    () -> resultLabel.setText(
                                            "http code: " + String.valueOf(finalCode)));

                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            logger.log(Level.SEVERE,"Http thread [" + IP + "] is interrupted!", e);
                        }
                    }
                }
            };

            new Thread(httpThread).start();

            return resultLabel;
        }
    }

    public void shutdown() { isWorking = false; }
}
