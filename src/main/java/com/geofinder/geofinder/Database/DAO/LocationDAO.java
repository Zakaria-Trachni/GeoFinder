package com.geofinder.geofinder.Database.DAO;

import com.geofinder.geofinder.Model.Location;
import java.util.List;

public interface LocationDAO{
    public void addLocationToCategory(String category, Location location) throws Exception;
    public void insertLocationsData(List<Location> categoryList, String Category);
    public void updateSpecificLocationData(Location newLocation, String oldLocationName);
    public List<Location> loadLocationsData(String Category);
    public List<Location> searchLocationsByName(String locationName);
    public void printCategoryData(String Category) throws Exception;
    public void printAllLocationsData() throws Exception;
    public void deleteSpecificLocationData(String category, String locationName) throws Exception;
    public void deleteCategoryData(String Category);
    public void deleteAllLocationsData() throws Exception;
}