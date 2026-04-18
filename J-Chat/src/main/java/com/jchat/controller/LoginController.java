package com.jchat.controller;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Toast;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
    private CheckBox rememberMeCheckbox;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        if (appBar != null) {
            appBar.setTitleText("Login to JChat");
        }
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank()) {
            showError("Please enter your username.");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.isBlank()) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        // Logic for "Remember Me" can be added here (e.g., saving to local storage)
        if (rememberMeCheckbox.isSelected()) {
            System.out.println("Remembering user: " + username);
        }

        // For now, allow any non-empty credentials
        MobileApplication.getInstance().switchView(Main.DASHBOARD_VIEW);
    }

    @FXML
    private void onRegister() {
        Toast toast = new Toast("Registration is coming soon!");
        toast.show();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        Toast toast = new Toast(message);
        toast.show();
    }
}
