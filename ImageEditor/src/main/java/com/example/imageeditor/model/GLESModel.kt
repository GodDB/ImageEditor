package com.example.imageeditor.model

import android.opengl.Matrix
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.utils.Size
import com.example.imageeditor.utils.Vector3D
import com.example.imageeditor.utils.asBuffer
import com.example.imageeditor.utils.createIdentity4Matrix
import com.example.imageeditor.utils.deepCopy
import com.example.imageeditor.utils.readOnlyIdentity4Matrix
import java.nio.FloatBuffer

internal abstract class GLESModel(
    protected val scaleM: FloatArray = createIdentity4Matrix(),
    protected val transM: FloatArray = createIdentity4Matrix(),
    protected val rotateM: FloatArray = createIdentity4Matrix()
) {
    private val combinedMatrix: FloatArray = createIdentity4Matrix()
    private val combinedBuffer: FloatBuffer = combinedMatrix.asBuffer()

    private val TRMatrix: FloatArray = createIdentity4Matrix()
    private val TRSMatrix: FloatArray = createIdentity4Matrix()
    protected fun getCombinedMatrix(): FloatArray {
        return this.combinedMatrix.apply {
            Matrix.multiplyMM(TRMatrix, 0, transM, 0, rotateM, 0)
            Matrix.multiplyMM(TRSMatrix, 0, TRMatrix, 0, scaleM, 0)
            Matrix.multiplyMM(this, 0, TRSMatrix, 0, readOnlyIdentity4Matrix, 0)
        }
    }

    protected fun getCombinedBuffer(): FloatBuffer {
        getCombinedMatrix()
        return combinedBuffer
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

    val projectionM: FloatArray = createIdentity4Matrix()
    val projectionBuffer: FloatBuffer = projectionM.asBuffer()

    protected val inverseProjectionM: FloatArray = createIdentity4Matrix()
        get() {
            Matrix.invertM(field, 0, projectionM, 0)
            return field
        }

    private val inverseSRMatrix = createIdentity4Matrix()
    private val inverseSRTMatrix = createIdentity4Matrix()

    protected val inverseCombinedM: FloatArray = createIdentity4Matrix()
        get() {
            // RTS의 역행렬
            Matrix.multiplyMM(inverseSRMatrix, 0, inverseScaleM, 0, inverseRotateM, 0)
            Matrix.multiplyMM(inverseSRTMatrix, 0, inverseSRMatrix, 0, inverseTransM, 0)
            Matrix.multiplyMM(field, 0, inverseSRTMatrix, 0, readOnlyIdentity4Matrix, 0)
            return field
        }
    protected val inverseRotateM: FloatArray = createIdentity4Matrix()
        get() {
            Matrix.invertM(field, 0, rotateM, 0)
            return field
        }
    protected val inverseTransM: FloatArray = createIdentity4Matrix()
        get() {
            Matrix.invertM(field, 0, transM, 0)
            return field
        }
    protected val inverseScaleM: FloatArray = createIdentity4Matrix()
        get() {
            // scale 메트릭스는 역행렬을 만들어 주는 기능이 없어서 직접 구현
            Matrix.setIdentityM(field, 0)
            field[0] = 1 / scaleM[0]
            field[5] = 1 / scaleM[5]
            field[10] = 1 / scaleM[10]
            return field
        }

    fun dispatchInit(width: Int, height: Int, newProjectionM: FloatArray) {
        Matrix.multiplyMM(projectionM, 0, newProjectionM, 0, readOnlyIdentity4Matrix, 0)
        init(width, height)
    }

    protected abstract fun init(width: Int, height: Int)

    fun dispatchDraw() {
        draw()
    }

    protected abstract fun draw()

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
        Matrix.multiplyMM(transM, 0, newTransM, 0, readOnlyIdentity4Matrix, 0)
    }

    fun updateRotation(newRotateM: FloatArray) {
        Matrix.multiplyMM(rotateM, 0, newRotateM, 0, readOnlyIdentity4Matrix, 0)
    }

    fun updateScale(newScaleM: FloatArray) {
        Matrix.multiplyMM(scaleM, 0, newScaleM, 0, readOnlyIdentity4Matrix, 0)
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

    fun setTranslation(x: Float, y: Float, z: Float) {
        val newTransM = createIdentity4Matrix().apply {
            Matrix.translateM(this, 0, x, y, z)
        }
        Matrix.setIdentityM(transM, 0)
        Matrix.multiplyMM(transM, 0, readOnlyIdentity4Matrix, 0, newTransM, 0)
    }

    fun setTranslation(newTransM: FloatArray) {
        Matrix.setIdentityM(transM, 0)
        Matrix.multiplyMM(transM, 0, readOnlyIdentity4Matrix, 0, newTransM, 0)
    }

    fun getCopiedScaleM(): FloatArray {
        return scaleM.deepCopy()
    }

    fun getCopiedTransM(): FloatArray {
        return transM.deepCopy()
    }

    fun getCopiedRotateM(): FloatArray {
        return rotateM.deepCopy()
    }
}
