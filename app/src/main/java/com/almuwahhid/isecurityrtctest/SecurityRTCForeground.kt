package com.almuwahhid.isecurityrtctest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import chat.rocket.android.call.GTRTC.GTRTCCLient
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.AUDIO_CODEC_OPUS
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.VIDEO_CODEC_VP9
import com.almuwahhid.isecurityrtctest.GTRTC.GTPeerConnectionParameters
import com.google.gson.Gson
import org.webrtc.MediaStream
import org.webrtc.VideoRendererGui

class SecurityRTCForeground : Service(), GTRTCCLient.RTCListener {

    var manager: NotificationManager? = null
    var rtcClient: GTRTCCLient? = null

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        manager = getSystemService(NotificationManager::class.java)
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("iSecurityRTC")
            .setContentText("input")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1201029, notification)

        init()
        if(rtcClient!=null){
            rtcClient!!.initPeer()
        }

        return START_NOT_STICKY
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

//            val input = intent!!.getStringExtra("foreground." + "BuildConfig.APPLICATION_ID_123321")
            manager = getSystemService(NotificationManager::class.java)
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)

            val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle("iSecurityRTC")
                .setContentText("input")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1201029, notification)

            init()
            if(rtcClient!=null){
                rtcClient!!.initPeer()
            }

            return START_NOT_STICKY
        } else {
            return START_STICKY
        }*/
    }

    private fun init(){
        val displaySize = Point()
        val params = GTPeerConnectionParameters(
            true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true)
        rtcClient = GTRTCCLient(this, params!!, this)
//        rtcClient = GTRTCCLient(this, params!!, VideoRendererGui.getEGLContext(), this)


    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCallReady(type: String, sdp: String) {
        TODO("Not yet implemented")
    }

    override fun onCandidateCall(label: Int, id: String, candidate: String) {
        TODO("Not yet implemented")
    }

    override fun onStatusChanged(newStatus: String) {
        TODO("Not yet implemented")
    }

    override fun onLocalStream(localStream: MediaStream) {
        TODO("Not yet implemented")
    }

    override fun onAddRemoteStream(remoteStream: MediaStream) {
        TODO("Not yet implemented")
    }

    override fun onRemoveRemoteStream() {
        TODO("Not yet implemented")
    }

}