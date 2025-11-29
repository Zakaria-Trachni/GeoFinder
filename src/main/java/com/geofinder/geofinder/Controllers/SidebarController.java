package com.geofinder.geofinder.Controllers;

import com.geofinder.geofinder.Model.Location;
import com.geofinder.geofinder.Model.MapFacade.MapFacade;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SidebarController {
    private final WebEngine webEngine;
    private final MapFacade mapFacade;

    public SidebarController(WebView webView, MapFacade mapFacade) {
        this.webEngine = webView.getEngine();
        this.mapFacade = mapFacade;
    }

    public void searchButtonClicked(String searchAddress) {
        System.out.println("Search button clicked - location: " + searchAddress);
        if (!searchAddress.isEmpty()) {
            mapFacade.geocodeAddress(searchAddress);
        }
    }

    public void searchRouteClicked(String startLocation, String endLocation, String mode) {
        double[] fromCoords = getCoordinates(startLocation);
        double[] toCoords = getCoordinates(endLocation);

        if (fromCoords != null && toCoords != null) {
            mapFacade.clearAll();
            mapFacade.showRouteWithMode(
                    fromCoords[0], fromCoords[1],
                    toCoords[0], toCoords[1],
                    mode
            );
        }
    }
    public static double[] getCoordinates(String placeName) {
        try {
            String query = placeName.replace(" ", "+");
            URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&limit=1");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "JavaFXApp"); // Required
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) response.append(inputLine);
            in.close();

            JSONArray results = new JSONArray(response.toString());
            if (results.length() > 0) {
                JSONObject location = results.getJSONObject(0);
                double lat = Double.parseDouble(location.getString("lat"));
                double lon = Double.parseDouble(location.getString("lon"));
                return new double[]{lat, lon};
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getWeatherData(String location, String whichLocation) {
        mapFacade.getWeatherData(location, whichLocation);
    }

    public void clearRoute() {
        mapFacade.clearRoute();
    }
    public void clearAll() {
        mapFacade.clearAll();
    }


    // Functions to fetch the nearby places:
    public List<Location> searchNearbyPlaces(String category, int searchRadius, int categoryRadius, double markerLat, double markerLon) throws IOException {
        List<Location> categoryList = new ArrayList<>();

        String jsonString = fetchNearbyPlaces(category, searchRadius, markerLat, markerLon);
        JSONObject obj = new JSONObject(jsonString);
        JSONArray elements = obj.getJSONArray("elements");

        for (int i = 0; i < elements.length(); i++) {
            JSONObject el = elements.getJSONObject(i);

            double latitude = el.has("lat") ? el.getDouble("lat") : el.getJSONObject("center").getDouble("lat");
            double longitude = el.has("lon") ? el.getDouble("lon") : el.getJSONObject("center").getDouble("lon");
            double distance = calculateHaversineDistance(markerLat, markerLon, latitude, longitude);

            String titledCategory = category.substring(0, 1).toUpperCase() + category.substring(1);;
            String name = el.has("tags") && el.getJSONObject("tags").has("name") ? el.getJSONObject("tags").getString("name") : titledCategory + " " + (i + 1);
            String address = el.has("tags") && el.getJSONObject("tags").has("addr:street") ? el.getJSONObject("tags").getString("addr:street") : "Unknown";
            String city = el.has("tags") && el.getJSONObject("tags").has("addr:city") ? el.getJSONObject("tags").getString("addr:city") : "Unknown";
            int postCode = el.has("tags") && el.getJSONObject("tags").has("addr:postcode") ? el.getJSONObject("tags").getInt("addr:postcode") : 0;

            categoryList.add(new Location(category, latitude, longitude, distance, name, address, city, postCode));
        }

        // show nearby places on the map:
        showNearbyPlaces(categoryList, categoryRadius);
        return categoryList; // (return the list to use it when it's necessary for the database side...)
    }
    public String fetchNearbyPlaces(String category, int searchRadius, double lat, double lon) throws IOException {
        String query;

        if (category.equals("mosque")) {
            query = "[out:json];"
                    + "("
                    + "node[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"](around:" + searchRadius + "," + lat + "," + lon + ");"
                    + "way[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"](around:" + searchRadius + "," + lat + "," + lon + ");"
                    + "relation[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"](around:" + searchRadius + "," + lat + "," + lon + ");"
                    + ");"
                    + "out center;";
        } else {
            String queryCategoryTag = "amenity";
            if (category.equals("supermarket")) {
                queryCategoryTag = "shop";
            } else if (category.equals("hotel")) {
                queryCategoryTag = "tourism";
            }

            query = "[out:json];"
                    + "("
                    + "node[\"" + queryCategoryTag + "\"=\"" + category + "\"](around:" + searchRadius + "," + lat + "," + lon + ");"
                    + "way[\"" + queryCategoryTag + "\"=\"" + category + "\"](around:" + searchRadius + "," + lat + "," + lon + ");"
                    + "relation[\"" + queryCategoryTag + "\"=\"" + category + "\"](around:" + searchRadius + "," + lat + "," + lon + ");"
                    + ");"
                    + "out center;";
        }

        String urlStr = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, "UTF-8");

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }
    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (double) Math.round((R * c) * 100.0) / 100.0; // distance in km
    }
    public void showNearbyPlaces(List<Location> categoryList, int categoryRadius) {
        for (Location location : categoryList) {
            String popup = String.format(
                    "<b>%s</b><br>%s<br>%s, %d<br>%.2f km",
                    location.getName(),
                    location.getAddress(),
                    location.getCity(),
                    location.getPostCode(),
                    location.getDistance()
            );

            mapFacade.addCategoryMarker(
                    location.getCategory(),
                    location.getLat(),
                    location.getLon(),
                    popup,
                    categoryRadius
            );
        }
    }
    public void addLocationMarker(double lat, double lon) {
        mapFacade.addCurrentPositionMarker(lat, lon);
    }
    public void showSearchRadius(int searchRadius, double lat, double lon) {
        mapFacade.showSearchRadius(searchRadius, lat, lon);
    }
    public void clearAllClicked() {
        mapFacade.clearAll();
    }
    public void clearAllCategories() {
        mapFacade.clearAllCategories();
    }
}