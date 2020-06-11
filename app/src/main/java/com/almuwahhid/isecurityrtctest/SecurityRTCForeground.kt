package com.almuwahhid.isecurityrtctest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson

class SecurityRTCForeground : Service() {

//    private var mWebSocketClient: WebSocketClient? = null
    var manager: NotificationManager? = null

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val input = intent!!.getStringExtra("foreground." + "BuildConfig.APPLICATION_ID_123321")
            manager = getSystemService(NotificationManager::class.java)
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)

            val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle("iSecurityRTC")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1201029, notification)
            return START_NOT_STICKY
        } else {
            return START_STICKY
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

}