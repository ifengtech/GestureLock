package com.example.gesturelock;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gesturelock.util.Foreground;
import com.example.gesturelock.util.GestureLockHelper;
import com.example.gesturelock.widget.GestureDetector;
import com.example.gesturelock.widget.GestureLock;
import com.example.gesturelock.widget.GestureLock.OnGestureEventListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity {

	@InjectView(R.id.btnTurnOn)	Button btnTurnOn;
	@InjectView(R.id.btnSetting)	Button btnSetting;

	private GestureLockHelper lockHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.inject(this);
		lockHelper = GestureLockHelper.getInstance(getApplicationContext());

		if (lockHelper.isSet()) {
			btnTurnOn.setVisibility(View.VISIBLE);
			btnSetting.setVisibility(View.VISIBLE);
		} else {
			btnSetting.setVisibility(View.GONE);
		}
	}
}
