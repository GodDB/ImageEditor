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
import com.example.imageeditor.utils.Vector3D
import com.example.imageeditor.utils.asBuffer
import com.example.imageeditor.utils.createIdentity4Matrix
import com.example.imageeditor.utils.floatBufferOf
import com.example.imageeditor.utils.intBufferOf
import com.example.imageeditor.utils.runGL

internal class ImageModel(
    bitmap: Bitmap,
    context: Context,
    inputScaleM: FloatArray = createIdentity4Matrix(),
    inputTransM: FloatArray = createIdentity4Matrix(),
    inputRotateM: FloatArray = createIdentity4Matrix()
) : GLESModel(
    scaleM = inputScaleM,
    transM = inputTransM,
    rotateM = inputRotateM
) {

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

    private val texture: Texture by lazy {
        Texture(bitmap)
    }

    override fun init(width: Int, height: Int) {
        // 텍스처는 기본적으로 gl과 상하반전이 있기 때문에 z축 180도 회전한다.
        val scaleVector = getTextureScaleVector(width, height, texture.width, texture.height)
        updateScale(scaleVector.x, scaleVector.y, scaleVector.z)
        updateRotation(180f, 0f, 0f, 1f)
    }

    private fun getTextureScaleVector(deviceWidth: Int, deviceHeight: Int, textureWidth: Int, textureHeight: Int): Vector3D {
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
        return Vector3D(scaleX, scaleY, 1f)
    }

    override fun draw() {
        if (!isVisible) return
        program.bind()
        texture.bind()
        program.updateUniformMatrix4f("u_Trans", transBuffer)
        program.updateUniformMatrix4f("u_Rotate", rotateBuffer)
        program.updateUniformMatrix4f("u_Scale", scaleBuffer)
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
}
