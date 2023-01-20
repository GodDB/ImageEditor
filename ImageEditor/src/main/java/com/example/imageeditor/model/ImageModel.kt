package com.example.imageeditor.model

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.Matrix
import com.example.imageeditor.R
import com.example.imageeditor.core.shader.Shader
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.core.texture.Texture
import com.example.imageeditor.utils.FLOAT_BYTE_SIZE
import com.example.imageeditor.utils.FileReader
import com.example.imageeditor.utils.asBuffer
import com.example.imageeditor.utils.createIdentity4Matrix
import com.example.imageeditor.utils.floatBufferOf
import com.example.imageeditor.utils.intBufferOf
import com.example.imageeditor.utils.runGL
import java.nio.FloatBuffer

internal class ImageModel(
    bitmap: Bitmap,
    context: Context,
) : GLESModel {

    private var _combinedM: FloatBuffer = createIdentity4Matrix().asBuffer()
    override val combinedM: FloatBuffer
        get() = _combinedM

    private var isPressed : Boolean = false

    private val vertices = floatBufferOf(
        // x, y, z, texture_x, texture_y
        -1f, 1f, 0.0f, 0.0f, 1.0f, // top left
        -1f, -1f, 0.0f, 0.0f, 0.0f, // bottom left
        1f, 1f, 0.0f, 1.0f, 1.0f, // top right
        1f, -1f, 0.0f, 1.0f, 0.0f,  // bottom right
        0f, 0f, 0f, 0.5f, 0.5f
    )

    private val vertexIndices = intBufferOf(
        1, 2, 0,
        2, 0, 3,
        1, 3, 4
    )

    override val program by lazy {
        runGL {
            val vertexShaderSourceCode = FileReader.readFile(context, R.raw.image_vertex_shader)
            val fragmentShaderSourceCode = FileReader.readFile(context, R.raw.image_fragment_shader)
            ShaderProgram(
                vertexShader = Shader(vertexShaderSourceCode, Shader.Type.VERTEX),
                fragmentShader = Shader(fragmentShaderSourceCode, Shader.Type.FRAGMENT)
            )
        }
    }

    private var _isVisible : Boolean = true
    override val isVisible: Boolean
        get() = _isVisible

    private val texture: Texture by lazy {
        Texture(bitmap)
    }

    override fun init(width: Int, height: Int) {
        // 텍스처는 기본적으로 gl과 상하반전이 있기 때문에 z축 180도 회전한다.
        val scaleM = getTextureScaleMatrix(width, height, texture.width, texture.height)
        val rotateM = getTextureRotateMatrix()
        val matrix = createIdentity4Matrix()
        Matrix.multiplyMM(matrix, 0, scaleM, 0, rotateM, 0)
        _combinedM = matrix.asBuffer()
    }

    private fun getTextureScaleMatrix(deviceWidth: Int, deviceHeight: Int, textureWidth: Int, textureHeight: Int): FloatArray {
        val scaleX = if (deviceWidth > textureWidth) {
            textureWidth.toFloat() / deviceWidth
        } else {
            1f
        }

        val scaleY = if (deviceHeight > textureHeight) {
            textureHeight.toFloat() / deviceHeight
        } else {
            1f
        }
        val scaleM = createIdentity4Matrix()
        Matrix.scaleM(scaleM, 0, scaleX, scaleY, 1f)
        return scaleM
    }

    private fun getTextureRotateMatrix(): FloatArray {
        val matrix = createIdentity4Matrix()
        Matrix.rotateM(matrix, 0, 180f, 0f, 0f, 1f)
        return matrix
    }

    override fun draw() {
        if(!isVisible) return
        program.bind()
        texture.bind()
        program.updateUniformMatrix4f("u_Model", _combinedM)
        drawVertices(program)
        drawTexture(program)
        runGL { GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexIndices.capacity(), GLES20.GL_UNSIGNED_INT, vertexIndices) }
        runGL { GLES20.glEnableVertexAttribArray(0) } // vertexArray를 비활성화 한다.

        texture.unbind()
        program.unbind()
    }

    private fun drawVertices(program: ShaderProgram) {
        val vertexPointer = runGL { program.getAttributePointer("v_Position") }

        vertices.position(0)
        runGL { GLES20.glVertexAttribPointer(vertexPointer, 3, GLES20.GL_FLOAT, false, 5 * FLOAT_BYTE_SIZE, vertices) } // vertex정보들을 vertexArray에게 전달한다.
        runGL { GLES20.glEnableVertexAttribArray(vertexPointer) } // vertexArray를 활성화 한다.
    }

    private fun drawTexture(program: ShaderProgram) {
        val texturePointer = runGL { program.getAttributePointer("v_Texture_Position") }

        vertices.position(3)
        runGL { GLES20.glVertexAttribPointer(texturePointer, 2, GLES20.GL_FLOAT, false, 5 * FLOAT_BYTE_SIZE, vertices) } // vertex정보들을 vertexArray에게 전달한다.
        runGL { GLES20.glEnableVertexAttribArray(texturePointer) } // vertexArray를 활성화 한다.
    }

    override fun setVisible(visible: Boolean) {
        _isVisible = visible
    }

    override fun onTouchDown(x: Float, y: Float): Boolean {
        // is contains
        isPressed = true
        return isPressed
    }

    override fun onTouchMove(x: Float, y: Float, deltaX : Float , deltaY : Float) {
        if(!isPressed) return

        Matrix.translateM(_combinedM.array(), 0, deltaX, deltaY, 0f)
    }

    override fun onTouchUp() {
        if(!isPressed) return

        isPressed = false
    }
}
