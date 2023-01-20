package com.example.imageeditor.utils

import android.content.Context

object AndroidUtil {

    fun Context.getStatusBarHeight() : Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

}
