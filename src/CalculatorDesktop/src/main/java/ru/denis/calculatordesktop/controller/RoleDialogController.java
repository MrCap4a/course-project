package ru.denis.calculatordesktop.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.denis.calculatordesktop.api.ApiClient;
import ru.denis.calculatordesktop.api.dto.PermissionDto;
import ru.denis.calculatordesktop.api.dto.UserRoleDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleDialogController {

    @FXML private Label     dialogTitle;
    @FXML private TextField nameField;
    @FXML private VBox      permissionsContainer;
    @FXML private Label     errorLabel;

    private Stage stage;
    private UserRoleDto existing;
    private boolean saved = false;

    private final List<CheckBox> permCheckboxes = new ArrayList<>();

    private static final List<String[]> PERM_GROUPS = List.of(
        new String[]{"Расчёты",      "calculations.view", "calculations.create", "calculations.edit", "calculations.delete"},
        new String[]{"Формулы",      "formulas.view",     "formulas.create",     "formulas.edit",     "formulas.delete"},
        new String[]{"Материалы",    "materials.view",    "materials.create",    "materials.edit",    "materials.delete"},
        new String[]{"Роли",         "roles.view",        "roles.create",        "roles.edit",        "roles.delete"},
        new String[]{"Пользователи", "users.view",        "users.create",        "users.edit",        "users.delete"}
    );

    private static final Map<String, String> ACTION_LABELS = Map.of(
        "view",   "Просмотр",
        "create", "Создание",
        "edit",   "Редактирование",
        "delete", "Удаление"
    );

    private static final String ERR_STYLE =
        "-fx-border-color:#ef4444;-fx-border-width:1.5;-fx-border-radius:6;-fx-background-radius:6;";

    public void init(UserRoleDto existing, List<PermissionDto> allPermissions) {
        this.existing = existing;

        if (existing != null) {
            dialogTitle.setText("Редактировать роль");
            nameField.setText(existing.name());
        }

        nameField.textProperty().addListener((obs, o, n) -> nameField.setStyle(""));

        Set<Integer> selected = existing != null && existing.permissions() != null
                ? existing.permissions().stream().map(PermissionDto::id).collect(Collectors.toSet())
                : Set.of();

        permCheckboxes.clear();
        permissionsContainer.getChildren().clear();

        for (String[] groupDef : PERM_GROUPS) {
            VBox section = buildGroupSection(groupDef, allPermissions, selected);
            if (section != null) permissionsContainer.getChildren().add(section);
        }
    }

    private VBox buildGroupSection(String[] groupDef, List<PermissionDto> allPerms, Set<Integer> selectedIds) {
        String groupName = groupDef[0];

        List<PermissionDto> groupPerms = new ArrayList<>();
        for (int i = 1; i < groupDef.length; i++) {
            String permName = groupDef[i];
            allPerms.stream().filter(p -> p.name().equals(permName)).findFirst()
                    .ifPresent(groupPerms::add);
        }
        if (groupPerms.isEmpty()) return null;

        // Individual permission checkboxes in a 2-column grid
        List<CheckBox> groupCbs = new ArrayList<>();
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        grid.setPadding(new Insets(4, 0, 0, 28));

        for (int i = 0; i < groupPerms.size(); i++) {
            PermissionDto perm = groupPerms.get(i);
            String action = perm.name().substring(perm.name().lastIndexOf('.') + 1);
            String label  = ACTION_LABELS.getOrDefault(action, action);

            CheckBox cb = new CheckBox(label);
            cb.setUserData(perm.id());
            cb.setSelected(selectedIds.contains(perm.id()));
            cb.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(cb, Priority.ALWAYS);

            groupCbs.add(cb);
            permCheckboxes.add(cb);
            grid.add(cb, i % 2, i / 2);
        }

        // Group header checkbox (tri-state driven by individual checkboxes)
        CheckBox groupCb = new CheckBox(groupName);
        groupCb.setAllowIndeterminate(false);
        groupCb.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");

        Runnable syncGroupState = () -> {
            long checked = groupCbs.stream().filter(CheckBox::isSelected).count();
            if (checked == groupCbs.size()) {
                groupCb.setIndeterminate(false);
                groupCb.setSelected(true);
            } else if (checked == 0) {
                groupCb.setIndeterminate(false);
                groupCb.setSelected(false);
            } else {
                groupCb.setSelected(false);
                groupCb.setIndeterminate(true);
            }
        };

        syncGroupState.run();
        groupCbs.forEach(cb -> cb.selectedProperty().addListener((obs, o, n) -> syncGroupState.run()));

        groupCb.setOnAction(e -> {
            boolean selectAll = !groupCbs.stream().allMatch(CheckBox::isSelected);
            groupCbs.forEach(cb -> cb.setSelected(selectAll));
        });

        // Dependency: view ↔ create/edit/delete
        CheckBox viewCb = null;
        List<CheckBox> nonViewCbs = new ArrayList<>();
        for (int i = 0; i < groupPerms.size(); i++) {
            String act = groupPerms.get(i).name().substring(groupPerms.get(i).name().lastIndexOf('.') + 1);
            if ("view".equals(act)) viewCb = groupCbs.get(i);
            else                    nonViewCbs.add(groupCbs.get(i));
        }
        if (viewCb != null) {
            final CheckBox fViewCb       = viewCb;
            final List<CheckBox> fOthers = nonViewCbs;
            // view снят → убрать все остальные
            fViewCb.selectedProperty().addListener((obs, o, n) -> {
                if (!n) fOthers.forEach(cb -> cb.setSelected(false));
            });
            // create/edit/delete выбран → включить view
            fOthers.forEach(cb -> cb.selectedProperty().addListener((obs, o, n) -> {
                if (n && !fViewCb.isSelected()) fViewCb.setSelected(true);
            }));
        }

        HBox header = new HBox(8, groupCb);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox section = new VBox(10, header, grid);
        section.setPadding(new Insets(12, 14, 12, 14));
        section.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );
        return section;
    }

    public void setStage(Stage stage) { this.stage = stage; }
    public boolean isSaved()          { return saved; }

    @FXML private void handleSelectAll() { permCheckboxes.forEach(cb -> cb.setSelected(true));  }
    @FXML private void handleClearAll()  { permCheckboxes.forEach(cb -> cb.setSelected(false)); }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            nameField.setStyle(ERR_STYLE);
            showError("Введите название роли");
            return;
        }
        nameField.setStyle("");

        List<Integer> permIds = permCheckboxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (Integer) cb.getUserData())
                .toList();

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                if (existing == null) ApiClient.getInstance().createRole(name, permIds);
                else                  ApiClient.getInstance().updateRole(existing.id(), name, permIds);
                return null;
            }
        };
        task.setOnSucceeded(e -> { saved = true; stage.close(); });
        task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException().getMessage())));
        new Thread(task).start();
    }

    @FXML private void handleCancel() { stage.close(); }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (stage != null) Platform.runLater(stage::sizeToScene);
    }
}
