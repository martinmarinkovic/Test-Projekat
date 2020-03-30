package com.martinmarinkovic.myapplication.lockscreen

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class LockWindowAccessibilityService : AccessibilityService() {

    private var lockScreenShow: Boolean = true // ???

    override fun onKeyEvent(event: KeyEvent): Boolean {
        LockScreen.getInstance().init(this)
        if (lockScreenShow) {
            // disable home
            if (event.keyCode == KeyEvent.KEYCODE_HOME || event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                return true
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Toast.makeText(applicationContext, event.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

}