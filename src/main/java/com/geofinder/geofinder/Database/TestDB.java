package com.geofinder.geofinder.Database;

import com.geofinder.geofinder.Model.Location;

import java.util.List;

public class TestDB {
    public static void main(String[] args) throws Exception{
        System.out.println("-------------- JDBC Test --------------");
        String Category = "hotel";

        //DB database = DB.getInstance();
        DBService database = new DBService();
        List<Location> myList = List.of(
                new Location(Category, 30.5, -9.5, 15.25, Category + " 1 Ibn Hazm", Category + " 1 Ibn Hazm, Oujda, 60000", "Oujda", 60000),
                new Location(Category, 29.2, -9.7, 12.70, Category + " 2 Ibn Hazm", Category + " 2 Ibn Hazm, Oujda, 60000", "Oujda", 60000)
        );

        // >> Insert:
//        database.insertMarkerData(10, 12); // **
//        database.addLocationToCategory(Category, myList.get(0)); // **
//        database.insertLocationsData(myList, Category); // **

        // >> Update:
//        database.updateSpecificLocationData(myList.get(0), "hotel 2 Ibn Hazm"); // **

        // >> Load:
//        XX --> Location location = database.loadSpecificLocationsData(2, Category);
//        System.out.println(location);

        // >> Print:
//        database.printCategoryData(Category); // **
//        database.printAllLocationsData(); // **

        // >> Search:
//        List<Location> searchLocations = database.searchLocationsByName("cafe"); // **
//        System.out.println("Seached locations: \n\n" + searchLocations);

        // >> Delete:
//        database.deleteCategoryData("hotel"); // **
//        database.deleteSpecificLocationData(Category, "hotel 2 Ibn Hazm"); // **
//        database.deleteMarkerData();
//        database.deleteAllLocationsData(); // **

        System.out.println("\n>> Done 100%");
    }
}