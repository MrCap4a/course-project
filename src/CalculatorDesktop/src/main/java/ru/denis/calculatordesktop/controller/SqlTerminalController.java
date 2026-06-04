package ru.denis.calculatordesktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.denis.calculatordesktop.CalculatorApp;
import ru.denis.calculatordesktop.api.ApiClient;
import ru.denis.calculatordesktop.api.dto.SqlResultDto;
import ru.denis.calculatordesktop.api.dto.SqlSchemaDto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SqlTerminalController implements Initializable {

    @FXML private TextArea queryArea;
    @FXML private Button btnRun;
    @FXML private Button btnSchema;
    @FXML private Label statusLabel;
    @FXML private TableView<ObservableList<String>> resultTable;
    @FXML private VBox errorBox;
    @FXML private Label errorLabel;

    private SqlSchemaDto cachedSchema;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        queryArea.setText("SELECT * FROM \"user\" LIMIT 10;");
        errorBox.setVisible(false);
        errorBox.setManaged(false);
        statusLabel.setText("");
        loadSchema();
    }

    @FXML
    private void handleRun() {
        String query = queryArea.getText().trim();
        if (query.isEmpty()) return;

        btnRun.setDisable(true);
        statusLabel.setText("Выполняется...");
        clearResults();

        Task<SqlResultDto> task = new Task<>() {
            @Override protected SqlResultDto call() throws Exception {
                return ApiClient.getInstance().executeSql(query);
            }
        };

        task.setOnSucceeded(e -> {
            btnRun.setDisable(false);
            SqlResultDto result = task.getValue();
            showResults(result);
            statusLabel.setText("Строк: " + result.rowCount());
            hideError();
        });

        task.setOnFailed(e -> {
            btnRun.setDisable(false);
            statusLabel.setText("");
            showError(extractMessage(task.getException()));
        });

        new Thread(task).start();
    }

    @FXML
    private void handleSchema() {
        if (cachedSchema == null) {
            statusLabel.setText("Схема ещё не загружена");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    CalculatorApp.class.getResource("fxml/schema-dialog.fxml"));
            Parent root = loader.load();
            SchemaDialogController ctrl = loader.getController();
            ctrl.init(cachedSchema);

            Stage stage = new Stage();
            stage.setTitle("Схема базы данных");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(CalculatorApp.getPrimaryStage());
            stage.setScene(new Scene(root, 1100, 750));
            stage.show();
        } catch (Exception ex) {
            showError("Не удалось открыть схему: " + ex.getMessage());
        }
    }

    private void loadSchema() {
        Task<SqlSchemaDto> task = new Task<>() {
            @Override protected SqlSchemaDto call() throws Exception {
                return ApiClient.getInstance().getSqlSchema();
            }
        };
        task.setOnSucceeded(e -> cachedSchema = task.getValue());
        new Thread(task).start();
    }

    private void showResults(SqlResultDto result) {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();

        List<String> columns = result.columns();
        for (int i = 0; i < columns.size(); i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(columns.get(i));
            col.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().size() > colIndex ? data.getValue().get(colIndex) : ""));
            col.setPrefWidth(140);
            resultTable.getColumns().add(col);
        }

        List<Map<String, Object>> rows = result.rows();
        for (Map<String, Object> row : rows) {
            ObservableList<String> rowData = FXCollections.observableArrayList();
            for (String col : columns) {
                Object val = row.get(col);
                rowData.add(val != null ? val.toString() : "null");
            }
            resultTable.getItems().add(rowData);
        }
    }

    private void clearResults() {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    private void hideError() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }

    private String extractMessage(Throwable t) {
        if (t == null) return "Неизвестная ошибка";
        String msg = t.getMessage();
        return msg != null ? msg : t.getClass().getSimpleName();
    }
}
