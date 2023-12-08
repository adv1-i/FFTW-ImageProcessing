package com.example.fourierlab2gui;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.example.fourierlab2gui.FilterMaskUtils.createImageFromMask;

public class FourierLibraryGUI extends Application {

    // Создание ImageView для отображения оригинального изображения, спектра, обратного преобразования, отфильтрованного спектра, отфильтрованного преобразования Фурье и маски фильтра
    private ImageView originalImageView = new ImageView();
    private ImageView spectrumImageView = new ImageView();
    private ImageView inverseImageView = new ImageView();
    private ImageView filteredSpectrumImageView = new ImageView();
    private ImageView filteredFourierImageView = new ImageView();
    private ImageView filterMaskImageView = new ImageView();

    // Создание радиокнопок для выбора канала изображения
    private RadioButton redButton = new RadioButton("R");
    private RadioButton greenButton = new RadioButton("G");
    private RadioButton blueButton = new RadioButton("B");
    private RadioButton rgbButton = new RadioButton("RGB");

    // Создание кнопок для загрузки изображения и применения преобразования Фурье
    private Button loadButton = new Button("Загрузить");
    private Button applyButton = new Button("Применить");

    // Создание переменных для хранения данных изображения
    private BufferedImage image;
    private double[][] redData;
    private double[][] greenData;
    private double[][] blueData;
    Label energyLabel = new Label();

    // Создание выпадающего списка для выбора фильтра и текстового поля для ввода частоты среза
    ComboBox<String> filterComboBox;
    TextField cutoffFrequencyField;
    private String fileName;

    public static void main(String[] args) {
        launch(args);
    }

    private TitledPane createTitledPaneWithDownloadButton(
            String title, ImageView imageView) {
        // Создание кнопки для загрузки изображения
        Button downloadButton = new Button("Скачать");
        downloadButton.setOnAction(e -> {
            // Создание диалога для выбора файла
            FileChooser fileChooser = new FileChooser();

            // Установка начального имени файла
            fileChooser.setInitialFileName(
                    fileName + "_" + title.replaceAll("\\s+", ""));

            // Установка фильтров расширений файлов
            FileChooser.ExtensionFilter extFilterJPG =
                    new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
            FileChooser.ExtensionFilter extFilterPNG =
                    new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            FileChooser.ExtensionFilter extFilterJPEG =
                    new FileChooser.ExtensionFilter("JPEG files (*.jpeg)", "*.jpeg");
            FileChooser.ExtensionFilter extFilterBMP =
                    new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.bmp");
            fileChooser.getExtensionFilters().addAll(
                    extFilterJPG, extFilterPNG, extFilterJPEG, extFilterBMP);

            // Отображение диалога для выбора файла
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    // Получение расширения файла
                    String ext =
                            file.getName().substring(file.getName().lastIndexOf('.') + 1);
                    // Сохранение изображения в файл
                    ImageIO.write(
                            SwingFXUtils.fromFXImage(imageView.getImage(), null), ext, file);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });

        // Создание панели с заголовком
        HBox titlePane = new HBox();
        Label label = new Label(title);
        HBox.setHgrow(label, Priority.ALWAYS);
        titlePane.getChildren().addAll(label, downloadButton);
        titlePane.setAlignment(Pos.CENTER_LEFT);

        // Создание раскрывающейся панели с изображением
        TitledPane titledPane = new TitledPane();
        titledPane.setGraphic(titlePane);
        titledPane.setContent(imageView);

        return titledPane;
    }

