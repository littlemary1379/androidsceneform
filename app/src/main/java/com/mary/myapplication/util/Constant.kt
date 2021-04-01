package com.mary.myapplication.util

object Constant {
    const val pinkHexColorCode : String = "#FFC3CE"
    const val serenityHexColorCode : String = "#b0cddf"
    const val gowoonwooriHexColorCode1 : String = "#6444b1"
    const val gowoonwooriHexColorCode2 : String = "#ab88ff"

    enum class Direction {
        Horizontal, Vertical, FLOOR
    }

    enum class DrawType {
        TYPE_ROOM, TYPE_ROOM_PART, TYPE_FLOOR, TYPE_FLOOR_MEASURE, TYPE_FLOOR_PART, TYPE_FLOOR_PART_MEASURE
    }

}