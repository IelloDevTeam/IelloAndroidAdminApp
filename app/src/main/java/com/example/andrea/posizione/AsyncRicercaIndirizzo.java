package com.example.andrea.posizione;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Created by riccardomaldini on 05/10/17.
 * AsyncTask strettamente collegato all'activity, quindi definito qua dentro. Sposta la mappa
 * a seconda dell'indirizzo selezionato dall'utente.
 */
public class AsyncRicercaIndirizzo extends AsyncTask<Void, JSONObject, JSONObject> {
    private String mQuery;
    private MainActivity mMainActivity;

    public AsyncRicercaIndirizzo(MainActivity a, String query) {
        mMainActivity = a;
        mQuery = query;
    }

    @Override
    protected void onPreExecute() {
        mMainActivity.showProgressBar();
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {

        String queryFormattata = mQuery.replaceAll(" ", "+" + "");
        String url = "https://maps.google.com/maps/api/geocode/json" +
                "?address=" + queryFormattata + "&key=" + mMainActivity.getString(R.string.google_geoc_key);

        return volleySyncRequest(url);
    }


    @Override
    protected void onPostExecute(JSONObject response) {

        // ottieni le coordinate dell'indirizzo tramite la risposta di GoogleApi
        try {
            Log.i("jsonresp", response.toString());

            double lng = ((JSONArray) response.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            double lat = ((JSONArray) response.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

            LatLng coordRicerca = new LatLng(lat, lng);
            mMainActivity.getMappa().muoviMappaConAnimazione(coordRicerca);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mMainActivity,
                    "Indirizzo non riconosciuto.", Toast.LENGTH_SHORT).show();
        }

        mMainActivity.hideProgressBar();
    }



    /**
     * Effettua una web request sincrona tramite Volley API, restituendo in risposta
     * l'oggetto JSON scaricato.
     */
    private JSONObject volleySyncRequest(String url) {

        // configurazione della webRequest
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(url, null, future, future);
        RequestQueue requestQueue = Volley.newRequestQueue(mMainActivity);
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