    @Override
    public void start(Stage primaryStage) {
        // Создание группы переключателей для выбора канала изображения
        ToggleGroup group = new ToggleGroup();
        redButton.setToggleGroup(group);
        greenButton.setToggleGroup(group);
        blueButton.setToggleGroup(group);
        rgbButton.setToggleGroup(group);

        // Установка размеров ImageView для отображения изображений
        originalImageView.setFitWidth(270);
        originalImageView.setFitHeight(270);
        originalImageView.setPreserveRatio(true);

        spectrumImageView.setFitWidth(270);
        spectrumImageView.setFitHeight(270);
        spectrumImageView.setPreserveRatio(true);

        inverseImageView.setFitWidth(270);
        inverseImageView.setFitHeight(270);
        inverseImageView.setPreserveRatio(true);

        filteredSpectrumImageView.setFitWidth(270);
        filteredSpectrumImageView.setFitHeight(270);
        filteredSpectrumImageView.setPreserveRatio(true);

        filteredFourierImageView.setFitWidth(270);
        filteredFourierImageView.setFitHeight(270);
        filteredFourierImageView.setPreserveRatio(true);

        filterMaskImageView.setFitWidth(270);
        filterMaskImageView.setFitHeight(270);
        filterMaskImageView.setPreserveRatio(true);

        // Создание панелей с заголовками и кнопками для загрузки изображений
        TitledPane originalPane = createTitledPaneWithDownloadButton("Исходное изображение", originalImageView);
        TitledPane spectrumPane = createTitledPaneWithDownloadButton("Спектр изображения", spectrumImageView);
        TitledPane fourierPane = createTitledPaneWithDownloadButton("Фурье изображение", inverseImageView);
        TitledPane filteredSpectrumPane = createTitledPaneWithDownloadButton("Отфильтрованный спектр", filteredSpectrumImageView);
        TitledPane filteredFourierPane = createTitledPaneWithDownloadButton("Отфильтрованное изображение", filteredFourierImageView);

        // Создание верхней панели с изображениями
        HBox topBox = new HBox(10);
        topBox.getChildren().addAll(originalPane, spectrumPane, fourierPane);

        // Создание выпадающего списка для выбора фильтра
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("Идеальный фильтр высоких частот", "Фильтр Гаусса низких частот");
        filterComboBox.setValue("Идеальный фильтр высоких частот");

        // Создание текстового поля для ввода частоты среза
        cutoffFrequencyField = new TextField();
        cutoffFrequencyField.setPromptText("Частота среза");

        // Установка обработчиков событий для кнопок загрузки изображения и применения преобразования Фурье
        loadButton.setOnAction(event -> loadImage());
        applyButton.setOnAction(e -> {
            long startTime = System.currentTimeMillis();
            // Выполнение преобразования Фурье
            applyFourierTransform();

            // Вывод времени выполнения преобразования
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
            System.out.println("Время выполнения: " + duration + " милисекунд");
        });

        // Создание нижней панели с изображениями
        HBox bottomBox = new HBox(10);
        bottomBox.getChildren().addAll(filteredSpectrumPane, filteredFourierPane);

        // Создание панели управления
        VBox controlPanel = new VBox();
        controlPanel.getChildren().addAll(loadButton, applyButton, redButton, greenButton, blueButton, rgbButton, filterComboBox, cutoffFrequencyField, energyLabel);

        // Создание вертикальной панели с изображениями
        VBox vBoxImages = new VBox(10);
        vBoxImages.getChildren().addAll(topBox, bottomBox);

        // Создание главной горизонтальной панели
        HBox hBoxMain = new HBox(10);
        hBoxMain.getChildren().addAll(controlPanel, vBoxImages);

        // Создание сцены и установка ее на главное окно
        Scene scene = new Scene(hBoxMain, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadImage() {
        // Создание диалога для выбора файла
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // Запоминаем имя файла
                fileName = file.getName();

                // Загрузка изображения
                image = ImageIO.read(file);
                int height = image.getHeight();
                int width = image.getWidth();

                // Инициализация массивов для хранения данных каналов изображения
                redData = new double[height][width];
                greenData = new double[height][width];
                blueData = new double[height][width];
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        // Получение RGB значений каждого пикселя
                        int rgb = image.getRGB(j, i);
                        redData[i][j] = ((rgb >> 16) & 0xFF);
                        greenData[i][j] = ((rgb >> 8) & 0xFF);
                        blueData[i][j] = (rgb & 0xFF);
                    }
                }

                // Преобразование BufferedImage в Image и установка его в ImageView
                Image fxImage = SwingFXUtils.toFXImage(image, null);
                originalImageView.setImage(fxImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double calculateEnergy(double[][] data) {
        double energy = 0.0;
        // Проходим по всем элементам данных
        for (double[] row : data) {
            for (double value : row) {
                // Вычисляем энергию как сумму квадратов значений
                energy += value * value;
            }
        }
        return energy;
    }


    private void applyFourierTransform() {
        try {
            // Определение массивов для хранения данных о цветах и соответствующих им
            // данных изображения
            String[] colors = {"red", "green", "blue"};
            double[][][] data = {redData, greenData, blueData};

            // Создание массивов для хранения данных о спектрах и обратных
            // преобразованиях
            TransformData[] filteredSpectrumData = new TransformData[3];
            TransformData[] filteredInverseData = new TransformData[3];
            TransformData[] spectrumData = new TransformData[3];
            TransformData[] inverseData = new TransformData[3];

            // Инициализация переменных для хранения энергии изображения
            double originalEnergy = 0.0;
            double filteredEnergy = 0.0;

            // Проходим по всем цветам
            for (int i = 0; i < colors.length; i++) {
                String color = colors[i];
                // Проверяем, выбран ли текущий цвет
                if ((color.equals("red") && redButton.isSelected())
                        || (color.equals("green") && greenButton.isSelected())
                        || (color.equals("blue") && blueButton.isSelected())
                        || rgbButton.isSelected()) {
                    // Создаем объект FFT2D и устанавливаем данные изображения
                    FFT2D fft = new FFT2D(data[i].length, data[i][0].length);
                    fft.setImageData(data[i]);

                    // Выполняем прямое преобразование Фурье
                    fft.forwardTransform();

                    // Получаем спектр и обратное преобразование
                    spectrumData[i] = fft.getSpectrum();
                    inverseData[i] = fft.getInverseTransform();

                    // Получаем выбранный фильтр и частоту среза
                    String selectedFilter = filterComboBox.getValue();
                    double cutoffFrequency =
                            Double.parseDouble(cutoffFrequencyField.getText());

                    // Применяем выбранный фильтр
                    if (selectedFilter.equals("Идеальный фильтр высоких частот")) {
                        fft.applyHighPassFilter(cutoffFrequency);
                    } else if (selectedFilter.equals("Фильтр Гаусса низких частот")) {
                        fft.applyGaussianLowPassFilter(cutoffFrequency);
                    }

                    // Получаем отфильтрованный спектр и обратное преобразование
                    filteredSpectrumData[i] = fft.getSpectrum();
                    filteredInverseData[i] = fft.getInverseTransform();

                    // Вычисляем энергию оригинального и отфильтрованного изображения
                    double originalEnergyChannel = calculateEnergy(data[i]);
                    double filteredEnergyChannel = calculateEnergy(fft.getImageData());

                    // Обновляем общую энергию
                    originalEnergy += originalEnergyChannel;
                    filteredEnergy += filteredEnergyChannel;

                    // Очищаем ресурсы, связанные с FFTW
                    fft.cleanup();
                }
            }

            // Если выбран RGB-режим, сохраняем и отображаем RGB-изображения
            if (rgbButton.isSelected()) {
                BufferedImage filteredSpectrumImage = saveRGBImage(
                        filteredSpectrumData[0].getData(), filteredSpectrumData[1].getData(),
                        filteredSpectrumData[2].getData(), "rgb_filtered_spectrum.jpg");
                BufferedImage filteredInverseImage = saveRGBImage(
                        filteredInverseData[0].getData(), filteredInverseData[1].getData(),
                        filteredInverseData[2].getData(), "rgb_filtered_inverse.jpg");

                BufferedImage spectrumImage =
                        saveRGBImage(spectrumData[0].getData(), spectrumData[1].getData(),
                                spectrumData[2].getData(), "rgb_spectrum.jpg");
                BufferedImage inverseImage =
                        saveRGBImage(inverseData[0].getData(), inverseData[1].getData(),
                                inverseData[2].getData(), "rgb_inverse.jpg");

                // Устанавливаем изображения в ImageView
                filteredSpectrumImageView.setImage(
                        SwingFXUtils.toFXImage(filteredSpectrumImage, null));
                filteredFourierImageView.setImage(
                        SwingFXUtils.toFXImage(filteredInverseImage, null));

                spectrumImageView.setImage(SwingFXUtils.toFXImage(spectrumImage, null));
                inverseImageView.setImage(SwingFXUtils.toFXImage(inverseImage, null));
            } else {
                // Если выбран один из цветов, сохраняем и отображаем соответствующие
                // изображения
                for (int i = 0; i < colors.length; i++) {
                    String color = colors[i];
                    int shift = 0;
                    if (color.equals("red")) {
                        shift = 16;
                    } else if (color.equals("green")) {
                        shift = 8;
                    } else if (color.equals("blue")) {
                        shift = 0;
                    }
                    if ((color.equals("red") && redButton.isSelected())
                            || (color.equals("green") && greenButton.isSelected())
                            || (color.equals("blue") && blueButton.isSelected())) {
                        BufferedImage filteredSpectrumImage =
                                saveImage(filteredSpectrumData[i].getData(),
                                        color + "_filtered_spectrum", shift);
                        BufferedImage filteredInverseImage =
                                saveImage(filteredInverseData[i].getData(),
                                        color + "_filtered_inverse", shift);

                        BufferedImage spectrumImage =
                                saveImage(spectrumData[i].getData(), color + "_spectrum", shift);
                        BufferedImage inverseImage =
                                saveImage(inverseData[i].getData(), color + "_inverse", shift);

                        // Устанавливаем изображения в ImageView
                        filteredSpectrumImageView.setImage(
                                SwingFXUtils.toFXImage(filteredSpectrumImage, null));
                        filteredFourierImageView.setImage(
                                SwingFXUtils.toFXImage(filteredInverseImage, null));

                        spectrumImageView.setImage(
                                SwingFXUtils.toFXImage(spectrumImage, null));
                        inverseImageView.setImage(SwingFXUtils.toFXImage(inverseImage, null));
                    }
                }
            }
            // Вычисляем и отображаем процент энергии после фильтрации
            double energyPercentage = (filteredEnergy / originalEnergy) * 100;
            energyLabel.setText(
                    String.format("Energy after filtering: %.4f%%", energyPercentage));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage saveRGBImage(double[][] redData, double[][] greenData,
                                       double[][] blueData, String filename) throws IOException {
        // Определение размеров изображения
        int height = redData.length;
        int width = redData[0].length;
        // Создание нового изображения с заданными размерами и типом INT_RGB
        BufferedImage image =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Инициализация переменных для хранения минимальных и максимальных значений
        // каждого канала
        double minRed = Double.MAX_VALUE, maxRed = Double.MIN_VALUE;
        double minGreen = Double.MAX_VALUE, maxGreen = Double.MIN_VALUE;
        double minBlue = Double.MAX_VALUE, maxBlue = Double.MIN_VALUE;

        // Поиск минимальных и максимальных значений каждого канала
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                minRed = Math.min(minRed, redData[i][j]);
                maxRed = Math.max(maxRed, redData[i][j]);
                minGreen = Math.min(minGreen, greenData[i][j]);
                maxGreen = Math.max(maxGreen, greenData[i][j]);
                minBlue = Math.min(minBlue, blueData[i][j]);
                maxBlue = Math.max(maxBlue, blueData[i][j]);
            }
        }

        // Нормализация данных каждого канала и сохранение в изображении
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int red = (int) ((redData[i][j] - minRed) / (maxRed - minRed) * 255);
                int green =
                        (int) ((greenData[i][j] - minGreen) / (maxGreen - minGreen) * 255);
                int blue = (int) ((blueData[i][j] - minBlue) / (maxBlue - minBlue) * 255);
                int rgb = (red << 16) | (green << 8) | blue;
                image.setRGB(j, i, rgb);
            }
        }
        // Сохранение изображения в файл
        ImageIO.write(image, "jpg", new File(filename));
        return image;
    }

    private BufferedImage saveImage(double[][] data, String filename, int shift)
            throws IOException {
        // Определение размеров изображения
        int height = data.length;
        int width = data[0].length;
        // Создание нового изображения с заданными размерами и типом INT_RGB
        BufferedImage image =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Инициализация переменных для хранения минимального и максимального значений
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        // Поиск минимального и максимального значений
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double value = data[i][j];
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
        }

        // Нормализация данных и сохранение в изображении
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int value = (int) ((data[i][j] - min) / (max - min) * 255);
                int rgb = (value << shift);
                image.setRGB(j, i, rgb);
            }
        }
        // Сохранение изображения в файл
        ImageIO.write(image, "jpg", new File(filename));
        return image;
    }
}


