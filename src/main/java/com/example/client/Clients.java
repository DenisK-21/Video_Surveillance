package com.example.client;

import nu.pattern.OpenCV;


import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

import javafx.scene.image.Image;


import java.io.IOException;
import java.util.Objects;

public class Clients extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Clients.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 800);
        //scene.getStylesheets().add(getClass().getResource("").toExternalForm());
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/img.png")));
        stage.getIcons().add(image);
        stage.setTitle("Video Surveillance");
        stage.setScene(scene);

        stage.show();

        ClientController controller = fxmlLoader.getController();
        stage.setOnCloseRequest((new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we)
            {
                controller.setClosed();
            }
        }));
    }

    public static void main(String[] args) {
        OpenCV.loadLocally();
        launch();
    }
}
