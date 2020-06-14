package com.almuwahhid.isecurityrtctest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(this, SecurityRTCForeground::class.java)
        serviceIntent.putExtra("foreground." + BuildConfig.APPLICATION_ID, "iSecurity aktif")
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
