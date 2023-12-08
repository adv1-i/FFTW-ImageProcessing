package com.example.fourierlab2gui;

import java.util.stream.IntStream;

public class FourierTransform {
    // Метод для выполнения двумерного преобразования Фурье
    public double[][][] twoDimensionalFourierTransform(double[][] PictureInRe,
                                                       double[][] PictureInIm, int width, int height) {
        // Инициализация массивов для хранения реальной и мнимой части выходного изображения
        double[][] PictureOutRe = new double[width][height];
        double[][] PictureOutIm = new double[width][height];

        // Предварительный расчет косинусов и синусов для оптимизации
        double[] cosValuesWidth = new double[width];
        double[] sinValuesWidth = new double[width];
        for (int i = 0; i < width; i++) {
            cosValuesWidth[i] = Math.cos((2 * Math.PI * i) / width);
            sinValuesWidth[i] = Math.sin((2 * Math.PI * i) / width);
        }

        // Параллельное выполнение преобразования Фурье по строкам
        IntStream.range(0, height).parallel().forEach(k -> {
            for (int j = 0; j < width; j++) {
                for (int i = 0; i < width; i++) {
                    double cos = cosValuesWidth[(i * j) % width];
                    double sin = sinValuesWidth[(i * j) % width];
                    PictureOutRe[j][k] += PictureInRe[i][k] * cos + PictureInIm[i][k] * sin;
                    PictureOutIm[j][k] += - (PictureInRe[i][k] * sin - PictureInIm[i][k] * cos);
                }
            }
        });

        // Инициализация массивов для хранения реальной и мнимой части спектра изображения
        double[][] PictureSpectrumRe = new double[width][height];
        double[][] PictureSpectrumIm = new double[width][height];

        // Предварительный расчет косинусов и синусов для оптимизации
        double[] cosValuesHeight = new double[height];
        double[] sinValuesHeight = new double[height];
        for (int i = 0; i < height; i++) {
            cosValuesHeight[i] = Math.cos((2 * Math.PI * i) / height);
            sinValuesHeight[i] = Math.sin((2 * Math.PI * i) / height);
        }

        // Параллельное выполнение преобразования Фурье по столбцам
        IntStream.range(0, width).parallel().forEach(k -> {
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < height; i++) {
                    double cos = cosValuesHeight[(i * j) % height];
                    double sin = sinValuesHeight[(i * j) % height];
                    PictureSpectrumRe[k][j] += PictureOutRe[k][i] * cos + PictureOutIm[k][i] * sin;
                    PictureSpectrumIm[k][j] += - (PictureOutRe[k][i] * sin - PictureOutIm[k][i] * cos);
                }
            }
        });

        // Возвращаем реальную и мнимую части выходного изображения, а также реальную и мнимую части спектра изображения
        return new double[][][]{PictureOutRe, PictureOutIm, PictureSpectrumRe, PictureSpectrumIm};
    }

    // Метод для выполнения обратного двумерного преобразования Фурье
    public double[][][] inverseTwoDimensionalFourierTransform(double[][] PictureSpectrumRe,
                                                              double[][] PictureSpectrumIm, int width, int height) {
        // Инициализация массивов для хранения реальной и мнимой части выходного изображения
        double[][] PictureOutRe = new double[width][height];
        double[][] PictureOutIm = new double[width][height];

        // Предварительный расчет косинусов и синусов для оптимизации
        double[] cosValuesHeight = new double[height];
        double[] sinValuesHeight = new double[height];
        for (int i = 0; i < height; i++) {
            cosValuesHeight[i] = Math.cos((2 * Math.PI * i) / height);
            sinValuesHeight[i] = Math.sin((2 * Math.PI * i) / height);
        }

        // Параллельное выполнение обратного преобразования Фурье по столбцам
        IntStream.range(0, width).parallel().forEach(k -> {
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < height; i++) {
                    double cos = cosValuesHeight[(i * j) % height];
                    double sin = sinValuesHeight[(i * j) % height];
                    PictureOutRe[k][j] += PictureSpectrumRe[k][i] * cos - PictureSpectrumIm[k][i] * sin;
                    PictureOutIm[k][j] += PictureSpectrumRe[k][i] * sin + PictureSpectrumIm[k][i] * cos;
                }
                // Нормализация результата
                PictureOutRe[k][j] /= height;
                PictureOutIm[k][j] /= height;
            }
        });

        // Инициализация массивов для хранения реальной и мнимой части входного изображения
        double[][] PictureInRe = new double[width][height];
        double[][] PictureInIm = new double[width][height];

        // Предварительный расчет косинусов и синусов для оптимизации
        double[] cosValuesWidth = new double[width];
        double[] sinValuesWidth = new double[width];
        for (int i = 0; i < width; i++) {
            cosValuesWidth[i] = Math.cos((2 * Math.PI * i) / width);
            sinValuesWidth[i] = Math.sin((2 * Math.PI * i) / width);
        }

        // Параллельное выполнение обратного преобразования Фурье по строкам
        IntStream.range(0, height).parallel().forEach(k -> {
            for (int j = 0; j < width; j++) {
                for (int i = 0; i < width; i++) {
                    double cos = cosValuesWidth[(i * j) % width];
                    double sin = sinValuesWidth[(i * j) % width];
                    PictureInRe[j][k] += PictureOutRe[i][k] * cos - PictureOutIm[i][k] * sin;
                    PictureInIm[j][k] += PictureOutRe[i][k] * sin + PictureOutIm[i][k] * cos;
                }
                // Нормализация результата
                PictureInRe[j][k] /= width;
                PictureInIm[j][k] /= width;
            }
        });

        // Возвращаем реальную и мнимую части входного изображения
        return new double[][][]{PictureInRe, PictureInIm};
    }
}



