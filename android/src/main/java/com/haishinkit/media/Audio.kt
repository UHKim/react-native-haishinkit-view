package com.haishinkit.media

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder

class Audio: AudioRecord.OnRecordPositionUpdateListener, IDevice {
    var channel = DEFAULT_CHANNEL
    internal var stream: RTMPStream? = null
    private var encoding = DEFAULT_ENCODING

    private  var _buffer: ByteArray? = null
    var buffer: ByteArray?
        get() {
            if (_buffer == null) {
                _buffer = ByteArray(minBufferSize)
            }
            return _buffer
        }
        set(value) {
            _buffer = value
        }

    var samplingRate = DEFAULT_SAMPLING_RATE

    private var _minBufferSize: Int = -1
    var minBufferSize: Int
        get() {
            if (_minBufferSize == -1) {
                _minBufferSize = AudioRecord.getMinBufferSize(samplingRate, channel, encoding);
            }
            return _minBufferSize
        }
        set(value) {
            _minBufferSize = value
        }

    private var _audioRecord: AudioRecord? = null
    var audioRecord: AudioRecord?
        get() {
            if (_audioRecord == null) {
                _audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        samplingRate,
                        channel,
                        encoding,
                        minBufferSize
                )
            }
            return _audioRecord
        }
        set(value) {
            _audioRecord = value
        }

    var currentPresentationTimestamp:Double = 0.0

    fun startRecording() {
        audioRecord?.startRecording()
        audioRecord?.read(buffer, 0, minBufferSize)
    }

    override fun setUp() {
        audioRecord?.positionNotificationPeriod = minBufferSize / 2
        audioRecord?.setRecordPositionUpdateListener(this)
    }

    override fun tearDown() {
    }

    override fun onMarkerReached(audio: AudioRecord?) {
    }

    override fun onPeriodicNotification(audio: AudioRecord?) {
        audio?.read(buffer, 0, minBufferSize)
        stream?.appendBytes(buffer, currentPresentationTimestamp.toLong(), RTMPStream.BufferType.AUDIO)
        currentPresentationTimestamp += timestamp()
    }

    fun timestamp():Double {
        return 1000000 * (minBufferSize.toDouble() / 2 / samplingRate.toDouble())
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        val DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        val DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        val DEFAULT_SAMPLING_RATE = 44100
    }
}
