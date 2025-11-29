package com.geofinder.geofinder.Controllers;

import com.geofinder.geofinder.Model.Location;
import com.geofinder.geofinder.Model.CsvExport.CategoriesCsvExportStrategy;
import com.geofinder.geofinder.Model.CsvExport.CsvExportContext;
import com.geofinder.geofinder.Model.CsvExport.MarkerCsvExportStrategy;
import com.geofinder.geofinder.Model.MapFacade.MapFacade;
import com.geofinder.geofinder.Model.Routing.CyclingStrategy;
import com.geofinder.geofinder.Model.Routing.DrivingStrategy;
import com.geofinder.geofinder.Model.Routing.RouteCalculator;
import com.geofinder.geofinder.Model.Routing.WalkingStrategy;
import com.geofinder.geofinder.Database.DBService;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {
    @FXML
    private TextField searchTextField;
    @FXML
    private TextField startLocationTextField;
    @FXML
    private TextField endLocationTextField;
    @FXML
    private WebView webView;
    @FXML
    private VBox layersMenu;
    @FXML
    private VBox sidebarSearchVBox;
    @FXML
    private VBox sidebarGotoVBox;
    @FXML
    private Label startLocationLabel;
    @FXML
    private Label startTemperature;
    @FXML
    private Label startHumidity;
    @FXML
    private Label startPressure;
    @FXML
    private Label startWindSpeed;
    @FXML
    private Label endLocationLabel;
    @FXML
    private Label endTemperature;
    @FXML
    private Label endHumidity;
    @FXML
    private Label endPressure;
    @FXML
    private Label endWindSpeed;
    @FXML
    private Label distanceValueLabel;
    @FXML
    private Label durationValueLabel;


    @FXML
    private CheckBox restaurantChoice;
    @FXML
    private CheckBox pharmacyChoice;
    @FXML
    private CheckBox supermarketChoice;
    @FXML
    private CheckBox parkingChoice;
    @FXML
    private CheckBox hospitalChoice;
    @FXML
    private CheckBox schoolChoice;
    @FXML
    private CheckBox cafeChoice;
    @FXML
    private CheckBox hotelChoice;
    @FXML
    private CheckBox bankChoice;
    @FXML
    private CheckBox mosqueChoice;

    @FXML
    private Slider categorySlider;
    @FXML
    private Slider searchSlider;
    @FXML
    private Label categorySliderLabel;
    @FXML
    private Label searchSliderLabel;

    private MapController mapController;
    private SidebarController sidebarController;
    private Double markerLat;
    private Double markerLon;

    private List<Location> restaurantList = new ArrayList<>();
    private List<Location> pharmacyList = new ArrayList<>();
    private List<Location> supermarketList = new ArrayList<>();
    private List<Location> parkingList = new ArrayList<>();
    private List<Location> hospitalList = new ArrayList<>();
    private List<Location> schoolList = new ArrayList<>();
    private List<Location> cafeList = new ArrayList<>();
    private List<Location> hotelList = new ArrayList<>();
    private List<Location> bankList = new ArrayList<>();
    private List<Location> mosqueList = new ArrayList<>();

    private final CsvExportContext csvExportContext = new CsvExportContext();

    private final DBService database = new DBService();

    private RouteCalculator routeCalculator;

    private MapFacade mapFacade;


    // >> ------------------------------------- Initialization -------------------------------------
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapController = new MapController(webView);
        mapFacade = new MapFacade(webView.getEngine());
        sidebarController = new SidebarController(webView, mapFacade);
        routeCalculator = new RouteCalculator(sidebarController);

        // Put the focus on the webview not the textfield:
        Platform.runLater(() -> {
            webView.requestFocus();
        });

        // Javascript bridge:
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webView.getEngine().executeScript("window");
                window.setMember("javaApp", this);
            }
        });

        // Sliders setup:
        categorySlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            categorySliderLabel.setText(String.format("%d m", newValue.intValue()));
        });
        searchSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            searchSliderLabel.setText(String.format("%d m", newValue.intValue()));
        });
        // Clear all the existing data:
        try {
            database.deleteAllLocationsData();
            database.deleteMarkerData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // >> ------------------------------------ Map controller ------------------------------------
    // Zoom & user location control:
    @FXML
    private void zoomIn(MouseEvent event) {
        mapController.zoomIn();
    }
    @FXML
    private void zoomOut(MouseEvent event) {
        mapController.zoomOut();
    }
    @FXML
    private void userLocation(MouseEvent event) {
        mapController.locateUser();
    }


    // Layers control:
    @FXML
    private void showLayersMenu(MouseEvent event) {
        layersMenu.setVisible(!layersMenu.isVisible());
    }
    @FXML
    private void setOpenStreetMapLayer(ActionEvent event) {
        mapController.setLayer("OpenStreetMap");
        layersMenu.setVisible(false);
    }
    @FXML
    private void setSatelliteLayer(ActionEvent event) {
        mapController.setLayer("Satellite");
        layersMenu.setVisible(false);
    }
    @FXML
    private void setTopographicLayer(ActionEvent event) {
        mapController.setLayer("Topographic");
        layersMenu.setVisible(false);
    }
    @FXML
    private void setDarkModeLayer(ActionEvent event) {
        mapController.setLayer("Dark Mode");
        layersMenu.setVisible(false);
    }
    @FXML
    private void setTerrainLayer(ActionEvent event) {
        mapController.setLayer("Terrain");
        layersMenu.setVisible(false);
    }


    // >> ---------------------------------- Main Sidebar controller ----------------------------------
    @FXML
    private void searchButtonClicked(MouseEvent event) {
        sidebarController.clearAll();

        String location = searchTextField.getText();
        try {
            // Check the validity of the location:
            if (location == null || location.trim().isEmpty()) {
                System.out.println("Error: Location is empty");
                searchError("Please enter a valid location!");
                return;
            }

            // Search for the location:
            sidebarController.searchButtonClicked(location);
        }
        catch (Exception e) {
            System.out.println("Error when searching the location: " + location + ": " + e.getMessage());
            // Show error alert for the user:
            searchError("Error finding the location: " + e.getMessage());
        }
    }
    @FXML
    private void gotoButtonClicked(MouseEvent event) {
        sidebarController.clearRoute();

        sidebarSearchVBox.setVisible(false);
        sidebarSearchVBox.setManaged(false);

        startLocationTextField.clear();
        endLocationTextField.clear();
        webView.requestFocus();
        sidebarGotoVBox.setVisible(true);
        sidebarGotoVBox.setManaged(true);
    }

    // These "two" functions are called from main.js using "javaApp" on the constructor:
    // They could be called to control the user input(the city):
    public void searchResultFound(double lat, double lon, String address) {
        Platform.runLater(() -> {
            System.out.println("Found: " + address + " at " + lat + ", " + lon);
        });
    }
    public void searchError(String errorMessage) {
        Platform.runLater(() -> {
            System.out.println("Search error occurred: " + errorMessage);
            // Show error message to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Search Error");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
        });
    }

    // >> Add the curr position marker to the map;
    // Called from JavaScript when the map is clicked
    public void handleMapClick(double lat, double lng) {
        markerLat = lat;
        markerLon = lng;
        mapFacade.addCurrentPositionMarker(lat, lng);
    }

    // Method called from main.js when currPositionMarker is removed:
    public void markerRemoved() {
        markerLat = null;
        markerLon = null;
        mapFacade.removeCurrentPositionMarker();
    }

    // >> Locations selection control:
    public void locationsSearchButtonClicked(MouseEvent event) throws IOException {
        if(markerLat != null && markerLon != null) {
            clearAllCategories();
            addLocationMarker();
            showSearchRadius();
            locationsSelection();

            System.out.println("Last marker: Lat = " + markerLat + ", Lng = " + markerLon);
        }
    }
    private void addLocationMarker() {
        if (markerLat != null && markerLon != null) {
            mapFacade.addCurrentPositionMarker(markerLat, markerLon);
        }
    }
    private void showSearchRadius() {
        int searchRadius = (int) searchSlider.getValue();
        mapFacade.showSearchRadius(searchRadius, markerLat, markerLon);
    }
    private void locationsSelection() throws IOException {
        int searchRadius = (int)(searchSlider.getValue());
        int categoryRadius = (int)(categorySlider.getValue());

        if(markerLat != null && markerLon != null) {
            if(restaurantChoice.isSelected()) {
                System.out.println("Restaurant choice is selected");
                restaurantList = sidebarController.searchNearbyPlaces("restaurant", searchRadius, categoryRadius, markerLat, markerLon);
            }
            if(pharmacyChoice.isSelected()) {
                System.out.println("Pharmacie choice is selected");
                 pharmacyList = sidebarController.searchNearbyPlaces("pharmacy", searchRadius, categoryRadius, markerLat, markerLon);
            }
            if(supermarketChoice.isSelected()) {
                System.out.println("Supermarket choice is selected");
                supermarketList = sidebarController.searchNearbyPlaces("supermarket", searchRadius, categoryRadius, markerLat, markerLon);
            }
            if(parkingChoice.isSelected()) {
                System.out.println("Parking choice is selected");
                parkingList = sidebarController.searchNearbyPlaces("parking", searchRadius, categoryRadius, markerLat, markerLon);
            }
            if(hospitalChoice.isSelected()) {
                System.out.println("Hospital choice is selected");
                hospitalList = sidebarController.searchNearbyPlaces("hospital", searchRadius, categoryRadius, markerLat, markerLon);
            }
            if(schoolChoice.isSelected()) {
                System.out.println("School choice is selected");
                schoolList = sidebarController.searchNearbyPlaces("school", searchRadius, categoryRadius, markerLat, markerLon);
            }
            if(cafeChoice.isSelected()) {
                System.out.println("Cafe choice is selected");
                cafeList = sidebarController.searchNearbyPlaces("cafe", searchRadius, categoryRadius, markerLat, markerLon);
            }
            if(hotelChoice.isSelected()) {
                System.out.println("Hotel choice is selected");
                hotelList = sidebarController.searchNearbyPlaces("hotel", searchRadius, categoryRadius, markerLat, markerLon);
            }
            if(bankChoice.isSelected()) {
                System.out.println("Bank choice is selected");
                bankList = sidebarController.searchNearbyPlaces("bank", searchRadius, categoryRadius, markerLat, markerLon);
            }
            if(mosqueChoice.isSelected()) {
                System.out.println("Mosque choice is selected");
                mosqueList = sidebarController.searchNearbyPlaces("mosque", searchRadius, categoryRadius, markerLat, markerLon);
            }
        }
    }
    // This function is called from main.js to show the route between the current marker and a location when it's selected:
    public void getUserMarkerPosition(double locationLat, double locationLon) {
        System.out.println("Marker position: Lat = " + locationLat + ", Lng = " + locationLon);
        if (markerLat != 0 && markerLon != 0) {
            mapFacade.showMarkerToLocationRoute(
                    markerLat, markerLon, locationLat, locationLon
            );
        }
    }
    // This function is called from main.js to update the marker position:
    public void setMarkerPosition(double lat, double lon) {
        this.markerLat = lat;
        this.markerLon = lon;
        System.out.println("Marker position set from main.js to: Lat = " + lat + ", Lng = " + lon);
    }

    @FXML
    private void saveToDBClicked(MouseEvent event) throws Exception {
        // Clear all the existing data:
        database.deleteAllLocationsData();
        database.deleteMarkerData();
        // Store the marker position:
        database.insertMarkerData(markerLat, markerLon);
        // Store new ones:
        database.insertLocationsData(bankList, "bank");
        database.insertLocationsData(cafeList, "cafe");
        database.insertLocationsData(hotelList, "hotel");
        database.insertLocationsData(hospitalList, "hospital");
        database.insertLocationsData(mosqueList, "mosque");
        database.insertLocationsData(parkingList, "parking");
        database.insertLocationsData(pharmacyList, "pharmacy");
        database.insertLocationsData(restaurantList, "restaurant");
        database.insertLocationsData(schoolList, "school");
        database.insertLocationsData(supermarketList, "supermarket");
        System.out.println("Save to DB - Done 100%");
    }
    @FXML
    private void showDBClicked(MouseEvent event) throws Exception {
        try {
            showDatabaseWindow();
            System.out.println("Show DB - DONE 100%");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private void showDatabaseWindow() throws Exception {
        // FXML:
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/geofinder/geofinder/views/databasePage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // CSS:
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/geofinder/geofinder/stylesheets/databasePage.css")).toExternalForm());

        // Window details:
        stage.setTitle("GeoFinder - Database");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/geofinder/geofinder/assets/images/appIcon.png")));
        stage.getIcons().add(image);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    @FXML
    private void exportCSVClicked(MouseEvent event) {
        // Export CSV to the marker position:
        csvExportContext.setStrategy(new MarkerCsvExportStrategy(markerLat, markerLon));
        csvExportContext.executeExport();

        // Export CSV for categories:
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(bankList, "bank"));
        csvExportContext.executeExport();
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(cafeList, "cafe"));
        csvExportContext.executeExport();
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(hotelList, "hotel"));
        csvExportContext.executeExport();
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(hospitalList, "hospital"));
        csvExportContext.executeExport();
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(mosqueList, "mosque"));
        csvExportContext.executeExport();
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(parkingList, "parking"));
        csvExportContext.executeExport();
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(pharmacyList, "pharmacy"));
        csvExportContext.executeExport();
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(restaurantList, "restaurant"));
        csvExportContext.executeExport();
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(schoolList, "school"));
        csvExportContext.executeExport();
        csvExportContext.setStrategy(new CategoriesCsvExportStrategy(supermarketList, "supermarket"));
        csvExportContext.executeExport();

        System.out.println("Export CSV - Done 100%");
    }
    @FXML
    private void clearAllClicked(MouseEvent event) throws Exception{
        mapFacade.clearAll();
        database.deleteAllLocationsData();
        database.deleteMarkerData();
        System.out.println("Clear All clicked");
    }
    private void clearAllCategories() {
        mapFacade.clearAllCategories();
        System.out.println("Clear All Categories clicked");
    }



    // >> ---------------------------------- goto Sidebar controller ----------------------------------
    @FXML
    private void gobackButtonClicked(MouseEvent event) {
        // Remove the main sidebar:
        sidebarGotoVBox.setVisible(false);
        sidebarGotoVBox.setManaged(false);

        // Show the goto sidebar:
        searchTextField.clear();
        webView.requestFocus();
        sidebarSearchVBox.setVisible(true);
        sidebarSearchVBox.setManaged(true);

        // Initialize the weather & distance infos:
        initializeWeatherInfos();
        initializeDistanceInfos();
    }
    @FXML
    private void gotoSearchButtonClicked(MouseEvent event) {
        String startLocation = startLocationTextField.getText();
        String endLocation = endLocationTextField.getText();
        try {
            // Check the validity of the locations:
            if (startLocation == null || startLocation.trim().isEmpty() || endLocation == null || endLocation.trim().isEmpty()) {
                System.out.println("Error: Start or end location is empty");
                searchError("Please enter both start and end locations!");
                return;
            }

            // Delegate to route calculator
            routeCalculator.calculateRoute(startLocation, endLocation);
        }
        catch (Exception e) {
            System.out.println("Error when searching route between " + startLocation + " and " + endLocation + ": " + e.getMessage());
            // Show error alert for the user:
            searchError("Error finding route: " + e.getMessage());
        }
    }

    private void initializeDistanceInfos() {
        distanceValueLabel.setVisible(false);
        durationValueLabel.setVisible(false);

        distanceValueLabel.setText("0 km");
        durationValueLabel.setText("0 h 0 min");
    }
    @FXML
    private void walkingMethodSelected(MouseEvent event) {
        routeCalculator.setStrategy(new WalkingStrategy());
        findBestRoute();
    }
    @FXML
    private void cyclingMethodSelected(MouseEvent event) {
        routeCalculator.setStrategy(new CyclingStrategy());
        findBestRoute();
    }
    @FXML
    private void drivingMethodSelected(MouseEvent event) {
        routeCalculator.setStrategy(new DrivingStrategy());
        findBestRoute();
    }
    private void findBestRoute() {
        String start = startLocationTextField.getText();
        String end = endLocationTextField.getText();

        if (!start.isEmpty() && !end.isEmpty()) {
            routeCalculator.calculateRoute(start, end);
        }
    }

    // This method used to receive route information from JavaScript:
    public void receiveRouteInfo(double distance, double duration, String mode) {
        // Convert units to more readable format:
        String distanceString = String.format("%.2f km", distance / 1000.0);
        String durationString;
        int minutes = (int) Math.round(duration / 60.0);
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (hours > 0) {
            durationString = String.format("%2d h %2d min", hours, remainingMinutes);
        } else {
            durationString = String.format("%2d min", minutes);
        }

        distanceValueLabel.setVisible(true);
        durationValueLabel.setVisible(true);

        distanceValueLabel.setText(distanceString);
        durationValueLabel.setText(durationString);
    }

    // >> Weather Infos:
    // This method used to receive weather information from JavaScript:
    public void receiveWeatherInfos(String city, int minTemp, int maxTemp, String humidity, String windSpeed, String pressure, String whichLocation){
        if (whichLocation.equals("startLocation")) {
            updateStartLocationWeather(city, minTemp, maxTemp, humidity, windSpeed, pressure);
        }
        else if (whichLocation.equals("endLocation")) {
            updateEndLocationWeather(city, minTemp, maxTemp, humidity, windSpeed, pressure);
        }
    }
    public void initializeWeatherInfos() {
        updateStartLocationWeather("Start:", 0, 0, "00%", "00.00 m/s", "1000 hPa");
        updateEndLocationWeather("Destination:", 0, 0, "00%", "00.00 m/s", "1000 hPa");
    }
    public void updateStartLocationWeather(String city, int minTemp, int maxTemp, String humidity, String windSpeed, String pressure) {
        startLocationLabel.setText(city);
        startTemperature.setText(String.format("%2d°C / %2d°C", minTemp, maxTemp));
        startHumidity.setText(humidity);
        startWindSpeed.setText(windSpeed);
        startPressure.setText(pressure);
    }
    public void updateEndLocationWeather(String city, int minTemp, int maxTemp, String humidity, String windSpeed, String pressure) {
        endLocationLabel.setText(city);
        endTemperature.setText(String.format("%2d°C / %2d°C", minTemp, maxTemp));
        endHumidity.setText(humidity);
        endWindSpeed.setText(windSpeed);
        endPressure.setText(pressure);
    }

    // Method called from JavaScript when there's an error with weather infos:
    public void updateWeatherError(String errorMessage, String whichLocation) {
        // Show an error alert:
        searchError("Error related to: " + whichLocation + "\nError code: " + errorMessage);
        // Don't show distance infos in case of error:
        initializeDistanceInfos();
        // Show error's infos:
        System.out.println("Error when showing " + whichLocation + " - Eror message: " + errorMessage);
        if (whichLocation.equals("startLocation")) {
            updateStartLocationWeather("Start:", 0, 0, "00%", "00.00 m/s", "1000 hPa");
        }
        else if (whichLocation.equals("endLocation")) {
            updateEndLocationWeather("Destination:", 0, 0, "00%", "00.00 m/s", "1000 hPa");
        }
    }
}