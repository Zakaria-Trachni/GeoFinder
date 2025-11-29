module com.geofinder.geofinder {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires jdk.jsobject;
    requires org.json;
    requires java.desktop;
    requires java.sql;
    requires mysql.connector.j;

    opens com.geofinder.geofinder to javafx.fxml;
    exports com.geofinder.geofinder;
    exports com.geofinder.geofinder.Controllers;
    opens com.geofinder.geofinder.Controllers to javafx.fxml;
    exports com.geofinder.geofinder.Model;
    opens com.geofinder.geofinder.Model to javafx.fxml;
}