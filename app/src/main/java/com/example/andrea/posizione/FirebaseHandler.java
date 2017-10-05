package com.example.andrea.posizione;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.andrea.posizione.model.Posto;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by riccardomaldini on 05/10/17.
 * Classe per la gestione del collegamento firebase
 */
public class FirebaseHandler implements OnCompleteListener<AuthResult>{

    // riferimenti agli attributi per il collegamento al DB
    private FirebaseDatabase mFirebaseDB;
    private FirebaseAuth mFireAuth;

    // riferimento a MainActivity
    private MainActivity mMainActivity;

    // costanti utili
    private static final String TAG = MainActivity.class.getName();
    private static final String TAG_POSTI = "posti";


    /**
     * Costruttore con il quale vengono inizializzati i vari componenti
     */
    public FirebaseHandler(MainActivity a) {
        mMainActivity = a;

        // inizializza il db firebase
        mFirebaseDB = FirebaseDatabase.getInstance();
        mFireAuth = FirebaseAuth.getInstance();

        mFireAuth.signInWithEmailAndPassword("piattaforme@gmail.com", "piattaforme101")
                .addOnCompleteListener(mMainActivity, this);

    }


    /**
     * Metodo eseguito al termine dell'autenticazione
     */
    @Override
    public void onComplete(@NonNull Task task) {
            if (task.isSuccessful()) {
                // mostra un messaggio all'utente
                Log.d(TAG, "signInWithEmail:success");
                Toast.makeText(mMainActivity, "Connesso al DB Firebase! L'app è pronta a inviare posizioni.",
                        Toast.LENGTH_LONG).show();

                // aggiorna l'interfaccia rendendo disponibili le varie azioni
                mMainActivity.showFab();

            } else {
                // mostra un messaggio all'utente
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                Toast.makeText(mMainActivity, "Non sei connesso al Database. Non pioi effettuare operazioni.",
                        Toast.LENGTH_LONG).show();

                // nascondi i fab, in quanto senza autenticazione non si può
                // intraprendere nessuna azione
                mMainActivity.hideFab();
            }

        mMainActivity.hideProgressBar();
    }


    /**
     * Metodo per ottenere l'eventuale utente autenticato tramite firebase
     */
    public FirebaseUser getFirebaseUser() {
        return mFireAuth.getCurrentUser();
    }


    /**
     * Metodo per l'invio di una singola posizione al DB remoto
     */
    public void sendLocationToFirebase(LatLng location) {
        if (location != null && getFirebaseUser() != null) {
            Posto posto = new Posto(location.latitude, location.longitude);
            mFirebaseDB.getReference("/" + TAG_POSTI).push().setValue(posto);

        } else {
            Toast.makeText(mMainActivity, "Nessuna posizione selezionata.", Toast.LENGTH_SHORT).show();
        }
    }
}
