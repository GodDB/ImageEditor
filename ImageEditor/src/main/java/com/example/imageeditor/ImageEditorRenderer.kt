package com.example.imageeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.example.imageeditor.core.GLESRenderer
import com.example.imageeditor.model.GLESModel
import com.example.imageeditor.model.ImageModel
import com.example.imageeditor.model.OverlayModel
import com.example.imageeditor.utils.asBuffer
import com.example.imageeditor.utils.createIdentity4Matrix
import com.example.imageeditor.utils.runGL
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class ImageEditorRenderer(private val context: Context) : GLESRenderer() {

    private val models = mutableListOf<GLESModel>()

    private var glViewWidth: Int = 0
    private var glViewHeight: Int = 0

    private var pressedPoint: PointF? = null
    private var movedPoint: PointF? = null

    private val projectM: FloatArray = createIdentity4Matrix()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.e("godgod", "onSurfaceCreated")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.e("godgod", "onSurfaceChanged")
        glViewWidth = width
        glViewHeight = height
        runGL { GLES20.glViewport(0, 0, width, height) }
        //  runGL { GLES20.glEnable(GLES20.GL_CULL_FACE) } // 벡터 외적이 후면을 바라보는 부분 제거
        runGL { GLES20.glEnable(GLES20.GL_DEPTH_TEST) } // z버퍼 생성
        runGL { GLES20.glEnable(GLES20.GL_BLEND) } // 알파 채널 적용
        createProjectionM(glViewWidth, glViewHeight)

        models.forEach {
            it.dispatchInit(glViewWidth, glViewHeight, projectM)
        }
    }

    private fun createProjectionM(width: Int, height: Int) {
        val aspectRatio = if (width > height)
            width.toFloat() / height
        else
            height.toFloat() / width

        if (width >= height) {
            // Landscape
            Matrix.orthoM(projectM, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        } else {
            // Portrait or square
            Matrix.orthoM(projectM, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        runGL { GLES20.glClearColor(0f, 0f, 0f, 0f) }
        runGL { GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT) }

        models.forEach {
            it.dispatchDraw()
        }
    }

    fun addBitmap(bitmap: Bitmap) {
        val newModel = OverlayModel(
            context,
            ImageModel(bitmap, context)
        )
        if (currentState.value >= Lifecycle.DRAW.value) {
            newModel.dispatchInit(glViewWidth, glViewHeight, projectM)
        }
        models.add(newModel)
    }

    fun onTouchDown(x: Float, y: Float) {
        pressedPoint = PointF(x, y)
        movedPoint = PointF(x, y)
        for (model in models) {
            if (model.onTouchDown(x, y)) {
                return
            }
        }
    }

    fun onTouchMove(x: Float, y: Float) {
        for (model in models) {
            model.onTouchMove(
                pressedScreenX = pressedPoint!!.x,
                pressedScreenY = pressedPoint!!.y,
                movedScreenX = x,
                movedScreenY = y,
                prevMovedScreenX = movedPoint!!.x,
                prevMovedScreenY = movedPoint!!.y
            )
        }
        movedPoint?.x = x
        movedPoint?.y = y
    }

    fun onTouchUp() {
        for (model in models) {
            model.onTouchUp()
        }
        pressedPoint = null
        movedPoint = null
    }
}
