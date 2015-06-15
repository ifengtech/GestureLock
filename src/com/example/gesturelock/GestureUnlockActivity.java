package com.example.gesturelock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gesturelock.util.GestureLockHelper;
import com.example.gesturelock.util.LoginAction;
import com.example.gesturelock.widget.GestureLock;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wang Gensheng on 2015/5/13.
 */
public class GestureUnlockActivity extends FragmentActivity {

    @InjectView(R.id.iv_avatar)
    ImageView ivAvatar;
    @InjectView(R.id.tv_show_message)
    TextView tvShowMessage;
    @InjectView(R.id.gesture_lock)
    GestureLock gestureLock;
    @InjectView(R.id.tv_action_forget_gesture)
    TextView actionForgetGesture;

    private GestureLockHelper lockHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_unlock);

        ButterKnife.inject(this);

        lockHelper = GestureLockHelper.getInstance(getApplicationContext());

        gestureLock.setSecurePatternCode(GestureLockHelper.getInstance(getApplicationContext()).getGesturePasswd());
        gestureLock.setOnGestureEventListener(new GestureLock.OnGestureEventListener() {
            @Override
            public void OnGestureEvent(boolean match) {
                if (match) {
                    lockHelper.errorReset();
                    LoginAction.login();
                    if (lockHelper.getOccurence() == GestureLockHelper.LOCK_OCCURS_ON_BOOT) {
                        startActivity(new Intent(GestureUnlockActivity.this, MainActivity.class));
                        finish();
                    } else if (lockHelper.getOccurence() == GestureLockHelper.LOCK_OCCURS_ON_FORGROUND){
                        finish();
                    } else {
                        finish();
                    }
                } else {
                    int remaining = lockHelper.errorHappens();
                    if (remaining <= 0) {
                        Toast.makeText(getApplicationContext(), R.string.gesture_invalid, Toast.LENGTH_SHORT).show();
                        LoginAction.logout();
                        lockHelper.reset();
                        forceRedirectLogin();
                    } else {
                        tvShowMessage.setText(String.format(getString(R.string.passwd_error_count), remaining));
                        tvShowMessage.setTextColor(0xffff0000);
                    }
                }
            }
        });

        actionForgetGesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), R.string.gesture_invalid, Toast.LENGTH_SHORT).show();
                lockHelper.reset();
                CustomerHttpClient.clearCookie(GestureUnlockActivity.this);
                Constants.user = null;
                LoginAction.logout();
                forceRedirectLogin();
            }
        });
    }

    private void forceRedirectLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.KEY_SHOW_PRESS_BACK, false);
        startActivity(intent);

        // Safety logout!!
        Constants.user = null;
        LoginAction.logout();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish(); //关闭当前页面
            Intent intent=new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
