package com.martinmarinkovic.myapplication.lockscreen

interface PinLockListener {

    fun onComplete(pin: String?)

    fun onEmpty()

    fun onPinChange(pinLength: Int, intermediatePin: String?)
}