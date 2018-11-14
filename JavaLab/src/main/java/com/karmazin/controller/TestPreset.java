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
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TestPreset {
    private static LoggerAPI logger = new LoggerAPI(TestPreset.class.getName());

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
    private TextField testWindowsTextfield;

    @FXML
    void initialize() {
        testOkButton.setOnAction(event -> {
            try {
                int windowsCount = Integer.parseInt(testWindowsTextfield.getText());
                List<Integer> tabsCount = new ArrayList<>();
                tabsCount.add(Integer.parseInt(testTabs1Textfield.getText()));
                tabsCount.add(Integer.parseInt(testTabs2Textfield.getText()));
                tabsCount.add(Integer.parseInt(testTabs3Textfield.getText()));

                logger.log(Level.INFO, "Test started: " + windowsCount +
                        " windows with " + tabsCount + "tabs each");

                new Thread(() -> {
                    SelfTester.startTabsTest(windowsCount, tabsCount);
                }).start();
            } catch (Exception e) {
            }
        });
    }

    public void setupWindow() {
        try {
            xml = "/fxml/testPresetScreen.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(xml));

            window = new Stage();
            //window = new Stage(StageStyle.UNDECORATED);
            scene = new Scene(root);
            window.setScene(scene);
            window.setResizable(false);
            window.initModality(Modality.APPLICATION_MODAL);
            window.showAndWait();

            logger.log(Level.INFO, "Test window setted up!");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't host test setup window =(", e);
        }
    }
}

