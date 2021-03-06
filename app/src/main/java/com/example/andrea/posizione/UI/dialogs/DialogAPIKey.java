package com.example.andrea.posizione.UI.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.andrea.posizione.R;
import com.example.andrea.posizione.utilities.SharedPrefsHelper;

/**
 * Project iello-admin-app
 * Created by Petreti Andrea on 23/10/17.
 * Classe per la gestione e creazione del dialog che consente di inserire-modificare la password
 * per la modifica dal database Firebase.
 */

public class DialogAPIKey extends AppCompatDialogFragment
        implements DialogInterface.OnClickListener, DialogInterface.OnShowListener {

    /**
     * Interfaccia per la comunicazione da parte del dialog
     */
    public interface DialogAPIKeyCallback {
        void APIKeyChange(String apiKey);
    }


    private TextInputLayout mTxtInput;
    private DialogAPIKeyCallback mCallback;


    public static DialogAPIKey newInstance() {
        return new DialogAPIKey();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // creazione 'manuale' del dialog
        int margin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

        LinearLayout rootView = new LinearLayout(getActivity());
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setPadding(margin, margin, margin, margin);
        mTxtInput = new TextInputLayout(getActivity());
        mTxtInput.addView(new EditText(getActivity()));
        mTxtInput.setHint(getString(R.string.api_key_title));
        if (mTxtInput.getEditText() != null) {
            mTxtInput.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mTxtInput.getEditText().setHighlightColor(Color.YELLOW);
        }

        rootView.addView(mTxtInput);

        // Setto l'edit text con il testo dell'api key se presente
        String apiKey = SharedPrefsHelper.getInstance().getApiKey(getActivity());
        if(apiKey != null)
            mTxtInput.getEditText().setText(apiKey);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.title_insert_api_key))
                .setView(rootView)
                //.setCancelable(false)
                .setPositiveButton(getString(R.string.salva), null)
                .setNegativeButton(SharedPrefsHelper.getInstance()
                        .isApiKeyRegistered(getActivity()) ? getString(R.string.annulla) : getString(R.string.esci_app), this)
                .create();

        dialog.setOnShowListener(this);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof DialogAPIKeyCallback)
            mCallback = (DialogAPIKeyCallback) context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }


    /*
    public void setAPIKeyChangeListener(DialogAPIKeyCallback callback) {
        mCallback = callback;
    }
    */

    private void notifyChange(String apiKey) {
        if(mCallback != null)
            mCallback.APIKeyChange(apiKey);
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which)
        {
            case AlertDialog.BUTTON_POSITIVE:
                break;

            case AlertDialog.BUTTON_NEGATIVE:
                // se era gia presente una key, allora chiudo semplicmente il dialog,
                // altrimenti significa che non è mai stata inserita alcuna chiave, quindi chiudo l'app
                if(!SharedPrefsHelper.getInstance().isApiKeyRegistered(getActivity()))
                    getActivity().finish();
                else
                    dismiss();
                break;
        }
    }


    @Override
    public void onShow(DialogInterface dialogInterface) {
        Button positiveButton = ((AlertDialog)dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validazione
                String apiKey = "apiKey";
                if (mTxtInput.getEditText() != null)
                    apiKey = mTxtInput.getEditText().getText().toString();

                if(apiKey.isEmpty() || apiKey.trim().isEmpty())
                    mTxtInput.setError(getString(R.string.invalid_api_key));
                else {
                    SharedPrefsHelper.getInstance().registerApiKey(getContext(), apiKey);
                    notifyChange(apiKey);
                    dismiss();
                }
            }
        });
    }
}
