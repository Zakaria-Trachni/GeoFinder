package com.geofinder.geofinder.Controllers;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.util.Objects;

public class MapController {
    private final WebEngine webEngine;

    public MapController(WebView webView) {
        this.webEngine = webView.getEngine();
        initializeMap();
    }

    private void initializeMap() {
        webEngine.load(Objects.requireNonNull(getClass().getResource("/web/scripts/map.html")).toExternalForm());
    }

    public void zoomIn() {
        webEngine.executeScript("map.zoomIn();");
    }

    public void zoomOut() {
        webEngine.executeScript("map.zoomOut();");
    }

    public void locateUser() {
        webEngine.executeScript("clearAll();");
        webEngine.executeScript("locateUser();");
    }

    public void setLayer(String layerName) {
        webEngine.executeScript("setLayer('" + layerName + "');");
    }
}