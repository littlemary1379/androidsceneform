package com.mary.myapplication.temp

import com.google.ar.sceneform.math.Vector3
import org.json.JSONException

class TempPointBean {
    val startVector : List<Float>? = null
    val endVector : List<Float>? = null

    var startRawVector: Vector3? = null
    var endRawVector: Vector3? = null

    var lineVector : List<Vector3>? = null

    fun init(){
        startRawVector = Vector3(startVector!![0],0f,startVector[1]!!)
        endRawVector = Vector3(endVector!![0],0f,endVector[1]!!)

        lineVector = listOf(startRawVector!!, endRawVector!!)!!

    }

}