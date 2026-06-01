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
import ru.denis.calculatordesktop.api.dto.MaterialDto;
import ru.denis.calculatordesktop.api.dto.MaterialGroupDto;

import java.math.BigDecimal;
import java.util.List;

public class MaterialDialogController {

    private static final String ERR_STYLE =
        "-fx-border-color:#ef4444;-fx-border-width:1.5;-fx-border-radius:6;-fx-background-radius:6;";

    @FXML private Label    dialogTitle;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField unitsField;
    @FXML private ComboBox<MaterialGroupDto> groupCombo;
    @FXML private Label    errorLabel;

    private Stage stage;
    private MaterialDto existing;
    private boolean saved = false;

    public void init(MaterialDto existing, List<MaterialGroupDto> groups) {
        this.existing = existing;
        groupCombo.setItems(FXCollections.observableArrayList(groups));

        if (existing != null) {
            dialogTitle.setText("Редактировать материал");
            nameField.setText(existing.name());
            priceField.setText(existing.price() != null ? existing.price().toPlainString() : "");
            unitsField.setText(existing.units());
            groups.stream().filter(g -> g.id().equals(existing.groupId()))
                    .findFirst().ifPresent(groupCombo::setValue);
        }

        for (TextField f : List.of(nameField, priceField, unitsField)) {
            f.textProperty().addListener(clearOnType(f));
        }
    }

    public void setStage(Stage stage) { this.stage = stage; }
    public boolean isSaved()          { return saved; }

    @FXML
    private void handleSave() {
        String name      = nameField.getText().trim();
        String priceText = priceField.getText().trim().replace(',', '.');
        String units     = unitsField.getText().trim();
        Integer groupId  = groupCombo.getValue() != null ? groupCombo.getValue().id() : null;

        clearAllErrors();

        boolean valid = true;
        if (name.isEmpty())  { markError(nameField,  "Введите название");          valid = false; }
        if (units.isEmpty()) { markError(unitsField, "Введите единицу измерения"); valid = false; }

        BigDecimal price = null;
        try {
            price = new BigDecimal(priceText);
            if (price.signum() < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            markError(priceField, "Введите корректную цену (неотрицательное число)");
            valid = false;
        }

        if (!valid) return;

        BigDecimal finalPrice = price;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                if (existing == null) {
                    ApiClient.getInstance().createMaterial(name, finalPrice, units, groupId);
                } else {
                    ApiClient.getInstance().updateMaterial(existing.id(), name, finalPrice, units, groupId);
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
        for (Control f : List.of(nameField, priceField, unitsField)) f.setStyle("");
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
