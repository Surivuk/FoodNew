package com.example.aleksandarx.foodfinder.share;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by aleksandarx on 6/30/16.
 */
public class UserPreferences {
    private static String PREFERENCE_NAME = "USER_INFO";
    public static String PREFERENCE_DEFAULT_ERROR = "VALUE NOT FOUND";

    public static String USER_USERNAME = "USER_USERNAME";
    public static String USER_PASSWORD = "USER_PASSWORD";
    public static String USER_ID = "USER_ID";


    public static boolean setPreference(Context activityContext, String key, String value){
        SharedPreferences sharedpreferences = activityContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getPreference(Context activityContext, String key){
        SharedPreferences sharedpreferences = activityContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(key, PREFERENCE_DEFAULT_ERROR);
    }

    public static void removePreference(Context activityContext, String key){
        SharedPreferences sharedpreferences = activityContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(key);
        editor.commit();
    }
}
