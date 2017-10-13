package com.example.andrea.posizione;

import android.os.AsyncTask;

/**
 * Created by andrea on 12/10/17.
 */

public abstract class AsyncTask2<T, V, K> extends AsyncTask<T, V, K> {

    private Asyntask2Callback<T, V, K> _callback;

    public AsyncTask2()
    {
        _callback = null;
    }

    public AsyncTask2(Asyntask2Callback<T, V, K> callback)
    {
        _callback = callback;
    }

    @Override
    protected void onPostExecute(K k) {
        super.onPostExecute(k);
        if(_callback != null)
            _callback.OnComplete(k);
    }

    public Asyntask2Callback<T, V, K> getCallback() {
        return _callback;
    }

    public void setCallback(Asyntask2Callback<T, V, K> callback) {
        _callback = callback;
    }

    public interface Asyntask2Callback<T, V, K>
    {
        void OnComplete(K result);
    }
}