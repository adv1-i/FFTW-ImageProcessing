package com.example.fourierlab2gui;

public class TransformData {
    private double[][] data;
    private double min;
    private double max;

    public TransformData(double[][] data, double min, double max) {
        this.data = data;
        this.min = min;
        this.max = max;
    }

    public double[][] getData() {
        return data;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}

