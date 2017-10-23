package com.example.andrea.posizione.utilities;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Petreti Andrea on 22/10/17.
 * Handler per gestire le comunicazioni fondamentali con le API Iello
 */

// TODO: Migliorare controllo errori di autenticazione + varie migliorie
public class APIHandler extends ContextWrapper {

    private static final String TAG = "APIHandler";
    private static final String BASE_URL = "http://cloudpi.webhop.me:4000/iello/v1/parking/";

    public interface APICallback
    {
        void OnResult(boolean isError, JSONObject jsonObject);
        void OnAuthError();
    }

    public APIHandler(Context context) {
        super(context);
    }

    /**
     * Metodo per l'invio di una singola posizione al DB remoto
     */
    void sendLocation(final LatLng location, @NonNull final APICallback apiCallback) {
        // TODO: Terminare invio posizione
        if (location != null) {
            JSONObject body = new JSONObject();
            try {
                body.put("longitude", location.longitude);
                body.put("latitude", location.latitude);

                JsonObjectRequest volleyRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, body, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if(status.equals("Success"))
                                apiCallback.OnResult(false, response);
                            else
                                apiCallback.OnResult(true, response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && (networkResponse.statusCode == 500 || networkResponse.statusCode == 400)) {
                            // HTTP Status Code: 500 errore server di caricamento parcheggio
                            // 400 richesta malformata
                            apiCallback.OnResult(true, null);
                        }
                        Log.d(TAG, error.toString());
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> map = new HashMap<>();
                        map.put("Accept", "application/json");
                        map.put("Authorization", SharedPrefsHelper.getInstance().getApiKey(getApplicationContext()));
                        return map;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(volleyRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Errore inaspettato nell'invio della posizione");
        }
    }

    /**
     * Metodo cancellazione parcheggio sulla piattaforma
     * @param id Id del parcheggio
     * @param apiCallback callback
     */
    void deleteLocation(String id, @NonNull final APICallback apiCallback) {
        if(id != null && !id.isEmpty())
        {
            if(HelperRete.isNetworkAvailable(getApplicationContext())) {
                // TODO: cancella posizione tramite API
                JsonObjectRequest volleyRequest = new JsonObjectRequest(Request.Method.DELETE, BASE_URL + id, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("Success"))
                                apiCallback.OnResult(false, response);
                            else
                                apiCallback.OnResult(true, response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && (networkResponse.statusCode == 401 || networkResponse.statusCode == 403)) {
                            // HTTP Status Code: 401 Unauthorized, 403 Forbidden
                            System.out.println("aaaaa");
                            apiCallback.OnAuthError();
                        }
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> map = new HashMap<>();
                        map.put("Accept", "application/json");
                        map.put("Authorization", SharedPrefsHelper.getInstance().getApiKey(getApplicationContext()));
                        return map;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(volleyRequest);
            }
        }
    }

    // TODO: Terminare getLocation
    /*JsonObjectRequest getLocation(LatLng currentLocation, int radius, final APICallback apiCallback)
    {
        if(HelperRete.isNetworkAvailable(getApplicationContext()))
        {
            try
            {
                String url = BASE_URL +
                        "?latitude="    + currentLocation.latitude +
                        "&longitude="   + currentLocation.longitude;

                JsonObjectRequest volleyRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if(status.equals("OK"))
                                apiCallback.OnResult(false, response);
                            else
                                apiCallback.OnResult(true, response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, null){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return new HashMap<>();
                    }
                };

                volleyRequest.getHeaders().put("Accept", "application/json");
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(volleyRequest);

                return volleyRequest;

            } catch (AuthFailureError authFailureError) {
                authFailureError.printStackTrace();
            }
        }

        return null;
    }*/
}
