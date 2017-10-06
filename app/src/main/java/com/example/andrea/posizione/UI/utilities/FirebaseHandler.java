package com.example.andrea.posizione.UI.utilities;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.andrea.posizione.R;
import com.example.andrea.posizione.UI.MainActivity;
import com.example.andrea.posizione.dataLogic.Posto;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by riccardomaldini on 05/10/17.
 * Classe per la gestione del collegamento al DB Firebase remoto del progetto.
 */
public class FirebaseHandler implements OnCompleteListener<AuthResult>{

    // riferimenti agli attributi per il collegamento al DB
    private FirebaseDatabase mFirebaseDB;
    private FirebaseAuth mFireAuth;

    // riferimento a MainActivity
    private MainActivity mMainActivity;

    // costanti utili
    private static final String TAG = "FireHandler";
    private static final String TAG_POSTI = "posti";



    /**
     * Costruttore con il quale vengono inizializzati i vari componenti
     */
    public FirebaseHandler(MainActivity a) {
        // inizializza il riferimento all'activity
        mMainActivity = a;

        // inizializza il db firebase
        mFirebaseDB = FirebaseDatabase.getInstance();
        mFireAuth = FirebaseAuth.getInstance();
        mFireAuth.signInWithEmailAndPassword("piattaforme@gmail.com", "piattaforme101")
                .addOnCompleteListener(mMainActivity, this);
    }


    /**
     * Metodo eseguito al termine dell'autenticazione alla piattaforma Firebase
     */
    @Override
    public void onComplete(@NonNull Task task) {
            if (task.isSuccessful()) {
                // mostra un messaggio all'utente
                Log.d(TAG, "signInWithEmail:success");
                mMainActivity.creaToast(R.string.authentication_success);

                // aggiorna l'interfaccia rendendo disponibili le varie azioni
                mMainActivity.showFab();

            } else {
                // mostra un messaggio all'utente
                Log.d(TAG, "signInWithEmail:failure", task.getException());
                mMainActivity.creaToast(R.string.authentication_fail);

                // nascondi i fab, in quanto senza autenticazione non si può
                // intraprendere nessuna azione
                mMainActivity.hideFab();
            }

        mMainActivity.hideProgressBar();
    }


    /**
     * Metodo per ottenere l'eventuale utente autenticato tramite Firebase
     */
    public FirebaseUser getFirebaseUser() {
        return mFireAuth.getCurrentUser();
    }


    /**
     * Metodo per l'invio di una singola posizione al DB remoto
     */
    void sendLocationToFirebase(LatLng location) {
        if (location != null && getFirebaseUser() != null) {
            Posto posto = new Posto(location.latitude, location.longitude);
            mFirebaseDB.getReference("/" + TAG_POSTI).push().setValue(posto);
            Log.d(TAG, "Inviata nuova coordinata al DB, " + location.toString());

        } else {
            Log.d(TAG, "Errore inaspettato nell'invio della posizione");
        }
    }
}
