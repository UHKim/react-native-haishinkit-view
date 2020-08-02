package com.uhkim.haishinkitview

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext

class HaishinkitViewManager(reactContext: ReactApplicationContext) : SimpleViewManager<HaishinkitView>(reactContext) {
  private lateinit var view: HaishinkitView;
  private var streamUrl: String? = null;
  private var streamKey: String? = null;


  override fun getName() = "HaishinkitView"


  @ReactMethod
  fun startPublish(){
    view.startPublish()
  }
  @ReactMethod
  fun stopPublish(){
    view.stopPublish()
  }
  @ReactMethod
  fun toggleCamera(){
    view.toggleCamera()
  }

  override fun createViewInstance(reactContext: ThemedReactContext): HaishinkitView {
    if (!view) view = HaishinkitView(reactContext, null)
    return view;
  }


}
