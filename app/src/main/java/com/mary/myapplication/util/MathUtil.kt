package com.mary.myapplication.util

import com.google.ar.sceneform.math.Vector3
import kotlin.math.pow
import kotlin.math.sqrt

object MathUtil {

    private const val TAG = "MathUtil"

    fun squared(number: Float, root: Double): Float {
        return number.toDouble().pow(root).toFloat()
    }

    fun squared(number: Double, root: Double): Double {
        return number.pow(root)
    }

    fun calculationLength(numbers: List<Float>): Float {
        var total = 0f
        for (i in numbers.indices) {
            total += squared(numbers[i], 2.0)
        }
        return sqrt(total)
    }

    fun addVector(vector1: Vector3, vector2: Vector3, addPercentage: Int): Vector3 {

        var addVectorX: Float = vector2.x + (vector2.x - vector1.x) * addPercentage / 100
        var addVectorY: Float = vector2.y + (vector2.y - vector1.y) * addPercentage / 100
        var addVectorZ: Float = vector2.z + (vector2.z - vector1.z) * addPercentage / 100

        return Vector3(addVectorX, addVectorY, addVectorZ)
    }

    //기울기를 구함
    fun calculationSlopeNormalVector(vector1: Vector3, vector2: Vector3): Double {
        var x: Double
        var z: Double = 1.0

        return if ((vector2.z - vector1.z) == 0f) {
            DlogUtil.d(TAG, "법선벡터의 기울기 = 0")
            0.0
        } else {
            x = -(z * (vector2.x - vector1.x) / (vector2.z - vector1.z))
            DlogUtil.d(TAG, "법선벡터의 기울기 = ${x / z}")
            x / z
        }

    }

    //기울기를 통해, y=ax+b의 b값을 구함
    //slope가 0이고, b가 0일때 값 처리해야함
    fun calculationStraightLineEquation(
        vector: Vector3,
        slope: Double,
        length: Double,
        upper: Int
    ): List<Double> {

        var b: Double

        if (slope == 0.0) {
            b = vector.z.toDouble()
        } else {
            b = vector.z - slope * vector.x
            DlogUtil.d(TAG, "b = $b")
        }

        return calculationQuadratic(vector, slope, b, length, upper)

    }

    fun calculationQuadratic(
        vector: Vector3,
        slope: Double,
        lineB: Double,
        length: Double,
        upper: Int
    ): List<Double> {
        var a: Double = 1 + squared(slope, 2.0)
        var b: Double = -(vector.x * 2) + ((vector.z - lineB) * -slope * 2)
        var c: Double =
            squared(vector.x, 2.0) + squared(vector.z - lineB, 2.0) - squared(length, 2.0)

        var r1: Double = 0.0
        var r2: Double = 0.0

        var disc: Double = b * b - 4.0 * a * c
        var sqr: Double = sqrt(disc)

        // 실근
        when {
            disc > 0 -> {
                r1 = (-b + sqr) / (2.0 * a)
                r2 = (-b - sqr) / (2.0 * a)
            }

            //중근
            disc == 0.0 -> {
                r1 = -b / (2.0 * a)
            }

            //허근
            disc < 0.0 -> {
                DlogUtil.d(TAG, "허근")
            }
        }

        var x: Double = 0.0

        if (upper == 1) {
            if (r1 < vector.x) {
                x = r1
            } else if (r2 < vector.x) {
                x = r2
            }
        } else if(upper == -1){
            if (r1 > vector.x) {
                x = r1
            } else if (r2 > vector.x) {
                x = r2
            }
        } else {

        }

        var y: Double = slope * x + lineB
        return listOf(x, y)
    }
}