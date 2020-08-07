package com.uhkim.rnhaishinkitview

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class RNHaishinkitViewManager(reactContext: ReactApplicationContext) : SimpleViewManager<RNHaishinkitView>() {
  private var viewRN: RNHaishinkitView? = null;
  private var streamUrl: String? = null;
  private var streamKey: String? = null;
  private var mReactContext: ReactApplicationContext = reactContext

  private val reactClassName = "RNHaishinkitView"

  override fun getName(): String { return reactClassName }

  @ReactProp(name = "streamKey")
  fun setStreamKey(viewRN: RNHaishinkitView, newId: String?) {
    if (newId == null || newId == streamKey) return
    viewRN.streamKey = newId

  }
  @ReactProp(name = "streamUrl")
  fun setStreamUrl(viewRN: RNHaishinkitView, newId: String?) {
    if (newId == null || newId == streamUrl) return
    viewRN.streamUrl = newId
  }

  @ReactMethod()
  fun startPublish(){
    viewRN?.startPublish()
  }
  @ReactMethod
  fun stopPublish(){
    viewRN?.stopPublish()
  }
  @ReactMethod
  fun toggleCamera(){
    viewRN?.toggleCamera()
  }

  @ReactMethod
  fun initBroadcastView(){
    viewRN?.initBroadcastView()
  }

  @ReactMethod
  fun configRtmpStream(){
    viewRN?.configRtmpStream()
  }

  override fun createViewInstance(reactContext: ThemedReactContext): RNHaishinkitView {

    reactContext.currentActivity?.let {
      ActivityCompat.requestPermissions(it,
        arrayOf(Manifest.permission.CAMERA,
          Manifest.permission.RECORD_AUDIO),
        1323)
    }
    if (viewRN == null) viewRN = RNHaishinkitView(reactContext)
    return viewRN as RNHaishinkitView;
  }


}
