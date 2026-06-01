package ru.denis.calculatordesktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CalculatorApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Калькулятор");
        showLogin();
        stage.show();
    }

    public static void showLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(CalculatorApp.class.getResource("fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 480, 420);
        scene.getStylesheets().add(CalculatorApp.class.getResource("css/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
    }

    public static void showMain() throws IOException {
        FXMLLoader loader = new FXMLLoader(CalculatorApp.class.getResource("fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);
        scene.getStylesheets().add(CalculatorApp.class.getResource("css/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
