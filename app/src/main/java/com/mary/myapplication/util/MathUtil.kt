package com.mary.myapplication.util

import com.google.ar.sceneform.math.Vector3
import kotlin.math.pow
import kotlin.math.sqrt

object MathUtil {

    fun squared(number : Float, root : Double) : Float{
        return number.toDouble().pow(root).toFloat()
    }

    fun calculationLength(numbers : List<Float>) : Float {
        var total = 0f
        for(i in numbers.indices) {
            total += squared(numbers[i], 2.0)
        }
        return sqrt(total)
    }

    fun addVector(vector1 : Vector3, vector2 : Vector3, addPercentage : Int) : Vector3 {

        var addVectorX : Float = vector2.x + (vector2.x - vector1.x) * addPercentage / 100
        var addVectorY : Float = vector2.y + (vector2.y - vector1.y) * addPercentage / 100
        var addVectorZ : Float = vector2.z + (vector2.z - vector1.z) * addPercentage / 100

        return Vector3(addVectorX, addVectorY, addVectorZ)
    }
}