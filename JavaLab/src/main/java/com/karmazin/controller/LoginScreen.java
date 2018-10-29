package com.karmazin.controller;

import java.io.IOException;
import java.util.logging.Level;

import com.karmazin.model.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginScreen extends Application {
    private static LoggerAPI logger = new LoggerAPI(LoginScreen.class.getName());

    private static String xml;
    private static Stage window;
    private static Scene scene;

    @FXML
    private Button loginSignInButton;

    @FXML
    private TextField loginLoginTextfield;

    @FXML
    private PasswordField loginPasswordPassfield;

    @FXML
    private Button loginRegButton;

    // This group of methods will be run in order
    public static void main(String[] args) {
        // systeminfo - gives all info
        // systeminfo |find "Available Physical Memory" - gives only Available Physical Memory
        // https://ab57.ru/cmdlist/tasklist.html


        logger.log(Level.INFO, "Program started!");

        ConfigAPI.createConfigFile();
        logger.log(Level.INFO, ".ini-file created");

        // Starting JavaFX GUI
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new LoginScreen().setupWindow(primaryStage);
    }

    public void setupWindow(Stage primaryStage) {
        try {
            window = primaryStage;

            // Trying to login by config
            if (UserAPI.sessionByLastLogin()) {
                // Succesfully logged in
                new WorkScreen().setupWindow(window);
            } else {
                // Not logged in
                logger.log(Level.INFO, "Creating a login screen...");

                xml = "/fxml/loginScreen.fxml";
                FXMLLoader loader = new FXMLLoader();
                Parent root = (Parent) loader.load(getClass().getResourceAsStream(xml));

                scene = new Scene(root);

                window = primaryStage;
                window.setTitle("Pingovshique ver.0.(0)2");
                window.setScene(scene);
                window.setResizable(false);
                window.show();

                logger.log(Level.INFO, "Login screen created!");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Program files is corrupted. Please, reinstall the app!", e);
            System.exit(1337);
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        // 'SingIn' button logic
        loginSignInButton.setOnAction(event -> {
            try {
                String login = loginLoginTextfield.getText();
                String pass = loginPasswordPassfield.getText();

                if (login.length() > 3 && pass.length() > 3) {
                    if (UserAPI.logIn(login, pass)) {
                        new WorkScreen().setupWindow(window);
                    } else {
                        new SimplePopup().setupWindow("Incorrect login data!");
                    }
                } else {
                    new SimplePopup().setupWindow("Minimal length of login/password is 3 symbols!");
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Program files is corrupted. Please, reinstall the app!", e);
            }
        });

        // 'Register' button logic
        loginRegButton.setOnAction(event -> {
            try {
                String login = loginLoginTextfield.getText();
                String pass = loginPasswordPassfield.getText();

                if (login.length() > 3 && pass.length() > 3) {
                    if (UserAPI.createNewUser(login, pass, UserAPI.UserType.SimpleUser)) {
                        new WorkScreen().setupWindow(window);
                    } else {
                        new SimplePopup().setupWindow("Can't create profile with this data!");
                    }
                } else {
                    new SimplePopup().setupWindow("Minimal length of login/password is 3 symbols!");
                }
            } catch (IOException e) {
               logger.log(Level.SEVERE, "Program files is corrupted. Please, reinstall the app!", e);
            }
        });
    }
}
