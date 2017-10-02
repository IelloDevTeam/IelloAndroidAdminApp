package com.example.andrea.posizione;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andrea.posizione.model.Posto;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<Location>,
        OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = MainActivity.class.getName();
    private FusedLocationProviderClient _fusedLocationProviderClient;
    private FirebaseDatabase _database;
    private FirebaseAuth _firebaseAuth;

    // riferimento alla progressbar
    private FrameLayout _progBar;

    // riferimento agli altri elementi
    private EditText _editIndirizzo;


    private static final String TAG_POSTI = "posti";
    private static final int PERMISSION_CODE = 100;
    private GoogleMap _map;
    private boolean _locationPermission;
    private Marker _markerLocation;

    private FabSpeedDial _multiFabButton;

    // coordinate iniziale impostate su urbino di default
    public static LatLng COORD_INIZIALI = new LatLng(43.7262568, 12.6365634);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // mantiene lo schermo acceso durante l'utilizzo dell'app
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // inizializza la mappa
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            // actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.app_name);
        }

        // inizializza il db firebase
        _database = FirebaseDatabase.getInstance();
        _firebaseAuth = FirebaseAuth.getInstance();

        // inizializza i servizi di localizzazione google
        _fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // verifica se si dispone dell'autorizzazione alla posizione, ed eventualmente la chiede
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_CODE);
            }
        } else _locationPermission = true;


        // inizializza i fab button
        _multiFabButton = findViewById(R.id.customFab);
        _multiFabButton.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.action_send_location: {
                        if(_locationPermission)
                            try
                            {
                                _fusedLocationProviderClient
                                        .getLastLocation()
                                        .addOnSuccessListener(MainActivity.this, MainActivity.this);
                                Snackbar.make(findViewById(R.id.coordinator), "Posizione attuale inviata", Snackbar.LENGTH_LONG).show();
                            } catch (SecurityException ex)
                            {
                                ex.printStackTrace();
                            }
                        break;
                    }

                    case R.id.action_send_marker: {
                        if(_markerLocation != null)
                        {
                            sendLocationToFirebase(_markerLocation.getPosition());
                            _markerLocation.remove();
                            _markerLocation = null;
                            Snackbar.make(findViewById(R.id.coordinator), "Posizione markers inviata", Snackbar.LENGTH_LONG).show();
                        }
                        break;
                    }
                }
                return false;
            }
        });


        _firebaseAuth.signInWithEmailAndPassword("piattaforme@gmail.com", "piattaforme101")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");

                            Toast.makeText(MainActivity.this, "Connesso al DB Firebase! L'app è pronta a inviare posizioni.",
                                    Toast.LENGTH_LONG).show();

                            _multiFabButton.show();


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Non sei connesso al Database. Non pioi effettuare operazioni.",
                                    Toast.LENGTH_LONG).show();

                            // nascondi i fab, in quanto senza autenticazione non si può
                            // intraprendere nessuna azione
                            _multiFabButton.hide();

                        }
                        hideProgressBar();
                    }
                });


        _progBar = findViewById(R.id.clippedProgressBar);
        _editIndirizzo = findViewById(R.id.editIndirizzo);
        FloatingActionButton fabSearch = findViewById(R.id.fabSearch);


        _editIndirizzo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    AsyncRicercaRapidaIndirizzo rri = new AsyncRicercaRapidaIndirizzo();
                    rri.execute();
                    return true;
                }
                return false;
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncRicercaRapidaIndirizzo rri = new AsyncRicercaRapidaIndirizzo();
                rri.execute();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = _firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            _multiFabButton.hide();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                _locationPermission = true;
                if(_map != null)
                    _map.setMyLocationEnabled(true);
            }
        }
    }


    @Override
    public void onSuccess(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        sendLocationToFirebase(latLng);
    }


    private void sendLocationToFirebase(LatLng location)
    {
        if (location != null) {

            if (_firebaseAuth.getCurrentUser() != null) {
                Posto posto = new Posto(
                        location.latitude,
                        location.longitude);

                _database.getReference("/" + TAG_POSTI)
                        .push()
                        .setValue(posto);

                Toast.makeText(MainActivity.this,
                        location.latitude + " \n" + location.longitude,
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;
        _map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        _map.getUiSettings().setMyLocationButtonEnabled(true);
        _map.setMinZoomPreference(5);
        _map.setOnMapLongClickListener(this);
        _map.setOnMarkerClickListener(this);
        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(COORD_INIZIALI, 15.0f));

        if (_locationPermission)
        {
            try{
                _map.setMyLocationEnabled(true);
            } catch (SecurityException ex)
            {
                ex.printStackTrace();
            }
        }
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        _map.clear();
        _map.addMarker(new MarkerOptions().draggable(false).position(latLng));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        _markerLocation = marker;
        _markerLocation.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        return false;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // qua vanno gestiti i click sulle voci di menu
        switch(item.getItemId()) {

            case R.id.action_help:
                avviaDialogHelp();
                break;

            default:
                Log.i("tabbedMain", "errore click voci menu");
                break;
        }

        return super.onOptionsItemSelected(item);
    }




    private void avviaDialogHelp() {

        AlertDialog.Builder alertHelp = new AlertDialog.Builder(this);

        alertHelp.setTitle("Aiuto");
        alertHelp.setIcon(R.drawable.ic_help_outline_black_24dp);
        alertHelp.setMessage(getString(R.string.help));

        // button positivo: elimina partita dal singleton e aggiorna lista
        alertHelp.setPositiveButton("Fantastico!", null);

        AlertDialog alert = alertHelp.create();
        alert.show();

    }

      /*
     * Metodi per accedere alla progressBar, utilizzata come schermata di caricamento durante la
     * ricerca dei parcheggi.
     */

    public void showProgressBar() {
        _progBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        _progBar.setVisibility(View.GONE);
    }



    /**
     * AsyncTask strettamente collegato all'activity, quindi definito qua dentro. Sposta la mappa
     * a seconda dell'indirizzo selezionato dall'utente.
     */
    private class AsyncRicercaRapidaIndirizzo extends AsyncTask<Void, JSONObject, JSONObject> {
        private String mQuery;

        @Override
        protected void onPreExecute() {
            showProgressBar();
            mQuery = _editIndirizzo.getText().toString();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String queryFormattata = mQuery.replaceAll(" ", "+" + "");
            String url = "https://maps.google.com/maps/api/geocode/json" +
                    "?address=" + queryFormattata + "&key=" + getString(R.string.google_geoc_key);

            return HelperRete.volleySyncRequest(MainActivity.this, url);
        }


        @Override
        protected void onPostExecute(JSONObject response) {

            try {
                Log.i("jsonresp", response.toString());

                double lng = ((JSONArray) response.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                double lat = ((JSONArray) response.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                LatLng coordRicerca = new LatLng(lat, lng);

                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordRicerca, 16);
                _map.animateCamera(location);


            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this,
                        "Indirizzo non riconosciuto.", Toast.LENGTH_SHORT).show();
            }

            hideProgressBar();
        }
    }
}
