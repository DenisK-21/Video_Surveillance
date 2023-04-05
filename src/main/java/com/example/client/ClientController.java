package com.example.client;




import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;







public class ClientController {
    @FXML
    private Button start_btn;

   /* @FXML
    private Label label;*/


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

    private Thread camera_1;

    private Thread camera_2;
    private Thread camera_3;
    private Thread camera_4;

    private boolean cameraActive = false;


    @FXML
    protected void startCamera() throws InterruptedException {
        if (!this.cameraActive) {


            // start the video capture
            // the id of the camera to be used
            //this.capture = new VideoCapture(RTSP_URL, Videoio.CAP_ANDROID);

            this.cameraActive = true;


            String RTSP_URL_1 = "rtsp://admin:@192.168.1.10:554/mode=real&idc=1&ids=2";
            String RTSP_URL_2 = "rtsp://192.168.1.12:554/stream1";
            String RTSP_URL_3 = "rtsp://192.168.1.78.554/mode=real";
            String RTSP_URL_4 = "rtsp://192.168.1.120:554/mode=real";
            camera_1 = new Thread(new VideoStream(RTSP_URL_1, currentFrame_cam_1, "camera1_"));
            camera_1.start();
           // camera_2 = new Thread(new VideoStream(RTSP_URL_2, currentFrame_cam_2,"camera_2_"));
           // camera_2.start();
            Thread.sleep(1000);
            Image image = new Image("C://not_signal.png");
            currentFrame_cam_3.imageProperty().set(image);

            currentFrame_cam_4.imageProperty().set(image);



        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.start_btn.setText("Start Camera");

        }
    }

}
