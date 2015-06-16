package com.example.gesturelock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.gesturelock.util.Foreground;
import com.example.gesturelock.util.GestureLockHelper;
import com.example.gesturelock.widget.GestureDetector;
import com.example.gesturelock.widget.GestureLock;
import com.example.gesturelock.widget.GestureLock.OnGestureEventListener;
import com.kyleduo.switchbutton.SwitchButton;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity {

	private GestureLockHelper lockHelper;

	@InjectView(R.id.btnTurnOn)	SwitchButton mLockSwitch;
	@InjectView(R.id.itemSetting)	View		mItemSetting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.inject(this);
		lockHelper = GestureLockHelper.getInstance(getApplicationContext());

		mLockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Intent intent = new Intent(MainActivity.this, GestureSettingActivity.class);
				if (isChecked) {
					intent.putExtra(GestureSettingActivity.KEY_ACTIVITY_STATE, GestureSettingActivity.ACTIVITY_STATE_VERIFY);
				} else {
					intent.putExtra(GestureSettingActivity.KEY_ACTIVITY_STATE, GestureSettingActivity.ACTIVITY_STATE_RESET);
				}
				startActivity(intent);
			}
		});
		mItemSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, GestureSettingActivity.class);
				intent.putExtra(GestureSettingActivity.KEY_ACTIVITY_STATE, GestureSettingActivity.ACTIVITY_STATE_VERIFY);
				startActivity(intent);
			}
		});
		if (lockHelper.isSet()) {
			mLockSwitch.setChecked(true, true);
			mItemSetting.setVisibility(View.VISIBLE);
		} else {
			mLockSwitch.setChecked(false);
			mItemSetting.setVisibility(View.GONE);
		}
	}
}
