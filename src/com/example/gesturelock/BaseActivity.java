package com.example.gesturelock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.gesturelock.util.Foreground;
import com.example.gesturelock.util.GestureLockHelper;

public class BaseActivity extends Activity implements Foreground.Listener {

    private GestureLockHelper lockHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lockHelper = GestureLockHelper.getInstance(getApplicationContext());
    }

    @Override
    public void onBecameForeground() {
        if (GestureLockHelper.isLockModeOn() && lockHelper.isLocked()) {
            startActivity(new Intent(this, GestureUnlockActivity.class));
            GestureLockHelper.getInstance(getApplicationContext()).errorOccursOn(GestureLockHelper.LOCK_OCCURS_ON_FORGROUND);
        }
    }

    @Override
    public void onBecameBackground() {
        if (GestureLockHelper.isLockModeOn()) {
            lockHelper.traceOn();
        }
    }
}
