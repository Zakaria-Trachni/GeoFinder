package com.geofinder.geofinder.Controllers;

import com.geofinder.geofinder.Model.Location;
import com.geofinder.geofinder.Model.Position;
import com.geofinder.geofinder.Database.DBService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DatabaseController implements Initializable {
    private DBService database;
    private final String[] categories = {"Bank", "Cafe", "Hospital", "Hotel", "Mosque", "Parking", "Pharmacy", "Restaurant", "School", "Supermarket"};

    @FXML
    private ComboBox<String> choicesComboBox;
    @FXML
    private ComboBox<String> addLocationComboBox;
    @FXML
    private TextField searchIDTextField;
    @FXML
    private Label positionValues;
    @FXML
    private TableView<Location> locationsTableView;
    @FXML
    private TableColumn<Location, Double> latColumn;
    @FXML
    private TableColumn<Location, Double> lonColumn;
    @FXML
    private TableColumn<Location, Double> distanceColumn;
    @FXML
    private TableColumn<Location, String> nameColumn;
    @FXML
    private TableColumn<Location, String> addressColumn;
    @FXML
    private TableColumn<Location, String> cityColumn;
    @FXML
    private TableColumn<Location, Integer> postCodeColumn;

    private ObservableList<Location> locationsList = FXCollections.observableArrayList();

    @FXML
    private TextField latTextField;
    @FXML
    private TextField lonTextField;
    @FXML
    private TextField distanceTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField addressTextField;
    @FXML
    private TextField cityTextField;
    @FXML
    private TextField postcodeTextField;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the connection to the database:
        //database = new JDBC();
        //database = DB.getInstance();
        database = new DBService();

        // Both ComboBoxes initializations:
        choicesComboBox.getItems().addAll(categories);
        addLocationComboBox.getItems().addAll(categories);

        // Marker position:
        positionValues.setText(loadMarkerPosition());

        // TableView initialization:
        latColumn.setCellValueFactory(new PropertyValueFactory<Location, Double>("lat"));
        lonColumn.setCellValueFactory(new PropertyValueFactory<Location, Double>("lon"));
        distanceColumn.setCellValueFactory(new PropertyValueFactory<Location, Double>("distance"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<Location, String>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<Location, String>("address"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<Location, String>("city"));
        postCodeColumn.setCellValueFactory(new PropertyValueFactory<Location, Integer>("postCode"));

        locationsTableView.setItems(locationsList);
    }

    // ------------------------------------------------ Load functions -------------------------------------------------
    public String loadMarkerPosition() {
        Position position = database.loadMarkerPosition();
        if(position == null) {
            position = new Position(0.0, 0.0);
        }

        String positionString = String.format("(%.4f, %.4f)", position.getLat(), position.getLon());
        return positionString;
    }

    public void loadAllLocations(ActionEvent actionEvent) {}
    public void loadCategoryLocations(ActionEvent actionEvent) {
        String Category = choicesComboBox.getSelectionModel().getSelectedItem();
        try {
            List<Location> categoryList = database.loadLocationsData(Category.toLowerCase());
            locationsList.clear();
            locationsList.addAll(categoryList);
            locationsTableView.refresh();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void loadSearchedLocations(MouseEvent event) throws Exception {
        try {
            String Category = choicesComboBox.getSelectionModel().getSelectedItem();
            String locationName = searchIDTextField.getText();

            System.out.println("---------------------- " + Category + " ----------------------");
            if (Category == null || locationName == null || locationName.trim().isEmpty()) {
                System.out.println("Error while searching the location");
                // !! Add error notification here !!
            }
            else {
                Category = Category.toLowerCase();
                List<Location> searchedLocationsList = database.searchLocationsByName(locationName);
                locationsList.clear();
                locationsList.addAll(searchedLocationsList);
                locationsTableView.refresh();
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // -------------------------------------------- Add & Update functions ---------------------------------------------
    @FXML
    public void addLocationClicked(MouseEvent event) throws Exception {
        Location location = getLocationFromInputs();
        if (location != null) {
            // Add location to the database:
            database.addLocationToCategory(location.getCategory(), location);

            // refresh the table:
            selectCategoryInTableView();

            // Clear the textFields:
            clearAllInputs();
        }
    }

    @FXML
    public void updateSpecificLocation(MouseEvent event) {
        int selectedIndex = locationsTableView.getSelectionModel().getSelectedIndex();
        Location location = getLocationFromInputs();
        if (selectedIndex >= 0 && location != null) {
            Location selectedLocation = locationsTableView.getItems().get(selectedIndex);
            String category = choicesComboBox.getSelectionModel().getSelectedItem();

            if (location.getCategory().equals(selectedLocation.getCategory())) {
                try {
                    // Update the database:
                    database.updateSpecificLocationData(location, selectedLocation.getName());
                    locationsTableView.getItems().remove(selectedIndex);

                    // Refresh the table:
                    selectCategoryInTableView();

                    // Clear the textFields:
                    clearAllInputs();

                    System.out.println("Location deleted successfully 100%");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            else {
                System.out.println("It should be the same category to update the location!");
                // Show an error notification here with different categories message !!
                return;
            }
        } else {
            System.out.println("No location selected to delete.");
        }

        // Refresh the table:
        //selectCategoryInTableView();

        System.out.println("updateButtonClicked");
    }

    // ----------------------------------------------- Clear functions -------------------------------------------------
    @FXML
    public void clearSpecificLocation(MouseEvent event) {
        int selectedIndex = locationsTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Location selectedLocation = locationsTableView.getItems().get(selectedIndex);
            String category = choicesComboBox.getSelectionModel().getSelectedItem();

            try {
                database.deleteSpecificLocationData(category, selectedLocation.getName());
                locationsTableView.getItems().remove(selectedIndex);
                System.out.println("Location deleted successfully 100%");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("No location selected to delete.");
        }
    }

    @FXML
    public void clearCategoryLocations(MouseEvent event) throws Exception {
        String Category = choicesComboBox.getSelectionModel().getSelectedItem();
        if (Category != null) {
            Category = Category.toLowerCase();

            // Delete all from the database:
            database.deleteCategoryData(Category);

            // Refresh the table:
            locationsList.clear();
            locationsTableView.refresh();

            // Reset the choicesComboBox value:
            // choicesComboBox.setValue("Choose a location"); <---- Doesn't work ??

            System.out.println("All locations deleted successfully 100%");
        }
        else {
            System.out.println("Error while deleting the category");
        }
    }

    // ----------------------------------------------- Other functions  ------------------------------------------------
    private Location getLocationFromInputs(){
        String category = addLocationComboBox.getSelectionModel().getSelectedItem();
        if(category == null) {
            System.out.println("Please select a category!");
            // Here should be an error notification !!
            return null;
        }

        String lat = latTextField.getText();
        String lon = lonTextField.getText();
        category = category.toLowerCase();
        String distance = distanceTextField.getText();
        String name = nameTextField.getText();
        String address = addressTextField.getText();
        String city = cityTextField.getText();
        String postcode = postcodeTextField.getText();

        if(lat == null || lon == null || distance == null || name == null || address == null || city == null || postcode == null || name.trim().isEmpty() || address.trim().isEmpty() || city.trim().isEmpty() || postcode.trim().isEmpty()) {
            System.out.println("Error while adding the location");
            // Show error notification here !! "Please enter a valid location!"
            // Show an error if a type is different from needed!
            return null;
        }
        else {
            System.out.println("Location: " + lat + ", " + lon + ", " + category + ", " + distance + ", " + name + ", " + address + ", " + city + ", " + postcode);
            return new Location(category, Double.parseDouble(lat), Double.parseDouble(lon), Double.parseDouble(distance), name, address, city, Integer.parseInt(postcode));
        }
    }
    private void selectCategoryInTableView() throws Exception {
        try {
            String Category = addLocationComboBox.getSelectionModel().getSelectedItem();
            choicesComboBox.getSelectionModel().select(Category);
            List<Location> categoryList = database.loadLocationsData(Category.toLowerCase());
            locationsList.clear();
            locationsList.addAll(categoryList);
            locationsTableView.refresh();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private void clearAllInputs() {
        latTextField.clear();
        lonTextField.clear();
        addLocationComboBox.getSelectionModel().clearSelection();
        distanceTextField.clear();
        nameTextField.clear();
        addressTextField.clear();
        cityTextField.clear();
        postcodeTextField.clear();
    }
}