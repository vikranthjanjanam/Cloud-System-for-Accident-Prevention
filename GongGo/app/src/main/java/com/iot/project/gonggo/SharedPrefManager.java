package com.iot.project.gonggo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by My pc on 3/4/2018.
 */

public class SharedPrefManager {

    private static SharedPrefManager mInstance;
    private static Context mContext;

    private static final String SHARED_PREF_NAME = "mysharedprefgonggo";
    private static final String KEY_USER_MOBILE = "usermobile";
    private static final String KEY_USER_NAME = "username";
    private static final String KEY_USER_VEHICLE = "uservehicle";

    private SharedPrefManager(Context context){
        mContext = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context){
        if(mInstance == null){
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    public boolean userLogin(String mobile, String vehicle, String name){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_MOBILE, mobile);
        editor.putString(KEY_USER_VEHICLE, vehicle);

        editor.apply();

        return true;
    }

    public boolean isLoggedin(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if(sharedPreferences.getString(KEY_USER_VEHICLE, null) != null){
            return true;
        }
        return false;
    }

    public boolean logout(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();

        return true;
    }

    public String getKeyUserVehicle(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_VEHICLE, null);
    }

}
