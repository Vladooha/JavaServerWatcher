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
    private static boolean isButtonShows;

    @FXML
    private Label simplePopupMessageTextField;
    
    @FXML
    private Button simplePopupOkButton;

    @FXML
    void initialize() {
        simplePopupMessageTextField.setText(message);

        if (isButtonShows) {
            simplePopupOkButton.setOnAction(e -> close());
        } else {
            simplePopupOkButton.setVisible(false);
        }
    }

    public void setupWindow(String message) {
        setupWindow(message, true);
    }

    public void setupWindow(String message, boolean isButtonShows) {
        try {
            SimplePopup.message = message;
            SimplePopup.isButtonShows = isButtonShows;

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
            logger.log(Level.SEVERE, "Can't host error message =(");
        }
    }

    public void close() {
        window.close();
    }
}
