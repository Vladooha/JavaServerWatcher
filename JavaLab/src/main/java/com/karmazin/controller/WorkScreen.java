package com.karmazin.controller;

import java.io.File;
import java.io.IOException;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class WorkScreen {
    private static LoggerAPI logger = new LoggerAPI(WorkScreen.class.getName());

    private String xml;

    private boolean isTest;
    private int testCapacity;

    private static Stage window;
    private Scene scene;

    @FXML
    private TabPane tabTabPanel;

    @FXML
    private Button delServerButton;

    @FXML
    private Button addServerButton;

    @FXML
    private TextField adressTextField;

    // Map picture variables
    private static final YandexMapsAPI yandexMapsAPI = new YandexMapsAPI(400, 400);

    // Server ping variables
    private ArrayList<Long> timers;
    private Map<String, ServerPingWatcher> serversMap = new HashMap<>();
    private Map<String, HttpWatcher> httpsMap = new HashMap<>();
    private final int delay = 400;

    private static int tabCounter = 0;

    // Initialize JavaFX-method (invokes before setupWindow())
    @FXML
    void initialize() {
        // Start tab pre-set
        Tab startTab = createMainTab();
        tabTabPanel.getTabs().add(startTab);

        //window = (Stage) addServerButton.getScene().getWindow();

        if (this == null)
            addServerButton.setText("GOVNO");

        // 'Добавить сервер' button logic
        addServerButton.setOnAction(event -> {
            tabCounter++;

            logger.log(Level.INFO, "Adding new server...");

            long timingBegin = 0, timingEnd = 0;
            if (isTest) {
                timingBegin = System.currentTimeMillis();
            }

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
                        newTab.setContent(contentPanel(IP));
                        newTab.setOnCloseRequest((closeEvent) -> delServerButton.fire());

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

            if (isTest) {
                timingEnd = System.currentTimeMillis();
                if (timers.size() < testCapacity) {
                    timers.add(timingEnd - timingBegin);
                }
            }
        });

        // 'Удалить сервер' button logic
        delServerButton.setOnAction(event -> {
            logger.log(Level.INFO, "Deleting a server...");

            if (tabTabPanel.getTabs().size() > 1) {
                if (UserAPI.getStatus() != UserAPI.UserType.Unauthorized) {
                    Tab currTab = tabTabPanel.getSelectionModel().getSelectedItem();
                    logger.log(Level.INFO,"Server " + currTab.getText() + " is deleting...");

                    // UserAPI have authority for operation
                    ServerPingWatcher pinger = serversMap.get(currTab.getText());
                    if (pinger != null) {
                        pinger.shutdown();
                    }
                    serversMap.remove(currTab.getText());

                    HttpWatcher httpLabler = httpsMap.get(currTab.getText());
                    if (httpLabler != null) {
                        httpLabler.shutdown();
                    }
                    httpsMap.remove(currTab.getText());

                    tabTabPanel.getTabs().remove(currTab);

                    logger.log(Level.INFO, "Deletion complited!");
                } else {
                    // UserAPI doesn't have authority for operation
                    logger.log(Level.INFO, "User isn't authorized!");
                }
            } else {
                logger.log(Level.INFO, "Start tab force reload");

                tabTabPanel.getTabs().add(startTab);
                tabTabPanel.getTabs().remove(0);
            }
        });
    }

    public void setupWindow(Stage primaryStage) throws IOException {
        logger.log(Level.INFO, "Creating a work screen...");

        xml = "/fxml/workScreen.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent root = (Parent) loader.load(getClass().getResourceAsStream(xml));

        scene = new Scene(root);

        window = primaryStage;
        window.setTitle("Pingovshique ver.0.(0)2");
        window.setScene(scene);
        window.setResizable(true);
        window.setOnCloseRequest(event -> {
            exitPrep();

            logger.log(Level.INFO, "Program was closed. Bye!");
            System.exit(0);
        });
        window.show();

        isTest = false;

        ProcessWatcher localWatch = new ProcessWatcher();
    }

    public void setupTestWindow(Stage primaryStage, List<String> adresses) {
        try {
            logger.log(Level.INFO, "Creating a work screen...");

            xml = "/fxml/workScreen.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(xml));

            scene = new Scene(root);

            window = primaryStage;
            window.setScene(scene);
            window.setOnCloseRequest(event -> {
                exitPrep();

                System.exit(0);
            });

            isTest = true;
            testCapacity = adresses.size();
            timers = new ArrayList<Long>(testCapacity);
        } catch (Exception e) {

        }
    }


    /// Help-methods

    // Creates tab content
    private VBox contentPanel(String IP) {
        logger.log(Level.INFO, "Generating view elements in tab " + IP);

        GeolocationAPIData geoData = ConfigAPI.getGeoData(IP);
        VBox contentPanel = new VBox();
        contentPanel.setMouseTransparent(true);

        if (geoData != null) {
            // UserAPI have authority for operation
            contentPanel.setBackground(Background.EMPTY);

            HBox infoPanel = new HBox();
            //infoPanel.setStyle("-fx-background-color: #FFFFFF");
            infoPanel.setSpacing(20);

            Label graphLabel = new Label(geoData.toString());
            //graphLabel.setPrefSize(200, 200);
            graphLabel.setAlignment(Pos.CENTER);
            graphLabel.setPadding(new Insets(10.0));
            graphLabel.setWrapText(true);

            httpsMap.put(IP, new HttpWatcher());
            Label httpLabel = httpsMap.get(IP).httpCodeLabel(IP, delay);
            httpLabel.setPadding(new Insets(20.0));
            httpLabel.setText("Waiting for http code...");

            VBox textVBox = new VBox();
            textVBox.setPrefSize(400, 200);
            textVBox.setAlignment(Pos.CENTER);
            textVBox.getChildren().add(graphLabel);
            textVBox.getChildren().add(httpLabel);

            ImageView image = new ImageView(yandexMapsAPI.getMapImageIcon(geoData.getLat(), geoData.getLon()));

            infoPanel.getChildren().add(textVBox);
            infoPanel.getChildren().add(image);

            contentPanel.getChildren().add(createChart(IP, delay));
            contentPanel.getChildren().add(infoPanel);
        }

        return contentPanel;
    }

    // Creates a ping chart (part of tab content)
    // TODO Scroll
    private LineChart createChart(String IP, int delay) {
        ObservableList<XYChart.Data<String, Integer>> XYList = FXCollections.observableArrayList();
        ObservableList<String> xAxisCategories = FXCollections.observableArrayList();

        // creating axis for chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("time");
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("ms");

        // adding listener for control x axis
        XYList.addListener((ListChangeListener<XYChart.Data<String, Integer>>) change -> {
            // TODO Select right range time
            // 10 seconds in range of x axis
            if (change.getList().size() > 10*1000/delay) {
                xAxis.getCategories().remove(0);
            }
        });

        // creating new chart
        LineChart<String,Number> chart = new LineChart<>(xAxis,yAxis);
        chart.setTitle("Ping");
        chart.setAnimated(false);
        chart.setLegendVisible(false);

        // TODO Add byte size to config
        serversMap.put(IP, new ServerPingWatcher(IP, delay, 350));

        // creating task for update values
        Task<Date> task = new Task<Date>() {
            @Override
            protected Date call() throws Exception {
                while (serversMap.containsKey(IP) && serversMap.get(IP).isRunning()) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

                    if (isCancelled()) {
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
                    XYList.add(new XYChart.Data(strDate, serversMap.get(IP).getTime()));//ConfigAPI.getServerResponseTime()));
                }
            }
        });

        // execute task
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);

        xAxis.setCategories(xAxisCategories);
        // TODO Visible tick labels change chart height during work
        xAxis.setTickLabelsVisible(false);
        yAxis.setAutoRanging(true);

        // adding data to chart
        chart.getData().add(new XYChart.Series(XYList));

        // removing markers
        chart.setCreateSymbols(false);
        return chart;
    }

    private void exitPrep() {
        // Stopping all process
        for (ServerPingWatcher pinger : serversMap.values()) {
            pinger.shutdown();
        }

        for (HttpWatcher http : httpsMap.values()) {
            http.shutdown();
        }

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

    private Tab createMainTab() {
        Tab tab = new Tab();
        tab.setText("Меню:");
        VBox tabLayout = new VBox();
        tabLayout.setPadding(new Insets(10));
        tabLayout.setSpacing(20.0);
        tabLayout.setAlignment(Pos.CENTER);

        Button fileChooseButton = new Button("Загрузить файл с серверами");
        fileChooseButton.setOnAction((event) -> {
            File file = new FileChooser().showOpenDialog(window);

            SimplePopup alert = new SimplePopup();

//            new Thread(
//                    () -> Platform.runLater(
//                            () -> alert.setupWindow("Подождите, файл обрабатывается...\n" +
//                    "(Мы не висим, просто JavaFX долго отрисовывает вкладки T_T)", false))).start();

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
                        //Thread.sleep(100);
                        System.err.println("I'm workin...");
                    }

                    Platform.runLater(() -> adressTextField.setText(""));
                    Platform.runLater(() -> alert.close());
                } catch (Exception e) {
                    Platform.runLater(() -> alert.close());

                    Platform.runLater(() ->
                            alert.setupWindow("Ошибка при чтении файла!\nСкорее всего он имеет неверный формат."));
                }
            }).start();

            alert.setupWindow("Подождите, файл обрабатывается...\n" +
                    "(Мы не висим, просто JavaFX долго отрисовывает вкладки T_T)", false);


        });

        ObservableList<Node> childrens = tabLayout.getChildren();
        childrens.add(new Label("Приветствую, " + ConfigAPI.getLogin()));
        fileChooseButton.setMaxWidth(250.0);
        fileChooseButton.setMinWidth(250.0);
        childrens.add(fileChooseButton);

        // Developer's additional features
        if (UserAPI.getStatus().equals(UserAPI.UserType.Developer)) {
            // 'Debug' button logic
            Button debugButton = new Button("Отладка");
            debugButton.setOnAction(event -> {
                if (ConfigAPI.getDebug()) {
                    logger.log(Level.INFO, "Debug enabled");
                    ConfigAPI.setDebug(false);
                    logger.muteMode(true);
                } else {
                    ConfigAPI.setDebug(true);
                    logger.muteMode(false);
                    logger.log(Level.INFO, "Debug disabled");
                }
            });
            debugButton.setMaxWidth(250.0);
            debugButton.setMinWidth(250.0);
            childrens.add(debugButton);

            // 'Self tests' button logic
            Button selfTestButton = new Button("Тестирование");
            selfTestButton.setOnAction(event -> {
                ConfigAPI.setSelfTest(true);

                new TestPreset().setupWindow();
            });
            selfTestButton.setMaxWidth(250.0);
            selfTestButton.setMinWidth(250.0);
            childrens.add(selfTestButton);
        }

        // 'Выйти из аккаунта' button logic
        Button exitButton = new Button("Выйти из аккаунта");
        exitButton.setOnAction(event -> {
            ConfigAPI.unlogin();
            exitPrep();

            new LoginScreen().setupWindow(window);
        });
        exitButton.setMaxWidth(250.0);
        exitButton.setMinWidth(250.0);
        childrens.add(exitButton);

        tab.setContent(tabLayout);

        return tab;
    }

    // Test-methods
    public List<Long> getTestResults() {
        if (!isTest) {
            return null;
        } else {
            if (timers.size() < testCapacity) {
                return null;
            } else {
                return timers;
            }
        }
    }
}

