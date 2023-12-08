package com.example.fourierlab2gui;

public class HighPassFilter implements Filter {
    // Пороговое значение для фильтра
    private double D0;

    // Конструктор класса
    public HighPassFilter(double D0) {
        this.D0 = D0;
    }

    // Метод для применения фильтра к изображению
    @Override
    public double[][] applyFilter(double[][] F) {
        int width = F.length;
        int height = F[0].length;

        // Инициализация массива для хранения результата
        double[][] result = new double[width][height];

        // Вычисление центра изображения
        int centerX = width / 2;
        int centerY = height / 2;

        // Применение фильтра к каждому пикселю изображения
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Вычисление расстояния от текущего пикселя до центра изображения
                double distance = Math.sqrt(Math.pow(i - centerX, 2) + Math.pow(j - centerY, 2));

                // Если расстояние меньше порогового значения, то пиксель обнуляется
                if (distance < D0) {
                    result[i][j] = 0;
                } else {
                    // Иначе применяется формула фильтра
                    double ratio = (distance - D0) / (Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2)) - D0);
                    result[i][j] = F[i][j] * ratio;
                }
            }
        }

        // Возвращение результата
        return result;
    }
}









