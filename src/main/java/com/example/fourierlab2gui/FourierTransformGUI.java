package com.example.fourierlab2gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import static com.example.fourierlab2gui.FilterMaskUtils.createImageFromMask;

public class FourierTransformGUI extends Application {

    // Путь к исходному изображению
    private String imagePath;
    // Путь к спектру изображения
    private String spectrumPath;
    // Путь к отфильтрованному спектру
    private String filteredSpectrumPath;
    // Путь к отфильтрованному файлу
    private String filteredFilePath;
    // Путь к файлу
    private String filePath;
    // Канал изображения (R, G, B, RGB)
    private String channel;
    // Фильтр для преобразования Фурье
    private Filter filter;
    // Пороговое значение для фильтра
    private double D0;
    // Размеры изображения
    private int M, N;
    // Имя файла
    private String fileName;
    // Представления изображений для отображения на пользовательском интерфейсе
    private ImageView originalImageView = new ImageView();
    private ImageView spectrumImageView = new ImageView();
    private ImageView fourierImageView = new ImageView();
    private ImageView filteredSpectrumImageView = new ImageView();
    private ImageView filteredFourierImageView = new ImageView();

    // Радиокнопки для выбора канала изображения
    RadioButton rButton = new RadioButton("R");
    RadioButton gButton = new RadioButton("G");
    RadioButton bButton = new RadioButton("B");
    RadioButton rgbButton = new RadioButton("RGB");
    // Метка для отображения энергии изображения
    Label energyLabel = new Label();

    // Установка пути к изображению
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Получение пути к спектру
    public String getSpectrumPath() {
        return spectrumPath;
    }

    // Получение пути к файлу
    public String getFilePath() {
        return filePath;
    }

    // Установка канала изображения
    public void setChannel(String channel) {
        this.channel = channel;
    }

    // Получение пути к отфильтрованному спектру
    public String getFilteredSpectrumPath() {
        return filteredSpectrumPath;
    }

    // Получение пути к отфильтрованному файлу
    public String getFilteredFilePath() {
        return filteredFilePath;
    }

    public void execute(boolean applyFilter) {
        // Создание объектов для обработки изображений и преобразования Фурье
        ImageProcessor imageProcessor = new ImageProcessor();
        FourierTransform fourierTransform = new FourierTransform();

        try {
            // Загрузка изображения
            BufferedImage image = imageProcessor.loadImage(imagePath);
            // Получение ширины и высоты изображения
            int width = image.getWidth();
            int height = image.getHeight();

            // Получение цветовых компонентов изображения
            int[][] colorComponents = imageProcessor.getColorComponents(image);
            // Создание массивов для хранения цветовых компонентов
            int[][][] componentArrays = new int[3][height][width];
            double[][][] componentArraysDouble = new double[3][height][width];

            // Преобразование цветовых компонентов в double и умножение на -1
            IntStream.range(0, 3).parallel().forEach(componentIndex -> {
                componentArrays[componentIndex] = imageProcessor.getColorComponentArray(colorComponents, componentIndex);
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        componentArraysDouble[componentIndex][i][j] = (double) componentArrays[componentIndex][i][j];
                    }
                }
                componentArraysDouble[componentIndex] = imageProcessor.multiplyArrayByNegativeOne(componentArraysDouble[componentIndex]);
            });

            // Создание массива для мнимой части преобразования Фурье
            double[][] imaginaryPart = new double[height][width];

            // Создание массивов для хранения результатов преобразования Фурье
            int[][][] PictureInReInt = new int[3][width][height];
            int[][][] scaledPictureOutReInt = new int[3][width][height];
            M = width;
            N = height;

