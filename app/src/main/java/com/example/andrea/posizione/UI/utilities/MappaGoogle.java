package com.example.andrea.posizione.UI.utilities;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.example.andrea.posizione.R;
import com.example.andrea.posizione.UI.MainActivity;
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
    private List<Marker> mMarkerList = new ArrayList<>();

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
    private static final LatLng COORD_INIZIALI = new LatLng(43.7262568, 12.6365634);

    // costante utile
    private static final int PERMISSION_CODE = 100;



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
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(mMarkerProvvisorio)) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            mMarkerProvvisorio = null;
            mMarkerList.add(marker);

        } else {
            marker.remove();
            mMarkerList.remove(marker);
        }

        mMainActivity.modificaTxtMarkerDaCaricare(mMarkerList.size());

        return false;
    }


    void poniMarkerProvvisorio(LatLng posizione) {
        if (mMarkerProvvisorio != null)
            mMarkerProvvisorio.remove();

        mMarkerProvvisorio
                = mMappa.addMarker(new MarkerOptions().draggable(false).position(posizione));
    }


    /**
     * Metodo per eliminare tutti i marker dalla mappa, oltre ai relativi riferimenti
     */
    public void eliminaTuttiMarkers() {
        mMappa.clear();
        mMarkerList.clear();
        mMarkerProvvisorio = null;

        mMainActivity.modificaTxtMarkerDaCaricare(mMarkerList.size());
        mMainActivity.creaToast(R.string.markers_eliminati);
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
            mMainActivity.showProgressBar();

            try {
                mLocationProvider
                        .getLastLocation()
                        .addOnSuccessListener(mMainActivity, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMainActivity.getFireHandler().sendLocationToFirebase(latLng);
                                    mMainActivity.creaSnackbar(R.string.posizione_inviata);

                                } else {
                                    mMainActivity.creaToast(R.string.attivare_gps);
                                }
                            }
                        });
            } catch (SecurityException ex) {
                ex.printStackTrace();
            }

            mMainActivity.hideProgressBar();
        }
    }


    /**
     * Metodo per gestire l'invio dei markers al DB
     */
    public void inviaPosizioneMarkers() {
        if(mMarkerList.size() > 0) {
            mMainActivity.showProgressBar();

            for(Marker m : mMarkerList) {
                mMainActivity.getFireHandler().sendLocationToFirebase(m.getPosition());
                m.remove();
            }
            mMarkerList.clear();

            mMainActivity.modificaTxtMarkerDaCaricare(mMarkerList.size());
            mMainActivity.creaSnackbar(R.string.posizione_markers_inviata);
            mMainActivity.hideProgressBar();
        } else {
            mMainActivity.creaToast(R.string.markers_non_selezionati);
        }
    }
}
