package com.mary.myapplication.temp

import com.google.ar.sceneform.math.Vector3

class RoomData {
    var rawFirstVector = Vector3(0f, 0f, 0f)
    var rawSecondVector = Vector3(30f, 0f, 0f)
    var rawThirdVector = Vector3(40f, 0f, -20f)
    var rawFourthVector = Vector3(25f, 0f, -30f)
    var rawFifthVector = Vector3(20f, 0f, -40f)
    var rawSixthVector = Vector3(-5f, 0f, -30f)
    var rawSeventhVector = Vector3(-10f, 0f, -20f)
    var rawEighthVector = Vector3(-17f, 0f, -15f)
    var rawNinthVector = Vector3(-27f, 0f, -10f)
    var rawTenthVector = Vector3(-25f, 0f, -5f)

    var rawVectorList = listOf(
        rawFirstVector,
        rawSecondVector,
        rawThirdVector,
        rawFourthVector,
        rawFifthVector,
        rawSixthVector,
        rawSeventhVector,
        rawEighthVector,
        rawNinthVector,
        rawTenthVector
    )

    var rawFirstDoorVector = Vector3(10f, 0f, 0f)
    var rawSecondDoorVector = Vector3(18f, 0f, 0f)
    var rawDoorVectorList = listOf(rawFirstDoorVector, rawSecondDoorVector)

    var rawFirstWindowVector = Vector3(24f, 5f, -32f)
    var rawSecondWindowVector = Vector3(21f, 5f, -38f)
    var rawThirdWindowVector = Vector3(23f, 5f, -34f)
    var rawFourthWindowVector = Vector3(20f, 5f, -40f)
    var rawFifthWindowVector = Vector3(12f, 0f, 0f)
    var rawSixthWindowVector = Vector3(19f, 0f, 0f)
    var rawWindowVectorList = listOf(rawFirstWindowVector, rawSecondWindowVector, rawThirdWindowVector, rawFourthWindowVector, rawFifthWindowVector, rawSixthWindowVector)

}