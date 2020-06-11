package chat.rocket.android.call.GTRTC

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketListener: WebSocketListener() {
    private val TAG: String = "WebSocketListener"

    private val NORMAL_CLOSURE_STATUS = 1000
    private var onStatusChange: OnStatusChange? = null

    public fun setStatusChanged(o: OnStatusChange){
        onStatusChange = o
    }

    override fun onOpen(webSocket: WebSocket?, response: Response?) {
        super.onOpen(webSocket, response)
        onStatusChange!!.onTextChange("opened : \n "+response!!.message())
    }

    override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
        super.onFailure(webSocket, t, response)
        onStatusChange!!.onTextChange("error ")
//        Log.d(TAG, "error : \n"+response!!.message())
    }

    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
        super.onClosing(webSocket, code, reason)
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        super.onMessage(webSocket, text)
        Log.d(TAG, text)
        onStatusChange!!.onTextChange("has message: \n "+text)
    }

    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
        super.onMessage(webSocket, bytes)
    }

    interface OnStatusChange{
        fun onTextChange(text: String?)
    }
}