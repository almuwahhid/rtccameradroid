package com.almuwahhid.isecurityrtctest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import chat.rocket.android.call.GTRTC.GTRTCCLient
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.AUDIO_CODEC_OPUS
import com.almuwahhid.isecurityrtctest.GTRTC.CallStatic.Companion.VIDEO_CODEC_VP9
import com.almuwahhid.isecurityrtctest.GTRTC.GTPeerConnectionParameters
import org.webrtc.MediaStream

class SecurityRTCForeground : Service(), GTRTCCLient.RTCListener {

    var manager: NotificationManager? = null
    var rtcClient: GTRTCCLient? = null
    var notificationBuilder: NotificationCompat.Builder? = null

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        manager = getSystemService(NotificationManager::class.java)
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
        Log.d("iSecurity", sdp)
    }

    override fun onCandidateCall(label: Int, id: String, candidate: String) {
        Log.d("iSecurity id", ""+label)
        Log.d("iSecurity label", id)
        Log.d("iSecurity candidate", candidate)
    }

    override fun onStatusChanged(newStatus: String) {
        Log.d("iSecurity new Status", newStatus)
    }

    override fun onLocalStream(localStream: MediaStream) {
        Log.d("iSecurity local Stream", "local Stream ok")


        val handler = Handler()
        handler.postDelayed({
            Log.d("iSecurity delay", "1500")
            rtcClient!!.initStream()
//            runOnUithre

        }, 1500)
//        rtcClient!!.initStream()
        sendBroadcast(Intent("iSecurity").putExtra("data", "iSecurity local Stream ok"))
    }

    override fun onAddRemoteStream(remoteStream: MediaStream) {
        Log.d("iSecurity remote Stream", "remote Stream ok")
    }

    override fun onRemoveRemoteStream() {

    }

}