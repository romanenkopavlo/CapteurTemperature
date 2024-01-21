package org.example.monitoringtemperatures;

import jssc.SerialPortEvent;
import jssc.SerialPortException;

import java.text.DecimalFormat;

public class CapteurTemperature extends LiaisonSerie {
    private byte [] CHARS_START;
    private byte [] CHARS_START_IEEE;
    private final int STOP = 1;
    private final int PARITY = 0;
    private final int DATA = 8;
    private final int VITESSE = 9600;
    public int LONGUEUR_TRAME;
    public DecimalFormat df;
    public FXML_Controller fxml;
    public float valeurLue;

    public CapteurTemperature() {
    }
    public void fermerLiaisonSerieCapteur() {
        super.fermerPort();
    }
    public void configureLiaisonSerieCapteur(String portDeTravail) throws SerialPortException {
        super.initCom(portDeTravail);
        super.configurerParametres(VITESSE, DATA, PARITY, STOP);
    }
    public float decodageTrameCapteur(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        boolean first_byte = false;
        for (byte b: byteArray) {
            if (!first_byte) {
                sb.append((b & 0xff)).append(".");
            } else {
                sb.append((b & 0xff));
            }
            first_byte = true;
        }
        valeurLue = Float.parseFloat(String.valueOf(sb));
        return valeurLue;
    }
    public byte [] liretrameCapteurModeNormal() {
        return super.lireTrame(LONGUEUR_TRAME);
    }
    public float conversionTrameCapteur(byte[] byteArray) {
        valeurLue = BigEndian.fromArray(byteArray);
        return valeurLue;
    }
    public byte [] liretrameCaptuerModeIEEE() {
        return super.lireTrame(LONGUEUR_TRAME);
    }
    public float getValeurLue() {
        return valeurLue;
    }
    public void serialEvent(SerialPortEvent spe) {
        df = new DecimalFormat("0.##");
        super.serialEvent(spe);
        LONGUEUR_TRAME = spe.getEventValue();
        CHARS_START_IEEE = liretrameCaptuerModeIEEE();

        System.out.printf("""
                --- Reception ---
                %s Celsius
                %s Kelvin
                %n""", df.format(conversionTrameCapteur(CHARS_START_IEEE)), df.format(conversionTrameCapteur(CHARS_START_IEEE) + 273.15f));
    }
}