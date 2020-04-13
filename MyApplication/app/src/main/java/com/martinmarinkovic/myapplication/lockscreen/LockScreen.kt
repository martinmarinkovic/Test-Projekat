package com.martinmarinkovic.myapplication.lockscreen

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

class LockScreen {

    var context: Context? = null
    var disableHomeButton: Boolean? = null
    var prefs: SharedPreferences? = null

    companion object {
        private var singleton: LockScreen? = null
        fun getInstance(): LockScreen {
            if (singleton == null) {
                singleton = LockScreen()
            }
            return singleton as LockScreen
        }
    }

    fun init(context: Context){
        this.context = context
    }

/*    fun init(context: Context, disableHomeButton: Boolean){
        this.context = context
        this.disableHomeButton = disableHomeButton
    }*/

    fun active() {
        /*if (disableHomeButton!!) {
            //showSettingAccesability()
        }*/
        if (context != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context!!.startForegroundService(Intent(context, LockScreenService::class.java))
            } else {
                context!!.startService(Intent(context, LockScreenService::class.java))
            }
        }
    }


    fun deactivate() {
        context!!.stopService(Intent(context, LockScreenService::class.java))
    }

    fun isActive(): Boolean {
        return if (context != null) {
            isMyServiceRunning(LockScreenService::class.java)
        } else {
            false
        }
    }

    /*private fun showSettingAccesability() {
        if (!isMyServiceRunning(LockWindowAccessibilityService::class.java)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context!!.startActivity(intent)
        }
    }*/

    @SuppressWarnings("deprecation")
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}