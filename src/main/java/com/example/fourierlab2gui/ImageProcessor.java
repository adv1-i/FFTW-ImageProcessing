package com.example.fourierlab2gui;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageProcessor {
    // Метод для загрузки изображения из файла
    public BufferedImage loadImage(String imagePath) throws IOException {
        // Используется класс ImageIO для чтения изображения из файла
        return ImageIO.read(new File(imagePath));
    }

    // Метод для получения цветовых компонентов изображения
    public int[][] getColorComponents(BufferedImage image) {
        // Получение ширины и высоты изображения
        int width = image.getWidth();
        int height = image.getHeight();
        // Создание двумерного массива для хранения цветовых компонентов
        int[][] colorComponents = new int[height][width];

        // Цикл по всем пикселям изображения
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Получение RGB значения пикселя
                colorComponents[y][x] = image.getRGB(x, y);
            }
        }

        // Возвращение массива цветовых компонентов
        return colorComponents;
    }

    // Метод для получения массива определенной цветовой компоненты
    public int[][] getColorComponentArray(int[][] colorComponents, int componentIndex) {
        // Получение размеров массива цветовых компонентов
        int height = colorComponents.length;
        int width = colorComponents[0].length;
        // Создание двумерного массива для хранения цветовой компоненты
        int[][] componentArray = new int[height][width];

        // Цикл по всем элементам массива цветовых компонентов
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Получение RGB значения
                int rgb = colorComponents[y][x];
                int componentValue;
                // Выделение нужной цветовой компоненты из RGB значения
                if (componentIndex == 0) {
                    componentValue = (rgb >> 16) & 0xFF; // Красная компонента
                } else if (componentIndex == 1) {
                    componentValue = (rgb >> 8) & 0xFF; // Зеленая компонента
                } else {
                    componentValue = rgb & 0xFF; // Синяя компонента
                }
                // Запись значения цветовой компоненты в массив
                componentArray[y][x] = componentValue;
            }
        }

        // Возвращение массива цветовой компоненты
        return componentArray;
    }

    // Метод для сохранения изображения из массивов цветовых компонент
    public String saveColorComponentArrayAsImage(int[][] redComponentArray, int[][] greenComponentArray,
                                                 int[][] blueComponentArray, String fileName,
                                                 int width, int height) throws IOException {
        // Создание нового изображения
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Цикл по всем пикселям изображения
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Получение значений цветовых компонент
                int red = redComponentArray[y][x];
                int green = greenComponentArray[y][x];
                int blue = blueComponentArray[y][x];
                // Формирование RGB значения
                int rgb = (red << 16) | (green << 8) | blue;
                // Установка RGB значения для пикселя
                image.setRGB(x, y, rgb);
            }
        }

        // Создание файла для сохранения изображения
        File outputFile = new File(fileName);
        // Запись изображения в файл
        ImageIO.write(image, "jpg", outputFile);
        // Возвращение пути к файлу
        return outputFile.getAbsolutePath();
    }

    // Метод для сохранения изображений спектра
    public void saveSpectrumImages(int[][] redSpectrumArray, int[][] greenSpectrumArray, int[][] blueSpectrumArray,
                                   String fileName, int width, int height) throws IOException {
        // Сохранение изображений спектра для каждой цветовой компоненты
        saveColorComponentArrayAsImage(redSpectrumArray, new int[height][width], new int[height][width],
                fileName + "_red_spectrum.jpg", width, height);

        saveColorComponentArrayAsImage(new int[height][width], greenSpectrumArray, new int[height][width],
                fileName + "_green_spectrum.jpg", width, height);

        saveColorComponentArrayAsImage(new int[height][width], new int[height][width], blueSpectrumArray,
                fileName + "_blue_spectrum.jpg", width, height);

        // Сохранение изображения спектра для всех цветовых компонент
        saveColorComponentArrayAsImage(redSpectrumArray, greenSpectrumArray, blueSpectrumArray,
                fileName + "_rgb_spectrum.jpg", width, height);
    }

    // Метод для сохранения изображений после преобразования Фурье
    public void saveAfterFourierImages(int[][] redComponentArray, int[][] greenComponentArray,
                                       int[][] blueComponentArray, String fileName, int width,
                                       int height) throws IOException {
        // Сохранение изображений после преобразования Фурье для каждой цветовой компоненты
        saveColorComponentArrayAsImage(redComponentArray, new int[height][width], new int[height][width],
                fileName + "_red_after_fourier.jpg", width, height);

        saveColorComponentArrayAsImage(new int[height][width], greenComponentArray, new int[height][width],
                fileName + "_green_after_fourier.jpg", width, height);

        saveColorComponentArrayAsImage(new int[height][width], new int[height][width], blueComponentArray,
                fileName + "_blue_after_fourier.jpg", width, height);

        // Сохранение изображения после преобразования Фурье для всех цветовых компонент
        saveColorComponentArrayAsImage(redComponentArray, greenComponentArray, blueComponentArray,
                fileName + "_rgb_after_fourier.jpg", width, height);
    }

    // Метод для умножения массива на -1
    public double[][] multiplyArrayByNegativeOne(double[][] colorComponents) {
        // Получение размеров массива
        int height = colorComponents.length;
        int width = colorComponents[0].length;
        // Создание нового массива для хранения результата
        double[][] componentArray = new double[height][width];

        // Цикл по всем элементам массива
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Умножение элемента массива на -1 в степени (x + y)
                double componentValue = colorComponents[y][x] * Math.pow(-1, x + y);
                // Запись результата в новый массив
                componentArray[y][x] = componentValue;
            }
        }

        // Возвращение результата
        return componentArray;
    }

    // Метод для получения имени цветовой компоненты по индексу
    public static String getComponentName(int componentIndex) {
        switch (componentIndex) {
            case 0:
                return "R"; // Красная компонента
            case 1:
                return "G"; // Зеленая компонента
            case 2:
                return "B"; // Синяя компонента
            default:
                throw new IllegalArgumentException("Invalid component index: " + componentIndex);
        }
    }

    public double[][] scaleRealPartTo255(double[][] PictureOutRe, int width, int height) {
        // Создание нового двумерного массива для масштабированного изображения
        double[][] scaledPictureOutRe = new double[width][height];

        // Инициализация минимального и максимального значений первым элементом массива
        double minVal = PictureOutRe[0][0];
        double maxVal = PictureOutRe[0][0];

        // Поиск минимального и максимального значений в массиве
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                minVal = Math.min(minVal, PictureOutRe[i][j]);
                maxVal = Math.max(maxVal, PictureOutRe[i][j]);
            }
        }

        // Масштабирование значений массива от 0 до 255
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                scaledPictureOutRe[i][j] = ((PictureOutRe[i][j] - minVal) / (maxVal - minVal)) * 255;
            }
        }

        // Возвращение масштабированного массива
        return scaledPictureOutRe;
    }

    public double[][] spectrumImage(double[][] PictureSpectrumRe, double[][] PictureSpectrumIm, int width, int height) {
        // Создание нового двумерного массива для амплитудного спектра
        double[][] amplitudeSpectrum = new double[width][height];

        // Вычисление амплитудного спектра
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                amplitudeSpectrum[i][j] = Math.sqrt(Math.pow(PictureSpectrumRe[i][j], 2) +
                        Math.pow(PictureSpectrumIm[i][j], 2));
            }
        }

        // Применение логарифмического масштабирования к амплитудному спектру
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                amplitudeSpectrum[i][j] = Math.log(1 + amplitudeSpectrum[i][j]);
            }
        }

        // Масштабирование реальной части амплитудного спектра до 255
        amplitudeSpectrum = scaleRealPartTo255(amplitudeSpectrum, width, height);

        // Возвращение амплитудного спектра
        return amplitudeSpectrum;
    }
}



