package com.example.gesturelock.util;

public class LoginAction {

    private static boolean mLoginFlag;

    private LoginAction() { /**/ }

    public static boolean isUserLogin() {
        return mLoginFlag;
    }

    public static void login(LoginListener listener) {
        mLoginFlag = true;
        if (listener != null) {
            listener.onLogin();
        }
    }

    public static void logout(LogoutListener listener) {
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