package com.karmazin.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.karmazin.model.LoggerAPI;
import com.sun.javafx.charts.Legend;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class StatisticsScreen {
    private static LoggerAPI logger = new LoggerAPI(StatisticsScreen.class.getName());

    private static String xml;
    private static Stage window;
    private static Scene scene;

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;

    @FXML
    private DatePicker statisticDatePickEnd;
    @FXML
    private DatePicker statisticDatePickBegin;

    @FXML
    private ToggleGroup sortGroup;
    @FXML
    private RadioButton statisticButtonSortName;
    @FXML
    private RadioButton statisticButtonSortBetter;
    @FXML
    private RadioButton statisticButtonSortWorst;

    @FXML
    private ToggleGroup modeGroup;
    @FXML
    private RadioButton statisticButtonModeHttp;
    @FXML
    private RadioButton statisticButtonModePing;

    @FXML
    private TextField statisticTextMidtime;
    @FXML
    private TextField statisticTextGoodtime;

    @FXML
    private TextField statisticTextPing;

    @FXML
    private Button statisticButtonOk;

    @FXML
    private ScrollPane statisticScrollPane;
    @FXML
    private AnchorPane statisticScrollAnchorPane;
    @FXML
    private BarChart statisticChartPercent;


    private int counter = 0;
    enum SortOrder {
        NAME,
        BEST_TO_WORST,
        WORST_TO_BEST
    }

    @FXML
    void initialize() {
        // View pre-set
        statisticDatePickBegin.setUserData(LocalDate.now());
        statisticDatePickEnd.setUserData(LocalDate.now());
        statisticChartPercent.setLegendVisible(false);

        // Listeners
        statisticButtonOk.setOnAction((event) -> {
            SortOrder sortOrder = SortOrder.NAME;
            if (statisticButtonSortWorst.isSelected()) {
                sortOrder = SortOrder.WORST_TO_BEST;
            } else if (statisticButtonSortBetter.isSelected()) {
                sortOrder = SortOrder.BEST_TO_WORST;
            }

            String mode = "ping";
            if (statisticButtonModeHttp.isSelected()) {
                mode = "http";
            } else if (statisticButtonModePing.isSelected()) {
                mode = "serv";
            }

            LocalDate begin = statisticDatePickBegin.getValue();
            LocalDate end = statisticDatePickEnd.getValue();

            if (begin == null || end == null) {
                new SimplePopup().setupWindow("Укажите временной промежуток!");
                return;
            } else if (begin.isAfter(end)) {
                new SimplePopup().setupWindow("Неправильно указаны граничные значения дат!");
                return;
            }

            int goodTime;
            int midTime;
            try {
                goodTime = Integer.parseInt(statisticTextGoodtime.getText());
                midTime = Integer.parseInt(statisticTextMidtime.getText());
                if (goodTime <= midTime) {
                    throw new Exception();
                }
            } catch (Exception e) {
                new SimplePopup().setupWindow("Неправильно введено время отклика!");
                return;
            }

            int coolPing;
            try {
                coolPing = Integer.parseInt(statisticTextPing.getText());
            } catch (Exception e) {
                new SimplePopup().setupWindow("Неправильно введено время граничного времени отклика!");
                return;
            }

            setupGraph(begin, end, sortOrder, mode, goodTime, midTime, coolPing);
        });
    }

    public void setupWindow() {
        try {
            xml = "/fxml/statisticsScreenVert.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(xml));

            scene = new Scene(root);

            window = new Stage();
            window.setTitle("Статистика");
            window.setScene(scene);
            window.setResizable(false);
            window.showAndWait();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't open statisticsScreen.fxml", e);
            e.printStackTrace();
        }
    }

    // Help-methods
    private void setupGraph(LocalDate begin, LocalDate end, SortOrder sortOrder,
                            String mode, int goodTime, int midTime, int coolPing) {
        Map<String, Double> pingPercentage = calculatePingPercentage(begin, end, mode, coolPing);

        // Clearing old data
        statisticChartPercent.getData().clear();

        // Adding a new data with bar painting
        XYChart.Series seria = new XYChart.Series<>();
        statisticChartPercent.getData().add(seria);
        for (String IP : pingPercentage.keySet()) {
            XYChart.Data<String, Double> data = new XYChart.Data<String, Double>(IP, pingPercentage.get(IP));
            seria.getData().add(data);
            data.getNode().setStyle("-fx-bar-fill: "
                    .concat(data.getYValue().floatValue() > goodTime ? "green;":
                            data.getYValue().floatValue() > midTime ? "yellow;"
                                    : "red"));
            data.getNode().setOpacity(0.65);
        }

        // Sorting added seria
        CategoryAxis xAxis = (CategoryAxis) statisticChartPercent.getXAxis();
        ObservableList xAxisCategories = xAxis.getCategories();

        if (sortOrder == SortOrder.BEST_TO_WORST) {
            xAxisCategories.sort((o1, o2) -> {
                if (pingPercentage.get(o1) < pingPercentage.get(o2)) {
                    return 1;
                } else if (pingPercentage.get(o1) > pingPercentage.get(o2)) {
                    return -1;
                } else {
                    return 0;
                }
            });
        } else if (sortOrder == SortOrder.WORST_TO_BEST) {
            xAxisCategories.sort((o1, o2) -> {
                if (pingPercentage.get(o1) > pingPercentage.get(o2)) {
                    return 1;
                } else if (pingPercentage.get(o1) < pingPercentage.get(o2)) {
                    return -1;
                } else {
                    return 0;
                }
            });

        } else {
            xAxisCategories.sort((o1, o2) -> {
                return ((String)o1).compareTo((String)o2);
            });
        }

        xAxis.setCategories(xAxisCategories);
        xAxis.setAutoRanging(true);

        // Legend setup
        statisticChartPercent.setLegendVisible(true);
        statisticChartPercent.setLegendSide(Side.TOP);
        Legend legend = (Legend)statisticChartPercent.lookup(".chart-legend");
        Legend.LegendItem li1 = new Legend.LegendItem("Быстрые сервера",
                new Rectangle(10, 4, Color.GREEN));
        Legend.LegendItem li2 = new Legend.LegendItem("Средние сервера",
                new Rectangle(10,4, Color.YELLOW));
        Legend.LegendItem li3 = new Legend.LegendItem("Медленные сервера",
                new Rectangle(10,4, Color.RED));
        legend.getItems().setAll(li1, li2, li3);

        // Scroll positions pre-set
        if (pingPercentage.keySet().size() > 10) {
            statisticChartPercent.setMinWidth(985.0 + (pingPercentage.keySet().size() - 10) * 100);
            statisticScrollAnchorPane.setMinWidth(985.0 + (pingPercentage.keySet().size() - 10) * 100);
        } else {
            statisticChartPercent.setMinWidth(985.0);
            statisticScrollAnchorPane.setMinWidth(985.0);
        }
    }

    private Map<String, Double> calculatePingPercentage(LocalDate begin, LocalDate end, String logType, int coolPing) {
        class ServerPingValues {
            int goodPing;
            int allPing;

            ServerPingValues(int _goodPing, int _allPing) {
                goodPing = _goodPing;
                allPing = _allPing;
            }
        }

        Map<String, ServerPingValues> serverSummaryPing = new HashMap<>();

        File logsDir = new File("./logs");
        String path = logsDir.getPath();

        while (!begin.isAfter(end)) {
            File log = new File(path + "/" +
                    logType + "Log_" +
                    begin.getDayOfMonth() + "_" +
                    begin.getMonthValue() + "_" +
                    begin.getYear() + ".txt");
            if (log.exists()) {
                logger.log(Level.INFO, "I found serverLog: '" + log.getName() + "'");

                try {
                    Scanner scan = new Scanner(log);
                    while (scan.hasNext()) {
                        String singlePing = scan.nextLine();

                        Pattern ipPattern = Pattern.compile("\\|{1,1}[^\\|]*\\|{1,1}");
                        Matcher ipMatcher = ipPattern.matcher(singlePing);
                        if (ipMatcher.find()) {
                            Pattern pingPattern = Pattern.compile("\\d+$");
                            Matcher pingMatcher = pingPattern.matcher(singlePing);
                            if (pingMatcher.find()) {
                                String key = ipMatcher.group();
                                double ping = Double.parseDouble(pingMatcher.group());

                                ServerPingValues singlePingData;
                                if (serverSummaryPing.containsKey(key)) {
                                    singlePingData = serverSummaryPing.get(key);
                                } else {
                                    logger.log(Level.INFO, "Adding new server: " + key );
                                    singlePingData = new ServerPingValues(0, 0);
                                }
                                singlePingData.allPing += 1;
                                if (ping != 0 && ping <= coolPing) {
                                    singlePingData.goodPing += 1;
                                }

                                serverSummaryPing.put(key, singlePingData);
                            } else {
                                throw new NoSuchFieldException();
                            }
                        } else {
                            throw new NoSuchFieldException();
                        }
                    }
                    scan.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "I/O error occured during readin '" + log.getName() + "'");
                    continue;
                } catch (NoSuchFieldException e) {
                    logger.log(Level.SEVERE, "Wrong logger format at '" + log.getName() + "'");
                    continue;
                }
            }

            begin = begin.plusDays(1);
        }

//        for (File log : logsDir.listFiles()) {
//            if (log.getName().startsWith("servLog_")) {
//                logger.log(Level.INFO, "I found serverLog: '" + log.getName() + "'");
//
//                try {
//                    Scanner scan = new Scanner(log);
//                    while (scan.hasNext()) {
//                        String singlePing = scan.nextLine();
//
//                        Pattern ipPattern = Pattern.compile("\\|{1,1}[^\\|]*\\|{1,1}");
//                        Matcher ipMatcher = ipPattern.matcher(singlePing);
//                        if (ipMatcher.find()) {
//                            Pattern pingPattern = Pattern.compile("\\d+$");
//                            Matcher pingMatcher = pingPattern.matcher(singlePing);
//                            if (pingMatcher.find()) {
//                                String key = ipMatcher.group();
//                                double ping = Double.parseDouble(pingMatcher.group());
//
//                                ServerPingValues singlePingData;
//                                if (serverSummaryPing.containsKey(key)) {
//                                    singlePingData = serverSummaryPing.get(key);
//                                } else {
//                                    logger.log(Level.INFO, "Adding new server: " + key );
//                                    singlePingData = new ServerPingValues(0, 0);
//                                }
//                                singlePingData.allPing += 1;
//                                if (ping != 0 && ping <= coolPing) {
//                                    singlePingData.goodPing += 1;
//                                }
//
//                                serverSummaryPing.put(key, singlePingData);
//                            } else {
//                                throw new NoSuchFieldException();
//                            }
//                        } else {
//                            throw new NoSuchFieldException();
//                        }
//                    }
//                    scan.close();
//                } catch (IOException e) {
//                    logger.log(Level.SEVERE, "I/O error occured during readin '" + log.getName() + "'");
//                    continue;
//                } catch (NoSuchFieldException e) {
//                    logger.log(Level.SEVERE, "Wrong logger format at '" + log.getName() + "'");
//                    continue;
//                }
//            }
//        }

        Map<String, Double> serverPingPercentage = new HashMap<>();
        for (String IP : serverSummaryPing.keySet()) {
            ServerPingValues values = serverSummaryPing.get(IP);
            serverPingPercentage.put(IP, values.goodPing / (double)values.allPing * 100);
        }

        return serverPingPercentage;
    }
}