package com.xeniatechnologies.app.templekiosktirupati.utils;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xeniatechnologies.app.templekiosktirupati.ui.LoginActivity

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val myIntent = Intent(context, LoginActivity::class.java)
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(myIntent)
        }
    }
}
