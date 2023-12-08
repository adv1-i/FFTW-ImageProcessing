package com.example.fourierlab2gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static org.bytedeco.fftw.global.fftw3.*;
import static java.lang.Math.*;

import org.bytedeco.fftw.global.fftw3;
import org.bytedeco.javacpp.*;

import java.util.ArrayList;
import java.util.List;

public class FFTW1D extends Application {

    static final int REAL = 0;
    static final int IMAG = 1;

    public static void main(String[] args) {
        Loader.load(org.bytedeco.fftw.global.fftw3.class);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("FFTW Example");

        NumberAxis xAxis1 = new NumberAxis();
        NumberAxis yAxis1 = new NumberAxis();
        LineChart<Integer, Double> lineChart1 = new LineChart(xAxis1, yAxis1);
        lineChart1.setTitle("Исходный сигнал");

        NumberAxis xAxis2 = new NumberAxis();
        NumberAxis yAxis2 = new NumberAxis();
        LineChart<Integer, Double> lineChart2 = new LineChart(xAxis2, yAxis2);
        lineChart2.setTitle("Спектр сигнала");

        NumberAxis xAxis3 = new NumberAxis();
        NumberAxis yAxis3 = new NumberAxis();
        LineChart<Integer, Double> lineChart3 = new LineChart(xAxis3, yAxis3);
        lineChart3.setTitle("Восстановленный сигнал");

        Button button = new Button("Calculate FFT");
        button.setOnAction(e -> {
            lineChart1.getData().clear();
            lineChart2.getData().clear();
            lineChart3.getData().clear();

            List<double[]> fftDataList = calculateFFT();
            double[] source_signal = fftDataList.get(0);
            double[] fftData2 = fftDataList.get(1);
            double[] fftData3 = fftDataList.get(2);

            XYChart.Series<Integer, Double> series1 = new XYChart.Series<>();
            XYChart.Series<Integer, Double> series2 = new XYChart.Series<>();
            XYChart.Series<Integer, Double> series3 = new XYChart.Series<>();

            for (int i = 0; i < source_signal.length; i++) {
                series1.getData().add(new XYChart.Data<>(i, source_signal[i]));
            }

            for (int i = 0; i < fftData2.length; i++) {
                series2.getData().add(new XYChart.Data<>(i, fftData2[i]));
            }

            for (int i = 0; i < fftData3.length; i++) {
                series3.getData().add(new XYChart.Data<>(i, fftData3[i]));
            }

            lineChart1.getData().add(series1);
            lineChart2.getData().add(series2);
            lineChart3.getData().add(series3);
        });


        VBox vbox = new VBox(button, lineChart1, lineChart2, lineChart3);
        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void multiplyOnImag(DoublePointer data, int length) {
        double[] r = new double[(int)data.capacity()];
        data.get(r);
        for (int i = 0; i < length; i++) {
            r[2 * i + REAL] *= degree(i);
        }
        data.put(r);
    }
    private static List<Double> getSpectrum(DoublePointer result, int length) {
        double[] r = new double[(int)result.capacity()];
        result.get(r);
        List<Double> spectrum = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            spectrum.add(sqrt(pow(r[2 * i + REAL], 2) +
                    pow(r[2 * i + IMAG], 2)));
        }
        return spectrum;
    }

    private static List<Double> getListFromDFTArray(DoublePointer result, int length) {
        double[] r = new double[(int)result.capacity()];
        result.get(r);
        List<Double> dft_array = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            dft_array.add(r[2 * i + REAL]);
        }
        return dft_array;
    }

    private static void getScale(DoublePointer result, int length) {
        double[] r = new double[(int)result.capacity()];
        result.get(r);
        for (int i = 0; i < length; i++) {
            r[2 * i + REAL] /= length;
        }
        result.put(r);
    }

    public static List<double[]> calculateFFT() {
        int N = 1024;

        DoublePointer Array_In = new DoublePointer(2 * N);
        DoublePointer Array_Out = new DoublePointer(2 * N);

        double[] s = new double[(int)Array_In.capacity()];

        for (int i = 0; i < N; i++) {
            if (i < 20) {
                s[2 * i + REAL] = 1;
            } else {
                s[2 * i + REAL] = 0;
            }
            s[2 * i + IMAG] = 0;
        }
        Array_In.put(s);

        double[] source_signal = new double[N];
        for (int i = 0; i < N; i++) {
            source_signal[i] = s[2 * i + REAL];
        }

        multiplyOnImag(Array_In, N);

        fftw3.fftw_plan plan = fftw3.fftw_plan_dft_1d(N, Array_In, Array_Out, fftw3.FFTW_FORWARD, (int)FFTW_ESTIMATE);
        fftw3.fftw_execute(plan);

        List<Double> spectrum = getSpectrum(Array_Out, N);

        double[] spectrum_array = new double[spectrum.size()];
        for (int i = 0; i < spectrum.size(); i++) {
            spectrum_array[i] = spectrum.get(i);
        }

        plan = fftw3.fftw_plan_dft_1d(N, Array_Out, Array_In, fftw3.FFTW_BACKWARD, (int)FFTW_ESTIMATE);
        fftw3.fftw_execute(plan);

        getScale(Array_In, N);

        multiplyOnImag(Array_In, N);

        List<Double> dftArray = getListFromDFTArray(Array_In, N);

        double[] dft_array = new double[dftArray.size()];
        for (int i = 0; i < dftArray.size(); i++) {
            dft_array[i] = dftArray.get(i);
        }

        fftw3.fftw_destroy_plan(plan);

        return List.of(source_signal, spectrum_array, dft_array);
    }

    public static float degree(int n) {
        return (1 - 2 * (n % 2));
    }
}

