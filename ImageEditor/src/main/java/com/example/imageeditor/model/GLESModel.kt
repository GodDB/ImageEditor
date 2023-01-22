package com.example.imageeditor.model

import android.opengl.Matrix
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.utils.Size
import com.example.imageeditor.utils.Vector3D
import com.example.imageeditor.utils.asBuffer
import com.example.imageeditor.utils.createIdentity4Matrix

internal abstract class GLESModel(
    val scaleM: FloatArray = createIdentity4Matrix(),
    val transM: FloatArray = createIdentity4Matrix(),
    val rotateM: FloatArray = createIdentity4Matrix()
) {
    protected val scaleBuffer = scaleM.asBuffer()
    protected val transBuffer = transM.asBuffer()
    protected val rotateBuffer = rotateM.asBuffer()
    private val combinedMatrix: FloatArray = createIdentity4Matrix()

    protected fun getCombinedMatrix(): FloatArray {
        return this.combinedMatrix.apply {
            Matrix.setIdentityM(this, 0)
            Matrix.multiplyMM(this, 0, this, 0, transM, 0)
            Matrix.multiplyMM(this, 0, this, 0, rotateM, 0)
            Matrix.multiplyMM(this, 0, this, 0, scaleM, 0)
        }
    }

    open val size: Size
        get() = throw UnsupportedOperationException("not supported")

    open val center: Vector3D
        get() = throw UnsupportedOperationException("not supported")

    abstract val program: ShaderProgram
    private var _isVisible: Boolean = true
    val isVisible: Boolean
        get() = _isVisible

    protected var _isPressed: Boolean = false
    val isPressed: Boolean
        get() = _isPressed


    abstract fun init(width: Int, height: Int)

    abstract fun draw()

    fun setVisible(visible: Boolean) {
        _isVisible = visible
    }

    open fun onTouchDown(x: Float, y: Float): Boolean {
        return false
    }

    open fun onTouchMove(x: Float, y: Float, deltaX: Float, deltaY: Float) {

    }

    open fun onTouchUp() {

    }

    fun updateTranslation(newTransM: FloatArray) {
        Matrix.multiplyMM(transM, 0, transM, 0, newTransM, 0)
    }

    fun updateRotation(newRotateM: FloatArray) {
        Matrix.multiplyMM(rotateM, 0, rotateM, 0, newRotateM, 0)
    }

    fun updateScale(newScaleM: FloatArray) {
        Matrix.multiplyMM(scaleM, 0, scaleM, 0, newScaleM, 0)
    }

    fun updateTranslation(x: Float, y: Float, z: Float) {
        Matrix.translateM(transM, 0, x, y, z)
    }

    fun updateRotation(angle: Float, weightX: Float, weightY: Float, weightZ: Float) {
        Matrix.rotateM(rotateM, 0, angle, weightX, weightY, weightZ)
    }

    fun updateScale(scaleX: Float, scaleY: Float, scaleZ: Float) {
        Matrix.scaleM(scaleM, 0, scaleX, scaleY, scaleZ)
    }
}
