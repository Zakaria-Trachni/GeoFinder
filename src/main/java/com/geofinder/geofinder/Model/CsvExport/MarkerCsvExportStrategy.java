package com.geofinder.geofinder.Model.CsvExport;

import java.io.File;
import java.io.PrintWriter;

public class MarkerCsvExportStrategy implements CsvExportStrategy {
    private final double markerLat;
    private final double markerLon;

    public MarkerCsvExportStrategy(double markerLat, double markerLon) {
        this.markerLat = markerLat;
        this.markerLon = markerLon;
    }

    @Override
    public void exportToCsv() {
        try {
            File folder = new File("src/main/java/com/geofinder/geofinder/Model/CsvExport/LocationsCSVs");
            if (!folder.exists()) folder.mkdir();

            File csvFile = new File(folder, "marker.csv");
            try (PrintWriter writer = new PrintWriter(csvFile)) {
                writer.println("Latitude,Longitude");
                writer.printf("%.6f,%.6f", markerLat, markerLon);
                System.out.println("Exported CSV for marker");
            }
        } catch (Exception e) {
            System.out.println("Error exporting marker CSV: " + e.getMessage());
        }
    }
}