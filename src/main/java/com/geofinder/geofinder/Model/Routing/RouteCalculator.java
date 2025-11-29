package com.geofinder.geofinder.Model.Routing;

import com.geofinder.geofinder.Controllers.SidebarController;

public class RouteCalculator {
    private RoutingStrategy strategy;
    private final SidebarController sidebarController;

    public RouteCalculator(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
        this.strategy = new DrivingStrategy(); // Default strategy
    }

    public void setStrategy(RoutingStrategy strategy) {
        this.strategy = strategy;
    }

    public void calculateRoute(String startLocation, String endLocation) {
        // Handle weather updates specific to strategy
        strategy.handleWeatherUpdates(sidebarController, startLocation, endLocation);

        // Calculate route with selected strategy
        sidebarController.searchRouteClicked(startLocation, endLocation, strategy.getModeIdentifier());
    }
}