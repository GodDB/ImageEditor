package com.example.imageeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.opengl.GLES20
import android.util.Log
import com.example.imageeditor.core.GLESRenderer
import com.example.imageeditor.model.GLESModel
import com.example.imageeditor.model.ImageModel
import com.example.imageeditor.model.OverlayModel
import com.example.imageeditor.utils.runGL
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class ImageEditorRenderer(private val context: Context) : GLESRenderer() {

    private val models = mutableListOf<GLESModel>()

    private var glViewWidth: Int = 0
    private var glViewHeight: Int = 0

    private var pressedPoint: PointF? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.e("godgod", "onSurfaceCreated")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.e("godgod", "onSurfaceChanged")
        glViewWidth = width
        glViewHeight = height
        runGL { GLES20.glViewport(0, 0, width, height) }
        runGL { GLES20.glEnable(GLES20.GL_CULL_FACE) } // 벡터 외적이 후면을 바라보는 부분 제거
        runGL { GLES20.glEnable(GLES20.GL_DEPTH_TEST) } // z버퍼 생성

        models.forEach {
            it.init(glViewWidth, glViewHeight)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        runGL { GLES20.glClearColor(0f, 0f, 0f, 0f) }
        runGL { GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT) }

        models.forEach {
            it.draw()
        }
    }

    fun addBitmap(bitmap: Bitmap) {
        val newModel = OverlayModel(
            context,
            ImageModel(bitmap, context)
        )
        if (currentState.value >= Lifecycle.DRAW.value) {
            newModel.init(glViewWidth, glViewHeight)
        }
        models.add(newModel)
    }

    fun onTouchDown(x: Float, y: Float) {
        val normalizeX = ((x / glViewWidth) * 2) - 1
        val normalizeY = 1 - ((y / glViewHeight) * 2)
        pressedPoint = PointF(normalizeX, normalizeY)
        for (model in models) {
            if (model.onTouchDown(normalizeX, normalizeY)) {
                return
            }
        }
    }

    fun onTouchMove(x: Float, y: Float) {
        val normalizeX = ((x / glViewWidth) * 2) - 1
        val normalizeY = 1 - ((y / glViewHeight) * 2)
        for (model in models) {
            val deltaX = (pressedPoint?.x ?: 0f) - normalizeX
            val deltaY = (pressedPoint?.y ?: 0f) - normalizeY
            model.onTouchMove(normalizeX, normalizeY, deltaX, deltaY * 3)
        }
        pressedPoint?.x = normalizeX
        pressedPoint?.y = normalizeY
    }

    fun onTouchUp() {
        for (model in models) {
            model.onTouchUp()
        }
        pressedPoint = null
    }
}
