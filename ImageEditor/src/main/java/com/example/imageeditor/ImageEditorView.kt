package com.example.imageeditor

import android.content.Context
import android.opengl.GLSurfaceView
import androidx.annotation.DrawableRes
import com.example.imageeditor.utils.BitmapUtil

class ImageEditorView(_context: Context) : GLSurfaceView(_context) {

    private val renderer: ImageEditorRenderer by lazy {
        ImageEditorRenderer(context)
    }

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }

    fun init(imgRes: Int) {
        val bitmap = BitmapUtil.generate(context, imgRes)
        queueEvent {
            renderer.setImageBitmap(bitmap)
        }
    }

    fun init(imageUrl: String) {
        //todo
    }

}
