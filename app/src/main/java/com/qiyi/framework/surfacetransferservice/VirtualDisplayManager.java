package com.qiyi.framework.surfacetransferservice;

import android.app.Activity;
import android.app.IActivityContainerCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VirtualDisplayManager implements DisplayManager.DisplayListener {
	private final String TAG = "SurfaceTransferService.VirtualDisplayManager";
	private Surface mSurface;
	private static VirtualDisplayManager mInstance = null;
	private static Context mContext;
	private static VirtualDisplay mVirtualDisplay;
	private static DisplayManager mDisplayManager;
	private static Activity mActivity = null;
	private int mWidth = 1280;
	private int mHeight = 720;
	private int mDensity = 320;

	//reflection interface
	private Object mIActivityContainer;
	private Method mStartActivity;
	private Method mSetSurface;

	public VirtualDisplayManager() {
		init();
	}

	//the first init phase
	private IBinder getActivityToken() {
		Class clazz = Class.forName("android.app.Activity");
		Field token = clazz.getDeclaredField("mToken");
		//mToken is private field.
		token.setAccessible(true);
		return token.get(mActivity);
	}

	private void init() {
		//after we create virtual display, we should create relative container
		try {
			Class clazz = Class.forName("android.app.ActivityManagerNative");
			Class inf = Class.forName("android.app.IActivityContainer");

			Method getDefault = clazz.getDeclaredMethod("getDefault");
			Object IActivityManager = getDefault.invoke(null);
			Log.d(TAG, "get class ActivityManagerNative");
			Class interfaces[] = clazz.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {

				Method[] methods = interfaces[i].getMethods();
				for (int j = 0; j < methods.length; j++) {
					if (methods[j].getName().equals("createVirtualActivityContainer")) {
						Log.d(TAG, "Interface Method Name : " + methods[j].getName());
						//get Actiivty Token from base activity.
						IBinder activityToken = getActivityToken();
						Object IActivityContainer = methods[j].invoke(IActivityManager,
								activityToken, mIActivityContainerCallback);
						//prepare all interface here.
						mIActivityContainer = IActivityContainer;
						mStartActivity = inf.getDeclaredMethod("startActivity", Intent.class);
						mSetSurface = inf.getDeclaredMethod("setSurface", Surface.class, int.class, int.class, int.class);

						/*Intent intent = new Intent(this,MainActivity.class);
						Intent intent = new Intent("framework.com.hwmtest");
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						ComponentName cn = new ComponentName("com.android.calendar", "com.android.calendar.AllInOneActivity");
						//ComponentName cn = new ComponentName("framework.com.hwmtest", "framework.com.hwmtest.MainActivity");
						intent.setComponent(cn);
						startActivity.invoke(IActivityContainer,intent);*/

					}
				}

			}


		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public static VirtualDisplayManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new VirtualDisplayManager();
			mContext = context;
			doPrepare();
		}
		return mInstance;
	}

	public static int setBaseActivity(Activity activity) {
		//base Activity must be set before init.
		mActivity = activity;
		return 0;
	}

	private static void doPrepare() {
		mDisplayManager = (DisplayManager) mContext.getSystemService("display");
		mDisplayManager.registerDisplayListener(mInstance, null);
	}

	//update virtual display surface. surface provided by remote side.
	IActivityContainerCallback mIActivityContainerCallback = new IActivityContainerCallback.Stub() {
		@Override
		void setVisible(IBinder container, boolean visible) {
			Log.d(TAG, "IActivityContainerCallback setVisible called");
		}

		@Override
		void onAllActivitiesComplete(IBinder container) {
			Log.d(TAG, "IActivityContainerCallback onAllActivitiesComplete called");
		}
	};

	public int startActivity(Intent intent) {
		if (mStartActivity != null && mIActivityContainer != null && intent != null) {
			mStartActivity.invoke(mIActivityContainer, intent);
			return 0;
		}
		return -1;
	}

	public int releaseSurface() {
		updateSurface(null, 0, 0, 0);
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
		mSetSurface(mIActivityContainer, mWidth, mHeight, mDensity);
	}
//--------------------------------------------------------------------------------------------------
	boolean enableVirtual = false;
	{
if(enableVirtual){

	int updateSurface(Surface surface, int width, int height) {
		Log.e(TAG, "updateSurface called");
		if (surface == null) {
			Log.e(TAG, "surface is null , WARING!!!!!");
			return -1;
		}
		if (mVirtualDisplay == null) {
			//first time update surface
			if (width == 0 || height == 0) {
				mWidth = 1280;
				mHeight = 720;
			} else {
				mWidth = width;
				mHeight = height;
			}
			int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
			mVirtualDisplay = mDisplayManager.createVirtualDisplay("yidisplay", mWidth, mHeight, 320,
					surface, flags, mVirtualCallback, mVirtualHandler);

		} else {
			//update new Surface
			mVirtualDisplay.setSurface(surface);
			Log.i(TAG, "set new surface for VirtualDisplay");
		}
		return 0;
	};

	Handler mVirtualHandler = new Handler();
	VirtualDisplay.Callback mVirtualCallback = new VirtualDisplay.Callback() {
		/**
		 * Called when the virtual display video projection has been
		 * paused by the system or when the surface has been detached
		 * by the application by calling setSurface(null).
		 * The surface will not receive any more buffers while paused.
		 */
		public void onPaused() {
			Log.i(TAG, "CALLBACK onPaused");
		}

		/**
		 * Called when the virtual display video projection has been
		 * resumed after having been paused.
		 */
		public void onResumed() {
			Log.i(TAG, "CALLBACK onResumed");
		}

		/**
		 * Called when the virtual display video projection has been
		 * stopped by the system.  It will no longer receive frames
		 * and it will never be resumed.  It is still the responsibility
		 * of the application to release() the virtual display.
		 */
		public void onStopped() {
			Log.i(TAG, "CALLBACK onStopped");
		}
	};
}
	}
//--------------------------------------------------------------------------------------------------
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
