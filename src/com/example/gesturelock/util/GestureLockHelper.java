package com.example.gesturelock.util;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

/**
 * Created by Wang Gensheng on 2015/5/19.
 */
public class GestureLockHelper {

    private static GestureLockHelper helper;

    public static final boolean IS_LOCK_MODE_ON = true;

    public static final int ACCEPTED_ERROR_COUNT = 10;

    public static final int LOCK_THRESHOLD = 5 * 60 * 1000;

    public static final int LOCK_OCCURS_ON_BOOT = 1;

    public static final int LOCK_OCCURS_ON_FORGROUND = 2;

    public static final int LOCK_OCCURS_ON_LOGIN = 3;

    private int occursOn;

    private long mTimeTrace;

    private SharedPreferencesHelper sharedPreferencesHelper;

    private GestureLockHelper(Context context) {
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
    }

    public static GestureLockHelper getInstance(Context context) {
        if (!IS_LOCK_MODE_ON) {
            return null;
        }

        if (helper == null) {
            helper = new GestureLockHelper(context);
        }

        return helper;
    }

    public void traceOn() {
        mTimeTrace = SystemClock.elapsedRealtime();
    }

    public boolean isLocked() {
        String passwd = sharedPreferencesHelper.getStringValue(SharedPreferencesHelper.KEY_GESTURE_PASSWD);

        return IS_LOCK_MODE_ON && LoginAction.isUserLogin() && !TextUtils.isEmpty(passwd)
                && SystemClock.elapsedRealtime() - mTimeTrace > LOCK_THRESHOLD;
    }

    public static boolean isLockModeOn() {
        return IS_LOCK_MODE_ON;
    }

    /**
     * 手势输入错误
     * @return 返回还可以再输入的次数
     */
    public int errorHappens() {
        int errorCount = sharedPreferencesHelper.getIntValue(SharedPreferencesHelper.KEY_GESTURE_ERROR, 0);
        errorCount++;
        sharedPreferencesHelper.putIntValue(SharedPreferencesHelper.KEY_GESTURE_ERROR, errorCount);
        return ACCEPTED_ERROR_COUNT - errorCount;
    }

    public String getGesturePasswd() {
        return sharedPreferencesHelper.getStringValue(SharedPreferencesHelper.KEY_GESTURE_PASSWD);
    }

    public void setGesturePasswd(String passwd) {
        sharedPreferencesHelper.putStringValue(SharedPreferencesHelper.KEY_GESTURE_PASSWD, passwd);
    }

    public boolean isSet() {
        return !TextUtils.isEmpty(sharedPreferencesHelper.getStringValue(SharedPreferencesHelper.KEY_GESTURE_PASSWD));
    }

    public void reset() {
        sharedPreferencesHelper.putStringValue(SharedPreferencesHelper.KEY_GESTURE_PASSWD, "");
        sharedPreferencesHelper.putIntValue(SharedPreferencesHelper.KEY_GESTURE_ERROR, 0);
    }

    public void errorReset() {
        sharedPreferencesHelper.putIntValue(SharedPreferencesHelper.KEY_GESTURE_ERROR, 0);
    }

    public void errorOccursOn(int occurOn) {
        this.occursOn = occurOn;
    }

    public int getOccurence() {
        return this.occursOn;
    }
}
