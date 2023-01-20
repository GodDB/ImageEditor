package com.example.imageeditor.shape

import android.opengl.GLES20
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.utils.FLOAT_BYTE_SIZE
import com.example.imageeditor.utils.runGL
import com.example.imageeditor.utils.toBuffer
import java.lang.Math.cos
import java.lang.Math.sin

internal class Circle(
    private val centerX: Float = 0f, // -1 ~ 1
    private val centerY: Float = 0f, // -1 ~ 1
    private val centerZ: Float = 0f, // - 1 ~ 1
    private val radius: Float = 1f,
    private val direction: Direction = Direction.TOP,
) {
    enum class Direction {
        TOP, BOTTOM
    }

    private val pointSize: Int = 100

    private val vertices = kotlin.run {
        val floatArray = mutableListOf<Float>(centerX, centerY, centerZ)
        val range = when (direction) {
            Direction.TOP -> pointSize downTo 0
            Direction.BOTTOM -> 0..pointSize
        }
        for (i in range) {
            val radian = (i.toFloat() / pointSize.toFloat()) * (2 * Math.PI)
            // 해당 라디안의 점 구해서 넣기
            floatArray.add(centerX + (radius * cos(radian).toFloat())) // x
            floatArray.add(centerY) // y
            floatArray.add(centerZ + (radius * sin(radian).toFloat()))
        }
        floatArray.toBuffer()
    }

    private val vertexIndices = kotlin.run {
        (0..pointSize + 1).toList().toBuffer()
    }


    fun draw(program: ShaderProgram) {
        val vertexPointer = runGL { program.getAttributePointer("v_Position") }
        runGL { GLES20.glVertexAttribPointer(vertexPointer, 3, GLES20.GL_FLOAT, false, 3 * FLOAT_BYTE_SIZE, vertices) } // vertex정보들을 vertexArray에게 전달한다.
        runGL { GLES20.glEnableVertexAttribArray(vertexPointer) } // vertexArray를 활성화 한다.
        runGL { GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, vertexIndices.capacity(), GLES20.GL_UNSIGNED_INT, vertexIndices) }
        runGL { GLES20.glEnableVertexAttribArray(0) } // vertexArray를 비 활성화 한다.
    }
}
