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
import ru.denis.calculatordesktop.api.dto.MaterialDto;
import ru.denis.calculatordesktop.api.dto.MaterialGroupDto;
import ru.denis.calculatordesktop.util.Icons;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MaterialsController implements Initializable {

    // Materials tab
    @FXML private ComboBox<MaterialGroupDto> groupFilter;
    @FXML private TextField searchField;
    @FXML private TableView<MaterialDto> materialsTable;
    @FXML private TableColumn<MaterialDto, String> colName;
    @FXML private TableColumn<MaterialDto, String> colPrice;
    @FXML private TableColumn<MaterialDto, String> colUnits;
    @FXML private TableColumn<MaterialDto, String> colGroup;
    @FXML private TableColumn<MaterialDto, Void>   colActions;

    // Groups tab
    @FXML private TableView<MaterialGroupDto>       groupsTable;
    @FXML private TableColumn<MaterialGroupDto, String> colGroupName;
    @FXML private TableColumn<MaterialGroupDto, Void>   colGroupActions;

    private final ObservableList<MaterialDto> materials = FXCollections.observableArrayList();
    private final ObservableList<MaterialGroupDto> groups = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupMaterialsTable();
        setupGroupsTable();
        loadAll();
    }

    // =========================================================================
    // Table setup
    // =========================================================================

    private void setupMaterialsTable() {
        materialsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().name()));
        colPrice.setCellValueFactory(d -> {
            BigDecimal p = d.getValue().price();
            return new javafx.beans.property.SimpleStringProperty(p != null ? p.toPlainString() : "");
        });
        colUnits.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().units()));
        colGroup.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().groupName()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button edit = Icons.editButton();
            private final Button del  = Icons.deleteButton();
            private final HBox box = new HBox(2, edit, del);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                edit.setOnAction(e -> openMaterialDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> deleteMaterial(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        materialsTable.setItems(materials);
    }

    private void setupGroupsTable() {
        groupsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colGroupName.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().name()));

        colGroupActions.setCellFactory(col -> new TableCell<>() {
            private final Button edit = Icons.editButton();
            private final Button del  = Icons.deleteButton();
            private final HBox box = new HBox(2, edit, del);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                edit.setOnAction(e -> openGroupDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> deleteGroup(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        groupsTable.setItems(groups);
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    private void loadAll() {
        Task<List<MaterialGroupDto>> groupTask = new Task<>() {
            @Override protected List<MaterialGroupDto> call() throws Exception {
                return ApiClient.getInstance().getMaterialGroups();
            }
        };
        groupTask.setOnSucceeded(e -> {
            groups.setAll(groupTask.getValue());
            refreshGroupFilter();
            loadMaterials();
        });
        groupTask.setOnFailed(e -> handleApiError(groupTask.getException()));
        new Thread(groupTask).start();
    }

    private void loadMaterials() {
        MaterialGroupDto sel = groupFilter.getValue();
        Integer groupId = (sel != null && sel.id() != null && sel.id() >= 0) ? sel.id() : null;
        String search = searchField.getText().trim();

        Task<List<MaterialDto>> task = new Task<>() {
            @Override protected List<MaterialDto> call() throws Exception {
                return ApiClient.getInstance().getMaterials(groupId, search.isEmpty() ? null : search);
            }
        };
        task.setOnSucceeded(e -> materials.setAll(task.getValue()));
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    private void refreshGroupFilter() {
        MaterialGroupDto selected = groupFilter.getValue();
        ObservableList<MaterialGroupDto> items = FXCollections.observableArrayList();
        items.add(new MaterialGroupDto(-1, "Все группы"));
        items.addAll(groups);
        groupFilter.setItems(items);
        if (selected != null) {
            groupFilter.setValue(items.stream()
                    .filter(g -> g.id().equals(selected.id())).findFirst().orElse(items.get(0)));
        } else {
            groupFilter.setValue(items.get(0));
        }
    }

    // =========================================================================
    // Handlers
    // =========================================================================

    @FXML private void handleGroupFilter() { loadMaterials(); }

    @FXML private void handleSearch() { loadMaterials(); }

    @FXML private void handleAdd() { openMaterialDialog(null); }

    @FXML private void handleAddGroup() { openGroupDialog(null); }

    // =========================================================================
    // Material dialog
    // =========================================================================

    private void openMaterialDialog(MaterialDto existing) {
        try {
            FXMLLoader loader = new FXMLLoader(CalculatorApp.class.getResource("fxml/material-dialog.fxml"));
            Parent root = loader.load();
            MaterialDialogController ctrl = loader.getController();
            ctrl.init(existing, groups);

            Stage stage = buildDialog(root, existing == null ? "Добавить материал" : "Редактировать материал");
            ctrl.setStage(stage);
            stage.showAndWait();
            if (ctrl.isSaved()) loadAll();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void deleteMaterial(MaterialDto m) {
        if (!confirm("Удалить материал «" + m.name() + "»?")) return;
        runDelete(() -> ApiClient.getInstance().deleteMaterial(m.id()), this::loadAll);
    }

    // =========================================================================
    // Group dialog
    // =========================================================================

    private void openGroupDialog(MaterialGroupDto existing) {
        TextInputDialog dlg = new TextInputDialog(existing != null ? existing.name() : "");
        dlg.setTitle(existing == null ? "Добавить группу" : "Переименовать группу");
        dlg.setHeaderText(null);
        dlg.setContentText("Название группы:");
        Optional<String> result = dlg.showAndWait();
        result.ifPresent(name -> {
            if (name.isBlank()) return;
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    if (existing == null) {
                        ApiClient.getInstance().createMaterialGroup(name);
                    } else {
                        ApiClient.getInstance().updateMaterialGroup(existing.id(), name);
                    }
                    return null;
                }
            };
            task.setOnSucceeded(e -> loadAll());
            task.setOnFailed(e -> handleApiError(task.getException()));
            new Thread(task).start();
        });
    }

    private void deleteGroup(MaterialGroupDto g) {
        List<String> choices = List.of(
                "DEFAULT — переместить материалы в «Без группы»",
                "CASCADE — удалить группу и все материалы",
                "MOVE — переместить в другую группу"
        );
        ChoiceDialog<String> dlg = new ChoiceDialog<>(choices.get(0), choices);
        dlg.setTitle("Удаление группы");
        dlg.setHeaderText("Удаление группы «" + g.name() + "»");
        dlg.setContentText("Выберите стратегию:");
        dlg.showAndWait().ifPresent(choice -> {
            if (choice.startsWith("DEFAULT")) {
                runDelete(() -> ApiClient.getInstance().deleteMaterialGroup(g.id(), "DEFAULT", null), this::loadAll);
            } else if (choice.startsWith("CASCADE")) {
                if (confirm("Удалить группу и ВСЕ материалы в ней?"))
                    runDelete(() -> ApiClient.getInstance().deleteMaterialGroup(g.id(), "CASCADE", null), this::loadAll);
            } else {
                List<MaterialGroupDto> others = groups.stream().filter(x -> !x.id().equals(g.id())).toList();
                if (others.isEmpty()) { showError("Нет других групп для перемещения"); return; }
                ChoiceDialog<MaterialGroupDto> moveDlg = new ChoiceDialog<>(others.get(0), others);
                moveDlg.setTitle("Переместить материалы");
                moveDlg.setHeaderText("Куда переместить материалы из группы «" + g.name() + "»?");
                moveDlg.setContentText("Группа:");
                moveDlg.showAndWait().ifPresent(target ->
                        runDelete(() -> ApiClient.getInstance().deleteMaterialGroup(g.id(), "MOVE", target.id()), this::loadAll));
            }
        });
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Stage buildDialog(Parent root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(CalculatorApp.getPrimaryStage());
        stage.setResizable(false);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(CalculatorApp.class.getResource("css/styles.css").toExternalForm());
        stage.setScene(scene);
        return stage;
    }

    private void runDelete(CheckedRunnable action, Runnable onSuccess) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception { action.run(); return null; }
        };
        task.setOnSucceeded(e -> onSuccess.run());
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    private void handleApiError(Throwable ex) {
        String msg = ex instanceof ApiException ae ? ae.getMessage() : (ex != null ? ex.getMessage() : "Неизвестная ошибка");
        Platform.runLater(() -> showError(msg));
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean confirm(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    @FunctionalInterface
    interface CheckedRunnable { void run() throws Exception; }
}
