package com.example.andrea.posizione.utilities;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.andrea.posizione.R;
import com.example.andrea.posizione.UI.activities.MainActivity;
import com.example.andrea.posizione.parcheggiScaricati.ElencoParcheggi;
import com.example.andrea.posizione.parcheggiScaricati.Parcheggio;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by riccardomaldini on 05/10/17.
 * Metodo che racchiude metodologie per interagire con la mappa fornita da GoogleMaps
 */
public class MappaGoogle implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener  {

    // riferimento alla mappa fornita da GoogleMaps API
    private GoogleMap mMappa;

    // lista per la memorizzazione dei marker pronti per l'invio presenti nella mappa
    private List<Marker> mMarkerListDaInviare = new ArrayList<>();

    // lista dei markers presenti nella mappa
    private List<Marker> mMarkerListPresenti = new ArrayList<>();

    // attributo per la memorizzazione del marker provvisorio, mostrato nella mappa ma non pronto
    // per essere inviato a firebase
    private Marker mMarkerProvvisorio = null;

    // memorizza la disponibilità del permesso di geolocalizzazione
    private boolean mGeoPermessoDisponibile;

    // riferimento all'activity Main
    private MainActivity mMainActivity;

    // attributo per accedere alla posizione dell'utente sfruttando GooglePlayServices
    private FusedLocationProviderClient mLocationProvider;

    // coordinate iniziale impostate su urbino di default
    public static final LatLng COORD_INIZIALI = new LatLng(43.7262568, 12.6365634);

    // costante utile
    private static final int PERMISSION_CODE = 100;

    // costanti utilizzate per distinguere la tipologia del marker
    private static final String M_PRESENTE = "M_PRESENTE";
    private static final String M_PROVVISORIO = "M_PROVVISORIO";
    private static final String M_INVIARE = "M_INVIARE";



