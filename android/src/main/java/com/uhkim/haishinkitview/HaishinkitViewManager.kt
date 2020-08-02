package com.uhkim.haishinkitview

import android.Manifest
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class HaishinkitViewManager: SimpleViewManager<HaishinkitView>() {
  private var view: HaishinkitView? = null;
  private var streamUrl: String? = null;
  private var streamKey: String? = null;


  override fun getName() = "HaishinkitView"

  @ReactProp(name = "videoId")
  fun setStreamKey(view: HaishinkitView, newId: String?) {
    if (newId == null || newId == streamKey) return
    view.streamKey = newId

  }
  @ReactProp(name = "videoId")
  fun setStreamUrl(view: HaishinkitView, newId: String?) {
    if (newId == null || newId == streamUrl) return
    view.streamUrl = newId
  }

  @ReactMethod
  fun startPublish(){
    view?.startPublish()
  }
  @ReactMethod
  fun stopPublish(){
    view?.stopPublish()
  }
  @ReactMethod
  fun toggleCamera(){
    view?.toggleCamera()
  }

  override fun createViewInstance(reactContext: ThemedReactContext): HaishinkitView {
    if (view == null) view = HaishinkitView(reactContext)
    reactContext.currentActivity?.let {
      ActivityCompat.requestPermissions(it,
        arrayOf(Manifest.permission.CAMERA,
          Manifest.permission.RECORD_AUDIO),
        1)
    }
    return view as HaishinkitView;
  }


}
