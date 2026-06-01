package ru.denis.calculatordesktop.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.denis.calculatordesktop.api.ApiClient;
import ru.denis.calculatordesktop.api.dto.FormulaDto;
import ru.denis.calculatordesktop.api.dto.FormulaGroupDto;
import ru.denis.calculatordesktop.api.dto.MaterialGroupDto;

import java.util.List;

public class FormulaDialogController {

    private static final String ERR_STYLE =
        "-fx-border-color:#ef4444;-fx-border-width:1.5;-fx-border-radius:6;-fx-background-radius:6;";

    @FXML private Label    dialogTitle;
    @FXML private TextField nameField;
    @FXML private TextField expressionField;
    @FXML private ComboBox<FormulaGroupDto> groupCombo;
    @FXML private Label    errorLabel;

    @FXML private TextField matGroupSearch;
    @FXML private VBox      matGroupsBox;

    private Stage stage;
    private FormulaDto existing;
    private boolean saved = false;

    private final ObservableList<MaterialGroupDto> allMatGroups = FXCollections.observableArrayList();

    public void init(FormulaDto existing, List<FormulaGroupDto> groups) {
        this.existing = existing;
        groupCombo.setItems(FXCollections.observableArrayList(groups));

        if (existing != null) {
            dialogTitle.setText("Редактировать формулу");
            nameField.setText(existing.name());
            expressionField.setText(existing.expression());
            groups.stream().filter(g -> g.id().equals(existing.groupId()))
                    .findFirst().ifPresent(groupCombo::setValue);
        }

        for (TextField f : List.of(nameField, expressionField)) {
            f.textProperty().addListener(clearOnType(f));
        }

        matGroupSearch.textProperty().addListener((obs, old, val) -> renderMatGroups(val));
        loadMaterialGroups();
    }

    public void setStage(Stage stage) { this.stage = stage; }
    public boolean isSaved()          { return saved; }

    private void loadMaterialGroups() {
        Task<List<MaterialGroupDto>> task = new Task<>() {
            @Override protected List<MaterialGroupDto> call() throws Exception {
                return ApiClient.getInstance().getMaterialGroups();
            }
        };
        task.setOnSucceeded(e -> {
            allMatGroups.setAll(task.getValue());
            renderMatGroups(matGroupSearch.getText());
        });
        new Thread(task).start();
    }

    private void renderMatGroups(String query) {
        matGroupsBox.getChildren().clear();
        allMatGroups.stream()
                .filter(g -> query == null || query.isBlank()
                        || g.name().toLowerCase().contains(query.toLowerCase()))
                .forEach(g -> {
                    Button chip = new Button("{" + g.name() + "}");
                    chip.getStyleClass().add("mat-group-chip");
                    chip.setMaxWidth(Double.MAX_VALUE);
                    chip.setOnAction(e -> insertIntoExpression("{" + g.name() + "}"));
                    matGroupsBox.getChildren().add(chip);
                });
    }

    private void insertIntoExpression(String text) {
        int pos = expressionField.getCaretPosition();
        String current = expressionField.getText();
        expressionField.setText(current.substring(0, pos) + text + current.substring(pos));
        expressionField.positionCaret(pos + text.length());
        expressionField.requestFocus();
    }

    @FXML
    private void handleSave() {
        String name       = nameField.getText().trim();
        String expression = expressionField.getText().trim();
        FormulaGroupDto group = groupCombo.getValue();

        clearAllErrors();

        boolean valid = true;
        if (name.isEmpty())       { markError(nameField,       "Введите название");            valid = false; }
        if (expression.isEmpty()) { markError(expressionField, "Введите выражение формулы");   valid = false; }
        if (!valid) return;

        Integer groupId = group != null ? group.id() : null;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                if (existing == null) {
                    ApiClient.getInstance().createFormula(name, expression, groupId);
                } else {
                    ApiClient.getInstance().updateFormula(existing.id(), name, expression, groupId);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> { saved = true; stage.close(); });
        task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException().getMessage())));
        new Thread(task).start();
    }

    @FXML private void handleCancel() { stage.close(); }

    private void markError(Control field, String msg) {
        field.setStyle(ERR_STYLE);
        showError(msg);
    }

    private void clearAllErrors() {
        for (Control f : List.of(nameField, expressionField)) f.setStyle("");
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
