package com.example.client;



import javafx.application.Application;

import javafx.stage.Stage;

import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import javafx.scene.image.Image;




import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class Clients extends Application {
    public static Connection connection;
    public static final String DB_URL = "jdbc:postgresql://localhost:5433/postgres";
    public static final String DB_Driver = "org.postgresql.Driver";
    @Override
    public void start(Stage stage) throws IOException, SQLException {


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("2_cameras.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1540, 800);
        //scene.getStylesheets().add(getClass().getResource("").toExternalForm());
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/img.png")));
        stage.getIcons().add(image);
        stage.setTitle("Video Surveillance");
        stage.setScene(scene);

        stage.show();
        connection = DriverManager.getConnection(DB_URL,"recordeo_adm","recordeo_password");



    }


    public static void main(String[] args) {
        launch();
    }
}
