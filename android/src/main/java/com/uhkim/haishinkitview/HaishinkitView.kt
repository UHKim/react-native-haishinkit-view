package com.uhkim.haishinkitview

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder

import com.haishinkit.events.Event
import com.haishinkit.events.IEventListener
import com.haishinkit.media.Audio
import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.util.EventUtils
import com.haishinkit.util.Log
import com.haishinkit.view.CameraView

class HaishinkitView(context: Context): CameraView(context), IEventListener {
  private  var  connection:RTMPConnection = RTMPConnection()
  private var  stream:RTMPStream = RTMPStream(connection!!)
  var camera = com.haishinkit.media.Camera(Camera.open(0))

  var streamUrl: String? = null;
  var streamKey: String? = null;

  var useFront = false


  var width: Int? = 540;
  var height: Int? = 960;

  override fun surfaceCreated(holder: SurfaceHolder?) {
    super.surfaceCreated(holder)

    toggleCamera()

    stream?.attachCamera(camera)
    stream?.attachAudio(Audio())

    connection?.addEventListener("rtmpStatus", this)
    connection?.addEventListener("ioError", this)
  }

  override fun surfaceDestroyed(holder: SurfaceHolder?) {
    super.surfaceDestroyed(holder)
    connection?.removeEventListener("rtmpStatus", this)
    connection?.removeEventListener("ioError", this)

    connection.close()
  }

  fun startPublish(){
    streamUrl?.let { connection?.connect(it) }
  }

  fun stopPublish(){
    connection.close();
  }

  fun toggleCamera(){
    useFront = !useFront
    camera = com.haishinkit.media.Camera(Camera.open((if (useFront)  1 else 0)))
    camera.actualSize.width = this!!.width!!
    camera.actualSize.height = this!!.height!!
  }

  override fun handleEvent(event: Event) {
    val data = EventUtils.toMap(event)
    val code = data["code"].toString()
    if (code == RTMPConnection.Code.CONNECT_SUCCESS.rawValue) {
      Log.w(javaClass.name, "PUBLISH")
      stream!!.publish(streamKey)
    }
  }

}
