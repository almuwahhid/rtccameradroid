package com.almuwahhid.isecurityrtctest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import chat.rocket.android.call.GTRTC.GTRTCCLient
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.LOCAL_HEIGHT_CONNECTED
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.LOCAL_HEIGHT_CONNECTING
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.LOCAL_WIDTH_CONNECTED
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.LOCAL_WIDTH_CONNECTING
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.LOCAL_X_CONNECTED
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.LOCAL_X_CONNECTING
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.LOCAL_Y_CONNECTED
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.LOCAL_Y_CONNECTING
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.REMOTE_HEIGHT
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.REMOTE_WIDTH
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.REMOTE_X
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.REMOTE_Y
import com.almuwahhid.isecurityrtctest.GTRTC.GTPeerConnectionParameters
import com.google.gson.Gson
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.webrtc.MediaStream
import org.webrtc.VideoRenderer
import org.webrtc.VideoRendererGui

class MainActivity : AppCompatActivity(), GTRTCCLient.RTCListener {

    var rtcClient: GTRTCCLient? = null
    private var mSocket: Socket? = null
    internal var gson: Gson? = null

    private val scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL
    private var localRender: VideoRenderer.Callbacks? = null
    private var remoteRender: VideoRenderer.Callbacks? = null

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            tv_sdp.setText(tv_sdp.text.toString() +"\n" + intent.getStringExtra("data"))
        }
    }

    var filter: IntentFilter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn.setOnClickListener({
            rtcClient!!.offers()
        })


        gson = Gson()
        val singleton = SocketSingleton.get(applicationContext)
        mSocket = singleton.socket

        mSocket!!.on(Socket.EVENT_CONNECT, Emitter.Listener {
            Log.d("TAGSecurityRTCFore", "SOCKET CONNECTED")
            //                t.schedule(new ClassEmitNotifNews(), 0, 5000);
        })
        mSocket!!.on("callrtc") { args ->
            Log.d("TAGSecurityRTCFore", "emitGetListUser() received listen to room called " + args[0].toString())
//            Log.d("TAGSecurityRTCFore", "received message called " + args[0].toString())
            try {

                if(args[0].toString().contains("\"candidate\"")){
                    Log.d("TAGSecurityRTCFore", "candidate")
                    val candidate: Candidate = gson!!.fromJson(args[0].toString(), Candidate::class.java)
                    rtcClient!!.answerCandidate(candidate)
                } else if(args[0].toString().contains("\"answer\"")){
                    Log.d("TAGSecurityRTCFore", "truee")
                    val payload: Payload = gson!!.fromJson(args[0].toString(), Payload::class.java)
                    rtcClient!!.answerAnswer(payload)
                } else if(args[0].toString().contains("\"offer\"")){
                    Log.d("TAGSecurityRTCFore", "false")
                    val payload: Payload = gson!!.fromJson(args[0].toString(), Payload::class.java)
                    rtcClient!!.answerOffer(payload)
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        if (!mSocket!!.connected()){
            mSocket!!.connect()
            Log.d("connect", "connect")
        }

        glview_call.setPreserveEGLContextOnPause(true)
        glview_call.setKeepScreenOn(true)
        VideoRendererGui.setView(glview_call) {
            init()

        }

        remoteRender = VideoRendererGui.create(
            REMOTE_X, REMOTE_Y,
            REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false)
        localRender = VideoRendererGui.create(
            LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
            LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true)

        if(rtcClient!=null){
            rtcClient!!.initPeer()
        }


    }

    private fun init(){
        val displaySize = Point()
        windowManager.defaultDisplay.getSize(displaySize)
        val params = GTPeerConnectionParameters(
            true, false, displaySize.x, displaySize.y, 30, 1, CallStatic.VIDEO_CODEC_VP9, true, 1, CallStatic.AUDIO_CODEC_OPUS, true)
        rtcClient = GTRTCCLient(this, params!!, this, VideoRendererGui.getEGLContext())
    }

    override fun onCallReady(type: String, sdp: String) {
        mSocket!!.emit("callrtc", gson!!.toJson(Payload(Payload.Sdp("cccf2335-66bf-11e2-ffed-e7f9e438c5b8\"", type!!, sdp!!))),
            object : Ack {
                override fun call(vararg args: Any?) {
                    Log.d("TAGSecurityRTCFore", "call: getDatas " + args.size)
                    if (args.size > 0) {
                        Log.d("TAGSecurityRTCFore", """emitGetListUser() ACK :${args[0]}""".trimIndent())
                    }
                }
            })
        Log.d("iSecurity", sdp+" "+type )
    }

    override fun onCandidateCall(label: Int, id: String, candidate: String) {
        Log.d("iSecurity id", ""+label)
        Log.d("iSecurity label", id)
        Log.d("iSecurity candidate", candidate)
        mSocket!!.emit("callrtc", gson!!.toJson(Candidate(Candidate.Detail(candidate, ""+id, ""+label), "cccf2335-66bf-11e2-ffed-e7f9e438c5b8")),
            object : Ack{
                override fun call(vararg args: Any?) {
                    Log.d("TAGSecurityRTCFore", "call: getDatas " + args.size)
                    if (args.size > 0) {
                        Log.d("TAGSecurityRTCFore", """emitGetListUser() ACK :${args[0]}""".trimIndent())
                    }
                }
            })
    }

    override fun onStatusChanged(newStatus: String) {
        Log.d("iSecurity new Status", newStatus)
    }

    override fun onLocalStream(localStream: MediaStream) {
//        localStream.videoTracks[0].addRenderer(VideoRenderer(localRender))
//        VideoRendererGui.update(localRender,
//            LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
//            LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
//            scalingType, false)
    }

    override fun onAddRemoteStream(remoteStream: MediaStream) {
        Log.d("iSecurity remote Stream", "remote Stream ok")
        remoteStream.videoTracks[0].addRenderer(VideoRenderer(remoteRender))
        VideoRendererGui.update(remoteRender,
            REMOTE_X, REMOTE_Y,
            REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false)
        VideoRendererGui.update(localRender,
            LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
            LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
            scalingType, false)
    }

    override fun onRemoveRemoteStream() {
        VideoRendererGui.update(localRender,
            LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
            LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
            scalingType, false)
    }
}
