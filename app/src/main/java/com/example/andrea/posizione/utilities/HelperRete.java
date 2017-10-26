package com.example.andrea.posizione.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Created by riccardomaldini on 25/09/17.
 * Fornisce metodi utili per interagire con la rete ed ottenere lo stato di questa.
 */

class HelperRete {

    /**
     * Determina se internet è attivo
     */
    static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if(connectivityManager != null)
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    /**
     * Effettua una web request sincrona tramite Volley API, restituendo in risposta
     * l'oggetto JSON scaricato.
     */
    static JSONObject volleySyncRequest(Context c, String url) {

        // configurazione della webRequest
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(url, null, future, future);
        RequestQueue requestQueue = Volley.newRequestQueue(c);
        requestQueue.add(request);

        // esecuzione sincrona della webRequest
        try {
            // limita la richiesta bloccante a un massimo di 10 secondi, quindi restituisci
            // la risposta.
            return future.get(10, TimeUnit.SECONDS);

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }
}
