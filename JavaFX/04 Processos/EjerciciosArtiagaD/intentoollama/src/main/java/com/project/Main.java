package com.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        Parent largeroot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/assets/SuicideAi.fxml")));
        Scene scene = new Scene(largeroot);

        stage.setTitle("SuicideAi");
        stage.setWidth(920);
        stage.setHeight(635);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);

    }

}