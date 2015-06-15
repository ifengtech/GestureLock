package com.example.gesturelock.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefHelper {

    private SharedPreferences sp;

    private SharedPreferences.Editor       editor;

    private static final String PRES_NAME              = "gesture_lock";

    private static PrefHelper helper;

    public static final String KEY_FIRST_START = "first_start";
    public static final String KEY_GESTURE_PASSWD = "gesture_passwd";
    public static final String KEY_GESTURE_ERROR = "gesture_error";

    public static final String KEY_SAVE_LOGIN = "user_login";
    public static final String KEY_SAVE_USER_NAME = "user_name";

    private PrefHelper(Context context){
        sp = context.getSharedPreferences(PRES_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static PrefHelper getInstance(Context context){
        if(helper == null){
            helper = new PrefHelper(context.getApplicationContext());
        }

        return helper;
    }

    public void putStringValue(String key, String value){
        editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void putBooleanValue(String key, boolean value){
        editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void putIntValue(String key, int value){
        editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void putLongValue(String key, long value){
        editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public boolean getBooleanValue(String key){
        return sp.getBoolean(key, false);
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public String getStringValue(String key){
        return sp.getString(key, null);
    }

    public String getStringValue(String key, String defalutValue) {
        return sp.getString(key, defalutValue);
    }

    public int getIntValue(String key, int defaultValue){
        return sp.getInt(key, defaultValue);
    }

    public long getLongValue(String key, long defalutValue){
        return sp.getLong(key, defalutValue);
    }

    public void clearPreference(){
        editor = sp.edit();
        editor.clear();
        editor.commit();
    }
}
