package com.almuwahhid.isecurityrtctest

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity2 : AppCompatActivity() {

    private val MANDATORY_PERMISSIONS = arrayOf(
        "android.permission.MODIFY_AUDIO_SETTINGS",
        "android.permission.RECORD_AUDIO", "android.permission.INTERNET"
    )

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            tv_sdp.setText(tv_sdp.text.toString() +"\n" + intent.getStringExtra("data"))
        }
    }


    var filter: IntentFilter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        for (permission in MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission!!) != PackageManager.PERMISSION_GRANTED) {
//                setResult(Activity.RESULT_CANCELED)
//                finish()
//                return
            }
        }

        val serviceIntent = Intent(this, SecurityRTCForegroundNew::class.java)
        serviceIntent.putExtra("foreground." + BuildConfig.APPLICATION_ID, "iSecurity aktif")
        ContextCompat.startForegroundService(this, serviceIntent)

        filter = IntentFilter()
        filter!!.addAction("iSecurity")
        registerReceiver(receiver, filter)
    }
}