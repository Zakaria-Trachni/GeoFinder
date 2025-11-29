package com.geofinder.geofinder.Database.DAO;

import com.geofinder.geofinder.Model.Position;

public interface MarkerDAO {
    public void insertMarkerData(double lat, double lon);
    public Position loadMarkerPosition();
    public void deleteMarkerData() throws Exception;
}