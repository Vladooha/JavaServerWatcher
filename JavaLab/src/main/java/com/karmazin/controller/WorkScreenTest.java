package com.karmazin.controller;

import com.karmazin.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkScreenTest {
    public Stage windowTest;

    public TextField adressServerTextFieldTest;
    public Button addServerButtonTest;
    public Button delServerButtonTest;
    public TabPane serversTabPaneTest;

    private class ServerTab extends Tab {
        @FXML
        private Label serverTabGeoLabel;
        @FXML
        private ImageView serverTabGeoImage;
        @FXML
        private LineChart serverTabPingChart;

        private String IP;

        private HttpsAPI httpsApi;
        private GeolocationAPI geoData;
        private AtomicBoolean isImageSet;

        private PingAPI pingWatcher;
        private ArrayList<Integer> pingBuffer;

        // Creating a view of a tab
        public ServerTab(String serverIP) {
            super(serverIP);

            IP = serverIP;

            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setController(this);
                Pane root = (Pane) loader.load(
                        getClass().getResourceAsStream("/fxml/serverTab.fxml"));
                setContent(root);

                geoData = null;
                isImageSet = new AtomicBoolean(false);
                setupGeo();

                pingBuffer = new ArrayList<>(55);
                setupChart();

                serverTabGeoImage.setVisible(false);
                serverTabPingChart.setVisible(false);
                serverTabGeoLabel.setVisible(false);
            } catch (IOException e) {
                setContent(new Label("Файлы программы повреждены.\nДальнейшая работа невозможна!"));
            }
        }

        @FXML
        void initialize() {
            setOnSelectionChanged((event) -> {
                serverTabGeoImage.setVisible(isSelected());
                serverTabPingChart.setVisible(isSelected());
                serverTabGeoLabel.setVisible(isSelected());
            });

            setOnClosed((event) -> {
//                if (serversMap.containsKey(IP)) {
//                    serversMap.get(IP).shutdown();
//                    serversMap.remove(IP);
//                }
//
//                if (httpsMap.containsKey(IP)) {
//                    httpsMap.get(IP).shutdown();
//                    httpsMap.remove(IP);
//                }
                shutdown();

                if (graphUpdatersMap.containsKey(IP)) {
                    graphUpdatersMap.get(IP).cancel();
                    graphUpdatersMap.get(IP).purge();
                    graphUpdatersMap.remove(IP);
                }

                if (geoUpdatersMap.containsKey(IP)) {
                    geoUpdatersMap.get(IP).cancel();
                    geoUpdatersMap.get(IP).purge();
                    geoUpdatersMap.remove(IP);
                }

                if (serverDataMap.containsKey(IP)) {
                    serverDataMap.remove(IP);
                }
            });
        }

        private void setupGeo() {
            String unreacheableMessage = serverTabGeoLabel.getText() +
                    "\n\nЕсли это сообщение долго не исчезает, то возможны следующие проблемы:\n" +
                    "\t- Вы совершили слишком много запросов и geoAPI временно забанил вас.\n" +
                    "\t  Повторите запрос через некоторое время.\n" +
                    "\t- Сервер, который выхотите отслеживать, выключен или не существует\n" +
                    "\t- У вас могут быть неполадки с интернет соединением";

            httpsApi = new HttpsAPI(IP);
            //httpsMap.put(IP, httpsApi);

            // Adding to task map for geo scheduler
            Timer geoUpdater = new Timer("GeoTimer_" + IP);
            TimerTask geoUpdateTask = new TimerTask() {
                @Override
                public void run() {
                    //if (httpsMap.containsKey(IP) && httpsApi.isEnabled()) {
                    if (geoUpdatersMap.containsKey(IP) && httpsApi.isEnabled()) {
                        // TODO Remove GeolocationAPIData and refactor YandexMapsAPI
                        // TODO Hidden GUI optimization

                        boolean tabSelected = ServerTab.this.isSelected();

                        if (geoData == null) {
                            // Trying to get data again
                            geoData = GeolocationAPI.sendRequest(IP);
                        }

                        if (geoData != null) {
                            // Success

                            // Getting https code
                            httpsApi.startRecievingHttp();

                            // Updating GUI if tab opened
                            if (tabSelected) {
                                Platform.runLater(() ->
                                        serverTabGeoLabel.setText(
                                                geoData.toString() +
                                                        "\nHTTPS код: " +
                                                        httpsApi.getHttpCode()));

                                if (!isImageSet.get()) {
                                    Image geoImage = yandexMapsAPI.getMapImageIcon(geoData.getLat(), geoData.getLon());

                                    Platform.runLater(() -> {
                                        isImageSet.set(true);

                                        serverTabGeoImage.setImage(geoImage);
                                    });
                                }
                            }
                        } else {
                            // Fail

                            // Updating GUI if tab opened
                            if (tabSelected) {
                                Platform.runLater(() -> serverTabGeoLabel.setText(unreacheableMessage));
                            }

                            return;
                        }

                        return;
                    } else {
                        httpsApi.shutdown();
                        geoUpdater.cancel();
                        geoUpdater.purge();
                    }
                }
            };

            geoUpdater.schedule(geoUpdateTask, 0, ConfigWrapper.getHttpDelay());
            geoUpdatersMap.put(IP, geoUpdater);
        }

        // Creates a ping chart (part of server tab content)
        private void setupChart() {
            // X axis chart pre-set
            final CategoryAxis xAxis = (CategoryAxis)serverTabPingChart.getXAxis();
            ObservableList<String> xAxisCategories = xAxis.getCategories();
            xAxis.setTickMarkVisible(false);
            xAxis.setTickLabelsVisible(false);
            xAxis.setAutoRanging(true);

            // Y axis chart pre-set
            final NumberAxis yAxis = (NumberAxis)serverTabPingChart.getYAxis();
            yAxis.setLabel("Время (мс)");
            yAxis.setLowerBound(-5);
            yAxis.setAutoRanging(false);

            // Listener for axes
            ObservableList<XYChart.Data<String, Integer>> XYList = FXCollections.observableArrayList();
            XYList.addListener((ListChangeListener<XYChart.Data<String, Integer>>) change -> {
                if (change.getList().size() > 50 && xAxis.getCategories().size() > 0) {
                    xAxis.getCategories().remove(0);
                }
            });

            // Chart pre-set
            serverTabPingChart.getData().add(new XYChart.Series(XYList));
            serverTabPingChart.setCreateSymbols(false);
            serverTabPingChart.setTitle("Ping");
            serverTabPingChart.setAnimated(false);
            serverTabPingChart.setLegendVisible(false);
            serverTabPingChart.setVerticalGridLinesVisible(false);
            serverTabPingChart.setMinHeight(150);
            serverTabPingChart.setMinWidth(500);

            // TODO Add byte size to config
            // PingWatcher pre-set for chart
            pingWatcher = new PingAPI(IP, 32);
            //serversMap.put(IP, pingWatcher);

            // Adding to task map for ping scheduler
            Timer graphDataUpdater = new Timer("GraphTimer_" + IP);
            // Graph data updater task pre-set
            TimerTask graphDataUpdateTask = new TimerTask() {
                @Override
                public void run() {
                    if (graphUpdatersMap.containsKey(IP) && pingWatcher.isTurnedOn()) {
                        // TODO Situative ping
                        // TODO Value buffer for unselected tabs
                        // TODO Email sending
                        int ping = pingWatcher.ping(IP);

                        if (ping == PingAPI.TURNED_OFF_CODE || ping == PingAPI.UNREACHEABLE_CODE) {
                            ping = -1;
                        }

                        while (pingBuffer.size() > 49) {
                            pingBuffer.remove(0);
                        }
                        pingBuffer.add(ping);

                        if (ServerTab.this.isSelected()) {
                            // Start painting graph from last view
//                            String date = System.nanoTime() + "";
//                            XYChart.Data<String, Integer> value = new XYChart.Data<>(date, ping);
//
//                            updateValue(value);

                            // Load last values
                            forcelyUpdateGraph();
                        }

                        return;
                    } else {
                        System.err.println("Graph " + IP + " dying...");
                        pingWatcher.shutdown();
                        graphDataUpdater.cancel();
                        graphDataUpdater.purge();
                    }
                }

                // Graph data updater listener (Graph visual updater) pre-set
                private void updateValue(XYChart.Data<String, Integer> value) {
                    //if (value != null && serversMap.containsKey(IP) && serversMap.get(IP).isTurnedOn()) {
                    if (value != null && graphUpdatersMap.containsKey(IP) && pingWatcher.isTurnedOn()) {
                        String date = value.getXValue();
                        int ping = value.getYValue();

                        // Adding new point to graph (-1 means unreacheable server)
                        xAxisCategories.add(date);
                        XYList.add(value);

                        // Removing excess points from graph
                        XYChart.Series seria = (XYChart.Series)serverTabPingChart.getData().get(0);
                        if (seria.getData().size() > 50) {
                            seria.getData().remove(0, seria.getData().size() - 50);
                        }

                        // Updating Y axis' upper bound (with 5 ms in reserve)
                        int upperBound = -1337;
                        for (int i = 0; i < seria.getData().size(); ++i) {
                            Object objData = seria.getData().get(i);

                            XYChart.Data<String, Integer> graphData = (XYChart.Data<String, Integer>)objData;
                            if (graphData.getYValue() > upperBound) {
                                upperBound = graphData.getYValue() + (5 - (graphData.getYValue() % 5));
                            }
                        }

                        if (yAxis.getUpperBound() != upperBound) {
                            final int newUpperBound = upperBound;

                            Platform.runLater(() -> {
                                yAxis.setUpperBound(newUpperBound);

                                // Updating tick unit
                                yAxis.setTickUnit((newUpperBound + 5) / 5);
                            });
                        }
                    }
                }

                private void clearGraph() {
                    Platform.runLater(() -> {
                        serverTabPingChart.getData().set(0, new XYChart.Series<String, Integer>());
                    });
                }
                private void forcelyUpdateGraph() {
                    if (graphUpdatersMap.containsKey(IP) && pingWatcher.isTurnedOn()) {
                        int size = pingBuffer.size();
                        for (int i = 0; i < size; ++i) {
                            updateValue(new XYChart.Data<String, Integer>(System.nanoTime() + "", pingBuffer.get(i)));
                        }
                    }
                }
            };

            graphDataUpdater.schedule(graphDataUpdateTask, 0, ConfigWrapper.getPingDelay());
            graphUpdatersMap.put(IP, graphDataUpdater);
        }

        public void shutdown() {
            httpsApi.shutdown();
            pingWatcher.shutdown();
        }
    }

    // Map picture variables
    private final YandexMapsAPI yandexMapsAPI = new YandexMapsAPI(300, 300);

    // Server ping variables
    private Map<String, Timer> graphUpdatersMap = new HashMap<>();
    private Map<String, Timer> geoUpdatersMap = new HashMap<>();
    private Map<String, ServerDataContainer> serverDataMap = new HashMap<>();

    public WorkScreenTest() {
        windowTest = new Stage();

        serversTabPaneTest = new TabPane();
        adressServerTextFieldTest = new TextField();
        addServerButtonTest = new Button("Добавить");
        delServerButtonTest = new Button("Удалить");

        addServerButtonTest.setOnAction((event) -> {
            if (!adressServerTextFieldTest.getText().isEmpty()) {
                    String IP = adressServerTextFieldTest.getText();
                    ServerTab serverTab = new ServerTab(IP);
                    serversTabPaneTest.getTabs().add(serverTab);
                    serversTabPaneTest.getSelectionModel().select(serverTab);
                    serverTab = null;
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
        for (Tab tab : serversTabPaneTest.getTabs()) {
            if (tab instanceof ServerTab) {
                ServerTab serverTab = (ServerTab) tab;
                serverTab.shutdown();
            }
        }

        for (String key : graphUpdatersMap.keySet()) {
            graphUpdatersMap.get(key).cancel();
            graphUpdatersMap.get(key).purge();
        }
        graphUpdatersMap.clear();

        for (String key : geoUpdatersMap.keySet()) {
            geoUpdatersMap.get(key).cancel();
            geoUpdatersMap.get(key).purge();
        }
        geoUpdatersMap.clear();

        serverDataMap.clear();

        new Thread(() -> {
            for (int i = 0; i < 3; ++i) {
                System.gc();

                try {
                    Thread.sleep(4000);
                } catch (Exception e) { }
            }
        }).start();

        windowTest.close();
    }
}
