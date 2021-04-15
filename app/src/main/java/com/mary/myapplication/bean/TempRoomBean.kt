package com.mary.myapplication.bean

import com.google.ar.sceneform.math.Vector3
import org.json.JSONException

class TempRoomBean {
    val startvector0 : List<Float>? = null
    val startvector1 : List<Float>? = null
    val startvector2 : List<Float>? = null
    val endvector0 : List<Float>? = null
    val endvector1 : List<Float>? = null
    val endvector2 : List<Float>? = null

    var startRawVector0 : Vector3? = null
    var startRawVector1 : Vector3? = null
    var startRawVector2 : Vector3? = null
    var endRawVector0 : Vector3? = null
    var endRawVector1 : Vector3? = null
    var endRawVector2 : Vector3? = null

    var vectorList : List<List<Vector3>>? = null

    fun init(){
        startRawVector0 = Vector3(startvector0!![0], 0f, startvector0!![1])
        startRawVector1 = Vector3(startvector1!![0], 0f, startvector1!![1])
        startRawVector2 = Vector3(startvector2!![0], 0f, startvector2!![1])

        endRawVector0 = Vector3(endvector0!![0], 0f, endvector0!![1])
        endRawVector1 = Vector3(endvector1!![0], 0f, endvector1!![1])
        endRawVector2 = Vector3(endvector2!![0], 0f, endvector2!![1])

        var lineVector1 : List<Vector3> = listOf(startRawVector0!!, endRawVector0!!)
        var lineVector2 : List<Vector3> = listOf(startRawVector0!!, endRawVector0!!)
        var lineVector3 : List<Vector3> = listOf(startRawVector0!!, endRawVector0!!)

        vectorList = listOf(lineVector1, lineVector2, lineVector3)

    }

}

