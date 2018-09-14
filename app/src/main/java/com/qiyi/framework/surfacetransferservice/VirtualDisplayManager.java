package com.qiyi.framework.surfacetransferservice;

import android.app.Activity;
import android.app.IActivityContainerCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.nfc.Tag;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.Surface;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VirtualDisplayManager implements DisplayManager.DisplayListener {
	private static final String TAG = "SurfaceTransferService.VirtualDisplayManager";
	private Surface mSurface;
	private static VirtualDisplayManager mInstance = null;
	private static Context mContext;
	private static IBinder mBinder;
	private static VirtualDisplay mVirtualDisplay;
	private static DisplayManager mDisplayManager;
	private int mWidth = 1280;
	private int mHeight = 720;
	private int mDensity = 320;

	//reflection interface
	private Object mIActivityContainer;
	private Method mStartActivity;
	private Method mSetSurface;
	private Method mSetDrawn;
	private Method mInjectEvent;

	public VirtualDisplayManager() {
	}

	private void init() {
		//after we create virtual display, we should create relative container
		try {
			Class clazz = Class.forName("android.app.ActivityManagerNative");
			Class inf = Class.forName("android.app.IActivityContainer");

			Method getDefault = clazz.getDeclaredMethod("getDefault");
			Object IActivityManager = getDefault.invoke(null);//null represent static method
			Log.d(TAG, "get class ActivityManagerNative");
			Class interfaces[] = clazz.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {

				Method[] methods = interfaces[i].getMethods();
				for (int j = 0; j < methods.length; j++) {
					if (methods[j].getName().equals("createVirtualActivityContainer")) {
						Log.d(TAG, "Interface Method Name : " + methods[j].getName());
						//get Actiivty Token from base activity.
						IBinder activityToken = mBinder;
						Object IActivityContainer = methods[j].invoke(IActivityManager,
								activityToken, mIActivityContainerCallback);
						//prepare all interface here.
						mIActivityContainer = IActivityContainer;
						mStartActivity = inf.getDeclaredMethod("startActivity", Intent.class);
						mSetSurface = inf.getDeclaredMethod("setSurface", Surface.class, int.class, int.class, int.class);
						mInjectEvent = inf.getDeclaredMethod("injectEvent", InputEvent.class);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	public static VirtualDisplayManager getInstance(Context context, IBinder binder) {
		if (mInstance == null) {
			mInstance = new VirtualDisplayManager();
			mContext = context;
			mBinder = binder;
			doPrepare();
		}
		return mInstance;
	}

	private static void doPrepare() {
		mDisplayManager = (DisplayManager) mContext.getSystemService("display");
		mDisplayManager.registerDisplayListener(mInstance, null);
		mInstance.init();
	}

	//update virtual display surface. surface provided by remote side.
	IActivityContainerCallback mIActivityContainerCallback = new IActivityContainerCallback.Stub() {
		@Override
		public void setVisible(IBinder container, boolean visible) {
			Log.d(TAG, "IActivityContainerCallback setVisible called");
		}

		@Override
		public void onAllActivitiesComplete(IBinder container) {
			Log.d(TAG, "IActivityContainerCallback onAllActivitiesComplete called");
		}
	};

	public int startActivity(Intent intent) {
		if (mStartActivity != null && mIActivityContainer != null && intent != null) {
			try {
				mStartActivity.invoke(mIActivityContainer, intent);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return 0;
		}
		return -1;
	}

	public int releaseSurface() {
		updateSurface(null, 0, 0, 0);
		return 0;
	}
	public int injectEvent(MotionEvent event) {
		try {
			mInjectEvent.invoke(mIActivityContainer, event);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}
	public int prepareInterface(IBinder token) {
		mBinder = token;
		mInstance.init();
		return 0;
	}
	public int updateSurface(Surface surface, int width, int height, int density) {
		if (width == 0 || height == 0) {
			mWidth = 1280;
			mHeight = 720;
		} else {
			mWidth = width;
			mHeight = height;
		}
		if (density == 0) {
			density = 320;
		} else {
			mDensity = density;
		}
		try {
					mSetSurface.invoke(mIActivityContainer,surface, mWidth, mHeight, mDensity);
			//mSetDrawn.invoke(mIActivityContainer);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Called whenever a logical display has been added to the system.
	 * Use {@link DisplayManager#getDisplay} to get more information about
	 * the display.
	 *
	 * @param displayId The id of the logical display that was added.
	 */
	public void onDisplayAdded(int displayId) {
		Log.d(TAG, "onDisplayAdded " + displayId + ", display name " + mDisplayManager.getDisplay(displayId).getName());
	}

	/**
	 * Called whenever a logical display has been removed from the system.
	 *
	 * @param displayId The id of the logical display that was removed.
	 */
	public void onDisplayRemoved(int displayId) {
		Log.d(TAG, "onDisplayRemoved " + displayId + ", display name " + mDisplayManager.getDisplay(displayId).getName());
	}

	/**
	 * Called whenever the properties of a logical display have changed.
	 *
	 * @param displayId The id of the logical display that changed.
	 */
	public void onDisplayChanged(int displayId) {
		Log.d(TAG, "onDisplayChanged " + displayId + ", display name " + mDisplayManager.getDisplay(displayId).getName());
	}
}
