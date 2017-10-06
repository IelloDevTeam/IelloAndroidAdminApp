package com.example.andrea.posizione.UI;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.example.andrea.posizione.UI.utilities.AsyncRicercaIndirizzo;
import com.example.andrea.posizione.UI.utilities.FirebaseHandler;
import com.example.andrea.posizione.UI.utilities.MappaGoogle;
import com.example.andrea.posizione.R;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;


/**
 * Created by TeamPiattaforme on 25/09/17
 * L'activity principale dell'applicazione. Implementa le principali funzioni del programma.
 */
public class MainActivity extends AppCompatActivity {

    // riferimento a elementi d'interfaccia
    private FrameLayout mProgressBar;
    private EditText mEditIndirizzo;
    private FabSpeedDial mMultiFabButton;

    // istanza del gestore del collegamento a DB Firebase
    private FirebaseHandler _firebaseHandler;

    // istanza del gestore della mappa
    private MappaGoogle _mappa;



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

        // inizializza il gestore della mappa
        _mappa = new MappaGoogle(this);

        // inizializza gestore del collegamento Firebase
        _firebaseHandler = new FirebaseHandler(this);

        // inizializza i fab button
        mMultiFabButton = findViewById(R.id.customFab);
        mMultiFabButton.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.action_send_location: {
                        // invio della propria posizione al DB Firebase
                        _mappa.inviaPosizioneGeolocalizzata();
                        break;
                    }

                    case R.id.action_send_marker: {
                        // invio al DB Firebase dei marker selezionati in mappa
                        _mappa.inviaPosizioneMarkers();
                        break;
                    }

                    case R.id.action_elimina_marker: {
                        // eliminazione di tutti i marker dalla mappa
                        _mappa.eliminaTuttiMarkers();
                    }
                }
                return false;
            }
        });

        // inizializzazione vari elementi di interfaccia
        mProgressBar = findViewById(R.id.clippedProgressBar);
        mEditIndirizzo = findViewById(R.id.editIndirizzo);
        FloatingActionButton fabSearch = findViewById(R.id.fabSearch);

        // al click sulla lente d'ingrandimento presente sulla tastiera, avvia la ricerca
        // dell'indirizzo
        mEditIndirizzo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = v.getText().toString();
                    AsyncRicercaIndirizzo searchAddr = new AsyncRicercaIndirizzo(MainActivity.this, query);
                    searchAddr.execute();
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
                AsyncRicercaIndirizzo searchAddr = new AsyncRicercaIndirizzo(MainActivity.this, query);
                searchAddr.execute();
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(_firebaseHandler.getFirebaseUser() == null) {
            hideFab();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        _mappa.attivaPermessoGeolocalizzazione(requestCode);
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
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        if(mProgressBar != null)
            mProgressBar.setVisibility(View.GONE);
    }


    public void showFab() {
        if(mMultiFabButton != null)
        mMultiFabButton.show();
    }


    public void hideFab() {
        if(mMultiFabButton != null) {
            mMultiFabButton.hide();
        }
    }

    // todo crea metodi toast e snackbar
    public void creaToast(String testo) {
        Toast.makeText(this, testo, Toast.LENGTH_SHORT).show();
    }


    public void creaSnackbar(String testo) {
        Snackbar.make(findViewById(R.id.coordinator), testo, Snackbar.LENGTH_LONG).show();

    }



    public MappaGoogle getMappa() {
        return _mappa;
    }

    public FirebaseHandler getFireHandler() {
        return _firebaseHandler;
    }

}
