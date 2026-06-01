package ru.denis.calculatordesktop.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.denis.calculatordesktop.CalculatorApp;
import ru.denis.calculatordesktop.api.ApiClient;
import ru.denis.calculatordesktop.api.ApiException;
import ru.denis.calculatordesktop.api.dto.CurrentUserDto;
import ru.denis.calculatordesktop.api.dto.LoginResponse;
import ru.denis.calculatordesktop.session.SessionManager;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressBar progressBar;

    @FXML
    private void handleLogin() {
        String login    = loginField.getText().trim();
        String password = passwordField.getText();

        clearFieldError(loginField);
        clearFieldError(passwordField);

        boolean valid = true;
        if (login.isEmpty()) {
            markFieldError(loginField); valid = false;
        }
        if (password.isEmpty()) {
            markFieldError(passwordField); valid = false;
        }
        if (!valid) {
            showError("Заполните логин и пароль");
            return;
        }

        setLoading(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                LoginResponse resp = ApiClient.getInstance().login(login, password);
                SessionManager.getInstance().setToken(resp.token());
                CurrentUserDto user = ApiClient.getInstance().getCurrentUser();
                SessionManager.getInstance().setCurrentUser(user);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);
            try {
                CalculatorApp.showMain();
            } catch (Exception ex) {
                showError("Ошибка открытия главного окна: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = task.getException();
            if (ex instanceof ApiException ae && ae.getStatusCode() == 401) {
                showError("Неверный логин или пароль");
            } else {
                showError(ex != null ? ex.getMessage() : "Ошибка подключения к серверу");
            }
        });

        new Thread(task).start();
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loginField.setDisable(loading);
        passwordField.setDisable(loading);
        progressBar.setVisible(loading);
        progressBar.setManaged(loading);
        if (loading) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private static final String ERROR_STYLE =
        "-fx-border-color:#ef4444;-fx-border-width:1.5;-fx-border-radius:6;-fx-background-radius:6;";

    private void markFieldError(TextInputControl field) {
        field.setStyle(ERROR_STYLE);
        field.textProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
            @Override public void changed(javafx.beans.value.ObservableValue<? extends String> obs, String o, String n) {
                field.setStyle("");
                field.textProperty().removeListener(this);
            }
        });
    }

    private void clearFieldError(TextInputControl field) { field.setStyle(""); }
}
