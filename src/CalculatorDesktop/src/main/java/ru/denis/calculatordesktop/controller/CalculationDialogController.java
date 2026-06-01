package ru.denis.calculatordesktop.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.denis.calculatordesktop.api.ApiClient;
import ru.denis.calculatordesktop.api.dto.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculationDialogController implements Initializable {

    private static final String ERR_STYLE =
        "-fx-border-color:#ef4444;-fx-border-width:1.5;-fx-border-radius:6;-fx-background-radius:6;";

    @FXML private Label    dialogTitle;
    @FXML private TextField nameField;
    @FXML private ComboBox<FormulaDto> formulaCombo;
    @FXML private Label    expressionLabel;
    @FXML private VBox     itemsContainer;
    @FXML private Label    errorLabel;
    @FXML private Button   saveButton;

    private Stage stage;
    private CalculationDto existing;
    private boolean saved = false;

    private List<MaterialDto> allMaterials = new ArrayList<>();
    private final List<ItemRow> itemRows = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadInitialData();
    }

    public void init(CalculationDto existing) {
        this.existing = existing;
        if (existing != null) {
            dialogTitle.setText("Редактировать расчёт");
            nameField.setText(existing.name());
        }
        nameField.textProperty().addListener((obs, o, n) -> nameField.setStyle(""));
        formulaCombo.valueProperty().addListener((obs, o, n) -> formulaCombo.setStyle(""));
    }

    public void setStage(Stage stage) { this.stage = stage; }
    public boolean isSaved()          { return saved; }

    // =========================================================================
    // Data loading
    // =========================================================================

    private void loadInitialData() {
        Task<Object[]> task = new Task<>() {
            @Override protected Object[] call() throws Exception {
                List<FormulaDto>   formulas  = ApiClient.getInstance().getFormulas();
                List<MaterialDto>  materials = ApiClient.getInstance().getMaterials(null, null);
                return new Object[]{ formulas, materials };
            }
        };
        task.setOnSucceeded(e -> {
            @SuppressWarnings("unchecked")
            List<FormulaDto>  formulas  = (List<FormulaDto>)  ((Object[]) task.getValue())[0];
            @SuppressWarnings("unchecked")
            List<MaterialDto> materials = (List<MaterialDto>) ((Object[]) task.getValue())[1];

            allMaterials = materials;
            formulaCombo.setItems(FXCollections.observableArrayList(formulas));

            if (existing != null) {
                formulas.stream()
                        .filter(f -> f.id().equals(existing.formulaId()))
                        .findFirst()
                        .ifPresent(f -> {
                            formulaCombo.setValue(f);
                            buildItemRows(f);
                            populateExistingItems();
                        });
            }
        });
        task.setOnFailed(e -> Platform.runLater(() ->
                showError("Не удалось загрузить данные: " + task.getException().getMessage())));
        new Thread(task).start();
    }

    // =========================================================================
    // Formula selection
    // =========================================================================

    @FXML
    private void handleFormulaSelected() {
        FormulaDto formula = formulaCombo.getValue();
        if (formula == null) return;

        expressionLabel.setText("Выражение: " + formula.expression());
        expressionLabel.setVisible(true);
        expressionLabel.setManaged(true);

        buildItemRows(formula);
    }

    private void buildItemRows(FormulaDto formula) {
        List<String> placeholders = extractPlaceholders(formula.expression());
        itemRows.clear();
        itemsContainer.getChildren().clear();

        for (int i = 0; i < placeholders.size(); i++) {
            String ph = placeholders.get(i);
            ItemRow row = createRow(i, ph);
            itemRows.add(row);
            itemsContainer.getChildren().add(row.box);
        }
    }

    private void populateExistingItems() {
        if (existing == null || existing.items() == null) return;

        List<CalculationItemDto> sorted = existing.items().stream()
                .sorted((a, b) -> Short.compare(a.position(), b.position()))
                .toList();

        for (int i = 0; i < Math.min(sorted.size(), itemRows.size()); i++) {
            CalculationItemDto item = sorted.get(i);
            ItemRow row = itemRows.get(i);
            row.quantityField().setText(item.quantity() != null ? item.quantity().toPlainString() : "");
            if (!row.isConst() && row.materialCombo() != null && item.materialId() != null) {
                allMaterials.stream()
                        .filter(m -> m.id().equals(item.materialId()))
                        .findFirst()
                        .ifPresent(row.materialCombo()::setValue);
            }
        }
    }

    // =========================================================================
    // Save
    // =========================================================================

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        FormulaDto formula = formulaCombo.getValue();

        nameField.setStyle("");
        formulaCombo.setStyle("");

        if (name.isEmpty()) {
            nameField.setStyle(ERR_STYLE);
            showError("Введите название расчёта");
            return;
        }
        if (formula == null) {
            formulaCombo.setStyle(ERR_STYLE);
            showError("Выберите формулу");
            return;
        }
        if (itemRows.isEmpty()) { showError("Формула не содержит позиций"); return; }

        List<Map<String, Object>> items = new ArrayList<>();
        for (ItemRow row : itemRows) {
            String qtyText = row.quantityField().getText().trim().replace(',', '.');
            if (qtyText.isEmpty()) {
                row.quantityField().setStyle(ERR_STYLE);
                showError("Введите количество для позиции " + (row.position() + 1));
                return;
            }
            BigDecimal qty;
            try {
                qty = new BigDecimal(qtyText);
            } catch (NumberFormatException e) {
                row.quantityField().setStyle(ERR_STYLE);
                showError("Некорректное число для позиции " + (row.position() + 1));
                return;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("position", (short) row.position());
            item.put("quantity", qty);

            if (!row.isConst()) {
                MaterialDto mat = row.materialCombo().getValue();
                if (mat == null) {
                    row.materialCombo().setStyle(ERR_STYLE);
                    showError("Выберите материал для позиции " + (row.position() + 1));
                    return;
                }
                item.put("materialId", mat.id());
            }
            items.add(item);
        }

        saveButton.setDisable(true);

        Task<CalculationDto> task = new Task<>() {
            @Override protected CalculationDto call() throws Exception {
                if (existing == null) {
                    return ApiClient.getInstance().createCalculation(name, formula.id(), items);
                } else {
                    return ApiClient.getInstance().updateCalculation(existing.id(), name, formula.id(), items);
                }
            }
        };
        task.setOnSucceeded(e -> { saved = true; stage.close(); });
        task.setOnFailed(e -> {
            saveButton.setDisable(false);
            Platform.runLater(() -> showError(task.getException().getMessage()));
        });
        new Thread(task).start();
    }

    @FXML private void handleCancel() { stage.close(); }

    // =========================================================================
    // Item row builder
    // =========================================================================

    private ItemRow createRow(int position, String placeholder) {
        boolean isConst = "const".equalsIgnoreCase(placeholder);

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6; "
                + "-fx-padding: 10 12; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-border-width: 1;");

        String labelText = isConst
                ? "Константа " + (position + 1)
                : "Материал {" + placeholder + "}";
        Label lbl = new Label(labelText + ":");
        lbl.setStyle("-fx-font-weight: bold; -fx-min-width: 140px; -fx-text-fill: #374151;");

        ComboBox<MaterialDto> combo = null;
        if (!isConst) {
            List<MaterialDto> groupMaterials = allMaterials.stream()
                    .filter(m -> placeholder.equalsIgnoreCase(m.groupName()))
                    .toList();
            combo = new ComboBox<>(FXCollections.observableArrayList(groupMaterials));
            combo.setPromptText("Выберите материал");
            combo.setPrefWidth(200);
            HBox.setHgrow(combo, Priority.ALWAYS);
        }

        TextField qty = new TextField();
        qty.setPromptText("Количество");
        qty.setPrefWidth(110);

        box.getChildren().add(lbl);
        if (combo != null) {
            Label timesLbl = new Label("×");
            timesLbl.setStyle("-fx-text-fill: #6b7280;");
            box.getChildren().addAll(combo, timesLbl);
        }
        box.getChildren().add(qty);

        return new ItemRow(position, placeholder, box, combo, qty);
    }

    private List<String> extractPlaceholders(String expression) {
        List<String> result = new ArrayList<>();
        Matcher m = Pattern.compile("\\{([^}]+)\\}").matcher(expression);
        while (m.find()) result.add(m.group(1));
        return result;
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (stage != null) Platform.runLater(stage::sizeToScene);
    }

    // =========================================================================
    // Inner record
    // =========================================================================

    private record ItemRow(
            int position,
            String placeholder,
            HBox box,
            ComboBox<MaterialDto> materialCombo,
            TextField quantityField
    ) {
        boolean isConst() { return "const".equals(placeholder); }
    }
}
