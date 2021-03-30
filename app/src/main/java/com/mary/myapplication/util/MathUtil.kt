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
    fun calculationSlopeNormalVector(vector1: Vector3, vector2: Vector3) : Double{
        var x: Double
        var z: Double = 1.0

        x = -(z * (vector2.x - vector1.x) / (vector2.z - vector1.z))

        DlogUtil.d(TAG, "법선벡터의 기울기 = ${x / z}")

        return x/z

    }

    //기울기를 통해, y=ax+b의 b값을 구함
    fun calculationStraightLineEquation(vector: Vector3, slope: Double, length: Double) : List<Double> {
        val b: Double = vector.z - slope * vector.x

        DlogUtil.d(TAG, "b = $b")
        return calculationQuadratic(vector, slope, b, length)
    }


    fun calculationQuadratic(vector: Vector3, slope: Double, lineB: Double, length: Double) : List<Double> {
        var a: Double = 1 + squared(slope, 2.0)
        var b: Double = -(vector.x * 2) + ((vector.z - lineB) * -slope * 2)
        var c: Double =
            squared(vector.x, 2.0) + squared(vector.z - lineB, 2.0) - squared(length, 2.0)

        var r1: Double = 0.0
        var r2: Double = 0.0

        var x: Double = 0.0

//        DlogUtil.d(TAG, "squared(vector.x, 2.0) = ${squared(vector.x, 2.0)}")
//        DlogUtil.d(TAG, "vector.z = ${(vector.z)}")
//        DlogUtil.d(TAG, "b = ${lineB}")
//        DlogUtil.d(TAG, "vector.z - b = ${(vector.z - lineB)}")
//        DlogUtil.d(TAG, "squared(vector.z - b, 2.0) = ${squared(vector.z - lineB, 2.0)}")
//        DlogUtil.d(TAG, "squared(length, 2.0) = ${squared(length, 2.0)}")
        DlogUtil.d(TAG, "a = ${a}")
        DlogUtil.d(TAG, "b = ${b}")
        DlogUtil.d(TAG, "c = ${c}")


        var disc: Double = b * b - 4.0 * a * c
        var sqr: Double = sqrt(disc)

        // 실근
        if (disc > 0) {
            r1 = (-b + sqr) / (2.0 * a)
            r2 = (-b - sqr) / (2.0 * a)

            DlogUtil.d(TAG, "이게 실근인가? r1 : $r1")
            DlogUtil.d(TAG, "이게 실근인가? r2 : $r2")
        }

        //중근
        else if (disc == 0.0) {
            r1 = -b / (2.0 * a)

            DlogUtil.d(TAG, "이게 중근인가? r1 : $r1")
        }

        //허근
        else if (disc < 0.0) {
            var r = -b / (2.0 * a)
            var s1 = sqr / (2.0 * a)
            var s2 = -sqr / (2.0 * a)

            DlogUtil.d(TAG, "이게 허근인가? s1 : $r+$s1")
            DlogUtil.d(TAG, "이게 허근인가? s2 : $r+$s2")
        }

        if (r1 > vector.x) {
            x = r1
        } else if (r2 > vector.x) {
            x = r2
        }

        var y: Double = slope * x + lineB
        return listOf(x, y)

    }
}