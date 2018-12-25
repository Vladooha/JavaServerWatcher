package com.karmazin.controller;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import com.karmazin.model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class WorkScreen {
    public class MainTab {
        private Thread rotationThread;

        @FXML
        private Button menuSettingsButton;
        @FXML
        private Button menuStatisticButton;
        @FXML
        private Button menuServListButton;
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

        @FXML
        void initialize() {
            menuImage.setImage(new Image("/pngs/budgie.png"));

            // Listeners
            menuServListButton.setOnAction((event) -> {
                File file = new FileChooser().showOpenDialog(window);
                if (file != null) {

                    SimplePopup alert = new SimplePopup();

                    new Thread(() -> {
                        try {
                            Scanner scan = new Scanner(file);

                            if (!scan.hasNext()) {
                                throw new Exception();
                            }

                            while (scan.hasNext()) {
                                String next = scan.nextLine();
                                String adress = next.split("\\,")[1];
                                Platform.runLater(() -> adressTextField.setText(adress));
                                Platform.runLater(() -> addServerButton.fire());
                            }

                            Platform.runLater(() -> adressTextField.setText(""));
                            Platform.runLater(() -> alert.close());
                        } catch (Exception e) {
                            Platform.runLater(() -> alert.close());

                            Platform.runLater(() ->
                                    alert.setupWindow("Ошибка при чтении файла!\nСкорее всего он имеет неверный формат."));
                        }
                    }).start();

                    alert.setupWindow("Подождите, файл обрабатывается...", false);
                }
            });

            menuStatisticButton.setOnAction(event -> {
                new StatisticsScreen().setupWindow();
            });

            if (UserAPI.getStatus().equals(UserAPI.UserType.Developer)) {
                menuDebugButton.setOnAction(event -> {
                    if (ConfigAPI.getDebug()) {
                        logger.log(Level.INFO, "Debug disabled");
                        ConfigAPI.setDebug(false);
                        logger.muteMode(true);
                    } else {
                        ConfigAPI.setDebug(true);
                        logger.muteMode(false);
                        logger.log(Level.INFO, "Debug enabled");
                    }
                });

                menuTestButton.setOnAction(event -> {
                    ConfigAPI.setSelfTest(true);

                    new TestScreen().setupWindow();
                });
            } else {
                menuDebugButton.setManaged(false);
                menuTestButton.setManaged(false);
                //menuDebugButton.setDisable(true);
                //menuTestButton.setDisable(true);
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
                ConfigAPI.unlogin();
                clearSessionData();

                new LoginScreen().setupWindow(window);
            });
        }

        public Tab setup() {
            Tab tab = new Tab("Панель управления");
            tab.setClosable(false);
            tab.setStyle("-fx-background-color: rgba(0, 150, 225, 0.35);");
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setController(this);
                Pane root = (Pane) loader.load(
                        getClass().getResourceAsStream("/fxml/menuTab.fxml"));
                root.setStyle("-fx-background-color: rgba(255, 255, 255, 1);");
                tab.setContent(root);

//                tab.setOnSelectionChanged((event) -> {
//                    Task<Void> rotate = new Task<Void>() {
//                        @Override
//                        protected Void call() {
//                            if (tab.isSelected()) {
//                                for(int i = 0; i < 16 && tab.isSelected(); ++i) {
//                                    final int corner = i * 24;
//                                    menuImage.setRotate(corner);
//
//                                    if (i == 15) {
//                                        i = 0;
//                                    }
//
//                                    if (i % 8 == 0) {
//                                        menuImage.setImage(new Image("/pngs/budgie.png"));
//                                        System.err.println("You spin me right round, baby");
//                                    }
//                                    if (i % 8 == 2) {
//                                        menuImage.setImage(new Image("/pngs/budgie1.png"));
//                                        System.err.println("Right round like a record, baby");
//                                    }
//                                    if (i % 8 == 4) {
//                                        menuImage.setImage(new Image("/pngs/budgie2.png"));
//                                        System.err.println("Right round");
//                                    }
//                                    if (i % 8 == 6) {
//                                        menuImage.setImage(new Image("/pngs/budgie3.png"));
//                                        System.err.println("Round round");
//                                    }
//
//                                    try {
//                                        Thread.sleep(50, 10);
//                                    } catch (InterruptedException e) {
//                                        Thread.interrupted();
//                                    }
//                                }
//                            }
//
//                            return null;
//                        }
//                    };
//
//
//
//                    if (tab.isSelected()) {
//                        new Thread(rotate).start();
//                    }
//
//                });
            } catch (IOException e) {
                tab.setContent(new Label("MAIN TAB ERROR!"));
            }

            return tab;
        }
    }

    private LoggerAPI logger = new LoggerAPI(WorkScreen.class.getName());

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
    private TextArea consoleTextArea;

    // Map picture variables
    private final YandexMapsAPI yandexMapsAPI = new YandexMapsAPI(300, 300);

    // Server ping variables
    private static Map<String, Thread> threadMap = new HashMap<>();
    private static Map<String, ServerPingWatcher> serversMap = new HashMap<>();
    private static Map<String, HttpWatcher> httpsMap = new HashMap<>();
    private static Map<String, ServerDataContainer> serverDataMap = new HashMap<>();

    @FXML
    void initialize() {
        // View pre-set
        Tab startTab = new MainTab().setup();
        tabTabPanel.getTabs().add(startTab);

        // Listeners
        addServerButton.setOnAction(event -> {

            logger.log(Level.INFO, "Adding new server...");

            if (!adressTextField.getText().isEmpty()) {
                // --------------------------------------- MultiThread ----------------------------
                if (UserAPI.getStatus() != UserAPI.UserType.Unauthorized) {
                    // UserAPI has permisson to add server
                    String IP = adressTextField.getText();
                    if (serversMap.containsKey(IP)) {
                        new SimplePopup().setupWindow("Вы уже отслеживаете данный сервер!");
                    } else {
                        Tab newTab = new Tab();
                        newTab.setText(adressTextField.getText());
                        contentPanel(IP, newTab);
                        newTab.setOnCloseRequest((closeEvent) -> {
                            String currIP = newTab.getText();

                            serversMap.get(currIP).shutdown();
                            serversMap.remove(currIP);

                            httpsMap.get(currIP).shutdown();
                            httpsMap.remove(currIP);
                        });

                        tabTabPanel.getTabs().add(newTab);
                        tabTabPanel.getSelectionModel().select(newTab);
                    }

                    logger.log(Level.INFO, "Server " + adressTextField.getText() + " was added!");
                } else {
                    // UserAPI hasn't permission to add server
                    logger.log(Level.INFO, "User isn't authorized!");
                }
            } else {
                logger.log(Level.INFO, "Adress field is empty!");
            }
        });

        delServerButton.setOnAction(event -> {
            List<Tab> toClose = new ArrayList<>();
            for (Tab currTab : tabTabPanel.getTabs()) {
                if (currTab.isClosable()) {
                    String IP = currTab.getText();

                    ServerPingWatcher pinger = serversMap.get(IP);
                    if (pinger != null) {
                        pinger.shutdown();
                    }
                    serversMap.remove(IP);

                    HttpWatcher httpLabler = httpsMap.get(IP);
                    if (httpLabler != null) {
                        httpLabler.shutdown();
                    }
                    httpsMap.remove(IP);

                    Thread thread = threadMap.get(IP);
                    if (thread != null) {
                        thread.stop();
                    }
                    threadMap.remove(IP);

                    String tabName = currTab.getText();

                    toClose.add(currTab);

                    logger.log(Level.INFO, "Tab '" + tabName + "' was colsed");
                }
            }

            tabTabPanel.getTabs().removeAll(toClose);
        });

        // Graphic sub-task
        Task<Void> cpuLoadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!isCancelled()) {
                    if (ProcessWatcher.isCpuOverloaded()) {
                        cpuLoadLabel.setTextFill(Color.rgb(240, 10, 10));
                    } else {
                        cpuLoadLabel.setTextFill(Color.rgb(0, 0, 0));
                    }
                    updateMessage("Загрузка CPU: " + ProcessWatcher.getCpuLoad() + "%");

                    try {
                        Thread.sleep(ConfigAPI.getProcessDelay());
                    } catch (InterruptedException e) { }
                }

                return null;
            }
        };

        Task<Void> memLoadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!isCancelled()) {
                    if (ProcessWatcher.isMemLack()) {
                        memFreeLabel.setTextFill(Color.rgb(240, 10, 10));
                    } else {
                        memFreeLabel.setTextFill(Color.rgb(0, 0, 0));
                    }
                    updateMessage(String.format("Свободно памяти: %.2f", ProcessWatcher.getFreeMemPerc() * 100) + " %");

                    try {
                        Thread.sleep(ConfigAPI.getProcessDelay());
                    } catch (InterruptedException e) { }
                }

                return null;
            }
        };

        cpuLoadLabel.textProperty().bind(cpuLoadTask.messageProperty());
        memFreeLabel.textProperty().bind(memLoadTask.messageProperty());

        new Thread(cpuLoadTask).start();
        new Thread(memLoadTask).start();

