package com.example.imageeditor.model

import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Log
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.core.texture.Texture
import com.example.imageeditor.utils.FLOAT_BYTE_SIZE
import com.example.imageeditor.utils.floatBufferOf
import com.example.imageeditor.utils.intBufferOf
import com.example.imageeditor.utils.runGL

class ImageModel : GLESModel() {

    override val centerX: Float
        get() = TODO("Not yet implemented")
    override val centerY: Float
        get() = TODO("Not yet implemented")
    override val centerZ: Float
        get() = TODO("Not yet implemented")
    override val width: Float
        get() = TODO("Not yet implemented")
    override val height: Float
        get() = TODO("Not yet implemented")

    private val vertices = floatBufferOf(
        // x, y, z, texture_x, texture_y
        -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, // top left
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, // bottom left
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f, // top right
        1.0f, -1.0f, 0.0f, 1.0f, 0.0f  // bottom right
    )

    private val vertexIndices = intBufferOf(
        0, 1, 2, 3
    )

    private var texture: Texture? = null

    override fun onSurfaceCreated(program: ShaderProgram) {
        super.onSurfaceCreated(program)
        Log.e("godgod", "$this onSurfaceCreated")
    }

    override fun onSurfaceChanged(width: Int, height: Int, program: ShaderProgram) {
        super.onSurfaceChanged(width, height, program)
        Log.e("godgod", "$this onSurfaceChanged")
    }

    override fun onDrawFrame(program: ShaderProgram) {
        super.onDrawFrame(program)
        //  Log.e("godgod", "$this onDrawFrame")
        program.bind()
        texture?.bind()

        drawVertices(program)
        drawTexture(program)
        runGL { GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexIndices.capacity(), GLES20.GL_UNSIGNED_INT, vertexIndices) }
        runGL { GLES20.glEnableVertexAttribArray(0) } // vertexArray를 비활성화 한다.

        texture?.unbind()
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

    fun setBitmap(bitmap: Bitmap) {
        doOnDrawFrame {
            texture = Texture(bitmap)
        }
    }
}
