package com.example.andrea.posizione.UI.utilities;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.example.andrea.posizione.R;
import com.example.andrea.posizione.UI.MainActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by riccardomaldini on 05/10/17.
 * Classe per la gestione del collegamento al DB Firebase remoto del progetto.
 */
public class APIHandler {

    // riferimento a MainActivity
    private MainActivity mMainActivity;

    // costanti utili
    private static final String TAG = "FireHandler";
    private static final String TAG_POSTI = "posti";

    public interface APICallback
    {
        void OnResult(boolean isError, JSONObject jsonObject);
    }

    /**
     * Costruttore con il quale vengono inizializzati i vari componenti
     */
    public APIHandler(MainActivity a) {
        // inizializza il riferimento all'activity
        mMainActivity = a;

        mMainActivity.showFab();
    }


    /**
     * Metodo per l'invio di una singola posizione al DB remoto
     */
    void sendLocation(final LatLng location, @NonNull APICallback apiCallback) {

        // TODO: Terminare invio posizione
        if (location != null) {

            AsyncSendLocation sendLocation = new AsyncSendLocation(mMainActivity);
            sendLocation.execute(location);

            /*
            AsyncInviaPosizione asyncInviaPosizioni = new AsyncInviaPosizione(mMainActivity, location,
                    new AsyncInviaPosizione.SenderCallback() {
                        @Override
                        public void sendToFirebase(String address) {
                            // imposta il codice da eseguire una volta trovato l'indirizzo.
                            // In questo caso, invia la posizione a Firebase.
                            HashMap<String, Object> posto = new HashMap<>();
                            posto.put("latitudine", location.latitude);
                            posto.put("longitudine", location.longitude);
                            posto.put("street_address", address);
                            mFirebaseDB.getReference("/" + TAG_POSTI).push().setValue(posto);

                        }
                    });
            asyncInviaPosizioni.execute();*/
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
        if(HelperRete.isNetworkAvailable(mMainActivity)) {
            // TODO: cancella posizione tramite API
            JsonObjectRequest volleyRequest = new JsonObjectRequest(Request.Method.DELETE, "http://192.168.1.110:4000/iello/v1/parking/" + id, null, new Response.Listener<JSONObject>() {
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
                    Map<String, String> params = new HashMap<>();
                    params.put("Accept", "application/json");
                    params.put("Authorization", "0fd33a1b-0a9e-5c60-90d2-d438fde963f2");
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(mMainActivity);
            requestQueue.add(volleyRequest);
        }
    }
}
