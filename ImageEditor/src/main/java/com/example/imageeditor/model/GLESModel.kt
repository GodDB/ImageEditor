package com.example.imageeditor.model

import android.opengl.Matrix
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.utils.asBuffer
import com.example.imageeditor.utils.createIdentity4Matrix
import com.example.imageeditor.utils.deepCopy
import java.nio.FloatBuffer

internal abstract class GLESModel(
    val scaleM: FloatArray = createIdentity4Matrix(),
    val transM: FloatArray = createIdentity4Matrix(),
    val rotateM: FloatArray = createIdentity4Matrix(),
    val combinedM: FloatArray = createIdentity4Matrix()
) {
    val combineBuffer: FloatBuffer = combinedM.asBuffer()

    abstract val program: ShaderProgram
    abstract val isVisible: Boolean

    abstract fun init(width: Int, height: Int)

    abstract fun draw()

    abstract fun setVisible(visible: Boolean)

    open fun onTouchDown(x: Float, y: Float): Boolean {
        return false
    }

    open fun onTouchMove(x: Float, y: Float, deltaX: Float, deltaY: Float) {

    }

    open fun onTouchUp() {

    }

    protected fun updateTranslation(x: Float, y: Float, z: Float) {
        Matrix.translateM(transM, 0, x, y, z)
        Matrix.translateM(combinedM, 0, x, y, z)
    }

    protected fun updateRotation(angle: Float, weightX: Float, weightY: Float, weightZ: Float) {
        Matrix.rotateM(rotateM, 0, angle, weightX, weightY, weightZ)
        Matrix.rotateM(combinedM, 0, angle, weightX, weightY, weightZ)
    }

    protected fun updateScale(scaleX: Float, scaleY: Float, scaleZ: Float) {
        Matrix.scaleM(scaleM, 0, scaleX, scaleY, scaleZ)
        Matrix.scaleM(combinedM, 0, scaleX, scaleY, scaleZ)
    }

    fun getDeepCopiedScaleM(): FloatArray {
        return scaleM.deepCopy()
    }

    fun getDeepCopiedTransM(): FloatArray {
        return transM.deepCopy()
    }

    fun getDeepCopiedRotateM(): FloatArray {
        return rotateM.deepCopy()
    }

    fun getDeepCopiedCombinedM(): FloatArray {
        return combinedM.deepCopy()
    }

}
