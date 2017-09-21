package com.example.andrea.posizione;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.andrea.posizione.model.Segnalazione;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

public class MainActivity extends AppCompatActivity implements OnSuccessListener<Location>,
        OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = MainActivity.class.getName();
    private FusedLocationProviderClient _fusedLocationProviderClient;
    private FirebaseDatabase _database;
    private FirebaseAuth _firebaseAuth;

    private FloatingActionButton _sendLocation,
                                _sendMarker;
    private static final String TAG_SEGNALAZIONI = "segnalazioni";
    private static final int PERMISSION_CODE = 100;
    private GoogleMap _map;
    private boolean _locationPermission;
    private Marker _markerLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        _sendLocation = findViewById(R.id.btn_send_location);
        _sendMarker = findViewById(R.id.btn_send_marker_location);

        _database = FirebaseDatabase.getInstance();
        _firebaseAuth = FirebaseAuth.getInstance();
        _fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_CODE);
            }
        } else _locationPermission = true;

        mapFragment.getMapAsync(this);

        _sendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        _sendMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_markerLocation != null)
                {
                    sendLocationToFirebase(_markerLocation.getPosition());
                    _markerLocation.remove();
                    _markerLocation = null;
                    Snackbar.make(findViewById(R.id.coordinator), "Posizione puntatore inviata", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        _firebaseAuth.signInWithEmailAndPassword("piattaforme@gmail.com", "piattaforme101")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = _firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = _firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        _sendLocation.setEnabled(user != null);
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
                Segnalazione segnalazione = new Segnalazione(_firebaseAuth.getCurrentUser().getUid(),
                        location.latitude,
                        location.longitude);

                _database.getReference("/" + TAG_SEGNALAZIONI)
                        .push()
                        .setValue(segnalazione);

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
}
