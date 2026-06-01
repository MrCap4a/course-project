package ru.denis.calculatordesktop.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.denis.calculatordesktop.CalculatorApp;
import ru.denis.calculatordesktop.api.ApiClient;
import ru.denis.calculatordesktop.api.ApiException;
import ru.denis.calculatordesktop.api.dto.PermissionDto;
import ru.denis.calculatordesktop.api.dto.UserDto;
import ru.denis.calculatordesktop.api.dto.UserRoleDto;
import ru.denis.calculatordesktop.util.Icons;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class RolesController implements Initializable {

    private static final List<String[]> PERM_GROUP_DEFS = List.of(
        new String[]{"Расчёты",      "calculations.view", "calculations.create", "calculations.edit", "calculations.delete"},
        new String[]{"Формулы",      "formulas.view",     "formulas.create",     "formulas.edit",     "formulas.delete"},
        new String[]{"Материалы",    "materials.view",    "materials.create",    "materials.edit",    "materials.delete"},
        new String[]{"Роли",         "roles.view",        "roles.create",        "roles.edit",        "roles.delete"},
        new String[]{"Пользователи", "users.view",        "users.create",        "users.edit",        "users.delete"}
    );

    private static final Map<String, String> ACTION_SHORT = Map.of(
        "view", "просмотр", "create", "создание", "edit", "изменение", "delete", "удаление"
    );

    @FXML private TableView<UserRoleDto> table;
    @FXML private TableColumn<UserRoleDto, String> colName;
    @FXML private TableColumn<UserRoleDto, String> colPermissions;
    @FXML private TableColumn<UserRoleDto, Void>   colActions;

    private final ObservableList<UserRoleDto> roles = FXCollections.observableArrayList();
    private List<PermissionDto> allPermissions = List.of();
    // IDs of roles that belong to at least one superAdmin — these are protected
    private Set<Integer> superAdminRoleIds = new HashSet<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colName.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().name()));

        colPermissions.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(""));
        colPermissions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null); setGraphic(null); return;
                }
                List<PermissionDto> perms = getTableView().getItems().get(getIndex()).permissions();
                if (perms == null || perms.isEmpty()) {
                    setText("—"); setStyle("-fx-text-fill:#94a3b8;"); setGraphic(null); return;
                }
                List<String[]> groups = formatPermissions(perms);
                if (groups.isEmpty()) {
                    setText("—"); setStyle("-fx-text-fill:#94a3b8;"); setGraphic(null); return;
                }
                FlowPane flow = new FlowPane(6, 4);
                flow.setPadding(new Insets(2, 0, 2, 0));
                for (String[] g : groups) {
                    Label boldPart  = new Label(g[0] + ": ");
                    boldPart.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#374151;");
                    Label lightPart = new Label(g[1]);
                    lightPart.setStyle("-fx-font-size:11px;-fx-text-fill:#374151;");
                    HBox inner = new HBox(0, boldPart, lightPart);
                    inner.setAlignment(Pos.CENTER_LEFT);
                    Label badge = new Label();
                    badge.setGraphic(inner);
                    badge.setStyle(
                        "-fx-background-color:#f1f5f9;" +
                        "-fx-background-radius:4;" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-radius:4;" +
                        "-fx-border-width:1;" +
                        "-fx-padding:2 8;"
                    );
                    flow.getChildren().add(badge);
                }
                setText(null); setStyle(""); setGraphic(flow);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button edit = Icons.editButton();
            private final Button del  = Icons.deleteButton();
            private final HBox box = new HBox(2, edit, del);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                edit.setOnAction(e -> openDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> deleteRole(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                UserRoleDto role = getTableView().getItems().get(getIndex());
                // hide actions for roles belonging to superAdmins
                setGraphic(superAdminRoleIds.contains(role.id()) ? null : box);
            }
        });

        table.setItems(roles);
    }

    private void loadData() {
        Task<Object[]> task = new Task<>() {
            @Override protected Object[] call() throws Exception {
                return new Object[]{
                        ApiClient.getInstance().getRoles(),
                        ApiClient.getInstance().getPermissions(),
                        ApiClient.getInstance().getUsers()
                };
            }
        };
        task.setOnSucceeded(e -> {
            @SuppressWarnings("unchecked")
            List<UserRoleDto>  r = (List<UserRoleDto>)  ((Object[]) task.getValue())[0];
            @SuppressWarnings("unchecked")
            List<PermissionDto> p = (List<PermissionDto>) ((Object[]) task.getValue())[1];
            @SuppressWarnings("unchecked")
            List<UserDto>      u = (List<UserDto>)      ((Object[]) task.getValue())[2];

            roles.setAll(r);
            allPermissions = p;
            superAdminRoleIds = u.stream()
                    .filter(UserDto::superAdmin)
                    .map(UserDto::roleId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
            table.refresh();
        });
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    @FXML private void handleAdd() { openDialog(null); }

    private void openDialog(UserRoleDto existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CalculatorApp.class.getResource("fxml/role-dialog.fxml"));
            Parent root = loader.load();
            RoleDialogController ctrl = loader.getController();
            ctrl.init(existing, allPermissions);

            Stage stage = new Stage();
            stage.setTitle(existing == null ? "Добавить роль" : "Редактировать роль");
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

    private void deleteRole(UserRoleDto r) {
        if (!confirm("Удалить роль «" + r.name() + "»?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().deleteRole(r.id());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadData());
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    private List<String[]> formatPermissions(List<PermissionDto> perms) {
        Set<String> names = perms.stream().map(PermissionDto::name).collect(Collectors.toSet());
        String[] allActions = {"view", "create", "edit", "delete"};
        List<String[]> result = new ArrayList<>();
        for (String[] groupDef : PERM_GROUP_DEFS) {
            List<String> present = new ArrayList<>();
            for (String action : allActions) {
                for (int i = 1; i < groupDef.length; i++) {
                    if (groupDef[i].endsWith("." + action) && names.contains(groupDef[i])) {
                        present.add(action);
                    }
                }
            }
            if (!present.isEmpty()) {
                String label = present.size() == 4 ? "все"
                        : present.stream().map(a -> ACTION_SHORT.getOrDefault(a, a))
                                 .collect(Collectors.joining(", "));
                result.add(new String[]{groupDef[0], label});
            }
        }
        return result;
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
