package com.example.gesturelock.util;

/**
 * Created by Wang Gensheng on 2015/4/28.
 */
public class LoginAction {

    private static boolean loginFlag = false;
    public static boolean isUserLogin() {
        // TODO
        return loginFlag;
    }
    public static void login() {
        loginFlag = true;
    }

    public static void logout() {
        loginFlag = false;
    }
}
