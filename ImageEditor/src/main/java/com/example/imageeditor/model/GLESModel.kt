package com.example.imageeditor.model

import com.example.imageeditor.core.shader.ShaderProgram
import java.nio.FloatBuffer

internal interface GLESModel {
    val combinedM : FloatBuffer
    val program: ShaderProgram
    val isVisible : Boolean

    fun init(width: Int, height: Int)

    fun draw()

    fun setVisible(visible : Boolean)

    fun onTouchDown(x : Float, y : Float) : Boolean

    fun onTouchMove(x : Float, y : Float, deltaX : Float , deltaY : Float)

    fun onTouchUp()
}
