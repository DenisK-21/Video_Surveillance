module Video.Surveillance {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;
    requires javafx.swing;
    opens com.example.client to javafx.fxml;
    exports com.example.client;

}