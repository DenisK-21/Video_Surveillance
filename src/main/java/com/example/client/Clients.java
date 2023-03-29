package com.example.client;



import javafx.application.Application;

import javafx.stage.Stage;

import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import javafx.scene.image.Image;




import java.io.IOException;
import java.util.Objects;

public class Clients extends Application {

    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("2_cameras.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1540, 800);

        //scene.getStylesheets().add(getClass().getResource("").toExternalForm());
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/img.png")));
        stage.getIcons().add(image);
        stage.setTitle("Video Surveillance");
        stage.setScene(scene);

        stage.show();



    }


    public static void main(String[] args) {
        launch();
    }
}
