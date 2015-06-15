package com.example.gesturelock.util;

import android.content.Context;
import android.widget.Toast;

import com.example.gesturelock.R;

/**
 * Created by Wang Gensheng on 2015/4/28.
 */
public class LoginAction {

    private Context mContext;

    private static LoginAction mInstance;

    private boolean mLoginFlag;

    private LoginAction(Context context) {
        mContext = context;
    }

    public static LoginAction getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LoginAction(context);
        }
        return mInstance;
    }

    public boolean isUserLogin() {
        return mLoginFlag;
    }

    public void login(LoginListener listener) {
        mLoginFlag = true;
        if (listener != null) {
            listener.onLogin();
        }
    }

    public void logout(LogoutListener listener) {
        mLoginFlag = false;
        if (listener != null) {
            listener.onLogout();
        }
    }

    public interface LogoutListener {
        void onLogout();
    }

    public interface LoginListener {
        void onLogin();
    }
}