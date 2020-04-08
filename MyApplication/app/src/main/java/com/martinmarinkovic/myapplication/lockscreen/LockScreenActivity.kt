package com.martinmarinkovic.myapplication.lockscreen

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
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
        const val EXTRA_FONT_TEXT = "textFont"
        const val EXTRA_FONT_NUM = "numFont"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)
        mTextAttempts = findViewById<View>(R.id.attempts) as TextView
        mTextTitle = findViewById<View>(R.id.title) as TextView
        mIndicatorDots = findViewById<View>(R.id.indicator_dots) as IndicatorDots

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

        mPinLockView = findViewById<View>(R.id.pinlockView) as PinLockView
        mIndicatorDots = findViewById<View>(R.id.indicator_dots) as IndicatorDots
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
        if (sha256(pin) == getPinFromSharedPreferences()) {
            setResult(Activity.RESULT_OK)
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
        mTextAttempts!!.visibility = View.GONE
        mTextTitle!!.text = getString(R.string.pinlock_settitle)
    }

    override fun onBackPressed() {
        setResult(RESULT_BACK_PRESSED)
        super.onBackPressed()
    }

}
