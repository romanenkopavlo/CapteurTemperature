module org.example.monitoringtemperatures {
    requires javafx.controls;
    requires javafx.fxml;
    requires jssc;


    opens org.example.monitoringtemperatures to javafx.fxml;
    exports org.example.monitoringtemperatures;
}