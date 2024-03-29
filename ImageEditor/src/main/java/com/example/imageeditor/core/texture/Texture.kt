package com.example.imageeditor.core.texture

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.example.imageeditor.core.GLESBinder
import com.example.imageeditor.utils.runGL

class Texture(bitmap: Bitmap) : GLESBinder {

    val bufferId: Int

    val width = bitmap.width
    val height = bitmap.height

    init {
        bufferId = genTextureBuffer()
        loadTextureBuffer(bufferId, bitmap)
    }

    private fun genTextureBuffer(): Int {
        // textureBuffer 생성
        val textureObjectIds = IntArray(1)
        runGL { GLES20.glGenTextures(1, textureObjectIds, 0) }
        if (textureObjectIds.first() == 0) {
            throw java.lang.RuntimeException("not generated texture")
        }
        return textureObjectIds.first()
    }


    private fun loadTextureBuffer(bufferId: Int, bitmap: Bitmap) {
        // texture buffer activate
        runGL { GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bufferId) }
        // texture 옵션 지정 (mipmap)
        runGL { GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR) }
        runGL { GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR) }
        runGL { GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT) }
        runGL { GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT) }
        // texture buffer에 bitmap load
        runGL { GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0) }
        // bitmap release (native memory 제거 및 gc로 부터 제거되라고)
        bitmap.recycle()
        // mipmap 생성
        runGL { GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D) }
        // texture 해제 (로드 완료됬으므로 해제)
        unbind()
    }

    override fun bind() {
        runGL { GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bufferId) }
    }

    override fun unbind() {
        runGL { GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0) }
    }
}
