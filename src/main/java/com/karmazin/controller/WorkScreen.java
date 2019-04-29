package com.karmazin.controller;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import com.karmazin.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.mail.MessagingException;

public class WorkScreen {
    private static LoggerAPI logger = new LoggerAPI(WorkScreen.class.getName());

    private class MainTab extends Tab {
        @FXML
        private Button menuSettingsButton;
        @FXML
        private Button menuStatisticButton;
        @FXML
        private Button menuServListButton;
        @FXML
        private Button menuTabsListButton;
        @FXML
        private Button menuTestButton;
        @FXML
        private Button menuAboutButton;
        @FXML
        private Button menuDebugButton;
        @FXML
        private Button menuExitButton;

        @FXML
        private ImageView menuImage;

        private Thread rotateThread;
        private int easterEggCounter = 0;
        private boolean isEasterEggOn = false;

        @FXML
        void initialize() {
            // View pre-set
            menuImage.setImage(new Image("/pngs/budgie.png"));

            // Graphic sub-task
            Task<Void> rotate = new Task<Void>() {
                @Override
                protected Void call() {
                boolean infinityCondition = true;

                Media media = null;
                try {
                    media = new Media(
                            getClass().getResource("/music/YouSpinMeRound.mp3").toURI().toString());
                } catch (URISyntaxException e) { }
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(0.1);
                player.setOnEndOfMedia(() -> player.seek(Duration.ZERO));

                player.setMute(true);

                int i = -1;
                while (infinityCondition)
                {
                    if (MainTab.this.isSelected() && isEasterEggOn) {
                        if (player.isMute() && media != null) {
                            player.setMute(false);
                            player.play();
                            //System.err.println("Playing");
                        }

                        ++i;
                        if (i == 15) {
                            i = 0;
                        }

                        final int corner = i * 24;
                        menuImage.setRotate(corner);

                        if (i % 8 == 0) {
                            menuImage.setImage(new Image("/pngs/budgie.png"));
                            System.err.println("You spin me right round, baby");
                        }
                        if (i % 8 == 2) {
                            menuImage.setImage(new Image("/pngs/budgie1.png"));
                            System.err.println("Right round like a record, baby");
                        }
                        if (i % 8 == 4) {
                            menuImage.setImage(new Image("/pngs/budgie2.png"));
                            System.err.println("Right round");
                        }
                        if (i % 8 == 6) {
                            menuImage.setImage(new Image("/pngs/budgie3.png"));
                            System.err.println("Round round\n");
                        }
                    } else {
                        if (!player.isMute() && media != null) {
                            player.pause();
                            player.setMute(true);
                            //System.err.println("Muted");
                        }
                    }

                    try {
                        Thread.sleep(50, 10);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }

                return null;
                }
            };

            rotateThread = new Thread(rotate);
            rotateThread.start();

            // Listeners
            menuImage.setOnMouseClicked((event) -> {
                if (easterEggCounter < 3) {
                    easterEggCounter++;
                } else {
                    if (!isEasterEggOn) {
                        isEasterEggOn = true;
                    } else {
                        isEasterEggOn = false;
                    }
                }
            });

            menuServListButton.setOnAction((event) -> {
                File file = new FileChooser().showOpenDialog(window);
                if (file != null) {

                    SimplePopup alert = new SimplePopup();

                    new Thread(() -> {
                        try {
                            int counter = 0;
                            System.gc();

                            Platform.runLater(() -> alert.setupWindow("Подождите, файл обрабатывается..."));

                            Scanner scan = new Scanner(file);

                            if (!scan.hasNext()) {
                                throw new Exception();
                            }

                            while (scan.hasNext() && tabTabPanel.getTabs().size() <= TABS_LIMIT) {
                                String next = scan.nextLine();
                                String adress = next.split("\\,")[1];
                                Platform.runLater(() -> {
                                    if (tabTabPanel.getTabs().size() <= TABS_LIMIT) {
                                        adressTextField.setText(adress);
                                        addServerButton.fire();
                                    }
                                });

                                ++counter;

                                Thread.sleep(50);
                            }

                            if (tabTabPanel.getTabs().size() >= TABS_LIMIT) {
                                final int counterBuff = counter;
                                Platform.runLater(() -> {
                                    new SimplePopup()
                                            .forceSetupWindow("Открыты только первые " + counterBuff  + " вкладок файла,\n" +
                                                    "т.к. достигинут лимит вкладок в " + TABS_LIMIT + " штук\n" +
                                                    "или вы попытались открыть вкладки с уже отслеживаеми адресами!");
                                });
                            } else {
                                Platform.runLater(() -> alert.close());
                            }
                        } catch (Exception e) {
                            Platform.runLater(() ->
                                    new SimplePopup()
                                            .setupWindow("Ошибка при чтении файла!\n" +
                                                    "Скорее всего он имеет неверный формат."));
                        }
                    }).start();
                }
            });

            menuTabsListButton.setOnAction((event) -> {
                SimplePopup alert = new SimplePopup();
                ObservableList<Tab> tabs = tabTabPanel.getTabs();
                if (tabs.size() < 2) {
                    alert.setupWindow("Не открыто ни одной вкладки!");
                } else {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.getExtensionFilters()
                            .add(new FileChooser.ExtensionFilter("Список серверов", "*.tabs"));
                    File file = fileChooser.showSaveDialog(window);
                    if (file != null) {
                        new Thread(() -> {
                            try (FileWriter writer = new FileWriter(file, false)) {

                                Platform.runLater(() -> alert.setupWindow("Подождите, идёт запись в файл...", false));

                                for (int i = 1; i < tabs.size(); ++i) {
                                    writer.append(i + "," + tabs.get(i).getText());
                                }
                                writer.flush();

                                Platform.runLater(() -> alert.forceSetupWindow("Вкладки экспортированы!"));

                            } catch (IOException e) {
                                Platform.runLater(() -> {
                                    alert.close();

                                    alert.setupWindow("Ошибка при записи файла!");
                                });
                            }
                        }).start();
                    }
                }
            });

            menuStatisticButton.setOnAction(event -> {
                new StatisticsScreen().setupWindow();
            });

            if (UserAPI.getStatus().equals(UserAPI.UserType.Developer)) {
                menuDebugButton.setOnAction(event -> {
                    if (ConfigWrapper.getDebug()) {
                        logger.log(Level.INFO, "Debug disabled");
                        ConfigWrapper.setDebug(false);
                        logger.muteMode(true);
                    } else {
                        ConfigWrapper.setDebug(true);
                        logger.muteMode(false);
                        logger.log(Level.INFO, "Debug enabled");
                    }
                });

                menuTestButton.setOnAction(event -> {
                    ConfigWrapper.setSelfTest(true);

                    new TestScreen().setupWindow();
                });
            } else {
                menuDebugButton.setManaged(false);
                menuTestButton.setManaged(false);
            }

            menuSettingsButton.setOnAction((event) -> new SettingsScreen().setupWindow());

            menuAboutButton.setOnAction((event) -> new SimplePopup().setupWindow(
                    "Разработчики:\n" +
                            "\tКармазин Василий\n" +
                            "\tСмирнов Артём\n" +
                            "\tМежуев Владислав\n" +
                            "\nСпециально для кафедры ИПОВС",
                    "/pngs/miet_logo.png"
            ));

            menuExitButton.setOnAction(event -> {
                ConfigWrapper.unlogin();
                clearSessionData();

                new LoginScreen().setupWindow(window);
            });
        }

        public MainTab() {
            super("Панель управления");

            setClosable(false);
            setStyle("-fx-background-color: rgba(0, 150, 225, 0.35);");
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setController(this);
                Pane root = (Pane) loader.load(
                        getClass().getResourceAsStream("/fxml/mainTab.fxml"));
                root.setStyle("-fx-background-color: rgba(255, 255, 255, 1);");
                setContent(root);


            } catch (IOException e) {
                setContent(new Label("Файлы программы повреждены.\nДальнейшая работа невозможна!"));
            }
        }
    }

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
        private int unreacheableCount;
        private int unreacheableLimit;
        private long lastEmailTime;

