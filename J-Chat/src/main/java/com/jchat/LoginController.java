package com.jchat;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private AppBar appBar;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        appBar.setTitleText("Login");
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        // For now, allow any non-empty credentials
        // In a real app, you'd check against a database or service
        MobileApplication.getInstance().switchView(Main.MAIN_VIEW);
    }

    @FXML
    private void onRegister() {
        // Not implemented yet
        errorLabel.setText("Registration not yet implemented.");
    }
}
