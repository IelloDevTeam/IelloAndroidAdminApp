package com.example.andrea.posizione.UI.utilities;

import android.os.AsyncTask;
import android.util.Log;
import com.example.andrea.posizione.R;
import com.example.andrea.posizione.UI.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by riccardomaldini on 05/10/17.
 * Sposta la mappa a seconda dell'indirizzo selezionato dall'utente.
 */
public class AsyncRicercaPerIndirizzo extends AsyncTask<Void, LatLng, LatLng> {
    private String mQuery;
    private MainActivity mMainActivity;

    public AsyncRicercaPerIndirizzo(MainActivity a, String query) {
        mMainActivity = a;
        mQuery = query;
    }

    @Override
    protected void onPreExecute() {
        if(HelperRete.isNetworkAvailable(mMainActivity))
            mMainActivity.getProgHandler().setSearchingInd(true);
    }

    @Override
    protected LatLng doInBackground(Void... voids) {
        if(HelperRete.isNetworkAvailable(mMainActivity)) {

            String queryFormattata = mQuery.replaceAll(" ", "+" + "");
            String url = "https://maps.google.com/maps/api/geocode/json" +
                    "?address=" + queryFormattata + "&key=" + mMainActivity.getString(R.string.google_geoc_key);

            JSONObject response = HelperRete.volleySyncRequest(mMainActivity, url);

            // ottieni le coordinate dell'indirizzo tramite la risposta di GoogleApi
            try {
                if (response != null) {
                    Log.i("jsonresp", response.toString());

                    double lng = ((JSONArray) response.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lng");

                    double lat = ((JSONArray) response.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lat");

                    return new LatLng(lat, lng);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    @Override
    protected void onPostExecute(LatLng response) {

       if (response != null) {
           mMainActivity.getMappa().muoviMappaConAnimazione(response);
           mMainActivity.getMappa().poniMarkerProvvisorio(response);
           AsyncDownloadParcheggi adp = new AsyncDownloadParcheggi(mMainActivity, response);
           adp.execute();

       } else {
           mMainActivity.creaToast(R.string.no_indirizzo);
       }

        mMainActivity.getProgHandler().setSearchingInd(false);
    }
}
