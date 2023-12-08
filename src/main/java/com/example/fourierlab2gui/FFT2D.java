package com.example.fourierlab2gui;

import org.bytedeco.javacpp.*;
import static org.bytedeco.fftw.global.fftw3.*;


public class FFT2D {
    private final int height;
    private final int width;
    private final DoublePointer in;
    private final DoublePointer out;
    private fftw_plan plan;

    public FFT2D(int height, int width) {
        this.height = height;
        this.width = width;
        this.in = new DoublePointer((long) height * width * 2);
        this.out = new DoublePointer((long) height * width * 2);
    }

    public void setImageData(double[][] imageData) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                in.put(((long) i * width + j) * 2, imageData[i][j] * degree(i + j));
                in.put(((long) i * width + j) * 2 + 1, 0);
            }
        }
    }

    public double[][] getImageData() {
        double[][] imageData = new double[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                imageData[i][j] = in.get(((long) i * width + j) * 2) / (height * width) * degree(i + j);
            }
        }

        return imageData;
    }


    public void forwardTransform() {
        plan = fftw_plan_dft_2d(height, width, in, out, FFTW_FORWARD, (int)FFTW_ESTIMATE);
        fftw_execute(plan);
    }

    public TransformData getForwardTransform() {
        double[][] forwardTransform = new double[height][width];
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double real = out.get(((long) i * width + j) * 2);
                double imag = out.get(((long) i * width + j) * 2 + 1);
                forwardTransform[i][j] = Math.sqrt(real * real + imag * imag);
                if (forwardTransform[i][j] < min) {
                    min = forwardTransform[i][j];
                }
                if (forwardTransform[i][j] > max) {
                    max = forwardTransform[i][j];
                }
            }
        }
        return new TransformData(forwardTransform, min, max);
    }

    public TransformData getSpectrum() {
        double[][] spectrum = new double[height][width];
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double real = out.get(((long) i * width + j) * 2);
                double imag = out.get(((long) i * width + j) * 2 + 1);
                double magnitude = Math.log(Math.sqrt(real * real + imag * imag) + 1);
                spectrum[i][j] = magnitude;
                if (magnitude < min) {
                    min = magnitude;
                }
                if (magnitude > max) {
                    max = magnitude;
                }
            }
        }
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                spectrum[i][j] = (spectrum[i][j] - min) / (max - min);
            }
        }
        return new TransformData(spectrum, min, max);
    }

    public TransformData getInverseTransform() {
        fftw_plan inversePlan = fftw_plan_dft_2d(height, width, out, in, FFTW_BACKWARD, (int)FFTW_ESTIMATE);
        fftw_execute(inversePlan);
        fftw_destroy_plan(inversePlan);

        double[][] inverseTransform = new double[height][width];
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double value = in.get(((long) i * width + j) * 2) / (height * width) * degree(i + j);
                inverseTransform[i][j] = value;
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
        }
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                inverseTransform[i][j] = (inverseTransform[i][j] - min) / (max - min);
            }
        }
        return new TransformData(inverseTransform, min, max);
    }

    public void applyHighPassFilter(double cutoffFrequency) {
        double centerX = (double) width / 2;
        double centerY = (double) height / 2;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double distance = Math.sqrt(Math.pow(i - centerX, 2) + Math.pow(j - centerY, 2));

                if (distance < cutoffFrequency) {
                    out.put(((long) i * width + j) * 2, 0);
                    out.put(((long) i * width + j) * 2 + 1, 0);
                } else {
                    double ratio = (distance - cutoffFrequency) / (Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2)) - cutoffFrequency);
                    double real = out.get(((long) i * width + j) * 2);
                    double imag = out.get(((long) i * width + j) * 2 + 1);
                    out.put(((long) i * width + j) * 2, real * ratio);
                    out.put(((long) i * width + j) * 2 + 1, imag * ratio);
                }
            }
        }
    }

    public void applyGaussianLowPassFilter(double cutoffFrequency) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double distance = Math.sqrt(Math.pow(i - height / 2, 2) + Math.pow(j - width / 2, 2));
                double gaussian = Math.exp(-Math.pow(distance, 2) / (2 * Math.pow(cutoffFrequency, 2)));
                out.put(((long) i * width + j) * 2, out.get(((long) i * width + j) * 2) * gaussian);
                out.put(((long) i * width + j) * 2 + 1, out.get(((long) i * width + j) * 2 + 1) * gaussian);
            }
        }
    }


    public void cleanup() {
        fftw_destroy_plan(plan);
        fftw_cleanup();
    }

    public void setSpectrum(double[][] newSpectrum) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                out.put(((long) i * width + j) * 2, newSpectrum[i][j]);
                out.put(((long) i * width + j) * 2 + 1, 0);
            }
        }
    }

    private static float degree(int n) {
        return (1 - 2 * (n % 2));
    }
}


