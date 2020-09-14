package com.haishinkit.media

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.hardware.display.DisplayManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.util.DisplayMetrics
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MediaProjectionSource(private var mediaProjection: MediaProjection, private val metrics: DisplayMetrics): IVideoSource, ImageReader.OnImageAvailableListener {
    override var stream: RTMPStream? = null
    override var isRunning: Boolean = false
    private var virtualDisplay: VirtualDisplay? = null

    override fun setUp() {
        val width = (metrics.widthPixels).toInt()
        val height = (metrics.heightPixels).toInt()
        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2).also {
            it.setOnImageAvailableListener(this, null)
        }
        if (reader != null) {
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                    MediaProjectionSource.DEFAULT_DISPLAY_NAME,
                    width,
                    height,
                    metrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    reader.surface,
                    null,
                    null
            )
        }
    }

    override fun tearDown() {
        kotlin.runCatching {
            virtualDisplay?.release()
            mediaProjection.stop()
        }
    }

    override fun startRunning() {
        isRunning = true
    }

    override fun stopRunning() {
        isRunning = false
    }

    override fun onImageAvailable(reader: ImageReader) {
        reader.acquireLatestImage().use { img ->
            kotlin.runCatching {
                val plane = img?.planes?.get(0) ?: return@use null
                val width = plane.rowStride / plane.pixelStride
                val height = (metrics.heightPixels).toInt()
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                    copyPixelsFromBuffer(plane.buffer)
                }
                stream?.appendBytes(plane.buffer.array(), System.nanoTime() / 1000000L, RTMPStream.BufferType.VIDEO)
            }
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_DISPLAY_NAME = "MediaProjectionSourceDisplay"
    }
}
