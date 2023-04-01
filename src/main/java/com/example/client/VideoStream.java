package com.example.client;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.*;


import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.presets.opencv_core;
import org.opencv.imgproc.Imgproc;


import java.awt.image.BufferedImage;
import java.time.LocalTime;
import java.util.LinkedList;

import static org.bytedeco.opencv.global.opencv_core.TYPE_MARKER;
import static org.bytedeco.opencv.global.opencv_core.absdiff;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_ximgproc.dilate;

public class VideoStream implements Runnable {
    private final String RTSP_IRL;
    ImageView imageView;
    private final String number_camera;

    public VideoStream(String URL, ImageView imageView, String number_camera) {

        this.RTSP_IRL = URL;
        this.imageView = imageView;
        this.number_camera = number_camera;

    }

    @Override
    public void run() {
        try {
            int fps = 0;
            int prnt_fps = 0;
            int fps_sec = 0;
            int now_sec = 0;//Создаем переменные


            // запись и предзапись видио
            int range_10 = 0; //предзапись на 10 секунд
            LinkedList<Frame> queue = new LinkedList<>(); //очередб на предзапись или постзапись
            boolean check_video = false; // проверка на движение
            boolean end_video = false; // проверка на окончание записи
            int number = 0;
            //VideoWriter writer = new VideoWriter(name,875967048, 20.0, new Size(704,576), true);


            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(this.RTSP_IRL);
            grabber.setOption("rtsp_transport", "tcp"); // Use tcp, otherwise the packet loss will be very serious
            System.out.println("grabber start");
            grabber.start();
            FFmpegFrameRecorder recorder = FFmpegFrameRecorder.createDefault(this.number_camera + number + ".avi", 704, 576);
            recorder.start();
            //1. Play video


            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Mat previous_frame = null;
            while (true) {

                // prob
                Frame frame = grabber.grabImage();

                Mat image = converter.convertToMat(frame);

                Mat prepared_frame = new Mat();

                cvtColor(image, prepared_frame, Imgproc.COLOR_BGR2GRAY);
                GaussianBlur(prepared_frame, prepared_frame, new Size(15, 15), 0);

                if (previous_frame == null) {
                    previous_frame = new Mat(prepared_frame);
                }
                Mat dif_frame = new Mat();
                absdiff(previous_frame, prepared_frame, dif_frame);
                previous_frame = prepared_frame;
                threshold(dif_frame, dif_frame, 20, 255, THRESH_BINARY);
                //Mat kernel = getStructuringElement(MORPH_RECT, new Size(1, 1));
                // dilate(dif_frame,dif_frame, kernel);

                MatVector contours = new MatVector();
                findContours(dif_frame, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

                drawContours(image, contours, -1, Scalar.BLUE);
                //prob end

                if (contours.size() > 10) {
                    check_video = true;
                    range_10 = 0;
                    end_video = true;

                    putText(image, "Status: Movement", new Point(2000, 100),
                            FONT_HERSHEY_DUPLEX, 1.8, Scalar.RED, 4, TYPE_MARKER, false);
                } else {
                    check_video = false;
                    if (range_10 > 4)
                        queue.removeFirst();
                    queue.add(frame);
                }
                now_sec = LocalTime.now().getSecond();

                //расчёт fps
                if (fps_sec == now_sec) {
                    fps++;
                } else {
                    range_10++;
                    fps_sec = now_sec;
                    prnt_fps = fps;
                    fps = 0;
                }

                //работа с записью видио
                if (check_video) {
                    if (queue.size() != 0) {
                        for (Frame x : queue) {
                            recorder.record(x);
                        }
                        queue.clear();
                    }
                    recorder.record(frame);

                } else {
                    if (end_video) {
                        if (range_10 > 4) {
                            recorder.stop();
                            end_video = false;
                            number++;
                            recorder = FFmpegFrameRecorder.createDefault(this.number_camera + number + ".avi", grabber.getImageWidth(), grabber.getImageHeight());
                            recorder.start();
                        } else recorder.record(frame);
                    }
                }

                putText(image, "FPS: " + prnt_fps, new Point(2140, 1400),
                        FONT_HERSHEY_DUPLEX, 1.8, Scalar.WHITE, 2, TYPE_MARKER, false);
                frame = converter.convert(image);
                WritableImage image1 = SwingFXUtils.toFXImage(FrameToBufferedImage(frame), null);
                Platform.runLater(() -> imageView.imageProperty().set(image1));
                //onFXThread(imageView.imageProperty(),image);
                //System.out.println(this.RTSP_IRL);
            }
        } catch (FrameGrabber.Exception | FFmpegFrameRecorder.Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage FrameToBufferedImage(Frame frame) {

        Java2DFrameConverter converter = new Java2DFrameConverter();

        return converter.getBufferedImage(frame);
    }

}
