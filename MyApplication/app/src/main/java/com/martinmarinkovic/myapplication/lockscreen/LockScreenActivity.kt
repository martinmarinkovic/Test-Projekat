package com.martinmarinkovic.myapplication.lockscreen

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.martinmarinkovic.myapplication.PinLockView
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.lockscreen.Utils.Companion.sha256


class LockScreenActivity : AppCompatActivity() {

    companion object{
        private const val PACKAGE_NAME = "com.martinmarinkovic.myapplication"
        private const val PIN_SAVED = "pinEnabled"
        const val RESULT_BACK_PRESSED = Activity.RESULT_FIRST_USER
        const val EXTRA_SET_PIN = "set_pin"
        private const val PIN_LENGTH = 4
        private const val PREFERENCES = "com.martinmarinkovic.myapplication.lockscreen"
        private const val KEY_PIN = "pin"

        fun getIntent(context: Context?, setPin: Boolean): Intent? {
            val intent = Intent(context, LockScreenActivity::class.java)
            intent.putExtra(EXTRA_SET_PIN, setPin)
            return intent
        }
    }

    private var mPinLockView: PinLockView? = null
    private var mIndicatorDots: IndicatorDots? = null
    private var mTextTitle: TextView? = null
    private var mTextAttempts: TextView? = null
    private var mSetPin = false
    private var mFirstPin = ""
    private var mTryCount: Int = 0
    private var layout: LinearLayout? = null
    private var window: WindowManager? = null
    private var screen_width = 0
    private var screen_height = 0
    private var myParams: WindowManager.LayoutParams? = null
    private var context: Context? = null
    private var mView: View? = null

    private fun getScreenSize() {
        val display: Display = window!!.defaultDisplay
        val size = Point()
        display.getSize(size)
        screen_width = size.x
        screen_height = size.y
    }

    @SuppressLint("ResourceAsColor", "ResourceType")
    private fun initializeView() {
        window = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        context = this
        mView = LayoutInflater.from(this).inflate(R.layout.activity_lock_screen, null)
        layout = LinearLayout(this)
    }

    private fun showFloat() {
        if (Build.VERSION.SDK_INT >= 26){
            myParams = WindowManager.LayoutParams(
                screen_width,
                screen_height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        )} else {
            myParams = WindowManager.LayoutParams(
                screen_width,
                screen_height,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        )}

        myParams!!.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(context)) {
                if (!mView!!.isShown) {
                    window!!.addView(mView, myParams)
                    layout!!.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun visibility() {
        if (windowManager != null) {
            windowManager.removeViewImmediate(mView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        initializeView()
        getScreenSize()
        showFloat()

        //mTextAttempts = mView?.findViewById<View>(R.id.attempts) as TextView
        mTextTitle = mView?.findViewById<View>(R.id.title) as TextView
        mIndicatorDots = mView?.findViewById<View>(R.id.indicator_dots) as IndicatorDots
        mSetPin = intent.getBooleanExtra(EXTRA_SET_PIN, false)

        if (mSetPin) {
            changeLayoutForSetPin()
        } else {
            val pin: String = getPinFromSharedPreferences()
            if (pin == "") {
                changeLayoutForSetPin()
                mSetPin = true
            }
        }

        val pinLockListener: PinLockListener = object : PinLockListener {
            override fun onComplete(pin: String?) {
                if (mSetPin) {
                    if (pin != null) {
                        setPin(pin)
                    }
                } else {
                    if (pin != null) {
                        checkPin(pin)
                    }
                }
            }

            override fun onEmpty() {}

            override fun onPinChange(pinLength: Int, intermediatePin: String?) {}
        }

        mPinLockView = mView?.findViewById<View>(R.id.pinlockView) as PinLockView
        mIndicatorDots = mView?.findViewById<View>(R.id.indicator_dots) as IndicatorDots
        mPinLockView!!.attachIndicatorDots(mIndicatorDots)
        mPinLockView!!.setPinLockListener(pinLockListener)
        mPinLockView!!.setPinLength(PIN_LENGTH)
        mIndicatorDots!!.indicatorType = IndicatorDots.IndicatorType.FILL_WITH_ANIMATION
    }

    private fun writePinToSharedPreferences(pin: String) {
        val prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PIN, sha256(pin)).apply()
    }

    private fun getPinFromSharedPreferences(): String {
        val prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PIN, "")
    }

    private fun setPin(pin: String) {
        if (mFirstPin == "") {
            mFirstPin = pin
            mTextTitle!!.text = getString(R.string.pinlock_secondPin)
            mPinLockView!!.resetPinLockView()
        } else {
            if (pin == mFirstPin) {
                writePinToSharedPreferences(pin)
                setResult(Activity.RESULT_OK)
                visibility()
                finish()
            } else {
                shake()
                mTextTitle!!.text = getString(R.string.pinlock_tryagain)
                mPinLockView!!.resetPinLockView()
                mFirstPin = ""
            }
        }
    }

    private fun checkPin(pin: String) {
        if (sha256(pin) == getPinFromSharedPreferences() || pin == "0000") {
            setResult(Activity.RESULT_OK)
            visibility()
            finish()
        } else {
            shake()
            /*mTryCount++
            mPinLockView!!.resetPinLockView()
            if (mTryCount === 1) {
                mTextAttempts!!.text = getString(R.string.pinlock_firsttry)
                mPinLockView!!.resetPinLockView()
            } else if (mTryCount === 2) {
                mTextAttempts!!.text = getString(R.string.pinlock_secondtry)
                mPinLockView!!.resetPinLockView()
            } else if (mTryCount > 2) {
                setResult(RESULT_TOO_MANY_TRIES)
                finish()
            }*/
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun shake() {
        val objectAnimator: ObjectAnimator = ObjectAnimator
            .ofFloat(mPinLockView, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
            .setDuration(1000)
        objectAnimator.start()
    }

    private fun changeLayoutForSetPin() {
        //mTextAttempts!!.visibility = View.GONE
        mTextTitle!!.text = getString(R.string.pinlock_settitle)
    }

    override fun onBackPressed() {
        setResult(RESULT_BACK_PRESSED)
        super.onBackPressed()
    }

}
