package com.almuwahhid.isecurityrtctest

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.almuwahhid.isecurityrtctest.GTRTC.PeerConnectionClient
import com.almuwahhid.isecurityrtctest.GTRTC.WebRtcClient
import com.google.gson.Gson
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.webrtc.MediaStream
//import org.webrtc.ScreenCapturerAndroid
import org.webrtc.VideoCapturer

class SecurityRTCForegroundNew : Service(), WebRtcClient.RtcListener {

    var manager: NotificationManager? = null
    var rtcClient: WebRtcClient? = null
    var notificationBuilder: NotificationCompat.Builder? = null
    private var mSocket: Socket? = null

    private var mWebRtcClient: WebRtcClient? = null
    var sDeviceWidth = 0
    var sDeviceHeight = 0
    val SCREEN_RESOLUTION_SCALE = 2

    internal var gson: Gson? = null

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        gson = Gson()
        val singleton = SocketSingleton.get(applicationContext)
        mSocket = singleton.socket

        mSocket!!.on(Socket.EVENT_CONNECT, Emitter.Listener {
            Log.d("TAGSecurityRTCFore", "SOCKET CONNECTED")
        })
        mSocket!!.on("callrtc") { args ->
            Log.d("TAGSecurityRTCFore", "emitGetListUser() received listen to room called " + args[0].toString())
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

//        manager = getSystemService(NotificationManager::class.java)
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0, notificationIntent, 0)

        notificationBuilder = NotificationCompat.Builder(applicationContext, "iSecurity_id")

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val notificationChannel = NotificationChannel(
                "iSecurity_id",
                "My Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // Configure the notification channel.
            // Configure the notification channel.
            notificationChannel.description = "iSecurity"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
//            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            //            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.vibrationPattern = longArrayOf(0, 1000)
            notificationChannel.enableVibration(true)

            manager!!.createNotificationChannel(notificationChannel)
            notificationBuilder!!.setChannelId("iSecurity_id")
        };

        val notification = notificationBuilder!!
            .setContentTitle("iSecurityRTC")
            .setContentText("input")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1201029, notification)

//        val metrics = DisplayMetrics()
//        getWindowManager().getDefaultDisplay().getRealMetrics(metrics)
        sDeviceWidth = 100
        sDeviceHeight = 100
        init()

        return START_NOT_STICKY
    }

    private fun init() {
        val peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters(
            true,
            false,
            true,
            sDeviceWidth / SCREEN_RESOLUTION_SCALE,
            sDeviceHeight / SCREEN_RESOLUTION_SCALE,
            0,
            0,
            "VP8",
            false,
            true,
            0,
            "OPUS",
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            null
        )
        mWebRtcClient = WebRtcClient(
            applicationContext,
            this,
            createScreenCapturer(),
            peerConnectionParameters
        )
    }

    @TargetApi(21)
    private fun createScreenCapturer(): VideoCapturer? {
        /*return ScreenCapturerAndroid(
            null,
            object : MediaProjection.Callback() {
                override fun onStop() {
//                    report("User revoked permission to capture the screen.")
                }
            })*/
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCallReady(type: String, sdp: String) {
        mSocket!!.emit("callrtc", gson!!.toJson(Payload(Payload.Sdp("cccf2335-66bf-11e2-ffed-e7f9e438c5b8\"", type!!, sdp!!))),
            object : Ack{
                override fun call(vararg args: Any?) {
                    Log.d("TAGSecurityRTCFore", "call: getDatas " + args.size)
                    if (args.size > 0) {
                        Log.d("TAGSecurityRTCFore", """emitGetListUser() ACK :${args[0]}""".trimIndent())
                    }
                }
            })
        Log.d("iSecurity", sdp+" "+type )
    }

    override fun onCandidateCall(midIndex: Int, mid: String, candidate: String) {
        Log.d("iSecurity id", ""+midIndex)
        Log.d("iSecurity label", mid)
        Log.d("iSecurity candidate", candidate)
        mSocket!!.emit("callrtc", gson!!.toJson(Candidate(Candidate.Detail(candidate, ""+mid, ""+midIndex), "cccf2335-66bf-11e2-ffed-e7f9e438c5b8\"")),
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
        Log.d("iSecurity local Stream", "local Stream ok")
        sendBroadcast(Intent("iSecurity").putExtra("data", "iSecurity local Stream ok"))
    }

    override fun onAddRemoteStream(remoteStream: MediaStream) {
        Log.d("iSecurity remote Stream", "remote Stream ok")


    }

    override fun onRemoveRemoteStream() {

    }

}