package com.mary.myapplication.util

import com.google.ar.sceneform.math.Vector3
import kotlin.math.pow
import kotlin.math.sqrt

object MathUtil {

    private const val TAG = "MathUtil"

    fun squared(number: Float, root: Double): Float {
        return number.toDouble().pow(root).toFloat()
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

    fun calculationSlopeNormalVector(vector1: Vector3, vector2: Vector3){
        var x : Double
        var z : Double = 1.0

        x = - ( z * (vector2.x-vector1.x) / (vector2.z - vector1.z) )

        DlogUtil.d(TAG, "${(vector2.x-vector1.x)}")
        DlogUtil.d(TAG, "${(vector2.z - vector1.z)}")
        DlogUtil.d(TAG, "x = $x")
        DlogUtil.d(TAG, "법선벡터의 기울기 = ${x/z}")

    }

    fun calculationStraightLineEquation(vector: Vector3, slope : Double){
        val b : Double = vector.z - slope * vector.x
    }


    fun calculationQuadratic() {
        var a: Double = 1.25
        var b: Double = -75.0
        var c: Double = 1025.0

        var disc: Double = b * b - 4.0 * c
        var sqr: Double = sqrt(disc)

        // 실근
        if (disc > 0) {
            var r1 = (-b + sqr) / (2.0 * a)
            var r2 = (-b - sqr) / (2.0 * a)

            DlogUtil.d(TAG, "이게 실근인가? r1 : $r1")
            DlogUtil.d(TAG, "이게 실근인가? r2 : $r2")
        }

        //중근
        else if (disc == 0.0) {
            var r1 = -b / (2.0 * a)

            DlogUtil.d(TAG, "이게 중근인가? r1 : $r1")
        }

        //허근
        else if (disc == 0.0) {
            var r = -b / (2.0 * a)
            var s1 = sqr / (2.0 * a)
            var s2 = -sqr / (2.0 * a)

            DlogUtil.d(TAG, "이게 허근인가? s1 : $r+$s1")
            DlogUtil.d(TAG, "이게 허근인가? s2 : $r+$s2")
        }

    }
}