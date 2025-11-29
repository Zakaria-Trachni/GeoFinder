package com.geofinder.geofinder.Model.Routing;

import com.geofinder.geofinder.Controllers.SidebarController;

public class CyclingStrategy implements RoutingStrategy {
    @Override
    public String getModeIdentifier() {
        return "cycling";
    }

    @Override
    public void handleWeatherUpdates(SidebarController sidebarController, String startLocation, String endLocation) {
        sidebarController.getWeatherData(startLocation, "startLocation");
        sidebarController.getWeatherData(endLocation, "endLocation");
    }
}