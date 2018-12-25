package com.karmazin.controller;

import com.karmazin.model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkScreenTest {
    public Stage windowTest;

    public TextField adressServerTextFieldTest;
    public Button addServerButtonTest;
    public Button delServerButtonTest;
    public TabPane serversTabPaneTest;

    private List<Thread> threadList = new ArrayList<>();

    private Map<String, ServerPingWatcher> serversMapTest = new HashMap<>();
    private Map<String, HttpWatcher> httpsMapTest = new HashMap<>();

    public WorkScreenTest() {
        windowTest = new Stage();

        serversTabPaneTest = new TabPane();
        adressServerTextFieldTest = new TextField();
        addServerButtonTest = new Button("Добавить");
        delServerButtonTest = new Button("Удалить");

        addServerButtonTest.setOnAction((event) -> {
            if (!adressServerTextFieldTest.getText().isEmpty()) {
                // --------------------------------------- MultiThread ----------------------------
                if (UserAPI.getStatus() != UserAPI.UserType.Unauthorized) {
                    // UserAPI has permisson to add server
                    String IP = adressServerTextFieldTest.getText();
                    if (serversMapTest.containsKey(IP)) {
                        new SimplePopup().setupWindow("Вы уже отслеживаете данный сервер!");
                    } else {
                        Tab newTab = new Tab();
                        newTab.setText(IP);
                        contentPanel(IP, newTab);
                        newTab.setOnCloseRequest((closeEvent) -> {
                            String currIP = newTab.getText();

                            serversMapTest.get(currIP).shutdown();
                            serversMapTest.remove(currIP);

                            httpsMapTest.get(currIP).shutdown();
                            httpsMapTest.remove(currIP);
                        });

                        serversTabPaneTest.getTabs().add(newTab);
                        serversTabPaneTest.getSelectionModel().select(newTab);
                    }

                } else {
                    // UserAPI hasn't permission to add server
                }
            } else {
            }
        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(
                serversTabPaneTest,
                adressServerTextFieldTest,
                addServerButtonTest,
                delServerButtonTest
        );

        Scene scene = new Scene(vBox, 600, 400);
        windowTest.setScene(scene);
        windowTest.show();
        windowTest.hide();
    }

    public void close() {
        for (String key : serversMapTest.keySet()) {
            serversMapTest.get(key).shutdown();
        }
        serversMapTest.clear();

        for (String key : httpsMapTest.keySet()) {
            httpsMapTest.get(key).shutdown();
        }
        httpsMapTest.clear();

        for (Thread thread : threadList) {
            thread.stop();
        }
        threadList.clear();

        windowTest.close();
    }

    private boolean contentPanel(String IP, Tab tab) {
        VBox contentPanel = new VBox();
        contentPanel.setMouseTransparent(true);
        contentPanel.setPadding(new Insets(10, 50, 10, 50));
        contentPanel.setStyle("-fx-background-color: #FFFFFF;");

        HBox infoPanel = new HBox();
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.setSpacing(20);

        Label graphLabel = new Label("Получение информации...");
        graphLabel.setFont(new Font("Arial", 25));
        graphLabel.setAlignment(Pos.CENTER);
        graphLabel.setPadding(new Insets(10.0));
        graphLabel.setWrapText(true);

        ImageView image = new ImageView(new Image("/pngs/alert-128.png"));

        infoPanel.getChildren().add(graphLabel);
        infoPanel.getChildren().add(image);

        LineChart chart = createChart(IP);
        //System.err.println("Chart of '" + IP + "' created!");
        contentPanel.getChildren().add(infoPanel);

        Thread thread = new Thread(() -> {
            boolean repeat;

            String extendedMessage = graphLabel.getText() +
                    "\n\nЕсли это сообщение долго не исчезает, то возможны следующие проблемы:\n" +
                    "\t- Сервер, который выхотите отслеживать, выключен или не существует\n" +
                    "\t- У вас могут быть неполадки с интернет соединением";

            do {
                try {
                    repeat = false;

                    HttpWatcher httpWatcher = new HttpWatcher();
                    httpsMapTest.put(IP, httpWatcher);

                    //System.err.println("Http of '" + IP + "' opened!");

                    //System.err.println("Pic of '" + IP + "' downloaded!");

                    GeolocationAPIData geoData = GeolocationAPI.sendRequest(IP);
                    String geoDataStr = geoData.toString();

                    //System.err.println("Geo of '" + IP + "' recieved!");

                    YandexMapsAPI yandexMapsAPI = new YandexMapsAPI(400, 400);
                    image.setImage(yandexMapsAPI.getMapImageIcon(geoData.getLat(), geoData.getLon()));

                    //serverDataMap.put(IP, new ServerDataContainer(image, chart, geoData.toString()));

                    //System.err.println("Data of '" + IP + "' saved!");

                    List<Node> nodeList = new ArrayList<>();
                    nodeList.add(chart);
                    nodeList.addAll(contentPanel.getChildren());
                    Platform.runLater(() -> {
                        contentPanel.getChildren().setAll(nodeList);
                    });

                    //System.err.println("Node list of '" + IP + "' updated!");

                    int httpDelay = ConfigAPI.getHttpDelay();
                    httpWatcher.startRecievingHttp(IP);
                    while (httpWatcher.isEnabled()) {

                        if (tab.isSelected()) {
                            Platform.runLater(() -> {
                                graphLabel.setText("Информация о сервере:\n\n" + geoDataStr +
                                        "\nHTTPS код: " + httpWatcher.getHttpCode());
                            });
                        }

                        try {
                            Thread.sleep(httpDelay);
                        } catch (InterruptedException e) {
                            //System.err.println("Interupt");
                        }
                    }

                    //System.err.println("Thread '" + IP + "' is off...");
                } catch (Exception e) {
                    Platform.runLater(() -> graphLabel.setText(extendedMessage));

                    e.printStackTrace();
                    repeat = true;
                    continue;
                }
            } while (repeat);
        });

        threadList.add(thread);
        thread.start();

        tab.setContent(contentPanel);
        return true;
    }

    private LineChart createChart(String IP) {
        // Hosting charts
        ObservableList<XYChart.Data<String, Integer>> XYList = FXCollections.observableArrayList();
        ObservableList<String> xAxisCategories = FXCollections.observableArrayList();

        // Creating axis for chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("time");
        xAxis.setTickMarkVisible(false);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("ms");

        int delay = ConfigAPI.getPingDelay();

        // Adding listener for control x axis
        XYList.addListener((ListChangeListener<XYChart.Data<String, Integer>>) change -> {
            // TODO Select right range time
            // 10 seconds in range of x axis
            if (change.getList().size() > 50) {
                xAxis.getCategories().remove(0);
            }
        });

        // creating new chart
        LineChart<String,Number> chart = new LineChart<>(xAxis,yAxis);
        chart.setTitle("Ping");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setVerticalGridLinesVisible(false);

        // TODO Add byte size to config
        serversMapTest.put(IP, new ServerPingWatcher(IP));

//        yAxis.setForceZeroInRange(true);
//        yAxis.setLowerBound(0.0);
        yAxis.setAutoRanging(true);
        xAxis.setTickLabelsVisible(false);
        xAxis.setAutoRanging(true);

        // creating task for update values
        Task<Date> task = new Task<Date>() {
            @Override
            protected Date call() {
                while (serversMapTest.containsKey(IP) && serversMapTest.get(IP).isRunning()) {
                    try {
                        Thread.sleep(delay + 50);
                    } catch (InterruptedException ex) {
                        //logger.log(Level.INFO, IP + " stream is interrupted");

                        Thread.currentThread().interrupt();
                    }

                    if (isCancelled()) {
                        //logger.log(Level.INFO, IP + " stream is broke");

                        break;
                    }

                    updateValue(new Date());
                }

                return new Date();
            }
        };

        task.valueProperty().addListener(new ChangeListener<Date>() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SS");

            @Override
            public void changed(ObservableValue<? extends Date> observableValue, Date oldDate, Date newDate) {
                String strDate = dateFormat.format(newDate);

                xAxisCategories.add(strDate);
                if (serversMapTest.containsKey(IP) && serversMapTest.get(IP).isRunning()) {
                    int servTime = serversMapTest.get(IP).getTime();
                    if (servTime == 0) {
                        XYList.add(new XYChart.Data(strDate, -1.0));
                    } else {
                        XYList.add(new XYChart.Data(strDate, servTime));
                    }

                    //logger.log(Level.INFO, "Adding " + servTime + " to " + IP);
                    //XYList.add(new XYChart.Data(strDate, servTime));//ConfigAPI.getServerResponseTime()));
                }
            }
        });

        // execute task
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);

        xAxis.setCategories(xAxisCategories);
        // TODO Visible tick labels change chart height during work

        // adding data to chart
        chart.getData().add(new XYChart.Series(XYList));

        // removing markers
        chart.setCreateSymbols(false);
        return chart;
    }
}
