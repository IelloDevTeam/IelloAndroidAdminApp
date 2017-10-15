package com.example.andrea.posizione.UI.parcheggiScaricati;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by riccardomaldini on 25/09/17.
 * Singleton che memorizza a tempo di esecuzione i parcheggi scaricati da IelloApi.
 */

public class ElencoParcheggi {

    private static final ElencoParcheggi ourInstance = new ElencoParcheggi();

    public static ElencoParcheggi getInstance() {
        return ourInstance;
    }

    private ElencoParcheggi() {}

    // lista di memorizzazione dei parcheggi
    private List<Parcheggio> mListParcheggi = new ArrayList<>();

    /**
     * Restituisce la lista dei parcheggi memorizzata nel singleton.
     */
    public List<Parcheggio> getListParcheggi() {
        return mListParcheggi;
    }
}
