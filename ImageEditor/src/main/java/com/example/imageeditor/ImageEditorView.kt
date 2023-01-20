package com.example.imageeditor

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.imageeditor.utils.AndroidUtil.getStatusBarHeight
import com.example.imageeditor.utils.BitmapUtil

class ImageEditorView(_context: Context) : GLSurfaceView(_context) {

    private val renderer: ImageEditorRenderer by lazy {
        ImageEditorRenderer(context)
    }

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }

    fun init(imgRes: Int) {
        val bitmap = BitmapUtil.generate(context, imgRes)
        queueEvent {
            renderer.addBitmap(bitmap)
        }
    }

    fun init(imageUrl: String) {
        //todo
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                queueEvent {
                    renderer.onTouchDown(event.x, event.y - context.getStatusBarHeight())
                }
            }

            MotionEvent.ACTION_MOVE -> {
                queueEvent {
                    renderer.onTouchMove(event.x, event.y - context.getStatusBarHeight())
                }
            }
            MotionEvent.ACTION_UP -> {
                queueEvent {
                    renderer.onTouchUp()
                }
                return false
            }

            MotionEvent.ACTION_CANCEL -> {
                queueEvent {
                    renderer.onTouchUp()
                }
                return false
            }
        }
        return true
    }

}