        // Creating a view of a tab
        public ServerTab(String serverIP) {
            super(serverIP);

            IP = serverIP;

            logger.log(Level.INFO, "Generating view elements in " + IP + " tab");

//            VBox contentPanel = new VBox();
//            contentPanel.setMouseTransparent(true);
//            contentPanel.setPadding(new Insets(10, 50, 10, 50));
//            contentPanel.setStyle("-fx-background-color: #FFFFFF;");
//            contentPanel.setSpacing(50);
//            contentPanel.setAlignment(Pos.BOTTOM_CENTER);
//
//            HBox infoPanel = new HBox();
//            infoPanel.setAlignment(Pos.CENTER);
//            infoPanel.setSpacing(20);
//            infoPanel.setMinHeight(200);
//            infoPanel.setPrefHeight(300);
//            infoPanel.setMaxHeight(550);
//
//            Label geoLabel;
//            ImageView geoImage;
//            LineChart pingChart;
//
//            geoLabel = new Label("Получение информации...");
//            geoLabel.setFont(new Font("Arial", 20));
//            geoLabel.setAlignment(Pos.CENTER);
//            geoLabel.setPadding(new Insets(10.0));
//            geoLabel.setWrapText(true);
//
//            geoImage = new ImageView(new Image("/pngs/alert-128.png"));
//
//            infoPanel.getChildren().add(geoLabel);
//            infoPanel.getChildren().add(geoImage);
//
//            pingChart = new LineChart(new CategoryAxis(), new NumberAxis());
//            pingChart.setMaxHeight(250);
//
//            infoPanel.getChildren().addAll(geoLabel, geoImage);
//            contentPanel.getChildren().addAll(infoPanel, pingChart);
//
//            setContent(contentPanel);

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
                unreacheableCount = 0;
                unreacheableLimit = ConfigWrapper.getPingFaults();
                lastEmailTime = 0;
                setupChart();

                serverTabGeoImage.setVisible(false);
                serverTabPingChart.setVisible(false);
                serverTabGeoLabel.setVisible(false);
            } catch (IOException e) {
                setContent(new Label("Файлы программы повреждены.\nДальнейшая работа невозможна!"));
            }

