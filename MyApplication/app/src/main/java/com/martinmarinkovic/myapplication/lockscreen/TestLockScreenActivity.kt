package com.martinmarinkovic.myapplication.lockscreen

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.andrognito.pinlockview.PinLockView
import com.martinmarinkovic.myapplication.R

class TestLockScreenActivity : AppCompatActivity() {

    val TAG = "PinLockView"

    private val mPinLockListener: PinLockListener = object : PinLockListener {
        override fun onComplete(pin: String) {
            Log.d(TAG, "Pin complete: $pin")
        }

        override fun onEmpty() {
            Log.d(TAG, "Pin empty")
        }

        override fun onPinChange(pinLength: Int, intermediatePin: String) {
            Log.d(
                TAG,
                "Pin changed, new length $pinLength with intermediate pin $intermediatePin"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //val mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        setContentView(R.layout.activity_test_lock_screen)

        val mPinLockView: PinLockView? = findViewById(R.id.pin_lock_view);
        val mIndicatorDots: IndicatorDots? = findViewById(R.id.indicator_dots);

        mPinLockView?.attachIndicatorDots(mIndicatorDots)
        mPinLockView?.setPinLockListener(mPinLockListener)
        //pin_lock_view.customKeySet = intArrayOf(0, 0, 0, 0)
        //pin_lock_view.enableLayoutShuffling()
        mPinLockView?.pinLength = 4
        mPinLockView?.textColor = resources.getColor(R.color.white)
        mPinLockView?.buttonBackgroundDrawable = resources.getDrawable(R.drawable.btn_0)

        if (mIndicatorDots != null) {
            mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION)
        };
    }
}