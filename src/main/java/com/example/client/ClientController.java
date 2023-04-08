package com.example.client;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Scanner;


public class ClientController {
    @FXML
    private TextField int_cam_1_1;
    @FXML
    private TextField int_cam_1_2;
    @FXML
    private TextField int_cam_2_1;
    @FXML
    private TextField int_cam_2_2;

    @FXML
    private Button start_btn;
    @FXML
    private CheckBox check_box;




    // the OpenCV object that realizes the video capture
    // a flag to change the button behavior
    @FXML
    private ImageView currentFrame_cam_1;
    @FXML
    private ImageView currentFrame_cam_2;
    @FXML
    private ImageView currentFrame_cam_3;
    @FXML
    private ImageView currentFrame_cam_4;

    private VideoStream camera_1;

    private VideoStream camera_2;
    private VideoStream camera_3;
    private VideoStream camera_4;

    private boolean cameraActive = false;

    @FXML
    protected void start_mask() throws InterruptedException {


        System.out.println("check Box working");
        if (this.cameraActive) {
            if (check_box.isSelected()) {
                Scanner left_1 = new Scanner(int_cam_1_1.getText());
                Scanner right_1 = new Scanner(int_cam_1_2.getText());

                Scanner left_2 = new Scanner(int_cam_2_1.getText());
                Scanner right_2 = new Scanner(int_cam_2_2.getText());
                camera_1.setMask(new Mask(left_1.nextInt(), left_1.nextInt(), right_1.nextInt(), right_1.nextInt()));
                camera_2.setMask(new Mask(left_2.nextInt(), left_2.nextInt(), right_2.nextInt(), right_2.nextInt()));
            } else {
                camera_1.setMask(new Mask());
                camera_2.setMask(new Mask());
            }
        }
    }

    @FXML
    protected void startCamera(ActionEvent event) throws InterruptedException {
        if (!this.cameraActive) {

            this.cameraActive = true;


            String RTSP_URL_1 = "rtsp://admin:@192.168.1.10:554/mode=real&idc=1&ids=2";
            String RTSP_URL_2 = "rtsp://192.168.1.12:554/stream1";
            String RTSP_URL_3 = "rtsp://192.168.1.78.554/mode=real";
            String RTSP_URL_4 = "rtsp://192.168.1.120:554/mode=real";
            camera_1 = new VideoStream(RTSP_URL_1, currentFrame_cam_1, "camera1_",1);
            camera_1.start();
            camera_2 = new VideoStream(RTSP_URL_2, currentFrame_cam_2,"camera_2_",2);
            camera_2.start();
            Thread.sleep(4000);
            Image image = new Image("C://not_signal.png");
            currentFrame_cam_3.imageProperty().set(image);

            currentFrame_cam_4.imageProperty().set(image);

            this.start_btn.setText("Stop Camera");


        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            camera_1.finish();
            camera_2.finish();
            this.start_btn.setText("Start Camera");

        }
    }


}
