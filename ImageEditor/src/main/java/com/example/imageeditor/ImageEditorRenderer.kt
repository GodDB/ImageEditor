package com.example.imageeditor

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.imageeditor.core.shader.Shader
import com.example.imageeditor.core.shader.ShaderProgram
import com.example.imageeditor.model.ImageModel
import com.example.imageeditor.utils.FileReader
import com.example.imageeditor.utils.runGL
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImageEditorRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val imageProgram by lazy {
        runGL {
            val vertexShaderSourceCode = FileReader.readFile(context, R.raw.vertext_shader)
            val fragmentShaderSourceCode = FileReader.readFile(context, R.raw.fragment_shader)
            ShaderProgram(
                vertexShader = Shader(vertexShaderSourceCode, Shader.Type.VERTEX),
                fragmentShader = Shader(fragmentShaderSourceCode, Shader.Type.FRAGMENT)
            )
        }
    }

    // 모델과 프로그램의 순서는 동일해야 한다.
    private val models by lazy {
        listOf(
            ImageModel()
        )
    }

    private val programs by lazy {
        listOf(
            imageProgram
        )
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.e("godgod", "onSurfaceCreated")
        models.forEachIndexed { index, model ->
            val mappingProgram = programs[index]
            mappingProgram.bind()
            model.onSurfaceCreated(mappingProgram)
            mappingProgram.unbind()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.e("godgod", "onSurfaceChanged")
        runGL { GLES20.glViewport(0, 0, width, height) }
        runGL { GLES20.glEnable(GLES20.GL_CULL_FACE) } // 벡터 외적이 후면을 바라보는 부분 제거
        runGL { GLES20.glEnable(GLES20.GL_DEPTH_TEST) } // z버퍼 생성

        models.forEachIndexed { index, model ->
            val mappingProgram = programs[index]
            mappingProgram.bind()
            model.onSurfaceChanged(width, height, mappingProgram)
            mappingProgram.unbind()
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        runGL { GLES20.glClearColor(0f, 0f, 0f, 0f) }
        runGL { GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT) }

        models.forEachIndexed { index, model ->
            val mappingProgram = programs[index]
            mappingProgram.bind()
            model.onDrawFrame(mappingProgram)
            mappingProgram.unbind()
        }
    }

    fun setImageBitmap(bitmap: Bitmap) {
        models.first().setBitmap(bitmap)
    }
}
