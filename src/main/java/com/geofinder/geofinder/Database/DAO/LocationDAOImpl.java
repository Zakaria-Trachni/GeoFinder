package com.geofinder.geofinder.Database.DAO;

import com.geofinder.geofinder.Model.Location;
import com.geofinder.geofinder.Database.JDBC;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDAOImpl implements LocationDAO {
    private final Connection connection;

    public LocationDAOImpl() {
        this.connection = JDBC.getInstance().getConnection();
    }


    // ----------------------------------------------- Insert functions ------------------------------------------------
    @Override
    public void addLocationToCategory(String category, Location location) throws Exception {
        String sql = "INSERT INTO Location (locationCategory, locationLat, locationLon, " +
                "locationDistance, locationName, locationAddress, locationCity, " +
                "locationPostCode, markerID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            int markerID = getMarkerIdFromDatabase();

            preparedStatement.setString(1, category);
            preparedStatement.setDouble(2, location.getLat());
            preparedStatement.setDouble(3, location.getLon());
            preparedStatement.setDouble(4, location.getDistance());
            preparedStatement.setString(5, location.getName());
            preparedStatement.setString(6, location.getAddress());
            preparedStatement.setString(7, location.getCity());
            preparedStatement.setString(8, String.valueOf(location.getPostCode())); // Ensure String
            preparedStatement.setInt(9, markerID);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Insertion for location data in DB successful.");
            } else {
                throw new Exception("Failed to insert location data");
            }

        } catch (SQLException e) {
            throw new Exception("Error adding location to category: " + e.getMessage(), e);
        }
    }

    @Override
    public void insertLocationsData(List<Location> categoryList, String Category) {
        int markerID = getMarkerIdFromDatabase();
        String query = "INSERT INTO Location (locationCategory, locationLat, locationLon, locationDistance, locationName, locationAddress, locationCity, locationPostCode, markerID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            for (Location location : categoryList) {
                preparedStatement.setString(1, location.getCategory());
                preparedStatement.setDouble(2, location.getLat());
                preparedStatement.setDouble(3, location.getLon());
                preparedStatement.setDouble(4, location.getDistance());
                preparedStatement.setString(5, location.getName());
                preparedStatement.setString(6, location.getAddress());
                preparedStatement.setString(7, location.getCity());
                preparedStatement.setInt(8, location.getPostCode());
                preparedStatement.setInt(9, markerID);
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            System.out.println("Insertion for " + Category + " category data in DB successfully.");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    // ----------------------------------------------- Update function -------------------------------------------------
    @Override
    public void updateSpecificLocationData(Location newLocation, String oldLocationName) {
        int markerID = getMarkerIdFromDatabase();
        String query = "UPDATE Location SET " +
                "locationCategory = ?, " +
                "locationLat = ?, " +
                "locationLon = ?, " +
                "locationDistance = ?, " +
                "locationName = ?, " +
                "locationAddress = ?, " +
                "locationCity = ?, " +
                "locationPostCode = ?, " +
                "markerID = ? " +
                "WHERE locationName = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, newLocation.getCategory());
            preparedStatement.setDouble(2, newLocation.getLat());
            preparedStatement.setDouble(3, newLocation.getLon());
            preparedStatement.setDouble(4, newLocation.getDistance());
            preparedStatement.setString(5, newLocation.getName());
            preparedStatement.setString(6, newLocation.getAddress());
            preparedStatement.setString(7, newLocation.getCity());
            preparedStatement.setInt(8, newLocation.getPostCode());
            preparedStatement.setInt(9, markerID);
            preparedStatement.setString(10, oldLocationName);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Locations with name '" + oldLocationName + "' has been updated successfully.");
            } else {
                System.out.println("No location found with name: " + oldLocationName);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------ Load functions -------------------------------------------------
    private int getMarkerIdFromDatabase() {
        String query = "SELECT markerID FROM Marker";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int markerId = resultSet.getInt("markerID");
                return markerId;
            } else {
                System.out.println("No marker position found in the database.");
                return 0;
            }
        } catch (Exception e) {
            System.out.println("Error loading marker position: " + e.getMessage());
            return 0;
        }
    }
    @Override
    public List<Location> loadLocationsData(String Category){
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Location WHERE locationCategory = '" + Category + "'");

            List<Location> categoryList = new ArrayList<>();

            while(resultSet.next()){
                String locationCategory = resultSet.getString("locationCategory");
                double locationLat = resultSet.getDouble("locationLat");
                double locationLon = resultSet.getDouble("locationLon");
                double locationDistance = resultSet.getDouble("locationDistance");
                String locationName = resultSet.getString("locationName");
                String locationAddress = resultSet.getString("locationAddress");
                String locationCity = resultSet.getString("locationCity");
                int locationPostCode = resultSet.getInt("locationPostCode");

                categoryList.add(new Location(locationCategory, locationLat, locationLon, locationDistance, locationName, locationAddress, locationCity, locationPostCode));
            }

            return categoryList;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------- Search function -------------------------------------------------
    @Override
    public List<Location> searchLocationsByName(String locationName) {
        List<Location> locations = new ArrayList<>();

        String query = "SELECT * FROM Location WHERE LOWER(locationName) LIKE LOWER(?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "%" + locationName + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Location location = new Location(
                        resultSet.getString("locationCategory"),
                        resultSet.getDouble("locationLat"),
                        resultSet.getDouble("locationLon"),
                        resultSet.getDouble("locationDistance"),
                        resultSet.getString("locationName"),
                        resultSet.getString("locationAddress"),
                        resultSet.getString("locationCity"),
                        resultSet.getInt("locationPostCode")
                );

                locations.add(location);
            }

        } catch (Exception e) {
            System.out.println("Error while searching locations by name: " + e.getMessage());
        }

        return locations;
    }

    // ------------------------------------------------ Print function -------------------------------------------------
    @Override
    public void printCategoryData(String Category) throws Exception {
        List<Location> categoryList = loadLocationsData(Category);

        if (categoryList != null) {
            System.out.println("\n------ " + categoryList.size() + " locations loaded for " + Category + " category ------");
            System.out.println("-------------------------------------------------------");
            for (Location location : categoryList) {
                System.out.println(location);
                System.out.println("-------------------------------------------------------");
            }
        }
    }

    @Override
    public void printAllLocationsData() throws Exception {
        String[] categoryTables = {"bank", "cafe", "hospital", "hotel", "mosque", "parking", "pharmacy", "restaurant", "school", "supermarket"};
        for(String category : categoryTables){
            printCategoryData(category);
        }
    }

    // ----------------------------------------------- Delete functions ------------------------------------------------
    @Override
    public void deleteSpecificLocationData(String category, String locationName) throws Exception {
        String query = "DELETE FROM Location WHERE locationName = ? AND locationCategory = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, locationName);
            preparedStatement.setString(2, category);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Deleted '" + locationName + "' from category: " + category);
            } else {
                System.out.println("No matching location found.");
            }
        } catch (SQLException e) {
            throw new Exception("Failed to delete location: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCategoryData(String Category) {
        String query = "DELETE FROM Location WHERE locationCategory = ?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, Category);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deleted the location with category: " + Category);

                // Reset the auto-increment (optional, and only makes sense if you’re using IDs)
                String resetQuery = "ALTER TABLE " + Category + " AUTO_INCREMENT = 1";
                PreparedStatement resetStmt = connection.prepareStatement(resetQuery);
                resetStmt.executeUpdate();

                System.out.println(Category + " category data deleted from DB successfully.");
            } else {
                System.out.println("No location found with category: " + Category);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteAllLocationsData() throws Exception {
        String query = "TRUNCATE TABLE Location";

        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate(query);
            System.out.println("Location table cleared successfully");
        } catch (SQLException e) {
            throw new Exception("Failed to truncate Location table: " + e.getMessage(), e);
        }
    }
}