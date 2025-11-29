package com.geofinder.geofinder.Database;

import java.sql.*;

public class JDBC {
    private static JDBC instance = null;
    private Connection connection = null;

    private JDBC(){
        try{
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/locations_schema", "root", "Zakaria.19");
            System.out.println("Connected to database 100%");
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static JDBC getInstance() {
        if (instance == null) {
            synchronized (JDBC.class) {
                if (instance == null) {
                    instance = new JDBC();
                }
            }
        }
        return instance;
    }

    public Connection getConnection(){
        return connection;
    }

    public void closeConnection(){
        try{
            connection.close();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        finally{
            System.out.println("Connection closed");
        }
    }
}