package org.example.monitoringtemperatures;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import jssc.SerialPortException;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class FXML_Controller implements Initializable {
    @FXML
    NumberAxis xAxis;
    @FXML
    NumberAxis yAxis;
    @FXML
    Button RAZ;
    @FXML
    CheckBox checkBox_Continu;
    @FXML
    LineChart<Number, Number> lineChart;
    @FXML
    Label label_Valeur;
    @FXML
    Label cursorCoords;
    Series<Number, Number> series;
    String port = "COM10";
    boolean stop = false;
    SimpleDateFormat simpleDateFormat;
    DecimalFormat df = new DecimalFormat("0.##");
    Timer timer;
    CapteurTemperature capteurTemperature;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        series = new XYChart.Series<>();
        lineChart.setTitle("Graphique");
        lineChart.setCursor(Cursor.CROSSHAIR);
        series.setName("Capteur Z56");
        capteurTemperature = new CapteurTemperature();
        checkBox_Continu.setSelected(false);
        TimerTask updateTask = new TimerTask() {
            int iteration = 0;
            @Override
            public void run() {
                if (!stop) {
                    Platform.runLater(() -> {
                        try {
                            capteurTemperature.configureLiaisonSerieCapteur(port);
                            capteurTemperature.ecrire("IEEE".getBytes(StandardCharsets.US_ASCII));
                            capteurTemperature.ecrire("\r".getBytes(StandardCharsets.US_ASCII));
                            Thread.sleep(1000);
                            series.getData().add(new XYChart.Data<>(iteration, capteurTemperature.getValeurLue()));
                            label_Valeur.setText("Value received: " + df.format(capteurTemperature.getValeurLue()));
                            capteurTemperature.fermerLiaisonSerieCapteur();
                            iteration += 5;
                        } catch (SerialPortException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        };

        lineChart.getData().add(series);

        timer = new Timer(true);
        timer.scheduleAtFixedRate(updateTask, 0, 5000);

        RAZ.setOnAction(event -> series.getData().clear());

        final Node chartBackground = lineChart.lookup(".chart-plot-background");

        for (Node n: chartBackground.getParent().getChildrenUnmodifiable()) {
            n.setMouseTransparent(n != chartBackground);
        }

        chartBackground.setOnMouseEntered(event -> cursorCoords.setVisible(true));
        chartBackground.setOnMouseExited(event -> cursorCoords.setVisible(false));
        chartBackground.setOnMouseMoved(event ->  cursorCoords.setText("Time: " + df.format(xAxis.getValueForDisplay(event.getX())) + "  °C: " + df.format(yAxis.getValueForDisplay(event.getY()))));

        checkBox_Continu.setOnAction(event -> stop = checkBox_Continu.isSelected());
    }
    private void base_De_Temps() {
    }
}