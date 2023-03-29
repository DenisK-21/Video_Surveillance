package com.example.client;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.*;


import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Mat;



import java.awt.image.BufferedImage;

public class VideoStream implements Runnable {
    private final String RTSP_IRL;
    ImageView imageView;

    public VideoStream(String URL, ImageView imageView) {

        this.RTSP_IRL = URL;
        this.imageView = imageView;
    }

    @Override
    public void run() {
        try {

            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(this.RTSP_IRL);
            grabber.setOption("rtsp_transport", "tcp"); // Use tcp, otherwise the packet loss will be very serious
            System.out.println("grabber start");
            grabber.start();


            //1. Play video


            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            while (true) {
                Frame frame = grabber.grabImage();

                Mat mat = converter.convertToMat(frame);
                WritableImage image = SwingFXUtils.toFXImage(FrameToBufferedImage(frame), null);
                Platform.runLater(() -> imageView.imageProperty().set(image));
                //onFXThread(imageView.imageProperty(),image);
                //System.out.println(this.RTSP_IRL);
            }
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage FrameToBufferedImage(Frame frame) {

        Java2DFrameConverter converter = new Java2DFrameConverter();

        return converter.getBufferedImage(frame);
    }

}
