package com.example.client;

import org.bytedeco.opencv.opencv_core.Point;

public class Mask {
    private Point upper_left_corner;
    private Point lower_right_corner;
    private boolean status;

    public Mask() {
        this.upper_left_corner = new Point();
        this.lower_right_corner = new Point();
        this.status = false;
    }

    public Mask(int x_1, int y_1, int x_2, int y_2) {
        this.upper_left_corner = new Point(x_1, y_1);
        this.lower_right_corner = new Point(x_2, y_2);
        this.status = true;
    }

    public Point getUpper_left_corner() {
        return this.upper_left_corner;
    }

    public Point getLower_right_corner() {
        return this.lower_right_corner;
    }

    public void setUpper_left_corner(int x, int y) {
        this.upper_left_corner = new Point(x, y);
    }

    public void setLower_right_corner(int x, int y) {
        this.lower_right_corner = new Point(x, y);
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
