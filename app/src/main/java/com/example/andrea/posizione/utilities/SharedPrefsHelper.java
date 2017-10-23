package com.example.andrea.posizione.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Project iello-admin-app
 * Created by Petreti Andrea on 22/10/17.
 */

public class SharedPrefsHelper {

    private static final String SHARED_PREFS = SharedPrefsHelper.class.getName() + ".shared_prefs";
    private static final String API_KEY = SharedPrefsHelper.class.getName() + ".api_key";

    private static SharedPrefsHelper _instance;

    public static SharedPrefsHelper getInstance()
    {
        if(_instance == null)
            _instance = new SharedPrefsHelper();
        return _instance;
    }

    public boolean isApiKeyRegistered(Context context){
        return getSharedPrefs(context).getString(API_KEY, null) != null;
    }

    public void registerApiKey(Context context, String apiKey)
    {
        SharedPreferences pref = getSharedPrefs(context);
        pref.edit().putString(API_KEY, apiKey).apply();
    }

    public void unregisterApiKey(Context context)
    {
        getSharedPrefs(context).edit().remove(API_KEY).apply();
    }

    public String getApiKey(Context context)
    {
        return getSharedPrefs(context).getString(API_KEY, null);
    }

    private SharedPreferences getSharedPrefs(Context context)
    {
        return context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
    }
}
