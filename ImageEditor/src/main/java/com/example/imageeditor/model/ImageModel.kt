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
import com.example.imageeditor.utils.Size
import com.example.imageeditor.utils.Vector3D
import com.example.imageeditor.utils.createIdentity4Matrix
import com.example.imageeditor.utils.floatBufferOf
import com.example.imageeditor.utils.intBufferOf
import com.example.imageeditor.utils.runGL
import java.nio.FloatBuffer
import kotlin.math.abs

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
    private val topLeftVector3D = Vector3D(-1f, 1f, 0f)
    private val topRightVector3D = Vector3D(1f, 1f, 0f)
    private val bottomLeftVector3D = Vector3D(-1f, -1f, 0f)
    private val bottomRightVector3D = Vector3D(1f, -1f, 0f)

    private val vertices = floatBufferOf(
        // x, y, z, texture_x, texture_y
        topLeftVector3D.x, topLeftVector3D.y, topLeftVector3D.z, 0.0f, 1.0f, // top left
        bottomLeftVector3D.x, bottomLeftVector3D.y, topLeftVector3D.z, 0.0f, 0.0f, // bottom left
        topRightVector3D.x, topRightVector3D.y, topRightVector3D.z, 1.0f, 1.0f, // top right
        bottomRightVector3D.x, bottomRightVector3D.y, bottomRightVector3D.z, 1.0f, 0.0f,  // bottom right
        0f, 0f, 0f, 0.5f, 0.5f
    )


    private val vertexIndices = intBufferOf(
        1, 2, 0,
        2, 0, 3,
        1, 3, 4
    )

    override val size: Size
        get() = kotlin.run {
            val localCombinedMatrix = getCombinedMatrix()
            val leftTop = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, topLeftVector3D.array, 0)
            }
            val leftBottom = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, bottomLeftVector3D.array, 0)
            }
            val rightTop = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, topRightVector3D.array, 0)
            }
            Size(
                width = abs(rightTop[0]) + abs(leftTop[0]),
                height = abs(leftTop[1]) + abs(leftBottom[1])
            )
        }

    override val center: Vector3D
        get() = kotlin.run {
            val localCombinedMatrix = getCombinedMatrix()
            val leftTop = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, topLeftVector3D.array, 0)
            }
            val leftBottom = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, bottomLeftVector3D.array, 0)
            }
            val rightTop = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, topRightVector3D.array, 0)
            }

            Vector3D(
                x = (leftTop[0] + rightTop[0]) / 2,
                y = (leftTop[1] + leftBottom[1]) / 2,
                z = leftTop[2]
            )
        }

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
        //todo texture flip
        updateScale(0.5f,0.5f, 0.5f)
    }

    override fun draw() {
        if (!isVisible) return
        program.bind()
        texture.bind()
        program.updateUniformMatrix4f("u_Model", getCombinedBuffer())
        program.updateUniformMatrix4f("u_Projection", projectionBuffer)
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
