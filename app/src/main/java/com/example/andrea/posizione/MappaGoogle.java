package com.example.andrea.posizione;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

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
public class MappaGoogle implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener  {

    // riferimento alla mappa fornita da GoogleMaps API
    private GoogleMap mMappa;

    // lista per la memorizzazione dei marker presenti nella mappa
    private List<Marker> mMarkerList = new ArrayList<>();

    // memorizza la disponibilità del permesso di geolocalizzazione
    private boolean mGeoPermessoDisponibile;

    // riferimento all'activity Main
    private MainActivity mMainActivity;

    // riferimento all'istanza di gestione collegamento FireBase
    private FirebaseHandler mFirbaseHandler;

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
        mFirbaseHandler = mMainActivity.getFireHandler();

        // inizializza la mappa
        final SupportMapFragment mapFragment = (SupportMapFragment) a.getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
     * centrate la mappa nella propria posizione, oltre ad impostare il boolean di disp. geolocalizz.
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
        mMappa.setOnMapLongClickListener(this);
        mMappa.setOnMapClickListener(this);
        mMappa.setOnMarkerClickListener(this);
        mMappa.moveCamera(CameraUpdateFactory.newLatLngZoom(COORD_INIZIALI, 15.0f));

        if (mGeoPermessoDisponibile)
            try{
                mMappa.setMyLocationEnabled(true);
            } catch (SecurityException ex)
            {
                ex.printStackTrace();
            }
    }



    @Override
    public void onMapLongClick(LatLng latLng) {
        eliminaTuttiMarkers();
    }



    @Override
    public void onMapClick(LatLng latLng) {
        Marker newMarker = mMappa.addMarker(new MarkerOptions().draggable(false).position(latLng));
        newMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        mMarkerList.add(newMarker);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //_markerLocation = marker;
        //_markerLocation.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        //_markerLocation.remove();
        marker.remove();
        mMarkerList.remove(marker);
        return false;
    }

    public void eliminaTuttiMarkers() {
        mMappa.clear();
        mMarkerList.clear();
        Toast.makeText(mMainActivity, "Tutti i markers eliminati.", Toast.LENGTH_SHORT).show();
    }


    public void muoviMappaConAnimazione(LatLng posizione) {
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(posizione, 16);
        mMappa.animateCamera(location);
    }


    public void inviaPosizioneGeolocalizzata() {
        if(mGeoPermessoDisponibile)
            try
            {
                mLocationProvider
                        .getLastLocation()
                        .addOnSuccessListener(mMainActivity, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if(location != null) {
                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    mFirbaseHandler.sendLocationToFirebase(latLng);

                                    Toast.makeText(mMainActivity, "Posizione inviata:\n" +
                                                    "Lat: " + latLng.latitude + "\n" +
                                                    "Lng: " + latLng.longitude,
                                            Toast.LENGTH_LONG).show();

                                    Snackbar.make(mMainActivity.findViewById(R.id.coordinator), "Posizione attuale inviata", Snackbar.LENGTH_LONG).show();

                                } else {
                                    Toast.makeText(mMainActivity, "Attiva la geolocalizzazione.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } catch (SecurityException ex)
            {
                ex.printStackTrace();
                Toast.makeText(mMainActivity, "Errore: " + ex, Toast.LENGTH_LONG).show();
            }
    }



    public void inviaPosizioneMarkers() {
        if(mMarkerList.size() > 0)
        {
            for(Marker m : mMarkerList) {
                mFirbaseHandler.sendLocationToFirebase(m.getPosition());
                m.remove();
            }

            mMarkerList.clear();

            Snackbar.make(mMainActivity.findViewById(R.id.coordinator), "Posizione markers inviata", Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(mMainActivity, "Nessun marker selezionato.",Toast.LENGTH_SHORT).show();
        }
    }
}
