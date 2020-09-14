package com.haishinkit.rtmp

import android.util.Log
import com.haishinkit.events.Event
import java.nio.ByteBuffer
import com.haishinkit.net.Socket
import com.haishinkit.rtmp.messages.RTMPMessage
import org.apache.commons.lang3.builder.ToStringBuilder

internal class RTMPSocket(val connection: RTMPConnection) : Socket() {
    enum class ReadyState {
        Uninitialized,
        VersionSent,
        AckSent,
        HandshakeDone,
        Closing,
        Closed
    }

    var bandWidth = 0
        internal set
    var chunkSizeC = RTMPChunk.DEFAULT_SIZE
    var chunkSizeS = RTMPChunk.DEFAULT_SIZE
    var isConnected = false
        private set
    private val handshake = RTMPHandshake()
    private var readyState = ReadyState.Uninitialized

    @Synchronized fun doOutput(chunk: RTMPChunk, message: RTMPMessage) {
        for (buffer in chunk.encode(this, message)) {
            doOutput(buffer)
        }
    }

    override fun onTimeout() {
        close(false)
        connection?.dispatchEventWith(Event.IO_ERROR, false)
        Log.w(javaClass.name + "#onTimeout", "connection timedout")
    }

    override fun onConnect() {
        chunkSizeC = RTMPChunk.DEFAULT_SIZE
        chunkSizeS = RTMPChunk.DEFAULT_SIZE
        handshake.clear()
        readyState = ReadyState.VersionSent
        doOutput(handshake.c0C1Packet)
    }

    override fun close(disconnected: Boolean) {
        var data:Any? = null
        if (disconnected) {
            if (readyState == RTMPSocket.ReadyState.HandshakeDone) {
                data = RTMPConnection.Code.CONNECT_CLOSED.data("")
            } else {
                data = RTMPConnection.Code.CONNECT_FAILED.data("")
            }
        }
        readyState = RTMPSocket.ReadyState.Closing
        super.close(disconnected)
        if (data != null) {
            connection?.dispatchEventWith(Event.RTMP_STATUS, false, data)
        }
        readyState = RTMPSocket.ReadyState.Closed
    }

    override fun listen(buffer: ByteBuffer) {
        when (readyState) {
            RTMPSocket.ReadyState.VersionSent -> {
                if (buffer.limit() < RTMPHandshake.SIGNAL_SIZE + 1) {
                    return
                }
                handshake.s0S1Packet = buffer
                doOutput(handshake.c2Packet)
                buffer.position(RTMPHandshake.SIGNAL_SIZE + 1)
                readyState = ReadyState.AckSent
                if (buffer.limit() - buffer.position() == RTMPHandshake.SIGNAL_SIZE) {
                    listen(buffer.slice())
                    buffer.position(3073)
                }
            }
            RTMPSocket.ReadyState.AckSent -> {
                if (buffer.limit() < RTMPHandshake.SIGNAL_SIZE) {
                    return
                }
                handshake.s2Packet = buffer
                buffer.position(RTMPHandshake.SIGNAL_SIZE)
                readyState = ReadyState.HandshakeDone
                isConnected = true
                doOutput(RTMPChunk.ZERO, connection.createConnectionMessage())
            }
            RTMPSocket.ReadyState.HandshakeDone -> try {
                connection.listen(buffer)
            } catch (e: IndexOutOfBoundsException) {
                Log.w(javaClass.name + "#listen", "", e)
            } catch (e: IllegalArgumentException) {
                Log.w(javaClass.name + "#listen", "", e)
                throw e
            }
            else -> {}
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
