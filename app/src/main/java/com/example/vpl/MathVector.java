package com.example.vpl;


/**
 * Created by MashPlant on 2016/4/4.
 */
class MathVector {
    double x;
    double y;

    MathVector(double arg_x, double arg_y) {
        x = arg_x;
        y = arg_y;
    }

    MathVector(MathVector arg) {
        x = arg.x;
        y = arg.y;
    }

    public void substract(final MathVector mathVector) {
        this.x -= mathVector.x;
        this.y -= mathVector.y;
    }

    public void add(final MathVector mathVector) {
        this.x += mathVector.x;
        this.y += mathVector.y;
    }

    public double multiply(final MathVector mathVector) {
        return this.x * mathVector.x + this.y * mathVector.y;
    }

    public double getLength() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    static public MathVector formVectorAs(double length, MathVector mathVector) {
        return new MathVector(length * mathVector.x / mathVector.getLength(),
                length * mathVector.y / mathVector.getLength());
    }

    static public MathVector formVectorAs(double length, double vx, double vy) {
        double originLength = Math.sqrt(vx * vx + vy * vy);
        return new MathVector(length * vx / originLength, length * vy / originLength);
    }

    public MathVector shadow(MathVector mathVector) {
        double length = this.multiply(mathVector) / mathVector.getLength();
        return formVectorAs(length, mathVector);
    }

    public MathVector revolve(double angle) {
        return new MathVector((x * Math.cos(angle) - y * Math.sin(angle)), (x * Math.sin(angle) + y * Math.cos(angle)));
    }

    public static double distanceDotToLine(double dotX, double dotY, double x1, double y1, double x2, double y2) {
        if (x2 == x1) {
            return Math.abs(dotX - x1);
        }
        double k = (y2 - y1) / (x2 - x1);
        double b = y1 - x1 * k;
        return Math.abs(k * dotX - dotY + b) / Math.sqrt(k * k + 1);
    }

    public static double[] pedalDotToLine(double dotX, double dotY, double x1, double y1, double x2, double y2) {
        if (x2 == x1 || y1 == y2) {
            return new double[]{dotX, dotY};
        }
        double k = (y2 - y1) / (x2 - x1);
        double b = y1 - x1 * k;
        return new double[]{(k * dotY - k * b + dotX) / (k * k + 1), (k * dotY - k * b + dotX) / (k + 1 / k) + b};
    }
}