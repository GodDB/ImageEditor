package com.example.imageeditor.model

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.example.imageeditor.R
import com.example.imageeditor.core.shader.Shader
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.utils.FLOAT_BYTE_SIZE
import com.example.imageeditor.utils.FileReader
import com.example.imageeditor.utils.Size
import com.example.imageeditor.utils.Vector3D
import com.example.imageeditor.utils.createIdentity4Matrix
import com.example.imageeditor.utils.createVector4DArray
import com.example.imageeditor.utils.deepCopy
import com.example.imageeditor.utils.floatBufferOf
import com.example.imageeditor.utils.intBufferOf
import com.example.imageeditor.utils.runGL
import kotlin.math.abs
import kotlin.math.atan2


internal class OverlayModel(
    context: Context,
    private val contentsModel: GLESModel
) : GLESModel() {

    private val topLeftVector3D = Vector3D(-1f, 1f, 0f)
    private val topRightVector3D = Vector3D(1f, 1f, 0f)
    private val bottomLeftVector3D = Vector3D(-1f, -1f, 0f)
    private val bottomRightVector3D = Vector3D(1f, -1f, 0f)

    private val vertices = floatBufferOf(
        // x, y, z
        topLeftVector3D.x, topLeftVector3D.y, topLeftVector3D.z, // top left
        bottomLeftVector3D.x, bottomLeftVector3D.y, bottomLeftVector3D.z, // bottom left
        topRightVector3D.x, topRightVector3D.y, topRightVector3D.z, // top right
        bottomRightVector3D.x, bottomRightVector3D.y, bottomRightVector3D.z,  // bottom right
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

    private val scaleControlDragEventHandler: (Float, Float, Float, Float, Float, Float) -> Unit = { prevX, _, _, _, deltaX, _ ->
        val scale = 1 + (deltaX / prevX)
        contentsModel.updateScale(scale, scale, 0f)
        this.updateScale(scale, scale, 0f)
        controllerMap.forEach { key, value ->
            val newVector = createVector4DArray(0f, 0f, 0f).apply {
                Matrix.multiplyMV(this, 0, getCombinedMatrix(), 0, key.directionVector.array, 0)
            }
            value.setTranslation(newVector[0], newVector[1], newVector[2])
        }
    }

    private var prevRadian: Float? = null

    private val rotateControlDragEventHandler: (Float, Float, Float, Float, Float, Float) -> Unit = { prevX, prevY, curX, curY, deltaX, deltaY ->
        if (prevRadian == null) {
            val center = controllerMap.get(ControllerType.ROTATE)!!.center
            prevRadian = atan2(center.y, center.x)
        }
        val radian = atan2(curY - center.y, curX - center.x)
        val newRadian = radian - prevRadian!!
        prevRadian = radian
        val degree = Math.toDegrees(newRadian.toDouble())
        //  contentsModel.updateRotation(degree.toFloat(), 0f, 0f, 1f)
        //this.updateRotation(degree.toFloat(), 0f, 0f, 1f)
    }

    private val controllerMap by lazy {
        hashMapOf(
            ControllerType.SCALE to TextureCircleModel(
                context = context,
                imgRes = R.drawable.expand,
                centerX = 0f,
                centerY = 0f,
                centerZ = 0f,
                radius = 1f,
                onDragEvent = scaleControlDragEventHandler
            ),
            ControllerType.ROTATE to TextureCircleModel(
                context = context,
                imgRes = R.drawable.rotate,
                centerX = 0f,
                centerY = 0f,
                centerZ = 0f,
                radius = 1f,
                onDragEvent = rotateControlDragEventHandler
            ),
            ControllerType.CLOSE to TextureCircleModel(
                context = context,
                imgRes = R.drawable.close,
                centerX = 0f,
                centerY = 0f,
                centerZ = 0f,
                radius = 1f,
                onDragEvent = { prevX, prevY, newX, newY, deltaX, deltaY -> }
            )
        )
    }

    override fun init(width: Int, height: Int) {
        contentsModel.init(width, height)
        updateTranslation(contentsModel.getCopiedTransM())
        updateRotation(contentsModel.getCopiedRotateM())
        updateScale(contentsModel.getCopiedScaleM())
        updateScale(1.1f, 1.2f, 1f)
        controllerMap.forEach { key, value ->
            value.init(width, height)
            value.updateScale(0.1f, 0.1f, 0.1f)
            val newVector = createVector4DArray(0f, 0f, 0f).apply {
                Matrix.multiplyMV(this, 0, getCombinedMatrix(), 0, key.directionVector.array, 0)
            }
            value.setTranslation(newVector[0], newVector[1], newVector[2])
        }
    }

    override fun draw() {
        contentsModel.draw()
        if (!isVisible) return
        program.bind()
        program.updateUniformMatrix4f("u_Model", getCombinedBuffer())
        val vertexPointer = runGL { program.getAttributePointer("v_Position") }

        vertices.position(0)
        runGL { GLES20.glVertexAttribPointer(vertexPointer, 3, GLES20.GL_FLOAT, false, 3 * FLOAT_BYTE_SIZE, vertices) } // vertex정보들을 vertexArray에게 전달한다.
        runGL { GLES20.glEnableVertexAttribArray(vertexPointer) } // vertexArray를 활성화 한다.
        runGL { GLES20.glDrawElements(GLES20.GL_LINES, vertexIndices.capacity(), GLES20.GL_UNSIGNED_INT, vertexIndices) }
        runGL { GLES20.glEnableVertexAttribArray(0) } // vertexArray를 비활성화 한다.

        program.unbind()
        controllerMap.values.forEach {
            it.draw()
        }
    }

    override fun onTouchDown(x: Float, y: Float): Boolean {
        val controllerTouched = controllerMap.values.any { it.onTouchDown(x, y) }
        if (controllerTouched) {
            Log.e("godgod", "controller down")
            _isPressed = false
            return controllerTouched
        } else {
            _isPressed = isTouched(x, y)
            Log.e("godgod", "overlay down $isPressed")
            return isPressed
        }
    }

    private fun isTouched(x: Float, y: Float): Boolean {
        val localSize = size
        val localCenter = center
        val left = localCenter.x - (localSize.width / 2)
        val right = localCenter.x + (localSize.width / 2)
        val top = localCenter.y + (localSize.height / 2)
        val bottom = localCenter.y - (localSize.height / 2)
        return x >= left && x <= right && y >= bottom && y <= top
    }

    override fun onTouchMove(x: Float, y: Float, deltaX: Float, deltaY: Float) {
        val controllerTouched = controllerMap.values.any { it.isPressed }
        if (controllerTouched) {
            controllerMap.values.forEach {
                it.onTouchMove(x, y, deltaX, deltaY)
            }
        } else {
            if (!isPressed) return
            contentsModel.updateTranslation(deltaX, deltaY, 0f)
            updateTranslation(deltaX, deltaY, 0f)
            controllerMap.values.forEach {
                it.updateTranslation(deltaX, deltaY, 0f)
            }
        }
    }

    override fun onTouchUp() {
        controllerMap.values.forEach { it.onTouchUp() }
        contentsModel.onTouchUp()
        _isPressed = false
    }
}

private enum class ControllerType(val directionVector: Vector3D) {
    CLOSE(Vector3D(1f, 1f, 0f)), SCALE(Vector3D(-1f, 1f, 0f)), ROTATE(Vector3D(1f, -1f, 0f))
}
