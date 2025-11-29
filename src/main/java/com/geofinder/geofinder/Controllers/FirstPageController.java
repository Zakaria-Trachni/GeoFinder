package com.geofinder.geofinder.Controllers;

import com.geofinder.geofinder.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class FirstPageController {
    @FXML
    public void showMainPage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/geofinder/geofinder/views/mainPage.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/geofinder/geofinder/stylesheets/mainPage.css")).toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}