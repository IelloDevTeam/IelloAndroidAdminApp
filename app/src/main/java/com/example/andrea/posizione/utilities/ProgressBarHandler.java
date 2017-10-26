package com.example.andrea.posizione.utilities;

import android.view.View;
import android.widget.FrameLayout;

import com.example.andrea.posizione.R;
import com.example.andrea.posizione.UI.activities.MainActivity;

/**
 * Created by riccardomaldini on 15/10/17.
 * La progressBar della mainActivity viene utilizzata per mostrare l'attesa di processi multipli.
 * Questa classe permette di gestire in modo centralizzato e semplificato la progressBar, per
 * evitare che venga nascosta al momento sbagliato. In poche parole, la progressBar viene nascosta
 * in questo modo solo nel momento in cui non ci sono processi in background attivi.
 */

public class ProgressBarHandler {
    private FrameLayout mProgressBar;

    // boolean che permettono di stabilire se le tre tipologie di processi background
    // (collegamento firebase, ricerca indirizzo, download parcheggi) sono attivi
    private boolean mDownloadingPark, mSearchingByInd;

    public ProgressBarHandler(MainActivity mainActivity) {
        mProgressBar = mainActivity.findViewById(R.id.clippedProgressBar);

        mDownloadingPark = mSearchingByInd = false;
    }


    private void showProgressBar() {
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.VISIBLE);
    }


    private void hideProgressBar() {
        if(mProgressBar != null)
            mProgressBar.setVisibility(View.GONE);
    }


    void setDownloadingPark(boolean isRunning) {
        if (isRunning) {
            mDownloadingPark = true;
            showProgressBar();
        } else {
            mDownloadingPark = false;
            if(!mSearchingByInd)
                hideProgressBar();
        }
    }


    void setSearchingInd(boolean isRunning) {
        if (isRunning) {
            mSearchingByInd = true;
            showProgressBar();
        } else {
            mSearchingByInd = false;
            if(!mDownloadingPark)
                hideProgressBar();
        }
    }
}
