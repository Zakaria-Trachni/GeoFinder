package com.geofinder.geofinder.Model.MapFacade;

import javafx.scene.web.WebEngine;

public class MapFacade {
    private final WebEngine webEngine;

    public MapFacade(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    // Map operations
    public void clearAll() {
        webEngine.executeScript("clearAll()");
    }

    public void clearRoute() {
        webEngine.executeScript("clearRoute()");
    }

    public void clearAllCategories() {
        webEngine.executeScript("clearAllCategories()");
    }

    // Marker operations
    public void addCurrentPositionMarker(double lat, double lon) {
        webEngine.executeScript(String.format("addCurrPositionMarker(%f, %f)", lat, lon));
    }

    public void removeCurrentPositionMarker() {
        webEngine.executeScript("removeCurrPositionMarker()");
    }

    // Search operations
    public void geocodeAddress(String address) {
        webEngine.executeScript(String.format("geocodeAddress('%s')", sanitize(address)));
    }

    // Routing operations
    public void showRouteWithMode(double fromLat, double fromLon, double toLat, double toLon, String mode) {
        webEngine.executeScript(String.format("showRouteWithMode(%f, %f, %f, %f, '%s')", fromLat, fromLon, toLat, toLon, mode));
    }

    public void showMarkerToLocationRoute(double markerLat, double markerLon, double locationLat, double locationLon) {
        webEngine.executeScript(String.format("showMarkerToCategoryLocationRoute(%f, %f, %f, %f)", markerLat, markerLon, locationLat, locationLon));
    }

    // Nearby places operations
    public void showSearchRadius(int radius, double lat, double lon) {
        webEngine.executeScript(String.format("showSearchRadius(%d, %f, %f)", radius, lat, lon));
    }

    public void addCategoryMarker(String category, double lat, double lon, String popup, int radius) {
        webEngine.executeScript(String.format("addMarkerCategory('%s', %f, %f, '%s', %d)", category, lat, lon, sanitize(popup), radius));
    }

    // Weather operations
    public void getWeatherData(String location, String whichLocation) {
        webEngine.executeScript(String.format("getWeatherData('%s', '%s')", sanitize(location), whichLocation));
    }

    private String sanitize(String input) {
        return input.replace("'", "\\'");
    }
}