package com.example.client;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.*;


import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_videoio.VideoWriter;

import org.opencv.imgproc.Imgproc;


import java.awt.image.BufferedImage;
import java.time.LocalTime;

import java.util.LinkedList;

import static org.bytedeco.opencv.global.opencv_core.TYPE_MARKER;
import static org.bytedeco.opencv.global.opencv_core.absdiff;
import static org.bytedeco.opencv.global.opencv_imgproc.*;


public class VideoStream extends Thread {

    private boolean mFinish;

    private Mask mask;
    private int range_1;
    private final VideoWriter videoWriter;
    private boolean check_video;
    private final String RTSP_IRL;
    private Mat previous_frame;
    ImageView imageView;
    private final String number_camera;

    public VideoStream(String URL, ImageView imageView, String number_camera) {

        this.RTSP_IRL = URL;
        this.imageView = imageView;
        this.number_camera = number_camera;
        this.previous_frame = null;
        this.check_video = false;
        this.videoWriter = new VideoWriter();
        this.range_1 = 0;
        this.mask = new Mask();
        this.mFinish = true;

    }

    @Override
    public void run() {
        try {
            int fps = 0;
            int fps_sec = 0;


            // запись и предзапись видио
            LinkedList<Mat> queue = new LinkedList<>(); //очередб на предзапись или постзапись

            //на время маска будет здесь
            //this.mask.setUpper_left_corner(0, 0);
           // this.mask.setLower_right_corner(900, 700);
           // this.mask.setStatus(true);

            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(this.RTSP_IRL);
            System.out.println(grabber.hasVideo());
            grabber.setOption("rtsp_transport", "tcp"); // Use tcp, otherwise the packet loss will be very serious
            System.out.println("grabber start");
            grabber.start();


            Size size = new Size(grabber.getImageWidth(), grabber.getImageHeight());


            //1. Play video
            System.out.println(grabber.hasVideo());

            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            //что бы через раз обрабатывал MAt
            boolean change = true;
            // предназначен для того, что бы в будущем понимать было ли движение или нет
            boolean check_movement;
            while (this.mFinish) {

                // prob
                Frame frame = grabber.grabImage();

                Mat image = new Mat(converter.convertToMat(frame));
                check_movement = false;

                // условие нужно для того, чтобы кадры обрабатывались через один
                if (change) {
                    check_movement = MotionDetector(image);
                    change = false;
                    System.out.println(LocalTime.now().getSecond());
                } else change = true;
                if (check_movement)
                    putText(image, "Movement", new Point(1500, 100),
                            FONT_HERSHEY_DUPLEX, 1.8, Scalar.RED, 4, TYPE_MARKER, false);


                //запись

                WriteImage(check_movement, image, queue, size);
                int now_sec = LocalTime.now().getSecond();

                //расчёт fps
                if (fps_sec == now_sec) {
                    fps++;
                } else {
                    this.range_1++;
                    fps_sec = now_sec;
                    System.out.println("FPS = " + fps);
                    fps = 0;
                }

                // конец записи

                //маска
                if (this.mask.isStatus())
                    rectangle(image, this.mask.getUpper_left_corner(),
                            this.mask.getLower_right_corner(), Scalar.BLUE, 3, 4, 0);

                frame = converter.convert(image);
                WritableImage image1 = SwingFXUtils.toFXImage(FrameToBufferedImage(frame), null);
                Platform.runLater(() -> imageView.imageProperty().set(image1));
                //onFXThread(imageView.imageProperty(),image);
                //System.out.println(this.RTSP_IRL);
            }
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void WriteImage(boolean check_movement, Mat image, LinkedList<Mat> queue, Size size) {
        if (this.check_video) { // если происходит запись
            if (check_movement) {
                queue.clear();
                this.range_1 = 0;
            } else {
                queue.add(image);
            }
            if (this.range_1 > 5) {
                this.check_video = false;
                this.videoWriter.release();
                System.out.println("заканчивем видио");
            }
            this.videoWriter.write(image);
            System.out.println("записывфем кадр");//записываем видио
        } else {
            if (check_movement) {
                System.out.println("запись началась");
                this.videoWriter.open(this.number_camera + LocalTime.now().getSecond() + ".mp4",
                        VideoWriter.fourcc((byte) 'D', (byte) 'I', (byte) 'V', (byte) 'X'), 24.0, size, true);
                this.check_video = true;
                for (Mat mat : queue) {
                    this.videoWriter.write(mat);
                }

                this.range_1 = 0;
                queue.clear();
            } else {
                if (this.range_1 > 5) {
                    queue.removeFirst();
                }
                queue.addLast(new Mat(image));
            }
        }
    }

    public static BufferedImage FrameToBufferedImage(Frame frame) {

        Java2DFrameConverter converter = new Java2DFrameConverter();

        return converter.getBufferedImage(frame);
    }

    public Boolean MotionDetector(Mat image) {
        Mat prepared_frame = new Mat();

        cvtColor(image, prepared_frame, Imgproc.COLOR_BGR2GRAY);
        GaussianBlur(prepared_frame, prepared_frame, new Size(15, 15), 0);

        if (this.previous_frame == null) {
            this.previous_frame = new Mat(prepared_frame);
        }
        Mat dif_frame = new Mat();
        absdiff(this.previous_frame, prepared_frame, dif_frame);
        this.previous_frame = prepared_frame;


        threshold(dif_frame, dif_frame, 10, 255, THRESH_BINARY);

        //маска
        if (this.mask.isStatus())
            rectangle(dif_frame, this.mask.getUpper_left_corner(),
                    this.mask.getLower_right_corner(), Scalar.BLUE, FILLED, 4, 0);
        MatVector contours = new MatVector();
        findContours(dif_frame, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        //DrawingContours(image, contours);
        return contours.size() > 10;
    }

    private void DrawingContours(Mat image, MatVector contours) {

        for (long i = 0; i < contours.size(); i++) {
            Mat mat = contours.get(i);
            if (100 < contourArea(mat)) {
                Rect rect = boundingRect(mat);
                rectangle(image, rect, Scalar.GREEN);
            }
        }
    }

    public void finish()		//Инициирует завершение потока
    {
        mFinish = false;
    }

    public void setMask(Mask mask) {
        this.mask = mask;
    }
}
