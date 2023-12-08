package com.example.fourierlab2gui;

public class GaussianLowPassFilter implements Filter{
    // Размеры изображения
    private int M, N;
    // Пороговое значение для фильтра
    private double D0;

    // Конструктор класса
    public GaussianLowPassFilter(int M, int N, double D0) {
        this.M = M;
        this.N = N;
        this.D0 = D0;
    }

    // Метод для применения фильтра к изображению
    @Override
    public double[][] applyFilter(double[][] F) {
        // Инициализация массива для хранения результата
        double[][] H = new double[M][N];

        // Применение фильтра к каждому пикселю изображения
        for (int u = 0; u < M; u++) {
            for (int v = 0; v < N; v++) {
                // Вычисление расстояния от текущего пикселя до центра изображения
                double D = Math.sqrt(Math.pow(u - M / 2.0, 2) + Math.pow(v - N / 2.0, 2));
                // Применение формулы фильтра
                H[u][v] = Math.exp(-Math.pow(D, 2) / (2 * Math.pow(D0, 2)));
                F[u][v] *= H[u][v];
            }
        }

        // Возвращение результата
        return F;
    }
}

