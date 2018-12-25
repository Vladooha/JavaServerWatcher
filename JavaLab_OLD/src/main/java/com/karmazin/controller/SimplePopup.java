package com.karmazin.controller;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import com.karmazin.model.LoggerAPI;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class SimplePopup {
    private static LoggerAPI logger = new LoggerAPI(LoginScreen.class.getName());

    private static String xml;

    private static Stage window;
    private static Scene scene;

    private static String message;
    private static String pictureUrl;
    private static boolean isButtonShows;

    @FXML
    private Label simplePopupMessageTextField;

    @FXML
    private ImageView simplePopupImage;
    
    @FXML
    private Button simplePopupOkButton;

    @FXML
    void initialize() {

        if (message.length() < 35) {
            StringBuilder newMessage= new StringBuilder();
            for (int i = 35; i > message.length(); --i) {
                newMessage.append(" ");
            }
            newMessage.append(message);

            simplePopupMessageTextField.setText(newMessage.toString());
        } else {
            simplePopupMessageTextField.setText(message);
        }

        if (pictureUrl != null) {
            simplePopupImage.setImage(new Image(pictureUrl));
        }

        if (isButtonShows) {
            simplePopupOkButton.setOnAction(e -> close());
        } else {
            simplePopupOkButton.setVisible(false);
        }
    }

    public void setupWindow(String message) {
        setupWindow(message, true);
    }

    public void setupWindow(String message, String pictureUrl) {
        setupWindow(message, pictureUrl, true);
    }

    public void setupWindow(String message, boolean isButtonShows) { setupWindow(message, null, isButtonShows);}

    public void setupWindow(String message, String pictureUrl, boolean isButtonShows) {
        try {
            SimplePopup.message = message;
            SimplePopup.pictureUrl = pictureUrl;
            SimplePopup.isButtonShows = isButtonShows;

            xml = "/fxml/simplePopupScreen.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = (Parent) loader.load(getClass().getResourceAsStream(xml));

            window = new Stage(StageStyle.UNDECORATED);
            scene = new Scene(root);
            window.setScene(scene);
            window.setResizable(false);
            window.initModality(Modality.APPLICATION_MODAL);

            if (isButtonShows) {
                window.showAndWait();
            } else {
                window.show();
            }

            logger.log(Level.INFO, "Error popup: " + message);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't host error message =(");
        }
    }

    public void toFront() {
        window.toFront();
    }

    public void close() {
        window.close();
    }
}
