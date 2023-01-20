package com.example.imageeditor.model

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.example.imageeditor.R
import com.example.imageeditor.core.shader.Shader
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.utils.FLOAT_BYTE_SIZE
import com.example.imageeditor.utils.FileReader
import com.example.imageeditor.utils.asBuffer
import com.example.imageeditor.utils.createIdentity4Matrix
import com.example.imageeditor.utils.deepCopy
import com.example.imageeditor.utils.floatBufferOf
import com.example.imageeditor.utils.intBufferOf
import com.example.imageeditor.utils.runGL
import com.example.imageeditor.utils.toFloatArray
import java.nio.FloatBuffer

internal class ModelOverlay(
    context: Context,
    private val otherModel: GLESModel
) : GLESModel {

    private var _combinedM: FloatBuffer = createIdentity4Matrix().asBuffer()
    override val combinedM: FloatBuffer
        get() = _combinedM.deepCopy()


    private val vertices = floatBufferOf(
        // x, y, z
        -1f, 1f, 0.0f, // top left
        -1f, -1f, 0.0f, // bottom left
        1f, 1f, 0.0f, // top right
        1f, -1f, 0.0f,  // bottom right
    )

    private val vertexIndices = intBufferOf(
        0, 2,
        2, 3,
        3, 1,
        1, 0
    )

    override val program: ShaderProgram by lazy {
        runGL {
            val vertexShaderSourceCode = FileReader.readFile(context, R.raw.overlay_vertex_shader)
            val fragmentShaderSourceCode = FileReader.readFile(context, R.raw.overlay_fragment_shader)
            ShaderProgram(
                vertexShader = Shader(vertexShaderSourceCode, Shader.Type.VERTEX),
                fragmentShader = Shader(fragmentShaderSourceCode, Shader.Type.FRAGMENT)
            )
        }
    }

    private var _isVisible: Boolean = false
    override val isVisible: Boolean
        get() = _isVisible

    private var isPressed: Boolean = false

    override fun init(width: Int, height: Int) {
        otherModel.init(width, height)
        val otherMatrix = otherModel.combinedM.toFloatArray()
        Matrix.scaleM(otherMatrix, 0, 1.1f, 1.1f, 1.1f)
        _combinedM = otherMatrix.asBuffer()
    }

    override fun draw() {
        otherModel.draw()
        if (!isVisible) return
        program.bind()

        program.updateUniformMatrix4f("u_Model", _combinedM)

        val vertexPointer = runGL { program.getAttributePointer("v_Position") }

        vertices.position(0)
        runGL { GLES20.glVertexAttribPointer(vertexPointer, 3, GLES20.GL_FLOAT, false, 3 * FLOAT_BYTE_SIZE, vertices) } // vertex정보들을 vertexArray에게 전달한다.
        runGL { GLES20.glEnableVertexAttribArray(vertexPointer) } // vertexArray를 활성화 한다.
        runGL { GLES20.glDrawElements(GLES20.GL_LINES, vertexIndices.capacity(), GLES20.GL_UNSIGNED_INT, vertexIndices) }
        runGL { GLES20.glEnableVertexAttribArray(0) } // vertexArray를 비활성화 한다.

        program.unbind()
    }

    override fun setVisible(visible: Boolean) {
        _isVisible = visible
    }

    override fun onTouchDown(x: Float, y: Float): Boolean {
        isPressed = isTouched(x, y)
        setVisible(isPressed)
        if (isPressed) {
            otherModel.onTouchDown(x, y)
        }
        return isPressed
    }

    private fun isTouched(x: Float, y: Float): Boolean {
        val inverseCombinedM = createIdentity4Matrix()
        Matrix.invertM(inverseCombinedM, 0, _combinedM.array(), 0)
        val point = floatArrayOf(x, y, 0f, 1f)
        val notNormalizePoint = floatArrayOf(0f, 0f, 0f, 0f)
        Matrix.multiplyMV(notNormalizePoint, 0, inverseCombinedM, 0, point, 0)

        val (notNormalX, notNormalY) = notNormalizePoint
        return notNormalX >= -1 && notNormalX <= 1 && notNormalY >= -1 && notNormalY <= 1
    }

    override fun onTouchMove(x: Float, y: Float, deltaX: Float, deltaY: Float) {
        if (!isPressed) return
        otherModel.onTouchMove(x, y, deltaX, deltaY)
        Matrix.translateM(_combinedM.array(), 0, deltaX, deltaY, 0f)
    }

    override fun onTouchUp() {
        if (!isPressed) return
        otherModel.onTouchUp()
        isPressed = false
        setVisible(isPressed)
    }

}
