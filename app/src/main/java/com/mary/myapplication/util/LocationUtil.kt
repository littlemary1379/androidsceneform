package com.mary.myapplication.util

import com.google.ar.sceneform.math.Vector3
import kotlin.math.max

object LocationUtil {

    //바닥을 이루는 면 중 가장 긴 면을 구해야한다.
    fun longLength(locationList: List<Vector3>, height: Float) : Float {
        var maxLength = 0f
        var length1: Float
        var length2: Float

        for (i in locationList.indices) {

            when (i) {
                0 -> {
                    length1 = MathUtil.calculationLength(
                        listOf(
                            (locationList[0].x - locationList[1].x),
                            (locationList[0].y - locationList[1].y),
                            (locationList[0].z - locationList[1].z)
                        )
                    )
                    length2 = MathUtil.calculationLength(
                        listOf(
                            (locationList[0].x - locationList[locationList.size - 1].x),
                            (locationList[0].y - locationList[locationList.size - 1].y),
                            (locationList[0].z - locationList[locationList.size - 1].z)
                        )
                    )
                }

                locationList.size - 1 -> {
                    length1 = MathUtil.calculationLength(
                        listOf(
                            (locationList[0].x - locationList[i].x),
                            (locationList[0].y - locationList[i].y),
                            (locationList[0].z - locationList[i].z)
                        )
                    )
                    length2 = MathUtil.calculationLength(
                        listOf(
                            (locationList[i-1].x - locationList[i].x),
                            (locationList[i-1].y - locationList[i].y),
                            (locationList[i-1].z - locationList[i].z)
                        )
                    )
                }

                else -> {
                    length1 = MathUtil.calculationLength(
                        listOf(
                            (locationList[i].x - locationList[i+1].x),
                            (locationList[i].y - locationList[i+1].y),
                            (locationList[i].z - locationList[i+1].z)
                        )
                    )
                    length2 = MathUtil.calculationLength(
                        listOf(
                            (locationList[i].x - locationList[i-1].x),
                            (locationList[i].y - locationList[i-1].y),
                            (locationList[i].z - locationList[i-1].z)
                        )
                    )
                }
            }

            if (maxLength < length1) {
                maxLength = length1
            } else if (maxLength < length2) {
                maxLength = length2
            }


        }

        if(maxLength < height) {
            maxLength = height
        }

        return maxLength

    }

    //바닥을 이루는 면 중 가장 긴 면을 구해야한다.
    @JvmName("longLength1")
    fun longLength(locationList: List<List<Vector3>>, height: Float) : Float {
        var maxLength = 0f
        var length1: Float

        for (i in locationList.indices) {

            val list = listOf(
                locationList[i][0].x- locationList[i][1].x,
                locationList[i][0].y- locationList[i][1].y,
                locationList[i][0].z- locationList[i][1].z
            )

            length1= MathUtil.calculationLength(list)

            if (maxLength < length1) {
                maxLength = length1
            }

        }

        if(maxLength < height) {
            maxLength = height
        }

        return maxLength
    }

}