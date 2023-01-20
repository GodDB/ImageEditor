package com.example.imageeditor.model

import com.example.imageeditor.core.shader.ShaderProgram
import java.util.*

abstract class GLESModel {
    abstract val centerX: Float
    abstract val centerY: Float
    abstract val centerZ: Float

    abstract val width: Float
    abstract val height: Float

    enum class Lifecycle(val value: Int) {
        IDLE(0), SURFACE_CREATED(1), SURFACE_CHANGED(2), DRAW(3)
    }

    protected var currentState: Lifecycle = Lifecycle.IDLE
        private set

    private val scheduleSurfaceCreatedQueue: Queue<() -> Unit> = LinkedList()
    private val scheduleSurfaceChangedQueue: Queue<() -> Unit> = LinkedList()
    private val scheduleDrawFrameQueue: Queue<() -> Unit> = LinkedList()

    open fun onSurfaceCreated(program: ShaderProgram) {
        this.currentState = Lifecycle.SURFACE_CREATED
        while (scheduleSurfaceCreatedQueue.isNotEmpty()) {
            scheduleSurfaceCreatedQueue.poll()?.invoke()
        }
    }

    open fun onSurfaceChanged(width: Int, height: Int, program: ShaderProgram) {
        this.currentState = Lifecycle.SURFACE_CHANGED
        while (scheduleSurfaceChangedQueue.isNotEmpty()) {
            scheduleSurfaceChangedQueue.poll()?.invoke()
        }
    }

    open fun onDrawFrame(program: ShaderProgram) {
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
