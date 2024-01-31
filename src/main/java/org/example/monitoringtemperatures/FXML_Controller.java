package org.example.monitoringtemperatures;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import jssc.SerialPortException;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FXML_Controller implements Initializable {
    @FXML
    CategoryAxis xAxis;
    @FXML
    NumberAxis yAxis;
    @FXML
    Button RAZ;
    @FXML
    CheckBox checkBox_Continu;
    @FXML
    LineChart<String, Number> lineChart;
    @FXML
    Label label_Valeur;
    @FXML
    Label cursorCoords;
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    String port = "COM10";
    boolean stop = false;
    final int TAILLE_DE_FENETRE = 15;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    DecimalFormat df = new DecimalFormat("0.##");
    Timer timer;
    CapteurTemperature capteurTemperature;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lineChart.setAnimated(false);
        lineChart.setTitle("Graphique");
        lineChart.setCursor(Cursor.CROSSHAIR);
        series.setName("Capteur Z56");
        capteurTemperature = new CapteurTemperature();
        checkBox_Continu.setSelected(false);
        lineChart.getData().add(series);
        base_De_Temps();

        RAZ.setOnAction(event -> {
            lineChart.getData().clear();
            series.getData().clear();
            lineChart.getData().add(series);
        });

        final Node chartBackground = lineChart.lookup(".chart-plot-background");

        for (Node n: chartBackground.getParent().getChildrenUnmodifiable()) {
            n.setMouseTransparent(n != chartBackground);
        }

        chartBackground.setOnMouseEntered(event -> cursorCoords.setVisible(true));
        chartBackground.setOnMouseExited(event -> cursorCoords.setVisible(false));
        chartBackground.setOnMouseMoved(event ->  cursorCoords.setText("Time: " + xAxis.getValueForDisplay(event.getX()) + "  °C: " + df.format(yAxis.getValueForDisplay(event.getY()))));

        checkBox_Continu.setOnAction(event -> stop = checkBox_Continu.isSelected());
    }
    private void base_De_Temps() {
        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                if (!stop) {
                    Platform.runLater(() -> {
                        try {
                            capteurTemperature.configureLiaisonSerieCapteur(port);
                            capteurTemperature.ecrire("IEEE".getBytes(StandardCharsets.US_ASCII));
                            capteurTemperature.ecrire("\r".getBytes(StandardCharsets.US_ASCII));
                            Thread.sleep(1000);
                            series.getData().add(new XYChart.Data<>(simpleDateFormat.format(new Date()), capteurTemperature.getValeurLue()));
                            label_Valeur.setText("Value received: " + df.format(capteurTemperature.getValeurLue()));
                            if (series.getData().size() > TAILLE_DE_FENETRE) {
                                series.getData().remove(0);
                            }
                            capteurTemperature.fermerLiaisonSerieCapteur();
                        } catch (SerialPortException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        };
        timer = new Timer(true);
        timer.scheduleAtFixedRate(updateTask, 0, 5000);
    }
}