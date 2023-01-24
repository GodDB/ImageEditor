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
import com.example.imageeditor.utils.floatBufferOf
import com.example.imageeditor.utils.intBufferOf
import com.example.imageeditor.utils.normalizeX
import com.example.imageeditor.utils.normalizeY
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
            val localModelMatrix = modelM
            val leftTop = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localModelMatrix, 0, topLeftVector3D.array, 0)
            }
            val leftBottom = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localModelMatrix, 0, bottomLeftVector3D.array, 0)
            }
            val rightTop = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localModelMatrix, 0, topRightVector3D.array, 0)
            }
            Size(
                width = abs(rightTop[0]) + abs(leftTop[0]),
                height = abs(leftTop[1]) + abs(leftBottom[1])
            )
        }

    override val center: Vector3D
        get() = kotlin.run {
            val localModelMatrix = createIdentity4Matrix().apply {
                Matrix.multiplyMM(this, 0, projectionM, 0, transM, 0)
            }
            val leftTop = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localModelMatrix, 0, topLeftVector3D.array, 0)
            }
            val leftBottom = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localModelMatrix, 0, bottomLeftVector3D.array, 0)
            }
            val rightTop = createIdentity4Matrix().apply {
                Matrix.multiplyMV(this, 0, localModelMatrix, 0, topRightVector3D.array, 0)
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

    private val scaleControlDragEventHandler: (Float, Float, Float, Float, Float, Float) -> Unit =
        { pressedScreenX: Float, pressedScreenY: Float, movedScreenX: Float, movedScreenY: Float, prevMovedScreenX: Float, prevMovedScreenY: Float ->
            /* val scale = 1 + (deltaX / prevX)
             contentsModel.updateScale(scale, scale, 0f)
             this.updateScale(scale, scale, 0f)
             val localModelMatrix = modelM
             controllerMap.forEach { key, value ->
                 val newVector = createVector4DArray(0f, 0f, 0f).apply {
                     Matrix.multiplyMV(this, 0, localModelMatrix, 0, key.directionVector.array, 0)
                 }
                 value.setTranslation(newVector[0], newVector[1], newVector[2])
             }*/
        }

    private var prevRadian: Float? = null
    private var rotateCorrectValue: Float = 100f // rotation 보정값, GL은 -1, 1 사이의 좌표값을 가지고 있기 때문에 이 값으로 두 점의 각도를 구하기엔 너무나 값의 변화량이 작기에 보정값을 더해 정확도를 올린다.

    private val rotateControlDragEventHandler: (Float, Float, Float, Float, Float, Float) -> Unit =
        { pressedScreenX: Float, pressedScreenY: Float, movedScreenX: Float, movedScreenY: Float, prevMovedScreenX: Float, prevMovedScreenY: Float ->
            val normalizeVector = createVector4DArray(normalizeX(movedScreenX, glWidth), normalizeY(movedScreenY, glHeight), 0f)
            if (prevRadian == null) {
                prevRadian = atan2((normalizeVector[1] - center.y) * rotateCorrectValue, (normalizeVector[0] - center.x) * rotateCorrectValue)
            }
            val radian = atan2((normalizeVector[1] - center.y) * rotateCorrectValue, (normalizeVector[0] - center.x) * rotateCorrectValue)
            val newRadian = radian - prevRadian!!
            prevRadian = radian
            val degree = Math.toDegrees(newRadian.toDouble())
            contentsModel.updateRotation(degree.toFloat(), 0f, 0f, 1f)
            this.updateRotation(degree.toFloat(), 0f, 0f, 1f)
            val localModelMatrix = modelM
            controllerMap.forEach { key, value ->
                val newVector = createVector4DArray(0f, 0f, 0f).apply {
                    Matrix.multiplyMV(this, 0, localModelMatrix, 0, key.directionVector.array, 0)
                }
                value.setTranslation(newVector[0], newVector[1], newVector[2])
            }
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
        contentsModel.dispatchInit(width, height, projectionM)
        updateTranslation(contentsModel.getCopiedTransM())
        updateRotation(contentsModel.getCopiedRotateM())
        updateScale(contentsModel.getCopiedScaleM())
        updateScale(1.1f, 1.2f, 1f)
        val localModelMatrix = modelM
        controllerMap.forEach { key, value ->
            value.dispatchInit(width, height, projectionM)
            value.updateScale(0.1f, 0.1f, 0.1f)
            val newVector = createVector4DArray(0f, 0f, 0f).apply {
                Matrix.multiplyMV(this, 0, localModelMatrix, 0, key.directionVector.array, 0)
            }
            value.setTranslation(newVector[0], newVector[1], newVector[2])
        }
    }

    override fun draw() {
        contentsModel.dispatchDraw()
        if (!isVisible) return
        program.bind()
        program.updateUniformMatrix4f("u_Model", modelMBuffer)
        program.updateUniformMatrix4f("u_Projection", projectionBuffer)
        val vertexPointer = runGL { program.getAttributePointer("v_Position") }

        vertices.position(0)
        runGL { GLES20.glVertexAttribPointer(vertexPointer, 3, GLES20.GL_FLOAT, false, 3 * FLOAT_BYTE_SIZE, vertices) } // vertex정보들을 vertexArray에게 전달한다.
        runGL { GLES20.glEnableVertexAttribArray(vertexPointer) } // vertexArray를 활성화 한다.
        runGL { GLES20.glDrawElements(GLES20.GL_LINES, vertexIndices.capacity(), GLES20.GL_UNSIGNED_INT, vertexIndices) }
        runGL { GLES20.glEnableVertexAttribArray(0) } // vertexArray를 비활성화 한다.

        program.unbind()
        controllerMap.values.forEach {
            it.dispatchDraw()
        }
    }

    override fun onTouchDown(screenX: Float, screenY: Float): Boolean {
        val controllerTouched = controllerMap.values.any { it.onTouchDown(screenX, screenY) }
        if (controllerTouched) {
            Log.e("godgod", "controller down")
            _isPressed = false
            return controllerTouched
        } else {
            _isPressed = isTouched(normalizeX(screenX, glWidth), normalizeY(screenY, glHeight))
            Log.e("godgod", "overlay down $isPressed")
            return isPressed
        }
    }

    private fun isTouched(x: Float, y: Float): Boolean {
        val notNormalizePoint = createVector4DArray(0f, 0f, 0f).apply {
            val point = createVector4DArray(x, y, 0f)
            Matrix.multiplyMV(this, 0, inverseProjectionModelM, 0, point, 0)
        }

        val (notNormalX, notNormalY) = notNormalizePoint
        return notNormalX >= -1 && notNormalX <= 1 && notNormalY >= -1 && notNormalY <= 1
    }

    override fun onTouchMove(pressedScreenX: Float, pressedScreenY: Float, movedScreenX: Float, movedScreenY: Float, prevMovedScreenX: Float, prevMovedScreenY: Float) {
        val controllerTouched = controllerMap.values.any { it.isPressed }
        if (controllerTouched) {
            controllerMap.values.forEach {
                it.onTouchMove(pressedScreenX, pressedScreenY, movedScreenX, movedScreenY, prevMovedScreenX, prevMovedScreenY)
            }
        } else {
            if (!isPressed) return
            val normalizeDeltaX = normalizeX(movedScreenX, glWidth) - normalizeX(prevMovedScreenX, glWidth)
            val normalizeDeltaY = normalizeY(movedScreenY, glHeight) - normalizeY(prevMovedScreenY, glHeight)
            contentsModel.updateTranslation(normalizeDeltaX, normalizeDeltaY, 0f)
            updateTranslation(normalizeDeltaX, normalizeDeltaY, 0f)
            controllerMap.values.forEach {
                it.updateTranslation(normalizeDeltaX, normalizeDeltaY, 0f)
            }
        }
    }

    override fun onTouchUp() {
        controllerMap.values.forEach { it.onTouchUp() }
        contentsModel.onTouchUp()
        prevRadian = null
        _isPressed = false
    }
}

private enum class ControllerType(val directionVector: Vector3D) {
    CLOSE(Vector3D(1f, 1f, 0f)), SCALE(Vector3D(-1f, 1f, 0f)), ROTATE(Vector3D(1f, -1f, 0f))
}
