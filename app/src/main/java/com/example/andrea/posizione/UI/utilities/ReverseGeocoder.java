package com.example.andrea.posizione.UI.utilities;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.example.andrea.posizione.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by andrea on 11/10/17.
 * Il metodo è un particolare AsyncTask che permette di utilizzate la funzione di reverse geocoding
 * di Google Maps API per trovare l'indirizzo di una coordinata settata sulla mappa. Una volta
 * trovato l'indirizzo, può essere settato esternamente del codice da eseguire dopo l'esecuzione
 * di tale codice.
 */

public class ReverseGeocoder extends AsyncTask<Void, Void, String> {

    private Context mContext;
    private LatLng mCoordRicerca;
    private AsyncCallback mCallback;


    public interface AsyncCallback {
        void onComplete(String result);
    }


    public void setOnCompleteCallback(AsyncCallback callback) {
        mCallback = callback;
    }

    public ReverseGeocoder(Context context, LatLng coordRicerca) {
        mContext = context;
        mCoordRicerca = coordRicerca;
    }


    /**
     * In background viene effettuata la richiesta all'API Google Geocoding.
     */
    @Override
    protected String doInBackground(Void... voids) {
        // viene creato l'url da utilizzare per la richiesta a GoogleApi.
        String url = "https://maps.google.com/maps/api/geocode/json" +
                "?latlng=" + mCoordRicerca.latitude
                + "," + mCoordRicerca.longitude
                + "&key=" + mContext.getString(R.string.google_geoc_key);

        // viene avviata la ricerca
        JSONObject obj = HelperRete.volleySyncRequest(mContext, url);

        // viene restituito il risultato, se disponibile
        if(obj != null && obj.has("results")) {
            try {
                JSONArray results = obj.getJSONArray("results");
                if(results.length() > 0)
                    return results.getJSONObject(0).getString("formatted_address");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }



    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mCallback != null)
        mCallback.onComplete(s);
    }
}
