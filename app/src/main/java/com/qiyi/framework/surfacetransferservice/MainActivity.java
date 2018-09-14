package com.qiyi.framework.surfacetransferservice;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {
	private final String TAG = "SurfaceTransferService.MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
	}
	//the first init phase
	private IBinder getActivityToken(Activity activity) {
		IBinder binder = null;

		try {
			//Class clazz = Class.forName("com.qiyi.framework.surfacetransferservice.MainActivity");
			//Field token = clazz.getField("mToken");
			Class clazz = Class.forName("android.app.Activity");
			if(activity == null) {
				Log.d(TAG, "ACTIVITY IS NULL!!!");
			}
			//Class clazz = activity.getClass();
			Field token = clazz.getDeclaredField("mToken");
			if (token == null) return null;
			//mToken is private field.
			token.setAccessible(true);
			IBinder msg = (IBinder)token.get(activity);
			binder = msg;
			Log.e(TAG, "aaaaa ====" +  msg.toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return binder;
	}
	@Override
	protected void onResume() {
		super.onResume();
		IBinder token = getActivityToken(this);
		Intent intent = new Intent(this, AIDLSurfaceTransferService.class);
		Bundle bundle = new Bundle();
		bundle.putBinder("binder", token);
		intent.putExtra("Bundle", bundle);
		startService(intent);
	}

}
