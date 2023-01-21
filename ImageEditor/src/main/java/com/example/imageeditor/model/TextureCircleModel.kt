package com.example.imageeditor.model

import android.content.Context
import android.opengl.GLES20
import androidx.annotation.DrawableRes
import com.example.imageeditor.R
import com.example.imageeditor.core.shader.Shader
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.core.texture.Texture
import com.example.imageeditor.utils.BitmapUtil
import com.example.imageeditor.utils.FLOAT_BYTE_SIZE
import com.example.imageeditor.utils.FileReader
import com.example.imageeditor.utils.floatBufferOf
import com.example.imageeditor.utils.intBufferOf
import com.example.imageeditor.utils.runGL
import com.example.imageeditor.utils.toBuffer

internal class TextureCircleModel(
    context: Context,
    @DrawableRes imgRes: Int,
    centerX: Float,
    centerY: Float,
    centerZ: Float,
    radius: Float
) : GLESModel() {
    override val program: ShaderProgram by lazy {
        runGL {
            val vertexShaderSourceCode = FileReader.readFile(context, R.raw.texture_circle_vertex_shader)
            val fragmentShaderSourceCode = FileReader.readFile(context, R.raw.texture_circle_fragment_shader)
            ShaderProgram(
                vertexShader = Shader(vertexShaderSourceCode, Shader.Type.VERTEX),
                fragmentShader = Shader(fragmentShaderSourceCode, Shader.Type.FRAGMENT)
            )
        }
    }

    private val pointSize: Int = 100

    private val circleVertices = kotlin.run {
        val floatArray = mutableListOf<Float>(centerX, centerY, centerZ)
        for (i in 0..pointSize) {
            val radian = (i.toFloat() / pointSize.toFloat()) * (2 * Math.PI)
            // 해당 라디안의 점 구해서 넣기
            floatArray.add(centerX + (radius * Math.cos(radian).toFloat())) // x
            floatArray.add(centerY) // y
            floatArray.add(centerZ + (radius * Math.sin(radian).toFloat()))
        }
        floatArray.toBuffer()
    }

    private val circleVertexIndices = kotlin.run {
        (0..pointSize + 1).toList().toBuffer()
    }

    private val textureVertices = floatBufferOf(
        // x, y, z, texture_x, texture_y
        -1f * radius, 1f * radius, 0.0f, 0.0f, 1.0f, // top left
        -1f * radius, -1f * radius, 0.0f, 0.0f, 0.0f, // bottom left
        1f * radius, 1f * radius, 0.0f, 1.0f, 1.0f, // top right
        1f * radius, -1f * radius, 0.0f, 1.0f, 0.0f,  // bottom right
        0f, 0f, 0f, 0.5f, 0.5f
    )

    private val textureVertexIndices = intBufferOf(
        1, 2, 0,
        2, 0, 3,
        1, 3, 4
    )

    private val texture by lazy {
        Texture(BitmapUtil.generate(context, imgRes))
    }


    override fun init(width: Int, height: Int) {
        updateScale(1f, width / height.toFloat(), 1f)
    }

    override fun draw() {
        program.bind()
        texture.bind()
        program.updateUniformMatrix4f("u_Trans", transBuffer)
        program.updateUniformMatrix4f("u_Rotate", rotateBuffer)
        program.updateUniformMatrix4f("u_Scale", scaleBuffer)

        drawTexture()
        drawCircle()

        program.unbind()
        texture.unbind()
    }

    private fun drawTexture() {
        val vertexPointer = runGL { program.getAttributePointer("v_Position") }

        program.updateUniformInt("u_IsTexture", 0)

        textureVertices.position(0)
        runGL { GLES20.glVertexAttribPointer(vertexPointer, 3, GLES20.GL_FLOAT, false, 5 * FLOAT_BYTE_SIZE, textureVertices) } // vertex정보들을 vertexArray에게 전달한다.
        runGL { GLES20.glEnableVertexAttribArray(vertexPointer) } // vertexArray를 활성화 한다.

        val texturePointer = runGL { program.getAttributePointer("v_Texture_Position") }

        textureVertices.position(3)
        runGL { GLES20.glVertexAttribPointer(texturePointer, 2, GLES20.GL_FLOAT, false, 5 * FLOAT_BYTE_SIZE, textureVertices) } // vertex정보들을 vertexArray에게 전달한다.
        runGL { GLES20.glEnableVertexAttribArray(texturePointer) } // vertexArray를 활성화 한다.

        runGL { GLES20.glDrawElements(GLES20.GL_TRIANGLES, textureVertexIndices.capacity(), GLES20.GL_UNSIGNED_INT, textureVertexIndices) }
        runGL { GLES20.glEnableVertexAttribArray(0) } // vertexArray를 비활성화 한다.
    }

    private fun drawCircle() {
        val vertexPointer = runGL { program.getAttributePointer("v_Position") }

        program.updateUniformInt("u_IsTexture", 1)

        circleVertices.position(0)
        runGL { GLES20.glVertexAttribPointer(vertexPointer, 3, GLES20.GL_FLOAT, false, 3 * FLOAT_BYTE_SIZE, circleVertices) } // vertex정보들을 vertexArray에게 전달한다.
        runGL { GLES20.glEnableVertexAttribArray(vertexPointer) } // vertexArray를 활성화 한다.
        runGL { GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, circleVertexIndices.capacity(), GLES20.GL_UNSIGNED_INT, circleVertexIndices) }
        runGL { GLES20.glEnableVertexAttribArray(0) } // vertexArray를 비활성화 한다.
    }
}
