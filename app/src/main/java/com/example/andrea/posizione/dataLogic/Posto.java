package com.example.andrea.posizione.dataLogic;

/**
 * Created by andrea on 23/09/17.
 */

public class Posto {

    private double _latitudine;
    private double _longitudine;

    public Posto() {}

    public Posto(double latitudine, double longitudine) {
        _latitudine = latitudine;
        _longitudine = longitudine;
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

