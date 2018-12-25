package com.karmazin.controller;

import com.karmazin.model.LoggerAPI;
import com.karmazin.model.SelfTester;
import javafx.fxml.FXML;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TestScreen {
    private static LoggerAPI logger = new LoggerAPI(TestScreen.class.getName());

    private static String xml;

    private static Stage window;
    private static Scene scene;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button testOkButton;

    @FXML
    private TextField testTabs1Textfield;
    @FXML
    private TextField testTabs2Textfield;
    @FXML
    private TextField testTabs3Textfield;

    @FXML
    private TextField testTestTextfield;

    @FXML
    private TextField testWindowsTextfield;

    @FXML
    private StackedAreaChart<Integer, Long> testChart;

    @FXML
    void initialize() {
        // View pre-set
        testChart.getYAxis().setLabel("Время (мс)");
        testChart.getXAxis().setLabel("Кол-во вкладок");

        // Listeners
        testOkButton.setOnAction(event -> {
            try {
                int windowsCount = Integer.parseInt(testWindowsTextfield.getText());

                List<Integer> tabsCount = new ArrayList<>();
                tabsCount.add(Integer.parseInt(testTabs1Textfield.getText()));
                tabsCount.add(Integer.parseInt(testTabs2Textfield.getText()));
                tabsCount.add(Integer.parseInt(testTabs3Textfield.getText()));

                int testCount = Integer.parseInt(testTestTextfield.getText());

                logger.log(Level.INFO, "Test started: " + windowsCount +
                        " windows with " + tabsCount + "tabs each");

//                ArrayList<XYChart.Series<Integer, Long>> seriaList = new ArrayList<>();
//                seriaList.add(new XYChart.Series<Integer, Long>());
//                seriaList.get(0).setName("Ожидание результата...");
//                //new Thread(() -> {
//                    seriaList.set(0, selfTester.startTabsTest(windowsCount, tabsCount, testCount));
//                //}).start();

                new SimplePopup().setupWindow("Внимание! Для нагрузочного теста могут понадобиться " +
                                "все имеющиеся ресурсы компьютера. Постарайтесь не выполнять никаких фоновых " +
                                "процессов во время теста.");

                testChart.getData().add(new SelfTester().startTabsTest(windowsCount, tabsCount, testCount));

                new SimplePopup().setupWindow("Тест окончен!");

//                if (!testChart.getData().contains(seriaList.get(0))) {
//
//                } else {
//                    new SimplePopup().setupWindow("Результаты теста с такими входными данными уже присутствуют на грфаике." +
//                            "Пожалуйста, введите другие входные данные!");
//                }

            } catch (Exception e) {
                new SimplePopup().setupWindow("Пожалуйста, используйте целые положительные числа!");
            }
        });
    }

    public void setupWindow() {
        try {
            xml = "/fxml/testScreen.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(xml));

            window = new Stage();
            //window = new Stage(StageStyle.UNDECORATED);
            scene = new Scene(root);
            window.setScene(scene);
            window.setResizable(false);
            window.initModality(Modality.APPLICATION_MODAL);
            window.show();

            logger.log(Level.INFO, "Test window setted up!");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't host test setup window =(", e);
        }
    }
}

