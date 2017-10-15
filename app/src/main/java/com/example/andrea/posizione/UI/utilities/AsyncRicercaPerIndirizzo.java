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
public class AsyncRicercaPerIndirizzo extends AsyncTask<Void, String, String> {
    private String mQuery;
    private MainActivity mMainActivity;
    private LatLng mCoordinateCercate;

    // costanti di return
    private static final String RICERCA_COMPLETATA = "RICERCA_COMPLETATA";
    private static final String NO_INTERNET = "NO_INTERNET";


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
    protected String doInBackground(Void... voids) {
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

                    mCoordinateCercate = new LatLng(lat, lng);

                    return RICERCA_COMPLETATA;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            return NO_INTERNET;
        }

        return null;
    }


    @Override
    protected void onPostExecute(String result) {

        switch(result) {
            case RICERCA_COMPLETATA: {
                    mMainActivity.getMappa().muoviMappaConAnimazione(mCoordinateCercate);
                    mMainActivity.getMappa().poniMarkerProvvisorio(mCoordinateCercate);
                    AsyncDownloadParcheggi adp = new AsyncDownloadParcheggi(mMainActivity, mCoordinateCercate);
                    adp.execute();
                break;
            }

            case NO_INTERNET: {
                mMainActivity.creaToast(R.string.no_internet);
                break;
            }

            default: {
                mMainActivity.creaToast(R.string.no_indirizzo);
                break;
            }
        }

        // nasconde la barra di ricerca se necessario
        mMainActivity.getProgHandler().setSearchingInd(false);
    }
}
