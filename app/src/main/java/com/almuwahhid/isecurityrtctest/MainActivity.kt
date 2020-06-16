package com.almuwahhid.isecurityrtctest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            tv_sdp.setText(tv_sdp.text.toString() +"\n" + intent.getStringExtra("data"))
        }
    }

    var filter: IntentFilter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(this, SecurityRTCForeground::class.java)
        serviceIntent.putExtra("foreground." + BuildConfig.APPLICATION_ID, "iSecurity aktif")
        ContextCompat.startForegroundService(this, serviceIntent)

        filter = IntentFilter()
        filter!!.addAction("iSecurity")
        registerReceiver(receiver, filter)
    }
}