//        Task<Void> consoleTask = new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {
//                //StringBuilder string = new StringBuilder();
//
//
//
//                return null;
//            }
//        };
//
//        consoleTextArea.textProperty().bind(consoleTask.messageProperty());

//        new Thread(consoleTask).start();

        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                consoleTextArea.appendText(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                consoleTextArea.appendText(new String(b, off, len));
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
        window.setTitle("Pingovshique");
        window.setWidth(ConfigAPI.getResolution().getValue());
        window.setHeight(ConfigAPI.getResolution().getKey());
        window.setScene(scene);
        window.setResizable(true);
        window.setOnCloseRequest(event -> {
            clearSessionData();

            logger.log(Level.INFO, "Program was closed. Bye!");
            System.exit(0);
        });
        window.show();

        ProcessWatcher localWatch = ProcessWatcher.getProcessWatcher();
        new Thread(localWatch).start();
    }

    public void setupTestWindow(List<String> servers) {
        try {
            xml = "/fxml/workScreen.fxml";
            FXMLLoader loader = new FXMLLoader();
            InputStream xmlStream = getClass().getResourceAsStream(xml);
            Parent root = (Parent) loader.load(xmlStream);
            xmlStream.close();

            scene = new Scene(root);

            window = new Stage();
            window.setTitle("Pingovshique ver.0.(0)2");
            window.setWidth(ConfigAPI.getResolution().getValue());
            window.setHeight(ConfigAPI.getResolution().getKey());
            window.setScene(scene);
            window.setResizable(true);
            window.show();
        } catch (IOException e) { }
    }

    public static ServerDataContainer getServerData(String IP) {
        return serverDataMap.get(IP);
    }

    /// * Help-methods *

    // Creates tab content
    private boolean contentPanel(String IP, Tab tab) {
        logger.log(Level.INFO, "Generating view elements in tab " + IP);

        VBox contentPanel = new VBox();
        contentPanel.setMouseTransparent(true);
        contentPanel.setPadding(new Insets(10, 50, 10, 50));
        contentPanel.setStyle("-fx-background-color: #FFFFFF;");
        contentPanel.setSpacing(50);
        contentPanel.setAlignment(Pos.BOTTOM_CENTER);

        HBox infoPanel = new HBox();
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.setSpacing(20);
        infoPanel.setMinHeight(200);
        infoPanel.setPrefHeight(300);
        infoPanel.setMaxHeight(550);

        Label graphLabel = new Label("Получение информации...");
        graphLabel.setFont(new Font("Arial", 20));
        graphLabel.setAlignment(Pos.CENTER);
        graphLabel.setPadding(new Insets(10.0));
        graphLabel.setWrapText(true);

        ImageView image = new ImageView(new Image("/pngs/alert-128.png"));

        infoPanel.getChildren().add(graphLabel);
        infoPanel.getChildren().add(image);

        LineChart chart = createChart(IP);
        chart.setMaxHeight(250);
        //System.err.println("Chart of '" + IP + "' created!");
        contentPanel.getChildren().add(infoPanel);

        HttpWatcher httpWatcher = new HttpWatcher();
        httpsMap.put(IP, httpWatcher);

        String extendedMessage = graphLabel.getText() +
                "\n\nЕсли это сообщение долго не исчезает, то возможны следующие проблемы:\n" +
                "\t- Вы совершили слишком много запросов и geoAPI временно забанил вас.\n" +
                "\t  Повторите запрос через некоторое время.\n" +
                "\t- Сервер, который выхотите отслеживать, выключен или не существует\n" +
                "\t- У вас могут быть неполадки с интернет соединением";

        Thread thread = new Thread(() -> {
            boolean repeat;

            long beginTime = System.currentTimeMillis();
            do {
                try {
                    System.err.println("DO BEGIN");

                    repeat = false;

                    //System.err.println("Http of '" + IP + "' opened!");

                    //System.err.println("Pic of '" + IP + "' downloaded!");

                    GeolocationAPIData geoData = GeolocationAPI.sendRequest(IP);
                    if (geoData != null) {
                        //System.err.println("Geo of '" + IP + "' recieved!");

                        image.setImage(yandexMapsAPI.getMapImageIcon(geoData.getLat(), geoData.getLon()));

                        serverDataMap.put(IP, new ServerDataContainer(image, chart, geoData.toString()));
                    }

                    //System.err.println("Data of '" + IP + "' saved!");

                    List<Node> nodeList = new ArrayList<>();
                    nodeList.add(chart);
                    nodeList.addAll(contentPanel.getChildren());
                    Platform.runLater(() -> {
//                        for (Node node : nodeList) {
//                            if (!contentPanel.getChildren().contains(node)) {
//                                if (node instanceof LineChart) {
//                                    if (((LineChart) node).getData().size() > 0) {
//                                        XYChart.Series seria = (XYChart.Series) ((LineChart) node).getData().get(0);
//                                        for (Object data : seria.getData()) {
//                                            if ((Integer)((XYChart.Data)data).getYValue() > 0) {
//                                                contentPanel.getChildren().add(node);
//                                                break;
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    contentPanel.getChildren().add(node);
//                                }
//                            }
//                        }
                        for (Node node : nodeList) {
                            if (!contentPanel.getChildren().contains(node)) {
                                contentPanel.getChildren().add(node);
                            }
                        }
                    });

                    //System.err.println("Node list of '" + IP + "' updated!");

                    int httpDelay = ConfigAPI.getHttpDelay();
                    while (httpWatcher.isEnabled() && geoData != null) {

                        System.err.println("WHILE BEGIN");

                        httpWatcher.startRecievingHttp(IP);
                        if (tab.isSelected()) {
                            Platform.runLater(() -> {
                                graphLabel.setText("Информация о сервере:\n\n" + geoData.toString() +
                                        "\nHTTPS код: " + httpWatcher.getHttpCode());
                            });
                        }

                        try {
                            Thread.sleep(httpDelay);
                        } catch (InterruptedException e) {
                            //System.err.println("Interupt");
                        }
                    }

                    if (System.currentTimeMillis() - beginTime > 3500) {
                        Platform.runLater(() -> {
                            graphLabel.setText(extendedMessage);
                            graphLabel.setFont(new Font("Arial", 13));
                        });
                        System.err.println("Font change");
                    } else { }

                    try {
                        Thread.sleep(ConfigAPI.getPingDelay());
                    } catch (InterruptedException e) { }

                    continue;

                    //System.err.println("Thread '" + IP + "' is off...");
                } catch (Exception e) {
                    Platform.runLater(() -> graphLabel.setText(extendedMessage));

                    repeat = true;
                    continue;
                }
            } while (repeat);
        });

        threadMap.put(IP, thread);
        thread.start();

        tab.setContent(contentPanel);
        return true;
    }
    // Creates a ping chart (part of tab content)
    // TODO Scroll
    private LineChart createChart(String IP) {
        // Hosting charts
        ObservableList<XYChart.Data<String, Integer>> XYList = FXCollections.observableArrayList();
        ObservableList<String> xAxisCategories = FXCollections.observableArrayList();

        // Creating axis for chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setTickMarkVisible(false);

        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Время (мс)");


        int delay = ConfigAPI.getPingDelay();

        // Adding listener for control x axis
        XYList.addListener((ListChangeListener<XYChart.Data<String, Integer>>) change -> {
            // TODO Select right range time
            // 10 seconds in range of x axis
            if (change.getList().size() > 50) {
                xAxis.getCategories().remove(0);
            }
        });

        // Сreating new chart
        LineChart<String,Number> chart = new LineChart<>(xAxis,yAxis);
        chart.setTitle("Ping");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setMinHeight(150);
        chart.setMinWidth(500);


        // TODO Add byte size to config
        serversMap.put(IP, new ServerPingWatcher(IP));

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-5);
        xAxis.setTickLabelsVisible(false);
        xAxis.setAutoRanging(true);

        // creating task for update values
        Task<Date> task = new Task<Date>() {
            @Override
            protected Date call() {
                while (serversMap.containsKey(IP) && serversMap.get(IP).isRunning()) {
                    try {
                        Thread.sleep(delay + 50);
                    } catch (InterruptedException ex) {
                        logger.log(Level.INFO, IP + " stream is interrupted");

                        Thread.currentThread().interrupt();
                    }

                    if (isCancelled()) {
                        logger.log(Level.INFO, IP + " stream is broke");

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
                if (serversMap.containsKey(IP) && serversMap.get(IP).isRunning()) {
                    int servTime = serversMap.get(IP).getTime();
                    if (servTime == 0) {
                        XYList.add(new XYChart.Data(strDate, -1));
                    } else {
                        XYList.add(new XYChart.Data(strDate, servTime));
                    }

                    XYChart.Series seria = chart.getData().get(0);
                    if (seria.getData().size() > 50) {
                        seria.getData().remove(0, seria.getData().size() - 50);
                    }

                    int currYVal = (Integer)((XYChart.Data)seria.getData().get(0)).getYValue();
                    int upperBound = currYVal + (5 - (currYVal % 5));
                    for (Object data : seria.getData()) {
                        if ((Integer)((XYChart.Data)data).getYValue() > upperBound) {
                            currYVal = (Integer)((XYChart.Data)data).getYValue();
                            upperBound = currYVal + (5 - (currYVal % 5));
                        }

                    }
                    yAxis.setTickUnit((upperBound + 5) / 5);
                    yAxis.setUpperBound(upperBound);
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

    private void clearSessionData() {
        // Stopping all process
        for (String key : serversMap.keySet()) {
            serversMap.get(key).shutdown();
        }
        serversMap.clear();

        for (String key : httpsMap.keySet()) {
            httpsMap.get(key).shutdown();
        }
        httpsMap.clear();

        // Saving settings
        ConfigAPI.setResolution((int)window.getHeight(), (int)window.getWidth());
        ConfigAPI.setSelfTest(false);

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
}
