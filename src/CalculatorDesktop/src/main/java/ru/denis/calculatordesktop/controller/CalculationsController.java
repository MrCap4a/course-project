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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.denis.calculatordesktop.CalculatorApp;
import ru.denis.calculatordesktop.api.ApiClient;
import ru.denis.calculatordesktop.api.ApiException;
import ru.denis.calculatordesktop.api.dto.CalculationDto;
import ru.denis.calculatordesktop.api.dto.CalculationItemDto;
import ru.denis.calculatordesktop.api.dto.FormulaDto;
import ru.denis.calculatordesktop.api.dto.FormulaGroupDto;
import ru.denis.calculatordesktop.util.Icons;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CalculationsController implements Initializable {

    @FXML private ComboBox<FormulaGroupDto> groupFilter;
    @FXML private ComboBox<FormulaDto>     formulaFilter;
    @FXML private TextField                searchField;

    @FXML private TableView<CalculationDto>            table;
    @FXML private TableColumn<CalculationDto, String>  colName;
    @FXML private TableColumn<CalculationDto, String>  colFormula;
    @FXML private TableColumn<CalculationDto, String>  colResult;
    @FXML private TableColumn<CalculationDto, Void>    colActions;
    @FXML private VBox detailPanel;

    private final ObservableList<CalculationDto> calculations    = FXCollections.observableArrayList();
    private       List<CalculationDto>           allCalculations = List.of();
    private       List<FormulaDto>               allFormulas     = List.of();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colName.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().name()));
        colFormula.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().formulaName()));
        colResult.setCellValueFactory(d -> {
            var r = d.getValue().result();
            return new javafx.beans.property.SimpleStringProperty(r != null ? r.toPlainString() : "—");
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button edit = Icons.editButton();
            private final Button del  = Icons.deleteButton();
            private final HBox box = new HBox(2, edit, del);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                edit.setOnAction(e -> openDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> deleteCalc(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.setItems(calculations);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) showDetail(sel);
            else clearDetail();
        });
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    private void loadData() {
        Task<Object[]> task = new Task<>() {
            @Override protected Object[] call() throws Exception {
                return new Object[]{
                    ApiClient.getInstance().getCalculations(),
                    ApiClient.getInstance().getFormulas(),
                    ApiClient.getInstance().getFormulaGroups()
                };
            }
        };
        task.setOnSucceeded(e -> {
            @SuppressWarnings("unchecked")
            List<CalculationDto>  calcs    = (List<CalculationDto>)  ((Object[]) task.getValue())[0];
            @SuppressWarnings("unchecked")
            List<FormulaDto>      formulas = (List<FormulaDto>)      ((Object[]) task.getValue())[1];
            @SuppressWarnings("unchecked")
            List<FormulaGroupDto> groups   = (List<FormulaGroupDto>) ((Object[]) task.getValue())[2];

            allCalculations = calcs;
            allFormulas     = formulas;
            refreshGroupFilter(groups);
            refreshFormulaFilter(formulas);
            applyFilter();
            clearDetail();
        });
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    private void refreshGroupFilter(List<FormulaGroupDto> groups) {
        FormulaGroupDto selected = groupFilter.getValue();
        ObservableList<FormulaGroupDto> items = FXCollections.observableArrayList();
        items.add(new FormulaGroupDto(-1, "Все группы"));
        items.addAll(groups);
        groupFilter.setItems(items);
        if (selected != null && selected.id() >= 0) {
            groupFilter.setValue(
                items.stream().filter(g -> g.id().equals(selected.id())).findFirst().orElse(items.get(0))
            );
        } else {
            groupFilter.setValue(items.get(0));
        }
    }

    private void refreshFormulaFilter(List<FormulaDto> formulas) {
        FormulaDto selected = formulaFilter.getValue();
        ObservableList<FormulaDto> items = FXCollections.observableArrayList();
        items.add(new FormulaDto(-1, "Все формулы", "", null, null));
        items.addAll(formulas);
        formulaFilter.setItems(items);
        if (selected != null && selected.id() >= 0) {
            formulaFilter.setValue(
                items.stream().filter(f -> f.id().equals(selected.id())).findFirst().orElse(items.get(0))
            );
        } else {
            formulaFilter.setValue(items.get(0));
        }
    }

    private void applyFilter() {
        FormulaGroupDto selGroup   = groupFilter.getValue();
        FormulaDto      selFormula = formulaFilter.getValue();
        Integer groupId   = (selGroup   != null && selGroup.id()   != null && selGroup.id()   >= 0) ? selGroup.id()   : null;
        Integer formulaId = (selFormula != null && selFormula.id() != null && selFormula.id() >= 0) ? selFormula.id() : null;
        String search = searchField.getText().trim();

        List<CalculationDto> filtered = allCalculations.stream()
                .filter(c -> groupId   == null || groupId.equals(c.formulaGroupId()))
                .filter(c -> formulaId == null || formulaId.equals(c.formulaId()))
                .filter(c -> search.isEmpty()
                        || c.name().toLowerCase().contains(search.toLowerCase())
                        || c.formulaName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        calculations.setAll(filtered);
    }

    // =========================================================================
    // Handlers
    // =========================================================================

    @FXML private void handleGroupFilter() {
        FormulaGroupDto selGroup = groupFilter.getValue();
        Integer groupId = (selGroup != null && selGroup.id() >= 0) ? selGroup.id() : null;
        List<FormulaDto> visible = groupId == null ? allFormulas
                : allFormulas.stream().filter(f -> groupId.equals(f.groupId())).toList();
        refreshFormulaFilter(visible);
        applyFilter();
    }
    @FXML private void handleFormulaFilter() { applyFilter(); }
    @FXML private void handleSearch()        { applyFilter(); }
    @FXML private void handleAdd()           { openDialog(null); }

    // =========================================================================
    // Dialog
    // =========================================================================

    private void openDialog(CalculationDto existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CalculatorApp.class.getResource("fxml/calculation-dialog.fxml"));
            Parent root = loader.load();
            CalculationDialogController ctrl = loader.getController();
            ctrl.init(existing);

            Stage stage = new Stage();
            stage.setTitle(existing == null ? "Новый расчёт" : "Редактировать расчёт");
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

    private void deleteCalc(CalculationDto c) {
        if (!confirm("Удалить расчёт «" + c.name() + "»?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().deleteCalculation(c.id());
                return null;
            }
        };
        task.setOnSucceeded(e -> { loadData(); clearDetail(); });
        task.setOnFailed(e -> handleApiError(task.getException()));
        new Thread(task).start();
    }

    // =========================================================================
    // Detail panel
    // =========================================================================

    private void showDetail(CalculationDto calc) {
        detailPanel.getChildren().clear();

        Label title = new Label(calc.name());
        title.getStyleClass().add("detail-title");

        Label formulaLbl = new Label("Формула: " + calc.formulaName()
                + " [" + calc.formulaGroupName() + "]");
        formulaLbl.setStyle("-fx-text-fill: #374151;");

        Label exprLbl = new Label(calc.formulaExpression());
        exprLbl.getStyleClass().add("detail-expression");
        exprLbl.setWrapText(true);

        HBox resultBox = new HBox(10);
        resultBox.setAlignment(Pos.CENTER_LEFT);
        Label rl = new Label("Результат:");
        rl.getStyleClass().add("result-label");
        String rs = calc.result() != null ? calc.result().toPlainString() : "—";
        Label rv = new Label(rs);
        rv.getStyleClass().add("result-value");
        resultBox.getChildren().addAll(rl, rv);

        detailPanel.getChildren().addAll(title, formulaLbl, exprLbl, new Separator(), resultBox);

        if (calc.items() != null && !calc.items().isEmpty()) {
            Label itemsTitle = new Label("Позиции:");
            itemsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            detailPanel.getChildren().add(itemsTitle);

            for (CalculationItemDto item : calc.items()) {
                String desc;
                if (item.isConst()) {
                    desc = "Константа = " + item.quantity().toPlainString();
                } else {
                    String price = item.materialPrice() != null
                            ? item.materialPrice().toPlainString() : "?";
                    String units = item.materialUnits() != null ? item.materialUnits() : "";
                    desc = item.materialName() + "  ×  " + item.quantity().toPlainString()
                            + "  (" + price + " " + units + ")";
                }
                Label row = new Label("  •  " + desc);
                row.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px;");
                detailPanel.getChildren().add(row);
            }
        }
    }

    private void clearDetail() {
        detailPanel.getChildren().clear();
        Label hint = new Label("Выберите расчёт для просмотра деталей");
        hint.getStyleClass().add("hint-label");
        detailPanel.getChildren().add(hint);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

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
