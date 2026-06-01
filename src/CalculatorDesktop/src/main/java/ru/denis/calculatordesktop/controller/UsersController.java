package ru.denis.calculatordesktop.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.denis.calculatordesktop.CalculatorApp;
import ru.denis.calculatordesktop.api.ApiClient;
import ru.denis.calculatordesktop.api.ApiException;
import ru.denis.calculatordesktop.api.dto.UserDto;
import ru.denis.calculatordesktop.api.dto.UserRoleDto;
import ru.denis.calculatordesktop.util.Icons;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UsersController implements Initializable {

    @FXML private TableView<UserDto> table;
    @FXML private TableColumn<UserDto, Void>    colFio;
    @FXML private TableColumn<UserDto, String>  colLogin;
    @FXML private TableColumn<UserDto, String>  colRole;
    @FXML private TableColumn<UserDto, Void>    colActions;

    private final ObservableList<UserDto>     users = FXCollections.observableArrayList();
    private       List<UserRoleDto>           roles = List.of();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colFio.setCellFactory(col -> new TableCell<>() {
            private final Label nameLabel  = new Label();
            private final Label adminBadge = new Label("Суперадмин");
            private final HBox  box        = new HBox(8, nameLabel, adminBadge);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                adminBadge.getStyleClass().add("badge-blue");
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); return;
                }
                UserDto u = getTableView().getItems().get(getIndex());
                String surname = u.surname() != null ? u.surname() : "";
                String name    = u.name()    != null ? u.name()    : "";
                nameLabel.setText((surname + " " + name).trim());
                adminBadge.setVisible(u.superAdmin());
                adminBadge.setManaged(u.superAdmin());
                setGraphic(box);
            }
        });

        colLogin.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().login()));
        colRole.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().roleName() != null ? d.getValue().roleName() : "—"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button edit = Icons.editButton();
            private final Button del  = Icons.deleteButton();
            private final HBox box = new HBox(2, edit, del);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                edit.setOnAction(e -> openDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> deleteUser(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                UserDto user = getTableView().getItems().get(getIndex());
                setGraphic(user.superAdmin() ? null : box);
            }
        });

        table.setItems(users);
    }

    private void loadData() {
        Task<Object[]> task = new Task<>() {
            @Override protected Object[] call() throws Exception {
                return new Object[]{
                        ApiClient.getInstance().getUsers(),
                        ApiClient.getInstance().getRoles()
                };
            }
        };
        task.setOnSucceeded(e -> {
            @SuppressWarnings("unchecked")
            List<UserDto> u = (List<UserDto>) ((Object[]) task.getValue())[0];
            @SuppressWarnings("unchecked")
            List<UserRoleDto> r = (List<UserRoleDto>) ((Object[]) task.getValue())[1];
            users.setAll(u);
            roles = r;
        });
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    @FXML private void handleAdd() { openDialog(null); }

    private void openDialog(UserDto existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CalculatorApp.class.getResource("fxml/user-dialog.fxml"));
            Parent root = loader.load();
            UserDialogController ctrl = loader.getController();
            ctrl.init(existing, roles);

            Stage stage = new Stage();
            stage.setTitle(existing == null ? "Добавить пользователя" : "Редактировать пользователя");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(CalculatorApp.getPrimaryStage());
            stage.setResizable(false);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    CalculatorApp.class.getResource("css/styles.css").toExternalForm());
            stage.setScene(scene);
            ctrl.setStage(stage);
            stage.showAndWait();
            if (ctrl.isSaved()) loadData();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void deleteUser(UserDto u) {
        if (u.superAdmin()) { showError("Нельзя удалить суперадмина"); return; }
        if (!confirm("Удалить пользователя «" + u.login() + "»?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().deleteUser(u.id());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadData());
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    private void handleApiError(Throwable ex) {
        String msg = ex instanceof ApiException ae ? ae.getMessage()
                : (ex != null ? ex.getMessage() : "Ошибка");
        Platform.runLater(() -> showError(msg));
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Ошибка"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Подтверждение"); a.setHeaderText(null); a.setContentText(msg);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
