module com.timekeeper {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires json.simple;


    opens com.timekeeper to javafx.fxml;
    exports com.timekeeper;
}