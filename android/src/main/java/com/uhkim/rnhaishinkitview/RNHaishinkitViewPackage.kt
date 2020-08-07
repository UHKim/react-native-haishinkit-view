package com.uhkim.rnhaishinkitview

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.facebook.react.views.image.ReactImageManager




class RNHaishinkitViewPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
      return emptyList<ViewManager<*, *>>()
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
     return listOf<ViewManager<*, *>>(RNHaishinkitViewManager(reactContext))
    }
}
