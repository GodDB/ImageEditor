package com.example.imageeditor

import android.content.Context
import android.opengl.GLSurfaceView

class ImageEditorView(private val context1: Context) : GLSurfaceView(context1) {

    private val renderer : ImageEditorRenderer = ImageEditorRenderer()

    init {
        setRenderer(renderer)
    }
}
