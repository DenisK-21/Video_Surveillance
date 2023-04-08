package com.example.client;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;


import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_videoio.VideoWriter;


import org.opencv.imgproc.Imgproc;


import java.awt.image.BufferedImage;
import java.sql.Statement;
import java.time.LocalTime;

import java.util.LinkedList;

import static com.example.client.Clients.connection;

import static org.bytedeco.opencv.global.opencv_core.TYPE_MARKER;
import static org.bytedeco.opencv.global.opencv_core.absdiff;
import static org.bytedeco.opencv.global.opencv_imgproc.*;


public class VideoStream extends Thread {

    private final int id_cam;
    private boolean event;
    private boolean mFinish;

    private Mask mask;
    private int range_1;
    private final VideoWriter videoWriter;
    private boolean check_video;
    private final String RTSP_IRL;

    private int localtime;
    private Mat previous_frame;
    ImageView imageView;
    private final String number_camera;

    public VideoStream(String URL, ImageView imageView, String number_camera, int id) {

        this.id_cam = id;
        this.localtime = 0;
        this.RTSP_IRL = URL;
        this.imageView = imageView;
        this.number_camera = number_camera;
        this.previous_frame = null;
        this.check_video = false;
        this.videoWriter = new VideoWriter();
        this.range_1 = 0;
        this.mask = new Mask();
        this.mFinish = true;
        this.event = false;

    }

    @Override
    public void run() {
        try {
            int fps = 0;
            int fps_sec = 0;


            // запись и предзапись видио
            LinkedList<Mat> queue = new LinkedList<>(); //очередб на предзапись или постзапись


            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(this.RTSP_IRL);
            System.out.println(grabber.hasVideo());
            grabber.setOption("rtsp_transport", "tcp"); // Use tcp, otherwise the packet loss will be very serious
            System.out.println("grabber start");
            grabber.start();


            Size size = new Size(grabber.getImageWidth(), grabber.getImageHeight());


            //1. Play video
            System.out.println(grabber.hasVideo());
            //Statement statement = connection.createStatement();
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            //что бы через раз обрабатывал MAt
            int change = 0;
            String events = "0";
            int status;
            // предназначен для того, что бы в будущем понимать было ли движение или нет
            boolean check_movement;
            while (this.mFinish) {

                // prob
                Frame frame = grabber.grabImage();

                Mat image = new Mat(converter.convertToMat(frame));
                check_movement = false;

                // условие нужно для того, чтобы кадры обрабатывались через 10
                if (change > 10) {
                    check_movement = MotionDetector(image);

                    System.out.println(LocalTime.now().getSecond());
                    change = 0;
                }
                change++;
                if (check_movement)
                    putText(image, "Movement", new Point(1500, 100),
                            FONT_HERSHEY_DUPLEX, 1.8, Scalar.RED, 4, TYPE_MARKER, false);


                status = Server.getStatus(this.id_cam);
                if (status == 0) {
                    this.event = false;
                } else if (status / 10 == this.id_cam || status / 10 == 3) {
                    this.event = true;
                    // присваивать какие-то события events
                    int ev = status % 10;

                    switch (ev) {
                        case 0 ->{this.event = false;
                        events = "0";}
                        case 1 -> events ="fire alarm";
                        case 2 -> events ="open door";
                        case 3 -> events ="alarm";
                        case 4 -> events ="open ticket office";
                        case 5 -> events ="changing of the guard";
                        default -> events ="unidentified team, cancel";
                    };
                }

                check_movement = check_movement || this.event;

                //запись

                WriteImage(check_movement, image, queue, size, events);
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
                //System.out.println(this.RTSP_IRL);
            }
        } catch (FrameGrabber.Exception | SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void WriteImage(boolean check_movement, Mat image, LinkedList<Mat> queue, Size size, String events) throws SQLException {
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
                copyvideo(this.number_camera + this.localtime + ".mp4");
                // statement.executeUpdate("insert into videos(id, daterecord, videopath, duration, original)" +
                //        "values (" + "nextval('videos_id_seq')" + ",current_timestamp,'" + "database/videos/"
                //       + this.number_camera + this.localtime + ".mp4'" + ",current_time,true)");
                System.out.println("заканчивем видио");
            }
            if (this.event) {
                // событие на экран
                putText(image, events, new Point(800, 700),
                        FONT_HERSHEY_DUPLEX, 1.8, Scalar.RED, 4, TYPE_MARKER, false);

            }
            this.videoWriter.write(image);
            System.out.println("записывфем кадр");//записываем видио
        } else {
            if (check_movement) {
                System.out.println("запись началась");
                this.localtime = LocalTime.now().getSecond();
                this.videoWriter.open(this.number_camera + localtime + ".mp4",
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

    private void copyvideo(String name) {
        Path from = Paths.get(name);
        Path to = Paths.get("C:/denis_zahar/Recordeo-server/database/videos/" + name);

        try {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully.");
        } catch (IOException ex) {
            ex.printStackTrace();
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
        return contours.size() > 40;
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

    public void finish()        //Инициирует завершение потока
    {
        mFinish = false;
    }

    public void setMask(Mask mask) throws InterruptedException {
        this.mask = mask;
        Thread.sleep(1000);
    }
}
