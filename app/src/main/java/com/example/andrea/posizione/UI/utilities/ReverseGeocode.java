package com.example.andrea.posizione.UI.utilities;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.example.andrea.posizione.AsyncTask2;
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
 */

public class ReverseGeocode extends AsyncTask2<Double, Void, String> {

    private Context mContext;

    public ReverseGeocode(Context context)
    {
        mContext = context;
    }

    @Override
    protected String doInBackground(Double... latLng) {
        /* Su [0] latitudine e su [1] longitudine */
        String url = "https://maps.google.com/maps/api/geocode/json" +
                "?latlng=" + latLng[0] + "," + latLng[1] + "&key=" + mContext.getString(R.string.google_geoc_key);

        JSONObject obj = volleySyncRequest(url);
        String address = null;
        if(obj != null)
        {
            if(obj.has("results"))
            {
                try {
                    JSONArray results = obj.getJSONArray("results");
                    if(results.length() > 0){
                        address = results.getJSONObject(0).getString("formatted_address");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return address;
    }

    /**
     * Effettua una web request sincrona tramite Volley API, restituendo in risposta
     * l'oggetto JSON scaricato.
     */
    private JSONObject volleySyncRequest(String url) {

        // configurazione della webRequest
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(url, null, future, future);
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(request);

        // esecuzione sincrona della webRequest
        try {
            // limita la richiesta bloccante a un massimo di 10 secondi, quindi restituisci
            // la risposta.
            return future.get(10, TimeUnit.SECONDS);

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
