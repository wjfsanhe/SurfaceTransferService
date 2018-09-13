// ISurfaceTransfer.aidl
package com.qiyi.framework.surfacetransferservice;
import android.view.Surface;
import android.content.Intent;
import com.qiyi.framework.surfacetransferservice.ISurfaceTransferCallback;
// Declare any non-default types here with import statements

interface ISurfaceTransfer {
    int startActivity(in Intent intent);
    int setSurface(in Surface surface, int width, int height, int density);
    int registerListener(in ISurfaceTransferCallback callback);
}
