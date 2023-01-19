package com.example.imageeditor

import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImageEditorRenderer : GLSurfaceView.Renderer {

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.e("godgod", "onSurfaceCreated")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.e("godgod", "onSurfaceChanged")
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.e("godgod", "onDrawFrame")
    }
}
