package com.example.andrea.posizione.dataLogic;

/**
 * Created by andrea on 21/09/17.
 * Classe modello rappresentante una segnalazione. Getter e setter sono necessari per l'integrazione
 * con firebase.
 */

public class Segnalazione {

    private String _userId;
    private double _latitudine;
    private double _longitudine;

    public Segnalazione() {}

    public Segnalazione(String userId, double latitudine, double longitudine) {
        _userId = userId;
        _latitudine = latitudine;
        _longitudine = longitudine;
    }

    public String getUserId() {
        return _userId;
    }

    public void setUserId(String userId) {
        _userId = userId;
    }

    public double getLatitudine() {
        return _latitudine;
    }

    public void setLatitudine(double latitudine) {
        _latitudine = latitudine;
    }

    public double getLongitudine() {
        return _longitudine;
    }

    public void setLongitudine(double longitudine) {
        _longitudine = longitudine;
    }
}
