package com.geofinder.geofinder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // FXML:
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/geofinder/geofinder/views/firstPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // CSS:
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/geofinder/geofinder/stylesheets/firstPage.css")).toExternalForm());

        // Window details:
        stage.setTitle("GeoFinder");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/geofinder/geofinder/assets/images/appIcon.png")));
        stage.getIcons().add(image);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}