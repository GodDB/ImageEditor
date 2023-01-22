package com.example.imageeditor.model

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import androidx.annotation.DrawableRes
import com.example.imageeditor.R
import com.example.imageeditor.core.shader.Shader
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.core.texture.Texture
import com.example.imageeditor.utils.BitmapUtil
import com.example.imageeditor.utils.FLOAT_BYTE_SIZE
import com.example.imageeditor.utils.FileReader
import com.example.imageeditor.utils.Size
import com.example.imageeditor.utils.Vector3D
import com.example.imageeditor.utils.asBuffer
import com.example.imageeditor.utils.createIdentity4Matrix
import com.example.imageeditor.utils.createVector4DArray
import com.example.imageeditor.utils.intBufferOf
import com.example.imageeditor.utils.runGL
import com.example.imageeditor.utils.toBuffer
import kotlin.math.abs

internal class TextureCircleModel(
    context: Context,
    @DrawableRes imgRes: Int,
    private val centerX: Float,
    private val centerY: Float,
    private val centerZ: Float,
    private val radius: Float,
    private val onDragEvent: (prevX: Float, prevY: Float, curX: Float, curY: Float, deltaX: Float, deltaY: Float) -> Unit = { _, _, _, _, _, _ -> }
) : GLESModel() {

    private val leftCenterVector3D = Vector3D(centerX - radius, centerY, centerZ)
    private val topCenterVector3D = Vector3D(centerX, centerY + radius, centerZ)
    private val rightCenterVector3D = Vector3D(centerX + radius, centerY, centerZ)
    private val bottomCenterVector3D = Vector3D(centerX, centerY - radius, centerZ)
    private val centerVector3D = Vector3D(centerX, centerY, centerZ)

    override val size: Size
        get() = kotlin.run {
            val localCombinedMatrix = getCombinedMatrix()
            val leftCenter = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, leftCenterVector3D.array, 0)
            }
            val bottomCenter = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, bottomCenterVector3D.array, 0)
            }
            val rightCenter = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, rightCenterVector3D.array, 0)
            }
            val topCenter = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localCombinedMatrix, 0, topCenterVector3D.array, 0)
            }
            Size(
                width = abs(rightCenter[0]) + abs(leftCenter[0]),
                height = abs(topCenter[1]) + abs(bottomCenter[1])
            )
        }

    override val center: Vector3D
        get() = kotlin.run {
            val localcenter = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, getCombinedMatrix(), 0 , centerVector3D.array, 0)
            }
            Vector3D(
                x = localcenter[0],
                y = localcenter[1],
                z = localcenter[2]
            )
        }

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
        // x, y, z, texture_x, texture_y
        for (i in 0..pointSize) {
            val radian = (i.toFloat() / pointSize.toFloat()) * (2 * Math.PI)
            // 해당 라디안의 점 구해서 넣기
            floatArray.add(centerX + (radius * Math.cos(radian).toFloat())) // x
            floatArray.add(centerY + (radius * Math.sin(radian).toFloat())) // y
            floatArray.add(centerZ)
        }
        floatArray.toBuffer()
    }

    private val circleVertexIndices = kotlin.run {
        (0..pointSize + 1).toList().toBuffer()
    }

    private val textureVertices = floatArrayOf(
        // x, y, z, texture_x, texture_y
        centerX - radius / 2, centerY + radius / 2, 0.0f, 0.0f, 1.0f, // top left
        centerX - radius / 2, centerY - radius / 2, 0.0f, 0.0f, 0.0f, // bottom left
        centerX + radius / 2, centerY + radius / 2, 0.0f, 1.0f, 1.0f, // top right
        centerX + radius / 2, centerY - radius / 2, 0.0f, 1.0f, 0.0f,  // bottom right
        centerX + 0f, centerY + 0f, 0f, 0.5f, 0.5f
    ).toBuffer()

    private val textureVertexIndices = intBufferOf(
        1, 2, 0,
        2, 0, 3,
        1, 3, 4
    )

    private val texture by lazy {
        Texture(BitmapUtil.generate(context, imgRes))
    }


    override fun init(width: Int, height: Int) {
        updateRotation(180f, 0f, 0f, 1f)
        updateScale(1f, width / height.toFloat(), 1f)
    }

    override fun draw() {
        program.bind()
        texture.bind()

        program.updateUniformMatrix4f("u_Model", getCombinedBuffer())

        drawTexture()
        drawCircle()

        texture.unbind()
        program.unbind()
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

    override fun onTouchDown(x: Float, y: Float): Boolean {
        _isPressed = isTouched(x, y)
        return isPressed
    }

    override fun onTouchMove(x: Float, y: Float, deltaX: Float, deltaY: Float) {
        if (!isPressed) return
        val prevX = x - deltaX
        val prevY = y - deltaY
        onDragEvent.invoke(prevX, prevY, x, y, deltaX, deltaY)
    }

    override fun onTouchUp() {
        _isPressed = false
    }

    private fun isTouched(x: Float, y: Float): Boolean {
        val inverseM = createIdentity4Matrix().apply {
            Matrix.invertM(this, 0, getCombinedMatrix(), 0)
        }
        val notnormalizePoint = createVector4DArray(x, y, 0f).apply {
            Matrix.multiplyMV(this, 0, inverseM, 0, this, 0)
        }
        val left = centerX - radius
        val right = centerX + radius
        val top = centerY + radius
        val bottom = centerY - radius
        return notnormalizePoint[0] >= left && notnormalizePoint[0] <= right && notnormalizePoint[1] >= bottom && notnormalizePoint[1] <= top
    }
}
