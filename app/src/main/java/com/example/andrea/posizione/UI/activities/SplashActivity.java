package com.example.andrea.posizione.UI.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by Riccardo Maldini on 05/02/2017.
 * Questa classe implementa una splash screen nel modo giusto, ovvero senza mostrare una view
 * */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
    }

}
