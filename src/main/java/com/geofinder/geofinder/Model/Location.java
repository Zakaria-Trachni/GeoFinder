package com.geofinder.geofinder.Model;

public class Location {
    private String category;
    private double lat;
    private double lon;
    private double distance;
    private String name;
    private String address;
    private String city;
    private int postCode;

    public Location(String category, double lat, double lon, double distance, String name, String address, String city, int postCode) {
        this.category = category;
        this.lat = lat;
        this.lon = lon;
        this.distance = distance;
        this.name = name;
        this.address = address;
        this.city = city;
        this.postCode = postCode;
    }

    public String getCategory() {
        return category;
    }
    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public double getDistance() {
        return distance;
    }
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public String getCity() {
        return city;
    }
    public int getPostCode() {
        return postCode;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setPostCode(int postCode) {
        this.postCode = postCode;
    }

    @Override
    public String toString() {
        //double distance = Math.round(this.getDistance() * 100.0) / 100.0;
        return String.format("%s, (%f, %f), %.2f km\n%s\n%s\n%s, %d",
                this.getCategory(), this.getLat(), this.getLon(), this.getDistance(), this.getName(), this.getAddress(), this.getCity(), this.getPostCode());
    }
}