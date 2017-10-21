package com.example.andrea.posizione.UI.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.example.andrea.posizione.UI.MainActivity;
import com.example.andrea.posizione.UI.parcheggiScaricati.ElencoParcheggi;
import com.example.andrea.posizione.UI.parcheggiScaricati.Parcheggio;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Project iello-admin-app
 * Created by Petreti Andrea on 21/10/17.
 */

public class AsyncSendLocation extends AsyncTask<LatLng, Void, Boolean> {

    private static final String URL = "http://cloudpi.webhop.me:4000/iello/v1/parking";

    private Context mContext;

    public AsyncSendLocation(Context context) {
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(LatLng... latLngs) {

        if (HelperRete.isNetworkAvailable(mContext)) {

            JSONObject response = makeRequest(latLngs[0]);

            if(response != null)
            {
                // conversione dell'oggetto JSON in oggetto Java
                try {
                    String status = response.getString("status");

                    return status.equalsIgnoreCase("OK");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private JSONObject makeRequest(LatLng latLng) {

        // configurazione della webRequest
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JSONObject body = new JSONObject();
        try {
                /* Creazione corpo richiesta */
            body.put("latitude", latLng.latitude);
            body.put("longitude", latLng.longitude);

                /* Creazione richiesta */
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, body, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                }

            }, null) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-type", "application/json");
                    headers.put("Accept", "application/json");
                    headers.put("Authorization", "0fd33a1b-0a9e-5c60-90d2-d438fde963f2");
                    return headers;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(mContext);
            requestQueue.add(request);

            return future.get(10, TimeUnit.SECONDS);

        } catch (InterruptedException | ExecutionException | TimeoutException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
