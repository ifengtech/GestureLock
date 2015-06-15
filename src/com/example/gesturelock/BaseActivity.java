package com.example.gesturelock;

import android.app.Activity;

import com.example.gesturelock.util.Foreground;

public class BaseActivity extends Activity implements Foreground.Listener {
    @Override
    public void onBecameForeground() {

    }

    @Override
    public void onBecameBackground() {

    }
}
