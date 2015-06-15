package com.example.gesturelock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gesturelock.util.GestureLockHelper;
import com.example.gesturelock.util.LoginAction;
import com.example.gesturelock.util.PrefHelper;
import com.example.gesturelock.widget.GestureDetector;
import com.example.gesturelock.widget.GestureLock;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GestureSettingActivity extends Activity {

    public static final int COLOR_TRANSPARENT = 0x80000000;

    private static final int    REQUEST_RESET = 1;
    private static final int    RESULT_OK = 1;

    @InjectView(R.id.lyt_actionbar)
    View topBar;

    @InjectView(R.id.tv_top_title)
    TextView topTitle;
    @InjectView(R.id.iv_force_back)
    ImageView ivForceBack;

    @InjectView(R.id.lyt_gesture_indi)
    View lytGestureIndi;
    @InjectView(R.id.tv_input_origin)
    TextView tvInputOrigin;
//    @InjectView(R.id.tv_action_login_passwd)        TextView        actionLoginPasswd;


    @InjectView(R.id.gesture_lock)
    GestureLock gestureView;
    @InjectView(R.id.gesture_detector)
    GestureDetector gestureDetector;

    public static final String KEY_ACTIVITY_STATE = "activity_state";
    public static final int ACTIVITY_STATE_VERIFY = 1;
    public static final int ACTIVITY_STATE_RESET = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_setting);

        ButterKnife.inject(this);

        topBar.setBackgroundColor(COLOR_TRANSPARENT);

        Intent intent = getIntent();
        int activityState = intent.getIntExtra(KEY_ACTIVITY_STATE, ACTIVITY_STATE_RESET);
        if (activityState == ACTIVITY_STATE_VERIFY) {
            onStateVerify();
        } else if (activityState == ACTIVITY_STATE_RESET) {
            onStateReset();
        } else {
            throw new RuntimeException();
        }

        ivForceBack.setVisibility(View.VISIBLE);
        ivForceBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void onStateVerify() {
        topTitle.setText(R.string.verify_gesture_passwd);

        tvInputOrigin.setVisibility(View.VISIBLE);
//        actionLoginPasswd.setVisibility(View.VISIBLE);

        lytGestureIndi.setVisibility(View.GONE);
        PrefHelper helper = PrefHelper.getInstance(getApplicationContext());
        String passwd = helper.getStringValue(PrefHelper.KEY_GESTURE_PASSWD);
        gestureView.setSecurePatternCode(passwd);
        gestureView.setOnGestureEventListener(new GestureLock.OnGestureEventListener() {
            @Override
            public void OnGestureEvent(boolean match) {
                if (match) {
                    Intent intent = new Intent(GestureSettingActivity.this, GestureSettingActivity.class);
                    intent.putExtra(KEY_ACTIVITY_STATE, ACTIVITY_STATE_RESET);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, REQUEST_RESET);
                    finish();
                } else {
                    GestureLockHelper lockHelper = GestureLockHelper.getInstance(getApplicationContext());
                    int remaining = lockHelper.errorHappens();
                    if (remaining <= 0) {
                        lockHelper.reset();
                        forceRedirectLogin();
                    } else {
                        tvInputOrigin.setText(String.format(getString(R.string.passwd_error_count), remaining));
                        tvInputOrigin.setTextColor(0xffff0000);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESET) {
            if (resultCode == RESULT_OK) {
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void forceRedirectLogin() {
        LoginAction.logout(new LoginAction.LogoutListener() {
            @Override
            public void onLogout() {
                Toast.makeText(getApplicationContext(), R.string.gesture_invalid, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void onStateReset() {
        topTitle.setText(R.string.setting_gesture_passwd);
        lytGestureIndi.setVisibility(View.VISIBLE);

        tvInputOrigin.setVisibility(View.GONE);
//        actionLoginPasswd.setVisibility(View.GONE);

        gestureDetector.setGestureLock(gestureView);
        gestureDetector.setShowMode(GestureDetector.MODE_SHOW_ONCE);
        gestureView.setSaveMode(true);
        gestureView.setOnGestureEventListener(new GestureLock.OnGestureEventListener() {
            @Override
            public void OnGestureEvent(boolean match) {
                if (match) {
//                    Intent intent = new Intent(GestureSettingActivity.this, GestureSettingActivity.class);
//                    intent.putExtra(KEY_ACTIVITY_STATE, ACTIVITY_STATE_RESET);
//                    startActivity(intent);
                    Toast.makeText(GestureSettingActivity.this, "手势设置成功！", Toast.LENGTH_SHORT).show();
                    PrefHelper helper = PrefHelper.getInstance(getApplicationContext());
                    helper.putStringValue(PrefHelper.KEY_GESTURE_PASSWD, gestureView.patternToSecureCode());
                    setResult(RESULT_OK, null);
                    finish();

                } else {
                    Toast.makeText(GestureSettingActivity.this, "手势密码错误请重试！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}