            logger.log(Level.INFO, "Chart of '" + IP + "' created!");
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

                shutdown();
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
                        //System.err.println(IP + " began!");
                        // TODO Situative ping
                        // TODO Value buffer for unselected tabs
                        // TODO Email sending
                        int ping = pingWatcher.ping(IP);
                        //System.err.println(IP + ": " + ping);

                        boolean isPingProblem = ping == PingAPI.TURNED_OFF_CODE ||
                                ping == PingAPI.UNREACHEABLE_CODE ||
                                ping == PingAPI.TIMEOUT_CODE;

                        if (isPingProblem) {
                            ping = -1;
                            ++unreacheableCount;
                        } else {
                            unreacheableCount = 0;
                        }

                        while (pingBuffer.size() > 49) {
                            pingBuffer.remove(0);
                        }
                        pingBuffer.add(ping);

                        if (ServerTab.this.isSelected()) {
                            // Start painting graph from last view
                            String date = System.nanoTime() + "";
                            XYChart.Data<String, Integer> value = new XYChart.Data<>(date, ping);

                            updateValue(value);

                            // Load last values
//                            forcelyUpdateGraph();
                        }

                        if (unreacheableCount >= unreacheableLimit && geoData != null && isImageSet.get()) {
                            //System.err.println("Wanna send email...");
                            if (System.currentTimeMillis() - lastEmailTime > ConfigWrapper.getPingEmail() * 60000) {
                                //System.err.println("Have time for it...");
                                // Adding info for EmailAPI
                                serverDataMap.put(IP,
                                        new ServerDataContainer(serverTabGeoImage, serverTabPingChart, geoData));
                                forcelyUpdateGraph();
                                EmailAPI email = new EmailAPI(
                                        "javaexamplesas",
                                        "Qwerty1337");
                                try {
                                    email.serverFailureMessage(ConfigWrapper.getEmail(),
                                            "Сервер '" + IP + "' упал",
                                            IP);
                                    lastEmailTime = System.currentTimeMillis();
                                } catch (MessagingException e) {
//                                    System.err.println("Got an error... " + e.getMessage());
//                                    e.printStackTrace();
                                } catch (IOException e) {
//                                    System.err.println("Got an error... " + e.getMessage());
//                                    e.printStackTrace();
                                }
                            }
                        }
//                        else {
//                            System.err.println(unreacheableCount + " | " + (geoData == null));
//                        }

