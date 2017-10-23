package com.example.andrea.posizione.UI.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andrea.posizione.UI.dialogs.DialogAPIKey;
import com.example.andrea.posizione.utilities.AsyncDownloadParcheggi;
import com.example.andrea.posizione.utilities.AsyncRicercaPerIndirizzo;
import com.example.andrea.posizione.utilities.APIHandler;
import com.example.andrea.posizione.utilities.MappaGoogle;
import com.example.andrea.posizione.R;
import com.example.andrea.posizione.utilities.ProgressBarHandler;
import com.example.andrea.posizione.utilities.SharedPrefsHelper;
import com.google.android.gms.maps.model.LatLng;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;


/**
 * Created by TeamPiattaforme on 25/09/17
 * L'activity principale dell'applicazione. Implementa le principali funzioni del programma.
 */
public class MainActivity extends AppCompatActivity implements DialogAPIKey.DialogAPIKeyCallback{

    // riferimento a elementi d'interfaccia
    private EditText mEditIndirizzo;
    private TextView mTxtMarkerSelezionati, mTxtMarkerSospeso, mTxtMarkerPresenti;
    private FabSpeedDial mMultiFabButton;

    // istanza del gestore del collegamento a DB Firebase
    private APIHandler mAPIHandler;

    // istanza del gestore della mappa
    private MappaGoogle mMappa;

    // istanza del gestore della progBar
    private ProgressBarHandler mProgHandler;

    // compatibilità immagini vettoriali android pre-lollipop
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inizializza vari elementi base dell'interfaccia utente
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setTitle(R.string.app_name);

        // mantiene lo schermo acceso durante l'utilizzo dell'app
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // inizializza il gestore della progressBar
        mProgHandler = new ProgressBarHandler(this);

        // inizializza il gestore della mappa
        mMappa = new MappaGoogle(this);

        // inizializza gestore del collegamento Firebase
        mAPIHandler = new APIHandler(this);


        // inizializza i fab button
        mMultiFabButton = findViewById(R.id.customFab);
        mMultiFabButton.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.action_send_location: {
                        // invio della propria posizione al DB Firebase
                        AlertDialog.Builder alertInvia = new AlertDialog.Builder(MainActivity.this);
                        alertInvia.setMessage(R.string.invia_geolocalizzaz_desc);
                        alertInvia.setPositiveButton(R.string.si_invia, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mMappa.inviaPosizioneGeolocalizzata();
                            }
                        });
                        alertInvia.setNegativeButton(R.string.no, null);

                        AlertDialog alert = alertInvia.create();
                        alert.show();

                        break;
                    }

                    case R.id.action_send_marker: {
                        // invio al DB Firebase dei marker selezionati in mappa
                        mMappa.inviaPosizioneMarkers();
                        break;
                    }

                    case R.id.action_elimina_marker: {
                        // eliminazione di tutti i marker dalla mappa
                        mMappa.eliminaTuttiMarkers();
                        break;
                    }

                    case R.id.action_parcheggi_presenti: {
                        LatLng coordCorrenti = mMappa.getPosizioneAttualeMappa();
                        AsyncDownloadParcheggi adp
                                = new AsyncDownloadParcheggi(MainActivity.this, coordCorrenti);
                        adp.execute();
                    }
                }
                return false;
            }
        });

        // inizializzazione vari elementi di interfaccia
        mEditIndirizzo = findViewById(R.id.editIndirizzo);
        mTxtMarkerSelezionati = findViewById(R.id.txtMarkerSelezionati);
        mTxtMarkerSospeso = findViewById(R.id.txtMarkerInSospeso);
        mTxtMarkerPresenti = findViewById(R.id.txtMarkerPresenti);
        FloatingActionButton fabSearch = findViewById(R.id.fabSearch);

        // al click sulla lente d'ingrandimento presente sulla tastiera, avvia la ricerca
        // dell'indirizzo
        mEditIndirizzo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                String query = v.getText().toString();
                    AsyncRicercaPerIndirizzo searchAddr
                            = new AsyncRicercaPerIndirizzo(MainActivity.this, query);
                    searchAddr.execute();

                    // nasconde la tastiera
                    View viewC = getCurrentFocus();
                    if (viewC != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(viewC.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });

        // stessa cosa va fatta al click sul fabSearch
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = mEditIndirizzo.getText().toString();
                AsyncRicercaPerIndirizzo searchAddr
                        = new AsyncRicercaPerIndirizzo(MainActivity.this, query);
                searchAddr.execute();

                // nasconde la tastiera
                View viewC = getCurrentFocus();
                if (viewC != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(viewC.getWindowToken(), 0);
                }
            }
        });


        // Verico presenza di api key
        if(!SharedPrefsHelper.getInstance().isApiKeyRegistered(this))
            requestApiKey();
        else
            startParkingDownload();

        // inizializza la casella di testo dei markers
        modificaTxtMarkerDaCaricare(0);
        modificaTxtMarkerInSospeso(false);
        modificaTxtMarkerPresenti(0);

        showFab();
    }

    private void startParkingDownload()
    {
        // avvia una prima ricerca dei markers presenti, se ho già inserito la api key.
        AsyncDownloadParcheggi adp = new AsyncDownloadParcheggi(this, MappaGoogle.COORD_INIZIALI);
        adp.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mMappa.attivaPermessoGeolocalizzazione(requestCode);
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
                AlertDialog.Builder alertHelp = new AlertDialog.Builder(this);

                alertHelp.setTitle(R.string.help_title);
                alertHelp.setIcon(R.drawable.ic_help_black);
                alertHelp.setMessage(getString(R.string.help));
                alertHelp.setPositiveButton(R.string.fantastico, null);

                AlertDialog alert = alertHelp.create();
                alert.show();
                break;
            case R.id.action_api_key:
                requestApiKey();
                break;
        }

        return super.onOptionsItemSelected(item);
    }




    /*
     * Metodi per accedere a elementi dell'interfaccia dell'esterno della classe.
     */

    public void showFab() {
        if(mMultiFabButton != null)
            mMultiFabButton.show();
    }


    public void creaToast(int resTesto) {
        Toast.makeText(this, resTesto, Toast.LENGTH_SHORT).show();
    }


    public void modificaTxtMarkerDaCaricare(int numMarkers) {
        String testoTxtView = "" + numMarkers + " " + getString(R.string.marker_selezionati);
        mTxtMarkerSelezionati.setText(testoTxtView);
    }


    public void modificaTxtMarkerPresenti(int numMarkers) {
        String testoTxtView = "" + numMarkers + " " + getString(R.string.marker_gi_presenti_in_zona);
        mTxtMarkerPresenti.setText(testoTxtView);
    }


    public void modificaTxtMarkerInSospeso(boolean inSospeso) {
        String testoTxtView;
        if(inSospeso)
            testoTxtView = "1" + " " + getString(R.string.marker_in_sospeso);
        else {
            testoTxtView = "0" + " " + getString(R.string.marker_in_sospeso);
        }
        mTxtMarkerSospeso.setText(testoTxtView);
    }

    public ProgressBarHandler getProgHandler() {
        return mProgHandler;
    }

    public MappaGoogle getMappa() {
        return mMappa;
    }

    public APIHandler getAPIHandler() {
        return mAPIHandler;
    }

    /**
     * Richiede inserimento di una chiave tramite dialog
     */
    private void requestApiKey()
    {
        DialogAPIKey.newInstance().show(getSupportFragmentManager(), null);
    }

    // Invocata dal dialog API key, quando viene inserita una nuova key valida.
    @Override
    public void APIKeyChange(String apiKey) {
        startParkingDownload();
    }
}
