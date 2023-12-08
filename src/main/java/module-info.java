module com.example.fourierlab2gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    requires org.bytedeco.fftw;
    requires javafx.swing;


    opens com.example.fourierlab2gui to javafx.fxml;
    exports com.example.fourierlab2gui;
}