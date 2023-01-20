package com.example.imageeditor.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtil {

    fun generate(context: Context, imageRes: Int): Bitmap {
        val bitmapOptions = BitmapFactory.Options().apply {
            this.inScaled = false // 원본 이미지로 달라!
        }

        return BitmapFactory.decodeResource(context.resources, imageRes, bitmapOptions) ?: throw java.lang.RuntimeException("not generated bitmap")
    }
}
