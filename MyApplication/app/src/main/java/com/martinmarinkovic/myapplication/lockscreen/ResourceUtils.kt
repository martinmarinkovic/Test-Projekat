package com.martinmarinkovic.myapplication.lockscreen

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

class ResourceUtils {

    constructor() {
        throw AssertionError()
    }

    companion object {
        fun getColor(context: Context?, @ColorRes id: Int): Int {
            return ContextCompat.getColor(context!!, id)
        }

        fun getDimensionInPx(context: Context, @DimenRes id: Int): Float {
            return context.resources.getDimension(id)
        }

        fun getDrawable(context: Context?, @DrawableRes id: Int): Drawable? {
            return ContextCompat.getDrawable(context!!, id)
        }
    }
}