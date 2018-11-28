package com.karmazin.controller;

import com.karmazin.model.LoggerAPI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class OneElementScreen {
    private static LoggerAPI logger = new LoggerAPI(OneElementScreen.class.getName());

    private Stage window;
    private Scene scene;

    private Node node;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    void initialize() {

    }

    public void setupWindow(Node node) {
        this.node = node;

        FXMLLoader loader = new FXMLLoader();
        BorderPane root = new BorderPane();
        root.setCenter(node);

        window = new Stage(StageStyle.DECORATED);
        scene = new Scene(root);
        window.setScene(scene);
        //window.initModality(Modality.APPLICATION_MODAL);

        window.show();

        logger.log(Level.INFO, "One element window '" + node + "' created");
    }

    public void close() {
        window.close();
    }
}