    /**
     * Costruttore della classe. Inizializza i componenti della mappa.
     */
    public MappaGoogle(MainActivity a) {
        mMainActivity = a;

        // inizializza la mappa
        final SupportMapFragment mapFragment =
                (SupportMapFragment) a.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // verifica se si dispone dell'autorizzazione alla posizione, ed eventualmente la chiede.
        // Per le versioni di android precedenti a M, il permesso è confermato automaticamente
        if (ActivityCompat.checkSelfPermission(mMainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
         && ActivityCompat.checkSelfPermission(mMainActivity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
         && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                mMainActivity.requestPermissions(new String[]
                                {android.Manifest.permission.ACCESS_FINE_LOCATION,
                                 android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_CODE);

        } else {
            mGeoPermessoDisponibile = true;
        }

        // inizializza i servizi di localizzazione google
        mLocationProvider = LocationServices.getFusedLocationProviderClient(mMainActivity);
    }


    /**
     * Metodo richiamato dalla MainActivity al momento dell'ottenimento del permesso di
     * geolocalizzazione, tramite l'autorizzazione manuale dell'utente. Imposta il button per
     * centrare la mappa nella propria posizione, oltre ad impostare il boolean di controllo
     */
    public void attivaPermessoGeolocalizzazione(int requestCode) {
        if (requestCode == PERMISSION_CODE) {
            if (ActivityCompat.checkSelfPermission(mMainActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
             || ActivityCompat.checkSelfPermission(mMainActivity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mGeoPermessoDisponibile = true;
                if (mMappa != null)
                    mMappa.setMyLocationEnabled(true);
            }
        }
    }


    /**
     * Quando la mappa è pronta, vengono impostate le principali proprietà della mappa
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMappa = googleMap;
        mMappa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMappa.getUiSettings().setMyLocationButtonEnabled(true);
        mMappa.setMinZoomPreference(5);

        mMappa.setOnMapClickListener(this);
        mMappa.setOnMarkerClickListener(this);

        mMappa.moveCamera(CameraUpdateFactory.newLatLngZoom(COORD_INIZIALI, 15.0f));

        if (mGeoPermessoDisponibile)
            try {
                mMappa.setMyLocationEnabled(true);
            } catch (SecurityException ex) {
                ex.printStackTrace();
            }
    }


    /**
     * Al click sulla mappa, viene piazzato un marker provvisorio. Se è già presente un'altro marker
     * provvisorio nella mappa, questo viene rimosso
     */
    @Override
    public void onMapClick(LatLng latLng) {
        poniMarkerProvvisorio(latLng);
    }


    /**
     * Al click sul marker, se il marker selezionato è quello provvisorio, lo pone tra la lista di
     * invio. Altrimenti, lo elimina
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        // distingui la tipologia di marker
        String tipologia = tipologiaMarker(marker);

        switch (tipologia) {
            case M_PROVVISORIO: {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(52));
                mMarkerProvvisorio = null;
                mMarkerListDaInviare.add(marker);
                break;
            }
            case M_INVIARE: {
                marker.remove();
                mMarkerListDaInviare.remove(marker);
                break;
            }

            case M_PRESENTE: {
                    AlertDialog.Builder alertElimina = new AlertDialog.Builder(mMainActivity);

                    alertElimina.setTitle(R.string.title_elimina);
                    alertElimina.setMessage(R.string.desc_elimina);
                    alertElimina.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Recupero tramite Tag
                            final Parcheggio parcheggio = (Parcheggio) marker.getTag();
                            if(parcheggio != null)
                            {
                                mMainActivity.getAPIHandler().deleteLocation(parcheggio.getID(), new APIHandler.APICallback() {
                                    @Override
                                    public void onResult(boolean isError, JSONObject jsonObject) {
                                        if(!isError)
                                        {
                                            mMainActivity.creaToast(R.string.posto_eliminato);
                                            mMarkerListPresenti.remove(marker);
                                            ElencoParcheggi.getInstance().getListParcheggi().remove(parcheggio);
                                            mMainActivity.modificaTxtMarkerPresenti(mMarkerListPresenti.size());
                                            marker.remove();
                                        }
                                    }
                                    @Override
                                    public void onAuthError() {
                                        mMainActivity.creaToast(R.string.auth_error);
                                    }
                                });
                            }
                        }
                    });
                alertElimina.setNegativeButton(R.string.no, null);

                    AlertDialog alert = alertElimina.create();
                    alert.show();

                break;
            }
        }

        mMainActivity.modificaTxtMarkerDaCaricare(mMarkerListDaInviare.size());
        mMainActivity.modificaTxtMarkerInSospeso(false);

        // serve a gestire il comportamento di default della mappa:
        // - return true: non mostrare animazione predefinita google.
        // - return false: mostrala.
        return true;
    }


    /**
     * distingue la tipologia del marker passato in ingresso.
     */
    private String tipologiaMarker(Marker mDaTestare) {
        if (mDaTestare.equals(mMarkerProvvisorio))
            return M_PROVVISORIO;
        else {
            Marker result = null;

            for(Marker daInv : mMarkerListDaInviare) {
                if (mDaTestare.equals(daInv))
                    result = daInv;
            }

            if (result != null) {
                return M_INVIARE;
            }

            for(Marker pres : mMarkerListPresenti) {
                if (mDaTestare.equals(pres))
                    result = pres;
            }

            if (result != null) {
                return M_PRESENTE;
            }

            return M_PRESENTE;
        }
    }


    /**
     * Metodo per porre un marker provvisorio nella mappa. Il metodo viene invocato quando viene
     * premuto un punto sulla mappa, o quando viene spostata la mappa stessa tramite ricerca per
     * indirizzo.
     */
    void poniMarkerProvvisorio(LatLng posizione) {
        if (mMarkerProvvisorio != null)
            mMarkerProvvisorio.remove();

        mMarkerProvvisorio
                = mMappa.addMarker(new MarkerOptions().draggable(false).position(posizione));
        mMainActivity.modificaTxtMarkerInSospeso(true);
    }


    /**
     * Metodo per eliminare tutti i marker dalla mappa, oltre ai relativi riferimenti
     */
    public void eliminaTuttiMarkers() {
        if (mMarkerProvvisorio != null)
            mMarkerProvvisorio.remove();

        for(Marker m : mMarkerListDaInviare)
            m.remove();
        mMarkerListDaInviare.clear();
        mMarkerProvvisorio = null;

        mMainActivity.modificaTxtMarkerInSospeso(false);
        mMainActivity.modificaTxtMarkerDaCaricare(mMarkerListDaInviare.size());
        mMainActivity.creaToast(R.string.markers_eliminati);
    }


    /**
     * Elimina i markers 'verdi' e i loro riferimenti dalla mappa
     */
    void eliminaMarkersVerdi() {
        for (Marker m : mMarkerListPresenti)
            m.remove();
        mMarkerListPresenti.clear();

        mMainActivity.modificaTxtMarkerPresenti(0);
    }


    /**
     * Metodo per muovere la mappa in una determinata posizione
     */
    void muoviMappaConAnimazione(LatLng posizione) {
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(posizione, 16);
        mMappa.animateCamera(location);
    }


    /**
     * Metodo per gestire l'invio della propria posizione al DB
     */
    public void inviaPosizioneGeolocalizzata() {
        if(mGeoPermessoDisponibile) {

            try {
                mLocationProvider
                        .getLastLocation()
                        .addOnSuccessListener(mMainActivity, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMainActivity.getAPIHandler().sendLocation(latLng, new APIHandler.APICallback() {
                                        @Override
                                        public void onResult(boolean isError, JSONObject jsonObject) {
                                            if(isError)
                                                mMainActivity.creaToast(R.string.errore_invio_posizione);
                                            else
                                                mMainActivity.creaToast(R.string.posizione_inviata);
                                        }
                                        @Override
                                        public void onAuthError() {
                                            mMainActivity.creaToast(R.string.auth_error);
                                        }
                                    });
                                } else {
                                    mMainActivity.creaToast(R.string.attivare_gps);
                                }
                            }
                        });
            } catch (SecurityException ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * Metodo per gestire l'invio dei markers al DB
     */
    public void inviaPosizioneMarkers() {
        if(mMarkerListDaInviare.size() > 0) {

            // Rimozione marker provvisorio
            if(mMarkerProvvisorio != null)
                mMarkerProvvisorio.remove();
            mMarkerProvvisorio = null;

            for(final Marker m : mMarkerListDaInviare) {
                mMainActivity.getAPIHandler().sendLocation(m.getPosition(), new APIHandler.APICallback() {
                    @Override
                    public void onResult(boolean isError, JSONObject jsonObject) {
                        if(!isError)
                        {
                            m.remove();
                            // rimuovo dalla lista solo quelli che sono stati effetivamente caricati
                            // invece di pulire tutta la lista
                            mMarkerListDaInviare.remove(m);
                            mMainActivity.modificaTxtMarkerDaCaricare(mMarkerListDaInviare.size());
                            if(mMarkerListDaInviare.size() == 0)
                            {
                                mMainActivity.creaToast(R.string.posizione_markers_inviata);
                                mMainActivity.modificaTxtMarkerInSospeso(false);
                                // Al termine dell'upload ricarico la mappa
                                mMainActivity.downloadParking();
                            }
                        }
                    }
                    @Override
                    public void onAuthError() {
                        mMainActivity.creaToast(R.string.auth_error);
                    }
                });
            }
        } else {
            mMainActivity.creaToast(R.string.markers_non_selezionati);
        }
    }



    /**
     * Imposta un marker per ogni parcheggio già presente in zona
     */
    void settaMarkersGiaPresenti() {
        // rimuovi tutti i markers
        for(Marker m : mMarkerListPresenti)
            m.remove();
        mMarkerListPresenti.clear();

        // aggiungi un marker per ogni posizione
        for (Parcheggio p : ElencoParcheggi.getInstance().getListParcheggi()) {
            LatLng coordParcheggio = p.getCoordinate();

            Marker marker = mMappa.addMarker(new MarkerOptions()
                    .position(coordParcheggio)
                    .title(p.getIndirizzo())
                    .icon(BitmapDescriptorFactory.defaultMarker(138)));

            // Associo al marker un tag che corrisponde al parcheggio in questo modo posso
            // poi eliminarlo direttamente
            marker.setTag(p);

            mMarkerListPresenti.add(marker);
        }
        mMainActivity.modificaTxtMarkerPresenti(ElencoParcheggi.getInstance().getListParcheggi().size());
    }


    /**
     * Ricava le coordinate della posizione attuale della mappa.
     */
    public LatLng getPosizioneAttualeMappa() {
        return mMappa.getCameraPosition().target;
    }
}
