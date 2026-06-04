package ru.denis.calculatordesktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.denis.calculatordesktop.CalculatorApp;
import ru.denis.calculatordesktop.api.dto.CurrentUserDto;
import ru.denis.calculatordesktop.session.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private VBox adminSection;
    @FXML private VBox sqlSection;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private StackPane contentArea;
    @FXML private Button btnMaterials;
    @FXML private Button btnFormulas;
    @FXML private Button btnCalculations;
    @FXML private Button btnUsers;
    @FXML private Button btnRoles;
    @FXML private Button btnSql;

    private Button activeNav;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        CurrentUserDto user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.displayName());
            if (user.role() != null) userRoleLabel.setText(user.role().name());
        }
        if (SessionManager.getInstance().isSuperAdmin()) {
            adminSection.setVisible(true);
            adminSection.setManaged(true);
        }
        if (SessionManager.getInstance().hasPermission("sql.execute")) {
            sqlSection.setVisible(true);
            sqlSection.setManaged(true);
        }
        showMaterials();
    }

    @FXML private void showMaterials()     { navigate("materials.fxml",     btnMaterials); }
    @FXML private void showFormulas()      { navigate("formulas.fxml",      btnFormulas); }
    @FXML private void showCalculations()  { navigate("calculations.fxml",  btnCalculations); }
    @FXML private void showUsers()         { navigate("users.fxml",         btnUsers); }
    @FXML private void showRoles()         { navigate("roles.fxml",         btnRoles); }
    @FXML private void showSqlTerminal()   { navigate("sql-terminal.fxml",  btnSql); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clear();
        try { CalculatorApp.showLogin(); } catch (Exception ignored) {}
    }

    private void navigate(String fxml, Button btn) {
        try {
            FXMLLoader loader = new FXMLLoader(CalculatorApp.class.getResource("fxml/" + fxml));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);

            if (activeNav != null) activeNav.getStyleClass().remove("nav-item-active");
            if (!btn.getStyleClass().contains("nav-item-active"))
                btn.getStyleClass().add("nav-item-active");
            activeNav = btn;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
