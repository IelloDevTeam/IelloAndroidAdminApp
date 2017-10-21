package com.example.andrea.posizione.UI.utilities;

import android.os.AsyncTask;

import com.example.andrea.posizione.R;
import com.example.andrea.posizione.UI.MainActivity;
import com.example.andrea.posizione.UI.parcheggiScaricati.ElencoParcheggi;
import com.example.andrea.posizione.UI.parcheggiScaricati.Parcheggio;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

/**
 * Created by riccardomaldini on 25/09/17.
 * AsyncTask che gestisce il download dei dati da IelloApi in modo asincrono, popolando il Singleton
 * ElencoParcheggi.
 */

public class AsyncDownloadParcheggi extends AsyncTask<Void, Void, String> {

    // riferimento alla MainActivity che consente di intervenire sull'interfaccia
    private MainActivity mMainActivity;

    // costanti di return
    private static final String RICERCA_COMPLETATA = "RICERCA_COMPLETATA";
    private static final String COMPLETATA_NO_RIS = "COMPLETATA_NO_RIS";
    private static final String NO_INTERNET = "NO_INTERNET";

    // dati sulla posizione intorno cui cercare
    private LatLng mCoordRicerca;


    /**
     * Costruttore con popolamento degli attributi.
     */
    public AsyncDownloadParcheggi(MainActivity activity, LatLng coordinateRicerca) {
        mMainActivity = activity;
        mCoordRicerca = coordinateRicerca;
    }


    /**
     * Mostra la barra di caricamento prima della ricerca, se possibile effettuarla
     */
    @Override
    protected void onPreExecute() {
        if(HelperRete.isNetworkAvailable(mMainActivity))
            mMainActivity.getProgHandler().setDownloadingPark(true);

        mMainActivity.getMappa().eliminaMarkersVerdi();
    }


    /**
     * Se possibile, si cercano i parcheggi nelle vicinanze tramite IelloApi.
     */
    @Override
    protected String doInBackground(Void... params) {

        if(HelperRete.isNetworkAvailable(mMainActivity)) {
            // scarica i parcheggi tramite l'Api
            popolaElencoParcheggi();

            // restituisce la corrispondente stringa risultato
            if(ElencoParcheggi.getInstance().getListParcheggi().size() == 0)
                return COMPLETATA_NO_RIS;

            return RICERCA_COMPLETATA;


        } else {
            return NO_INTERNET;
        }
    }


    /**
     * crea, popola e mostra il fragment parcheggi.
     */
    @Override
    protected void onPostExecute(String result) {
            switch (result) {
                case RICERCA_COMPLETATA:
                    mMainActivity.getMappa().settaMarkersGiaPresenti();
                    mMainActivity.creaToast(R.string.desc_parcheggi);
                    break;

                case COMPLETATA_NO_RIS:
                    mMainActivity.creaToast(R.string.no_parcheggi);
                    break;

                case NO_INTERNET:
                    mMainActivity.creaToast(R.string.no_internet);
                    break;
            }

            mMainActivity.getProgHandler().setDownloadingPark(false);
    }


    /**
     * Il metodo interroga l'API con dati relativi alle coordinate e al raggio di ricerca, quindi
     * restituisce il risultato in modo sincrono.
     */
    private void popolaElencoParcheggi() {

        ElencoParcheggi.getInstance().getListParcheggi().clear();

        // creazione URL
        String url = "http://192.168.1.110:4000/iello/v1/parking" +
                "?latitude="    + mCoordRicerca.latitude +
                "&longitude="   + mCoordRicerca.longitude;
                //"&radius=" + mRange;

        // interrogazione dell'Api
        JSONObject response = HelperRete.volleySyncRequest(mMainActivity, url);

        if (response == null)
            return;

        // conversione dell'oggetto JSON in oggetto Java
        try {
            String status = response.getString("status");

            if(status.equals("OK")) {
                JSONArray jArrayParcheggi = response.getJSONObject("message").getJSONArray("parking");

                ElencoParcheggi.getInstance().getListParcheggi().clear();

                for(int i = 0; i < jArrayParcheggi.length(); i++) {
                    Parcheggio newPark = new Parcheggio(jArrayParcheggi.getJSONObject(i));
                    ElencoParcheggi.getInstance().getListParcheggi().add(newPark);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
