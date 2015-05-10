package com.example.gesturelock;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.example.gesturelock.widget.GestureDetector;
import com.example.gesturelock.widget.GestureLock;
import com.example.gesturelock.widget.GestureLock.OnGestureEventListener;

public class MainActivity extends Activity {

	private GestureLock gestureView;
	private GestureDetector gestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		gestureView = (GestureLock) findViewById(R.id.gesture_lock);
		gestureView.setCorrectGesture(new int[]{0, 3, 6, 7, 8, 5, 2, 1, 4});
		gestureView.setMode(GestureLock.MODE_EDIT);
		gestureDetector = (GestureDetector) findViewById(R.id.gesture_detector);
		gestureDetector.setGestureLock(gestureView);
		gestureDetector.setShowMode(GestureDetector.MODE_SHOW_ONCE);
	}

}
