module Video.Surveillance {
    requires javafx.controls;
    requires javafx.fxml;


    requires javafx.swing;

    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;

    opens com.example.client to javafx.fxml;
    exports com.example.client;

}