            // Применение преобразования Фурье к каждому цветовому каналу
            IntStream.range(0, 3).parallel().forEach(componentIndex -> {
                if (channel.equals("RGB") || channel.equals(ImageProcessor.getComponentName(componentIndex))) {
                    // Применение прямого преобразования Фурье
                    double[][][] fourierResult = fourierTransform.twoDimensionalFourierTransform(
                            componentArraysDouble[componentIndex], imaginaryPart, width, height);

                    // Применение фильтра, если необходимо
                    if (applyFilter && filter != null) {
                        for (int i = 0; i < fourierResult.length; i++) {
                            fourierResult[i] = filter.applyFilter(fourierResult[i]);
                        }
                    }

                    // Получение результатов прямого преобразования Фурье
                    double[][] PictureOutRe = fourierResult[0];
                    double[][] PictureOutIm = fourierResult[1];
                    double[][] PictureSpectrumRe = fourierResult[2];
                    double[][] PictureSpectrumIm = fourierResult[3];

                    // Применение обратного преобразования Фурье
                    double[][][] inverseFourierResult = fourierTransform.inverseTwoDimensionalFourierTransform( PictureSpectrumRe, PictureSpectrumIm, width, height);

                    // Получение результатов обратного преобразования Фурье
                    double[][] PictureInRe = inverseFourierResult[0];
                    double[][] PictureInIm = inverseFourierResult[1];

                    // Умножение результата на -1
                    PictureInRe = imageProcessor.multiplyArrayByNegativeOne(PictureInRe);
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            PictureInReInt[componentIndex][i][j] = (int) PictureInRe[i][j];
                        }
                    }

                    // Создание изображения спектра
                    double[][] scaledPictureOutRe = imageProcessor.spectrumImage(PictureSpectrumRe, PictureSpectrumIm, width, height);
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            scaledPictureOutReInt[componentIndex][i][j] = (int) scaledPictureOutRe[i][j];
                        }
                    }

                    // Вычисление процента энергии, сохраненной после фильтрации
                    if (applyFilter) {
                        double originalEnergy = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                originalEnergy += Math.pow(componentArraysDouble[componentIndex][i][j], 2);
                            }
                        }

                        double filteredEnergy = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                filteredEnergy += Math.pow(PictureInRe[i][j], 2);
                            }
                        }

                        double energyPercent = (filteredEnergy / originalEnergy) * 100;

                        // Обновление метки с процентом энергии на пользовательском интерфейсе
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                energyLabel.setText("Энергия для канала " +
                                        ImageProcessor.getComponentName(componentIndex) + " = " +
                                        energyPercent + "%");
                            }
                        });
                    }
                }
            });

            // Сохранение изображений после преобразования Фурье и спектров
            imageProcessor.saveAfterFourierImages(PictureInReInt[0], PictureInReInt[1], PictureInReInt[2], "after_fourier", width, height);
            imageProcessor.saveSpectrumImages(scaledPictureOutReInt[0], scaledPictureOutReInt[1], scaledPictureOutReInt[2], "spectrum", width, height);
            if (applyFilter) {
                // Сохранение отфильтрованных изображений и спектров
                filteredSpectrumPath = imageProcessor.saveColorComponentArrayAsImage(scaledPictureOutReInt[0], scaledPictureOutReInt[1], scaledPictureOutReInt[2], "filteredSpectrum.jpg", width, height);
                filteredFilePath = imageProcessor.saveColorComponentArrayAsImage(PictureInReInt[0], PictureInReInt[1], PictureInReInt[2], "filteredResWithAll2.jpg", width, height);
            } else {
                // Сохранение изображений спектра и файла
                spectrumPath = imageProcessor.saveColorComponentArrayAsImage(scaledPictureOutReInt[0], scaledPictureOutReInt[1], scaledPictureOutReInt[2], "spectrum.jpg", width, height);
                filePath = imageProcessor.saveColorComponentArrayAsImage(PictureInReInt[0], PictureInReInt[1], PictureInReInt[2], "resWithAll2.jpg", width, height);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Функция для создания TitledPane с кнопкой скачивания
    private TitledPane createTitledPaneWithDownloadButton(String title, ImageView imageView) {
        // Создание кнопки "Скачать"
        Button downloadButton = new Button("Скачать");
        downloadButton.setOnAction(e -> {
            // Создание диалога выбора файла для сохранения изображения
            FileChooser fileChooser = new FileChooser();

            // Установка начального имени файла
            fileChooser.setInitialFileName(fileName + "_" + title);

            // Установка фильтров расширений файлов
            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            FileChooser.ExtensionFilter extFilterJPEG = new FileChooser.ExtensionFilter("JPEG files (*.jpeg)", "*.jpeg");
            FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.bmp");
            fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG, extFilterJPEG, extFilterBMP);

            // Отображение диалога сохранения файла
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    // Получение расширения файла
                    String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                    // Сохранение изображения в файл
                    ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), ext, file);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });

        // Создание панели с заголовком и кнопкой "Скачать"
        HBox titlePane = new HBox();
        Label label = new Label(title);
        HBox.setHgrow(label, Priority.ALWAYS);
        titlePane.getChildren().addAll(label, downloadButton);
        titlePane.setAlignment(Pos.CENTER_LEFT);

        // Создание панели с заголовком, содержащей изображение
        TitledPane titledPane = new TitledPane();
        titledPane.setGraphic(titlePane);
        titledPane.setContent(imageView);

        // Возвращение созданной панели
        return titledPane;
    }




    @Override
    public void start(Stage primaryStage) {
        // Установка заголовка окна
        primaryStage.setTitle("Fourier Transform Interface");

        // Установка размеров и сохранение пропорций для изображений
        originalImageView.setFitWidth(270);
        originalImageView.setFitHeight(270);
        originalImageView.setPreserveRatio(true);

        spectrumImageView.setFitWidth(270);
        spectrumImageView.setFitHeight(270);
        spectrumImageView.setPreserveRatio(true);

        fourierImageView.setFitWidth(270);
        fourierImageView.setFitHeight(270);
        fourierImageView.setPreserveRatio(true);

        filteredSpectrumImageView.setFitWidth(270);
        filteredSpectrumImageView.setFitHeight(270);
        filteredSpectrumImageView.setPreserveRatio(true);

        filteredFourierImageView.setFitWidth(280);
        filteredFourierImageView.setFitHeight(270);
        filteredFourierImageView.setPreserveRatio(true);

        // Создание ImageView для маски фильтра
        ImageView filterMaskImageView = new ImageView();
        filterMaskImageView.setFitWidth(280);
        filterMaskImageView.setFitHeight(270);
        filterMaskImageView.setPreserveRatio(true);

        // Создание панелей с заголовками и кнопками для загрузки изображений
        TitledPane filterMaskPane = createTitledPaneWithDownloadButton("Маска фильтра", filterMaskImageView);

        TitledPane originalPane = createTitledPaneWithDownloadButton("Исходное изображение", originalImageView);
        TitledPane spectrumPane = createTitledPaneWithDownloadButton("Спектр изображения", spectrumImageView);
        TitledPane fourierPane = createTitledPaneWithDownloadButton("Фурье изображение", fourierImageView);
        TitledPane filteredSpectrumPane = createTitledPaneWithDownloadButton("Отфильтрованный спектр", filteredSpectrumImageView);
        TitledPane filteredFourierPane = createTitledPaneWithDownloadButton("Отфильтрованное изображение", filteredFourierImageView);

        // Создание выпадающего списка для выбора типа фильтра
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("HighPassFilter", "GaussianLowPassFilter");
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Установка выбранного фильтра
            if (newValue.equals("HighPassFilter")) {
                filter = new HighPassFilter(D0);
            } else if (newValue.equals("GaussianLowPassFilter")) {
                filter = new GaussianLowPassFilter(M, N, D0);
            }
        });

        // Создание текстового поля для ввода порогового значения D0
        TextField D0Field = new TextField();
        D0Field.textProperty().addListener((observable, oldValue, newValue) -> {
            // Установка введенного значения D0
            D0 = Double.parseDouble(newValue);
            if (filter instanceof HighPassFilter) {
                filter = new HighPassFilter(D0);
            } else if (filter instanceof GaussianLowPassFilter) {
                filter = new GaussianLowPassFilter(M, N, D0);
            }
        });

        // Создание кнопки для загрузки изображения
        Button loadButton = new Button("Загрузить");
        loadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                // Загрузка и отображение выбранного изображения
                Image image = new Image(file.toURI().toString());
                originalImageView.setImage(image);
                setImagePath(file.getAbsolutePath());

                // Сохранение имени файла
                fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
            }
        });

        // Создание кнопки для применения преобразования Фурье
        Button applyButton = new Button("Применить Фурье");
        applyButton.setOnAction(e -> {
            long startTime = System.currentTimeMillis();
            // Выполнение преобразования Фурье
            execute(false);
            execute(true);
            updateImages();

            // Создание маски фильтра и отображение ее на пользовательском интерфейсе
            double[][] filterMask = FilterMaskUtils.createFilterMask(M, N, D0, filter instanceof HighPassFilter);
            WritableImage filterMaskImage = createImageFromMask(filterMask); // Создайте изображение из маски фильтра
            filterMaskImageView.setImage(filterMaskImage);

            // Вывод времени выполнения преобразования
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
            System.out.println("Время выполнения: " + duration + " милисекунд");
        });

        // Создание группы радиокнопок для выбора цветового канала
        ToggleGroup group = new ToggleGroup();
        rButton.setToggleGroup(group);
        gButton.setToggleGroup(group);
        bButton.setToggleGroup(group);
        rgbButton.setToggleGroup(group);

        // Установка выбранного цветового канала
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (group.getSelectedToggle() != null) {
                setChannel(((RadioButton) newValue).getText());
            }
        });

        // Создание горизонтальных контейнеров для изображений
        HBox topBox = new HBox(10);
        topBox.getChildren().addAll(originalPane, spectrumPane, fourierPane); // Добавьте filterMaskPane здесь

        HBox bottomBox = new HBox(10);
        bottomBox.getChildren().addAll(filterMaskPane, filteredSpectrumPane, filteredFourierPane);

        // Создание вертикального контейнера для элементов управления
        VBox controlPanel = new VBox();
        controlPanel.getChildren().addAll(loadButton, applyButton, rButton, gButton, bButton, rgbButton, filterComboBox, D0Field, energyLabel);

        // Создание вертикального контейнера для изображений
        VBox vBoxImages = new VBox(10);
        vBoxImages.getChildren().addAll(topBox, bottomBox);

        // Создание главного горизонтального контейнера
        HBox hBoxMain = new HBox(10);
        hBoxMain.getChildren().addAll(controlPanel, vBoxImages);

        // Создание сцены и установка ее на главное окно
        Scene second_scene = new Scene(hBoxMain, 1000, 600);
        primaryStage.setScene(second_scene);
        primaryStage.show();
    }

    // Метод для обновления изображений на пользовательском интерфейсе
    private void updateImages() {
        // Загрузка и отображение изображений спектра и файла
        spectrumImageView.setImage(new Image("file:" + getSpectrumPath()));
        fourierImageView.setImage(new Image("file:" + getFilePath()));

        // Загрузка и отображение отфильтрованных изображений спектра и файла
        filteredSpectrumImageView.setImage(new Image("file:" + getFilteredSpectrumPath()));
        filteredFourierImageView.setImage(new Image("file:" + getFilteredFilePath()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}






