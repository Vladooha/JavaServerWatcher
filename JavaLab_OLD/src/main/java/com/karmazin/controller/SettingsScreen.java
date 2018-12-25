package com.karmazin.controller;

import com.karmazin.model.ConfigAPI;
import com.karmazin.model.LoggerAPI;
import com.karmazin.model.UserAPI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsScreen {
    private static final LoggerAPI logger = new LoggerAPI(SettingsScreen.class.getName());

    private static String xml;
    private static Stage window;
    private static Scene scene;

    @FXML
    private Label settingProfileLabel;

    @FXML
    private TextField settingPingDelayTextField;
    @FXML
    private TextField settingPingFaultsTextField;
    @FXML
    private TextField settingProcWaitTextField;
    @FXML
    private TextField settingMailTextField;
    @FXML
    private PasswordField settingPasswordTextField;
    @FXML
    private TextField settingHttpDelayTextField;
    @FXML
    private TextField settingPingEmailTextField;

    @FXML
    private Button settingAcceptButton;
    @FXML
    private Button settingCloseButton;

    @FXML
    void initialize() {
        // View pre-set
        settingProfileLabel.setText("Профиль (" + ConfigAPI.getLogin() + ")");

        settingMailTextField.setText(ConfigAPI.getEmail());

        settingPingDelayTextField.setText(ConfigAPI.getPingDelay() + "");
        settingPingFaultsTextField.setText(ConfigAPI.getPingFaults() + "");
        settingPingEmailTextField.setText(ConfigAPI.getPingEmail() + "");
        settingHttpDelayTextField.setText(ConfigAPI.getHttpDelay() + "");

        settingProcWaitTextField.setText(ConfigAPI.getProcessDelay() + "");

        // Listeners
        settingAcceptButton.setOnAction((event) -> {
            // Email check
            String email = settingMailTextField.getText();
            if (email.length() != 0) {
                Pattern pattern = Pattern.compile("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*\\.\\w{2,4}$");
                Matcher emailMatcher = pattern.matcher(email);
                if (!emailMatcher.find()) {
                    new SimplePopup().setupWindow("Указан некорректный email!");
                    return;
                }
            }

            // Password check
            String pass = settingPasswordTextField.getText();
            if (pass.length() != 0 && pass.length() < 3) {
                new SimplePopup().setupWindow("Пароль должен быть больше 3 символов!");
                return;
            }

            // Ping, http and proc settings check
            int pingDelay, pingFaults, pingEmail, httpDelay, procWait;
            try {
                pingDelay = Integer.parseInt(settingPingDelayTextField.getText());
                pingFaults = Integer.parseInt(settingPingFaultsTextField.getText());
                pingEmail = Integer.parseInt(settingPingEmailTextField.getText());
                httpDelay = Integer.parseInt(settingHttpDelayTextField.getText());
                procWait = Integer.parseInt(settingProcWaitTextField.getText());

                if (pingDelay <= 0 || pingFaults <= 0 || pingEmail <= 0 || httpDelay <= 0 || procWait <= 0) {
                    throw new Exception();
                }
            } catch (Exception e) {
                new SimplePopup().setupWindow("Время/кол-во должно указываться в целых положительных числах!");
                return;
            }

            // If all inputs are ok
            ConfigAPI.setEmail(email);

            if (pass.length() != 0) {
                UserAPI.updatePassword(ConfigAPI.getLogin(), pass);
            }

            ConfigAPI.setPingDelay(pingDelay);
            ConfigAPI.setPingFaults(pingFaults);
            ConfigAPI.setPingEmail(pingEmail);
            ConfigAPI.setHttpDelay(httpDelay);
            ConfigAPI.setProcessDelay(procWait);

            new SimplePopup().setupWindow("Настройки сохранены!", "/pngs/ok-100.png");
        });

        settingCloseButton.setOnAction((event) -> {
            window.close();
        });
    }

    public void setupWindow() {
        try {
            xml = "/fxml/settingsScreen.fxml";
            FXMLLoader loader = new FXMLLoader();
            Parent root = null;
            root = (Parent) loader.load(getClass().getResourceAsStream(xml));

            scene = new Scene(root);

            window = new Stage();
            window.setTitle("Настройки");
            window.setScene(scene);
            window.setResizable(false);
            window.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
