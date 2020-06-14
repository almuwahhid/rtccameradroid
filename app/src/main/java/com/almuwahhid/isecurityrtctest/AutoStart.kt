package com.almuwahhid.isecurityrtctest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AutoStart: BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        Log.d("AutoStart", "here i'm coming")

        var intent = Intent(context!!, SecurityRTCForeground::class.java)
        intent.putExtra("NEED_FOREGROUND_KEY", false)
        try {
//            context!!.startService(intent)
            intent.putExtra("NEED_FOREGROUND_KEY", true)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            }
            else {
                context.startService(intent)
            }
        }
        catch (ex: IllegalStateException) {

        }

    }
}