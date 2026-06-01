package ru.denis.calculatordesktop.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.denis.calculatordesktop.api.ApiClient;
import ru.denis.calculatordesktop.api.dto.UserDto;
import ru.denis.calculatordesktop.api.dto.UserRoleDto;

import java.util.List;

public class UserDialogController {

    private static final String ERR_STYLE =
        "-fx-border-color:#ef4444;-fx-border-width:1.5;-fx-border-radius:6;-fx-background-radius:6;";

    @FXML private Label         dialogTitle;
    @FXML private TextField     loginField;
    @FXML private Label         passwordLabel;
    @FXML private Label         passwordAsterisk;
    @FXML private PasswordField passwordField;
    @FXML private TextField     nameField;
    @FXML private TextField     surnameField;
    @FXML private ComboBox<UserRoleDto> roleCombo;
    @FXML private Label         errorLabel;

    private Stage stage;
    private UserDto existing;
    private boolean saved = false;

    public void init(UserDto existing, List<UserRoleDto> roles) {
        this.existing = existing;
        roleCombo.setItems(FXCollections.observableArrayList(roles));

        if (existing != null) {
            dialogTitle.setText("Редактировать пользователя");
            passwordLabel.setText("Пароль (оставьте пустым, чтобы не менять)");
            passwordAsterisk.setVisible(false);
            passwordAsterisk.setManaged(false);
            loginField.setText(existing.login());
            nameField.setText(existing.name()    != null ? existing.name()    : "");
            surnameField.setText(existing.surname() != null ? existing.surname() : "");
            if (existing.roleId() != null) {
                roles.stream().filter(r -> r.id().equals(existing.roleId()))
                        .findFirst().ifPresent(roleCombo::setValue);
            }
        }

        for (TextField f : List.of(loginField, nameField, surnameField)) {
            f.textProperty().addListener(clearOnType(f));
        }
        passwordField.textProperty().addListener(clearOnType(passwordField));
    }

    public void setStage(Stage stage) { this.stage = stage; }
    public boolean isSaved()          { return saved; }

    @FXML
    private void handleSave() {
        String login    = loginField.getText().trim();
        String password = passwordField.getText();
        String name     = nameField.getText().trim();
        String surname  = surnameField.getText().trim();
        UserRoleDto role = roleCombo.getValue();

        clearAllErrors();

        boolean valid = true;
        if (login.isEmpty())   { markError(loginField,   "Введите логин");    valid = false; }
        if (name.isEmpty())    { markError(nameField,    "Введите имя");      valid = false; }
        if (surname.isEmpty()) { markError(surnameField, "Введите фамилию"); valid = false; }
        if (existing == null && password.isEmpty()) {
            markError(passwordField, "Введите пароль"); valid = false;
        }
        if (role == null) { showError("Выберите роль для пользователя"); return; }
        if (!valid) return;

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                if (existing == null) {
                    ApiClient.getInstance().createUser(login, password, name, surname, role.id());
                } else {
                    ApiClient.getInstance().updateUser(existing.id(), login,
                            password.isEmpty() ? null : password, name, surname, role.id());
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> { saved = true; stage.close(); });
        task.setOnFailed(e -> Platform.runLater(() ->
                showError(task.getException().getMessage())));
        new Thread(task).start();
    }

    @FXML private void handleCancel() { stage.close(); }

    private void markError(Control field, String msg) {
        field.setStyle(ERR_STYLE);
        showError(msg);
    }

    private void clearAllErrors() {
        for (Control f : List.of(loginField, passwordField, nameField, surnameField)) {
            f.setStyle("");
        }
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private <T extends TextInputControl> ChangeListener<String> clearOnType(T field) {
        return new ChangeListener<>() {
            @Override public void changed(ObservableValue<? extends String> obs, String o, String n) {
                field.setStyle("");
                field.textProperty().removeListener(this);
            }
        };
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (stage != null) Platform.runLater(stage::sizeToScene);
    }
}
