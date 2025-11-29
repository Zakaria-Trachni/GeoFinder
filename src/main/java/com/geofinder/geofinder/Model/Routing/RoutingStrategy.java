package com.geofinder.geofinder.Model.Routing;

import com.geofinder.geofinder.Controllers.SidebarController;

public interface RoutingStrategy {
    String getModeIdentifier();
    void handleWeatherUpdates(SidebarController sidebarController, String startLocation, String endLocation);
}