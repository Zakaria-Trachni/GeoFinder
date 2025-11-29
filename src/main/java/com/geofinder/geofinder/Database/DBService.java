package com.geofinder.geofinder.Database;

import com.geofinder.geofinder.Model.Location;
import com.geofinder.geofinder.Model.Position;
import com.geofinder.geofinder.Database.DAO.LocationDAO;
import com.geofinder.geofinder.Database.DAO.LocationDAOImpl;
import com.geofinder.geofinder.Database.DAO.MarkerDAO;
import com.geofinder.geofinder.Database.DAO.MarkerDAOImpl;

import java.util.List;

public class DBService {
    private final LocationDAO locationDAO;
    private final MarkerDAO markerDAO;

    public DBService() {
        this.locationDAO = new LocationDAOImpl();
        this.markerDAO = new MarkerDAOImpl();
    }

    // ---------------------------------------------- Location operations ----------------------------------------------
    public void addLocationToCategory(String Category, Location location) throws Exception {
        locationDAO.addLocationToCategory(Category, location);
    }

    public void insertLocationsData(List<Location> categoryList, String Category) throws Exception {
        locationDAO.insertLocationsData(categoryList, Category);
    }

    public void updateSpecificLocationData(Location newLocation, String oldLocationName) throws Exception {
        locationDAO.updateSpecificLocationData(newLocation, oldLocationName);
    }

    public List<Location> loadLocationsData(String Category) throws Exception {
        return locationDAO.loadLocationsData(Category);
    }

    public List<Location> searchLocationsByName(String locationName) {
        return locationDAO.searchLocationsByName(locationName);
    }

    public void printCategoryData(String Category) throws Exception {
        locationDAO.printCategoryData(Category);
    }

    public void printAllLocationsData() throws Exception {
        locationDAO.printAllLocationsData();
    }

    public void deleteSpecificLocationData(String category, String locationName) throws Exception {
        locationDAO.deleteSpecificLocationData(category, locationName);
    }

    public void deleteCategoryData(String Category) throws Exception {
        locationDAO.deleteCategoryData(Category);
    }

    public void deleteAllLocationsData() throws Exception {
        locationDAO.deleteAllLocationsData();
    }


    // ----------------------------------------------- Marker operations -----------------------------------------------
    public void insertMarkerData(double lat, double lon) {
        markerDAO.insertMarkerData(lat, lon);
    }

    public Position loadMarkerPosition() {
        return markerDAO.loadMarkerPosition();
    }

    public void deleteMarkerData() throws Exception {
        markerDAO.deleteMarkerData();
    }
}