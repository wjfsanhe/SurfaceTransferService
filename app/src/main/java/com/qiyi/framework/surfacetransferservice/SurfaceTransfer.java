package com.qiyi.framework.surfacetransferservice;

import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import com.qiyi.framework.surfacetransferservice.ISurfaceTransfer.Stub;

public class SurfaceTransfer extends Stub {
	private final String TAG = "SurfaceTransferService.SurfaceTransfer";
	AIDLSurfaceTransferService mService = null;
	ISurfaceTransferCallback mCallback = null;
	public SurfaceTransfer(AIDLSurfaceTransferService service) {
		mService = service;
	}
	public void response (Message msg) {
		Log.d(TAG, "feed back message to client");
		if (mCallback == null) return ;
		try {
			mCallback.handleMessage(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	@Override
	public int registerListener (ISurfaceTransferCallback callback) {
		Log.d(TAG, "registerListener update callback");
		mCallback = callback ;
		return 0;
	}
	@Override
	public int prepareInterface(IBinder token) {
		Log.d(TAG, "prepareInterface() called");
		if (mService == null) return -1;

		VirtualDisplayManager manager;
		manager = mService.getVirtualDisplayManager();
		if (manager != null) {
			manager.prepareInterface(token);
		} else {
			Log.e(TAG, "VirtualDisplayManager is null");
			return -1;
		}

		return 0;
	}
	@Override
	public int injectEvent(MotionEvent event) {
		Log.d(TAG, "injectEvent() called");
		if (mService == null) return -1;

		VirtualDisplayManager manager;
		manager = mService.getVirtualDisplayManager();
		if (manager != null) {
			manager.injectEvent(event);
		} else {
			Log.e(TAG, "VirtualDisplayManager is null");
			return -1;
		}

		return 0;
	}
	@Override
	public int setSurface(Surface surface, int width, int height, int density){
		Log.d(TAG, "setSurface() called");
		if (mService == null) return -1;

		VirtualDisplayManager manager;
		manager = mService.getVirtualDisplayManager();
		if (manager != null) {
			manager.updateSurface(surface, width, height, density);
		} else {
			Log.e(TAG, "VirtualDisplayManager is null");
			return -1;
		}

		return 0;
	}
	@Override
	public int startActivity(Intent intent) {
		Log.d(TAG, "startActivity() called");
		if (mService == null) return -1;

		VirtualDisplayManager manager;
		manager = mService.getVirtualDisplayManager();
		if (manager != null) {
			manager.startActivity(intent);
		} else {
			Log.e(TAG, "VirtualDisplayManager is null");
			return -1;
		}

		return 0;
	}
}
