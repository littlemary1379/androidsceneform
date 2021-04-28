package com.mary.myapplication.constant

import com.google.ar.sceneform.math.Vector3

object Constant {
    const val pinkHexColorCode : String = "#FFC3CE"
    const val serenityHexColorCode : String = "#b0cddf"
    const val serenityHexColorCodeTransparent50 : String = "#80b0cddf"
    const val gowoonwooriHexColorCode1 : String = "#6444b1"
    const val gowoonwooriHexColorCode2 : String = "#ab88ff"

    var vectorList : MutableList<List<Vector3>>? = mutableListOf()

    enum class Direction {
        Horizontal, Vertical, FLOOR
    }

    enum class DrawType {
        TYPE_ROOM, TYPE_ROOM_PART, TYPE_FLOOR, TYPE_FLOOR_MEASURE, TYPE_FLOOR_DOOR, TYPE_FLOOR_WINDOW, TYPE_FLOOR_PART_MEASURE
    }

}