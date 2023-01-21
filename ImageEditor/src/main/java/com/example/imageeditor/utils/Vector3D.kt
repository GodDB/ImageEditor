package com.example.imageeditor.utils

data class Vector3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    val array : FloatArray = floatArrayOf(x, y, z, 1f)

    operator fun plus(other : Vector3D) : Vector3D {
        return Vector3D(
            x = this.x + other.x,
            y = this.y + other.y,
            z = this.z + other.z
        )
    }

    operator fun minus(other: Vector3D) : Vector3D {
        return Vector3D(
            x = this.x - other.x,
            y = this.y - other.y,
            z = this.z - other.z
        )
    }
}
