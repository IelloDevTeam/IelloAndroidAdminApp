package com.example.andrea.posizione.utilities;

import android.os.AsyncTask;

import com.example.andrea.posizione.R;
import com.example.andrea.posizione.UI.activities.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andrea on 11/10/17.
 * Il metodo è un particolare AsyncTask che permette di utilizzate la funzione di reverse geocoding
 * di Google Maps API per trovare l'indirizzo di una coordinata settata sulla mappa. Una volta
 * trovato l'indirizzo, può essere settato esternamente del codice da eseguire per inviare la
 * posizione a Firebase DB.
 */

public class AsyncInviaPosizione extends AsyncTask<Void, Void, String> {

    private MainActivity mMainActivity;
    private LatLng mCoordRicerca;
    private SenderCallback mSender;

    // costanti return
    private static final String ERRORE = "ERRORE";
    private static final String NO_INTERNET = "NO_INTERNET";



    public interface SenderCallback {
        void sendToFirebase(String result);
    }


    public AsyncInviaPosizione(MainActivity mainActivity, LatLng coordRicerca, SenderCallback sender) {
        mMainActivity = mainActivity;
        mCoordRicerca = coordRicerca;
        mSender = sender;
    }


    /**
     * In background viene effettuata la richiesta all'API Google Geocoding.
     */
    @Override
    protected String doInBackground(Void... voids) {
        if (HelperRete.isNetworkAvailable(mMainActivity)) {
            // viene creato l'url da utilizzare per la richiesta a GoogleApi.
            String url = "https://maps.google.com/maps/api/geocode/json" +
                    "?latlng=" + mCoordRicerca.latitude
                    + "," + mCoordRicerca.longitude
                    + "&key=" + mMainActivity.getString(R.string.google_geoc_key);

            // viene avviata la ricerca
            JSONObject obj = HelperRete.volleySyncRequest(mMainActivity, url);

            // viene restituito il risultato, se disponibile
            if (obj != null && obj.has("results")) {
                try {
                    JSONArray results = obj.getJSONArray("results");
                    if (results.length() > 0)
                        return results.getJSONObject(0).getString("formatted_address");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return ERRORE;

        } else {
            return NO_INTERNET;
        }
    }



    @Override
    protected void onPostExecute(String address) {
        switch (address) {
            case NO_INTERNET: {
                mMainActivity.creaToast(R.string.no_internet);
                break;
            }
            case ERRORE: {
                mMainActivity.creaToast(R.string.errore_invio_posizione);
                break;
            }
            default: {
                mSender.sendToFirebase(address);
                break;
            }
        }
    }
}
