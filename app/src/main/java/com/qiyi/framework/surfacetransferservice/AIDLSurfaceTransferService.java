package com.qiyi.framework.surfacetransferservice;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AIDLSurfaceTransferService extends Service {
	private static final String TAG = "SurfaceTransferService.AIDLService";
	private VirtualDisplayManager mVirtualDisplayManager = null;
	private IBinder mBinder;

	SurfaceTransfer stub = new SurfaceTransfer(this);

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate() called");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		Log.i(TAG, "onStartCommand " + intent.getBundleExtra("Bundle").getBinder("binder"));
		mBinder = intent.getBundleExtra("Bundle").getBinder("binder");
		initVirtualDisplayManager();
		return super.onStartCommand(intent, flags, startId);
	}

	private void initVirtualDisplayManager() {
		mVirtualDisplayManager = VirtualDisplayManager.getInstance(getBaseContext(),mBinder);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind() called");
		initVirtualDisplayManager();
		return stub;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "onUnbind() called");

		mVirtualDisplayManager.releaseSurface();
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy() called");
	}
	public VirtualDisplayManager getVirtualDisplayManager() {
		return mVirtualDisplayManager;
	}
}
