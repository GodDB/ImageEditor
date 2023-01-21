package com.example.imageeditor.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap

object BitmapUtil {

    fun generate(context: Context, @DrawableRes imageRes: Int): Bitmap {
        return AppCompatResources.getDrawable(context, imageRes)?.toBitmap() ?: throw Exception("not convert image resource to bitmap")
    }
}
