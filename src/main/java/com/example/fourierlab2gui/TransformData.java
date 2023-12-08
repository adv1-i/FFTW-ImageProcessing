package com.example.fourierlab2gui;

public class TransformData {
    // Объявление переменных для хранения данных преобразования, минимального и максимального значений
    private double[][] data;
    private double min;
    private double max;

    // Конструктор класса TransformData
    public TransformData(double[][] data, double min, double max) {
        // Инициализация переменных данными, переданными в конструктор
        this.data = data;
        this.min = min;
        this.max = max;
    }

    // Метод для получения данных преобразования
    public double[][] getData() {
        return data;
    }

    // Метод для получения минимального значения
    public double getMin() {
        return min;
    }

    // Метод для получения максимального значения
    public double getMax() {
        return max;
    }
}

