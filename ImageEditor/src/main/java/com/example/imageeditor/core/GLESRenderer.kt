package com.example.imageeditor.core

import android.opengl.GLSurfaceView
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal abstract class GLESRenderer : GLSurfaceView.Renderer {

    enum class Lifecycle(val value: Int) {
        IDLE(0), SURFACE_CREATED(1), SURFACE_CHANGED(2), DRAW(3)
    }

    protected var currentState: Lifecycle = Lifecycle.IDLE
        private set


    private val scheduleSurfaceCreatedQueue: Queue<() -> Unit> = LinkedList()
    private val scheduleSurfaceChangedQueue: Queue<() -> Unit> = LinkedList()
    private val scheduleDrawFrameQueue: Queue<() -> Unit> = LinkedList()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        this.currentState = Lifecycle.SURFACE_CREATED
        while (scheduleSurfaceCreatedQueue.isNotEmpty()) {
            scheduleSurfaceCreatedQueue.poll()?.invoke()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.currentState = Lifecycle.SURFACE_CHANGED
        while (scheduleSurfaceChangedQueue.isNotEmpty()) {
            scheduleSurfaceChangedQueue.poll()?.invoke()
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        this.currentState = Lifecycle.DRAW
        while (scheduleDrawFrameQueue.isNotEmpty()) {
            scheduleDrawFrameQueue.poll()?.invoke()
        }
    }

    protected fun doOnSurfaceCreated(block: () -> Unit) {
        if (currentState.value >= Lifecycle.SURFACE_CREATED.value) {
            block()
        } else {
            scheduleSurfaceCreatedQueue.offer(block)
        }
    }

    protected fun doOnSurfaceChanged(block: () -> Unit) {
        if (currentState.value >= Lifecycle.SURFACE_CHANGED.value) {
            block()
        } else {
            scheduleSurfaceChangedQueue.offer(block)
        }
    }

    protected fun doOnDrawFrame(block: () -> Unit) {
        if (currentState.value >= Lifecycle.DRAW.value) {
            block()
        } else {
            scheduleDrawFrameQueue.offer(block)
        }
    }
}
