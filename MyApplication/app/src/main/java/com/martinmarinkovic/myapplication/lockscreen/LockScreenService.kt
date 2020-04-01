package com.martinmarinkovic.myapplication.lockscreen

import android.app.*
import android.app.Service.START_STICKY
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat.START_STICKY
import com.martinmarinkovic.myapplication.MainActivity
import com.martinmarinkovic.myapplication.R

class LockScreenService : Service() {

    private val NOTIFICATION_CHANNEL_ID = "com.martinmarinkovic.myapplication"
    private val TAG = "LockScreenService"
    private var serviceStartId = 0
    private var mContext: Context? = null

    private val lockScreenReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (null != context) {
                if (intent.action == Intent.ACTION_SCREEN_OFF) {
                    startLockScreenActivity()
               }
            }
        }
    }

    private fun stateReceiver(isStartReceiver: Boolean) {
        if (isStartReceiver) {
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            registerReceiver(lockScreenReceiver, filter)
        } else {
            if (null != lockScreenReceiver) {
                try {
                    unregisterReceiver(lockScreenReceiver)
                } catch (e: Exception) {
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        mContext = this
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            showNotification()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceStartId = startId
        stateReceiver(true)
        if (null != intent) {
            Log.d(TAG, "$TAG onStartCommand intent  existed")
        } else {
            Log.d(TAG, "$TAG onStartCommand intent NOT existed")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stateReceiver(false)
        super.onDestroy()
    }

    private fun startLockScreenActivity() {
        val lockScreenIntent = Intent(mContext, LockScreenActivity::class.java)
        lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(lockScreenIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification() {

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java)
            .let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Lock Screen")
            .setContentText("Running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

    }
}