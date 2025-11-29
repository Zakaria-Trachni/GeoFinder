package com.geofinder.geofinder.Model.CsvExport;

import com.geofinder.geofinder.Model.Location;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class CategoriesCsvExportStrategy implements CsvExportStrategy {
    private final List<Location> categoryList;
    private final String category;

    public CategoriesCsvExportStrategy(List<Location> categoryList, String category) {
        this.categoryList = categoryList;
        this.category = category;
    }

    @Override
    public void exportToCsv() {
        System.out.println("Exporting categories to csv");
        try {
            File folder = new File("src/main/java/com/geofinder/geofinder/Model/CsvExport/LocationsCSVs");
            if (!folder.exists()) folder.mkdir();

            File csvFile = new File(folder, category + ".csv");
            try (PrintWriter writer = new PrintWriter(csvFile)) {
                writer.println("Category,Latitude,Longitude,Distance (km),Name,Address,City,PostCode");
                for (Location loc : categoryList) {
                    writer.printf(
                            "%s,%.6f,%.6f,%.2f,%s,%s,%s,%d%n",
                            loc.getCategory(), loc.getLat(), loc.getLon(),
                            loc.getDistance(), loc.getName(), loc.getAddress(),
                            loc.getCity(), loc.getPostCode()
                    );
                }
                System.out.println("Exported CSV for category: " + category);
            }
        } catch (Exception e) {
            System.err.println("Error exporting category CSV: " + e.getMessage());
        }
    }
}