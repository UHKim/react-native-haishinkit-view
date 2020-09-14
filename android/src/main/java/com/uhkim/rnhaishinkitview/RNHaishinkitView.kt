package com.uhkim.rnhaishinkitview

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.haishinkit.events.Event
import com.haishinkit.events.IEventListener
import com.haishinkit.media.AudioSource
import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.util.EventUtils
import com.haishinkit.view.CameraView


class RNHaishinkitView(context: Context): CameraView(context, attributes), IEventListener {
  private var rtmpConnection: RTMPConnection = RTMPConnection()
  private var rtmpStream: RTMPStream? = null;
  var camera = com.haishinkit.media.CameraSource(Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK))

  var streamUrl: String? = null;
  var streamKey: String? = null;

  var useFront = false


  var outputWidth: Int? = 540;
  var outputHeight: Int? = 960;

  override fun surfaceCreated(holder: SurfaceHolder?) {
    super.surfaceCreated(holder)

    configRtmpStream()
    initBroadcastView()
  }

  override fun surfaceDestroyed(holder: SurfaceHolder?) {
    super.surfaceDestroyed(holder)
    rtmpConnection?.removeEventListener("rtmpStatus", this)
    rtmpConnection?.removeEventListener("ioError", this)

    rtmpConnection.close()
  }

  fun onViewStatus(){
    val event = Arguments.createMap()
    event.putString("message", "MyMessage")
    val reactContext = context as ReactContext
    reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
      id,
      "topChange",
      event)
  }

  fun initBroadcastView(){
    rtmpStream?.let { attachStream(it) }
  }

  fun configRtmpStream(){
    rtmpStream = RTMPStream(rtmpConnection!!)

    rtmpStream?.attachCamera(camera)
    rtmpStream?.attachAudio(AudioSource())

    toggleCamera()
  }

  fun startPublish(){
    rtmpConnection?.addEventListener("rtmpStatus", this)
    rtmpConnection?.addEventListener("ioError", this)

    streamUrl?.let { rtmpConnection?.connect(it) }
  }

  fun stopPublish(){
    rtmpConnection?.removeEventListener("rtmpStatus", this)
    rtmpConnection?.removeEventListener("ioError", this)
    rtmpConnection.close();
  }

  fun toggleCamera(){
    useFront = !useFront
    camera = com.haishinkit.media.CameraSource(Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK))
    camera.actualSize.width = this!!.outputWidth!!
    camera.actualSize.height = this!!.outputHeight!!
  }

  override fun handleEvent(event: Event) {
    val data = EventUtils.toMap(event)
    val code = data["code"].toString()
    if (code == RTMPConnection.Code.CONNECT_SUCCESS.rawValue) {
      Log.w(javaClass.name, "PUBLISH")
      rtmpStream!!.publish(streamKey)
    }
  }

}
