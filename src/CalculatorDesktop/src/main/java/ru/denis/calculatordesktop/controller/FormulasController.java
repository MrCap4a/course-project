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
import ru.denis.calculatordesktop.api.dto.FormulaDto;
import ru.denis.calculatordesktop.api.dto.FormulaGroupDto;
import ru.denis.calculatordesktop.util.Icons;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FormulasController implements Initializable {

    @FXML private ComboBox<FormulaGroupDto> groupFilter;
    @FXML private TextField searchField;
    @FXML private TableView<FormulaDto> formulasTable;
    @FXML private TableColumn<FormulaDto, String> colName;
    @FXML private TableColumn<FormulaDto, String> colExpression;
    @FXML private TableColumn<FormulaDto, String> colGroup;
    @FXML private TableColumn<FormulaDto, Void>   colActions;

    @FXML private TableView<FormulaGroupDto>          groupsTable;
    @FXML private TableColumn<FormulaGroupDto, String> colGroupName;
    @FXML private TableColumn<FormulaGroupDto, Void>   colGroupActions;

    private final ObservableList<FormulaDto>      formulas = FXCollections.observableArrayList();
    private final ObservableList<FormulaGroupDto> groups   = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFormulasTable();
        setupGroupsTable();
        loadAll();
    }

    private void setupFormulasTable() {
        formulasTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colName.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().name()));
        colExpression.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().expression()));
        colGroup.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().groupName()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button edit = Icons.editButton();
            private final Button del  = Icons.deleteButton();
            private final HBox box = new HBox(2, edit, del);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                edit.setOnAction(e -> openFormulaDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> deleteFormula(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        formulasTable.setItems(formulas);
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

    private void loadAll() {
        Task<List<FormulaGroupDto>> groupTask = new Task<>() {
            @Override protected List<FormulaGroupDto> call() throws Exception {
                return ApiClient.getInstance().getFormulaGroups();
            }
        };
        groupTask.setOnSucceeded(e -> {
            groups.setAll(groupTask.getValue());
            refreshGroupFilter();
            loadFormulas();
        });
        groupTask.setOnFailed(e -> handleApiError(groupTask.getException()));
        new Thread(groupTask).start();
    }

    private void loadFormulas() {
        FormulaGroupDto sel = groupFilter.getValue();
        Integer groupId = (sel != null && sel.id() != null && sel.id() >= 0) ? sel.id() : null;
        String search = searchField.getText().trim();

        Task<List<FormulaDto>> task = new Task<>() {
            @Override protected List<FormulaDto> call() throws Exception {
                List<FormulaDto> all = ApiClient.getInstance().getFormulas();
                return all.stream()
                        .filter(f -> groupId == null || groupId.equals(f.groupId()))
                        .filter(f -> search.isEmpty() || f.name().toLowerCase().contains(search.toLowerCase()))
                        .toList();
            }
        };
        task.setOnSucceeded(e -> formulas.setAll(task.getValue()));
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    private void refreshGroupFilter() {
        ObservableList<FormulaGroupDto> items = FXCollections.observableArrayList();
        items.add(new FormulaGroupDto(-1, "Все группы"));
        items.addAll(groups);
        groupFilter.setItems(items);
        groupFilter.setValue(items.get(0));
    }

    @FXML private void handleGroupFilter() { loadFormulas(); }
    @FXML private void handleSearch()      { loadFormulas(); }
    @FXML private void handleAdd()         { openFormulaDialog(null); }
    @FXML private void handleAddGroup()    { openGroupDialog(null); }

    private void openFormulaDialog(FormulaDto existing) {
        try {
            FXMLLoader loader = new FXMLLoader(CalculatorApp.class.getResource("fxml/formula-dialog.fxml"));
            Parent root = loader.load();
            FormulaDialogController ctrl = loader.getController();
            ctrl.init(existing, groups);

            Stage stage = buildDialog(root, existing == null ? "Добавить формулу" : "Редактировать формулу");
            ctrl.setStage(stage);
            stage.showAndWait();
            if (ctrl.isSaved()) loadAll();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void deleteFormula(FormulaDto f) {
        if (!confirm("Удалить формулу «" + f.name() + "»?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().deleteFormula(f.id());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadAll());
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    private void openGroupDialog(FormulaGroupDto existing) {
        TextInputDialog dlg = new TextInputDialog(existing != null ? existing.name() : "");
        dlg.setTitle(existing == null ? "Добавить группу" : "Переименовать группу");
        dlg.setHeaderText(null);
        dlg.setContentText("Название группы:");
        Optional<String> result = dlg.showAndWait();
        result.ifPresent(name -> {
            if (name.isBlank()) return;
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    if (existing == null) ApiClient.getInstance().createFormulaGroup(name);
                    else ApiClient.getInstance().updateFormulaGroup(existing.id(), name);
                    return null;
                }
            };
            task.setOnSucceeded(e -> loadAll());
            task.setOnFailed(e -> handleApiError(task.getException()));
            new Thread(task).start();
        });
    }

    private void deleteGroup(FormulaGroupDto g) {
        if (!confirm("Удалить группу «" + g.name() + "»?\nВсе формулы в группе также будут удалены.")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().deleteFormulaGroup(g.id());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadAll());
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

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

    private void handleApiError(Throwable ex) {
        String msg = ex instanceof ApiException ae ? ae.getMessage() : (ex != null ? ex.getMessage() : "Ошибка");
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
