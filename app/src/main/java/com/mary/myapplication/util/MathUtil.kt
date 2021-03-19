package com.mary.myapplication.util

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
}