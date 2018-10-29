package com.karmazin.controller;

import java.io.IOException;
import java.util.logging.Level;

import com.karmazin.model.LoggerAPI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SimplePopup {
    private static LoggerAPI logger = new LoggerAPI(LoginScreen.class.getName());

    private static String xml;

    private static Stage window;
    private static Scene scene;

    private static String message;

    @FXML
    private Label simplePopupMessageTextField;
    
    @FXML
    private Button simplePopupOkButton;

    @FXML
    void initialize() {
        simplePopupMessageTextField.setText(message);

        simplePopupOkButton.setOnAction(e -> window.close());
    }

    void setupWindow(String message) {
        try {
            this.message = message;

            xml = "/fxml/simplePopupScreen.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(xml));

            window = new Stage(StageStyle.UNDECORATED);
            scene = new Scene(root);
            window.setScene(scene);
            window.setResizable(false);
            window.initModality(Modality.APPLICATION_MODAL);
            window.showAndWait();

            logger.log(Level.INFO, "Error popup: " + message);
        } catch (IOException e) {
            logger.log(Level.INFO, "Can't host error message =(");
        }
    }
}
