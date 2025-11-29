package com.geofinder.geofinder.Database.DAO;

import com.geofinder.geofinder.Model.Position;
import com.geofinder.geofinder.Database.JDBC;

import java.sql.*;

public class MarkerDAOImpl implements MarkerDAO {
    private final Connection connection;

    public MarkerDAOImpl() {
        this.connection = JDBC.getInstance().getConnection();
    }


    @Override
    public void insertMarkerData(double lat, double lon) {
        String query = "INSERT INTO Marker (markerLat, markerLon) VALUES (?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setDouble(1, lat);
            preparedStatement.setDouble(2, lon);

            preparedStatement.executeUpdate();
            System.out.println("Insertion for marker position data in DB successfully.");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Position loadMarkerPosition() {
        String query = "SELECT markerLat, markerLon FROM marker";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double lat = resultSet.getDouble("markerLat");
                double lon = resultSet.getDouble("markerLon");
                return new Position(lat, lon);
            } else {
                System.out.println("No marker position found in the database.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error loading marker position: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteMarkerData() throws Exception {
        try {
            Statement statement = connection.createStatement();

            // Disable foreign key checks
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Truncate the table
            statement.execute("TRUNCATE TABLE Marker");

            // Re-enable foreign key checks
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("Marker table cleared successfully");

        } catch (SQLException e) {
            throw new Exception("Failed to truncate Marker table: " + e.getMessage(), e);
        }
    }
}