                        return;
                    } else {
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
                    if (!ServerTab.this.isSelected()) {
                        ServerDataContainer data = serverDataMap.get(IP);

                        XYChart.Series seria = new XYChart.Series();
                        NumberAxis yAxis = new NumberAxis(
                                "Время (мс)",
                                -5,
                                5,
                                5);
                        LineChart newChart = new LineChart(
                                new CategoryAxis(), yAxis);
                        newChart.setMaxHeight(ServerDataContainer.GRAPH_HEIGHT);
                        newChart.setPrefHeight(ServerDataContainer.GRAPH_HEIGHT);
                        newChart.setMinHeight(ServerDataContainer.GRAPH_HEIGHT);

                        int biggest = -1337;
                        for (int i = 0; i < pingBuffer.size(); ++i) {
                            int ping = pingBuffer.get(i);

                            seria.getData().add(new XYChart.Data<>(i + "", ping));
                            if (ping > biggest) {
                                biggest = ping;
                            }
                            biggest = biggest + (5 - (biggest % 5));
                        }

                        yAxis.setUpperBound(biggest);
                        yAxis.setTickUnit((biggest + 5) / 5);
                        yAxis.setAutoRanging(false);

                        newChart.getData().add(seria);

                        data.updateGraph(newChart);
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

    private String xml;

    private static Stage window;
    private Scene scene;

    /// * Global HUD elements *
    @FXML
    private TabPane tabTabPanel;
    @FXML
    private Button delServerButton;
    @FXML
    private Button addServerButton;
    @FXML
    private TextField adressTextField;

    @FXML
    private Label cpuLoadLabel;
    @FXML
    private Label memFreeLabel;

    @FXML
    private SplitPane splitPaneMain;
    @FXML
    private TextArea consoleTextArea;

    // Map picture variables
    private final YandexMapsAPI yandexMapsAPI = new YandexMapsAPI(300, 300);

    // Server ping variables
//    private static Map<String, PingAPI> serversMap = new HashMap<>();
//    private static Map<String, HttpsAPI> httpsMap = new HashMap<>();
    private static Map<String, Timer> graphUpdatersMap = new HashMap<>();
    private static Map<String, Timer> geoUpdatersMap = new HashMap<>();
    private static Map<String, ServerDataContainer> serverDataMap = new HashMap<>();

    final int TABS_LIMIT = 100;

    @FXML
    void initialize() {
        // View pre-set
        Tab startTab = new MainTab();
        tabTabPanel.getTabs().add(startTab);

        consoleTextArea.setMinHeight(0);
        consoleTextArea.setPrefHeight(0);
        consoleTextArea.setMaxHeight(0);

        // Listeners
        addServerButton.setOnAction(event -> {

            logger.log(Level.INFO, "Adding new server...");

            if (tabTabPanel.getTabs().size() <= TABS_LIMIT) {
                if (!adressTextField.getText().isEmpty()) {
                    if (UserAPI.getStatus() != UserAPI.UserType.Unauthorized) {
                        // User has permisson to add server
                        String IP = adressTextField.getText();
                        if (graphUpdatersMap.containsKey(IP) || geoUpdatersMap.containsKey(IP)) {
                            new SimplePopup().setupWindow("Вы уже отслеживаете данный сервер!");
                        } else {
                            ServerTab serverTab = new ServerTab(IP);
                            tabTabPanel.getTabs().add(serverTab);
                            tabTabPanel.getSelectionModel().select(serverTab);

                            logger.log(Level.INFO, "Server " + adressTextField.getText() + " was added!");
                        }
                    } else {
                        // UserAPI hasn't permission to add server
                        logger.log(Level.INFO, "User isn't authorized!");
                    }
                } else {
                    logger.log(Level.INFO, "Adress field is empty!");
                }
            } else {
                new SimplePopup().setupWindow("Максимальное кол-во вкладок - " + TABS_LIMIT + " штук!");
            }
        });

        delServerButton.setOnAction(event -> {
            stopAllTabsProcess();

            for (Tab tab : tabTabPanel.getTabs()) {
                if (tab instanceof ServerTab) {
                    ServerTab serverTab = (ServerTab) tab;
                    serverTab.shutdown();
                }
            }

            tabTabPanel.getTabs().clear();
            tabTabPanel.getTabs().add(startTab);
        });

        splitPaneMain.setOnMouseEntered((event) -> consoleTextArea.setMaxHeight(200));


        // Graphic sub-task
        Timeline cpuLoadUpdater = new Timeline(new KeyFrame(Duration.millis(ConfigWrapper.getProcessDelay()),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (SysLoadAPI.isCpuOverloaded()) {
                            cpuLoadLabel.setTextFill(Color.rgb(240, 10, 10));
                        } else {
                            cpuLoadLabel.setTextFill(Color.rgb(0, 0, 0));
                        }
                        cpuLoadLabel.setText("Загрузка CPU: " + SysLoadAPI.getCpuLoad() + "%");
                    }
        }));
        cpuLoadUpdater.setCycleCount(Timeline.INDEFINITE);
        cpuLoadUpdater.play();

        Timeline memLoadUpdater = new Timeline(new KeyFrame(Duration.millis(ConfigWrapper.getProcessDelay()),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if (SysLoadAPI.isMemLack()) {
                            memFreeLabel.setTextFill(Color.rgb(240, 10, 10));
                        } else {
                            memFreeLabel.setTextFill(Color.rgb(0, 0, 0));
                        }
                        memFreeLabel.setText(String.format("Свободно памяти: %.2f %%", SysLoadAPI.getFreeMemPerc()));
                    }
                }));
        memLoadUpdater.setCycleCount(Timeline.INDEFINITE);
        memLoadUpdater.play();

        // Console-to-GUI binding
        OutputStream out = new OutputStream() {
            private static final int strLimit = 500;

            @Override
            public void write(int b) throws IOException {
                Platform.runLater(() -> {
                    String[] consoleLines = consoleTextArea.getText().split("\n");
                    int strCount = consoleLines.length;

                    if (strCount > strLimit) {
                        consoleTextArea.setText("");

                        StringBuilder consoleBuff = new StringBuilder("");
                        for (int i = strCount - strLimit; i < strCount; ++i) {
                            consoleBuff.append(consoleLines[i] + "\n");
                        }
                        consoleBuff.delete(consoleBuff.length() - 1, consoleBuff.length());

                        consoleTextArea.setText(consoleBuff.toString());
                    }

                    try {
                        consoleTextArea.appendText(new String(String.valueOf((char) b).getBytes( "UTF-8")));
                    } catch (UnsupportedEncodingException e) {
                        consoleTextArea.appendText(String.valueOf((char) b));
                    }
                });
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    public void setupWindow(Stage primaryStage) throws IOException {

        xml = "/fxml/workScreen.fxml";
        FXMLLoader loader = new FXMLLoader();
        InputStream xmlStream = getClass().getResourceAsStream(xml);
        Parent root = (Parent) loader.load(xmlStream);
        xmlStream.close();

        scene = new Scene(root);

        window = primaryStage;
        window.setTitle("Pingovsheeque");
        window.setWidth(ConfigWrapper.getResolution().getValue());
        window.setHeight(ConfigWrapper.getResolution().getKey());
        window.setScene(scene);
        window.setResizable(true);
        window.setOnCloseRequest(event -> {
            clearSessionData();

            logger.log(Level.INFO, "Program was closed. Bye!");
            System.exit(0);
        });

        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) ->
                ConfigWrapper.setResolution((int)window.getHeight(), (int)window.getWidth());
        window.heightProperty().addListener(stageSizeListener);
        window.widthProperty().addListener(stageSizeListener);
        window.show();

        SysLoadAPI localWatch = SysLoadAPI.getSysLoadAPI();
        new Thread(localWatch).start();

        new Thread(() -> {
            boolean absolutlyTruth = true;

            while (absolutlyTruth) {
                System.gc();

                try {
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                }
            }
        }, "FlushAttempt").start();
    }

    public static ServerDataContainer getServerData(String IP) {
        return serverDataMap.get(IP);
    }

    /// * Help-methods *

    private void clearSessionData() {
        stopAllTabsProcess();

        // Saving settings
        ConfigWrapper.setResolution((int)window.getHeight(), (int)window.getWidth());
        ConfigWrapper.setSelfTest(false);

        // Deleting temporary files
        try {
            File folder = new File(".temp");
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    logger.log(Level.INFO,"Removing temporary file: " + file.getName());
                    file.delete();
                }
            }

            folder.delete();
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "Temporary files can't be deleted", e);
        }

        window.close();
    }

    private void stopAllTabsProcess() {
//        for (String key : serversMap.keySet()) {
//            serversMap.get(key).shutdown();
//        }
//        serversMap.clear();
//
//        for (String key : httpsMap.keySet()) {
//            httpsMap.get(key).shutdown();
//        }
//        httpsMap.clear();

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
    }
}
