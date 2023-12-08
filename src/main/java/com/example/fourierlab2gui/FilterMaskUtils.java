package com.example.fourierlab2gui;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class FilterMaskUtils {

    // Создание маски фильтра
    public static double[][] createFilterMask(int width, int height, double D0, boolean isHighPass) {
        // Инициализация двумерного массива для маски фильтра
        double[][] mask = new double[width][height];

        // Определение центра изображения
        int centerX = width / 2;
        int centerY = height / 2;

        // Проход по всем пикселям изображения
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Вычисление расстояния от текущего пикселя до центра изображения
                double distance = Math.sqrt(Math.pow(i - centerX, 2) + Math.pow(j - centerY, 2));

                // Если это фильтр высоких частот и расстояние меньше D0, или это фильтр низких частот и расстояние больше D0,
                // то устанавливаем значение маски в 0, иначе - в 1
                if ((isHighPass && distance < D0) || (!isHighPass && distance > D0)) {
                    mask[i][j] = 0;
                } else {
                    mask[i][j] = 1;
                }
            }
        }

        // Возвращаем маску фильтра
        return mask;
    }

    // Создание изображения из маски
    public static WritableImage createImageFromMask(double[][] mask) {
        // Получение размеров маски
        int width = mask.length;
        int height = mask[0].length;

        // Создание нового изображения с заданными размерами
        WritableImage writableImage = new WritableImage(width, height);

        // Получение объекта PixelWriter для записи пикселей в изображение
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        // Проход по всем пикселям маски
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Получение значения пикселя маски
                double colorValue = mask[i][j];

                // Создание цвета на основе значения пикселя
                Color color = Color.gray(colorValue);

                // Запись цвета в изображение
                pixelWriter.setColor(i, j, color);
            }
        }

        // Возвращаем изображение
        return writableImage;
    }
}



