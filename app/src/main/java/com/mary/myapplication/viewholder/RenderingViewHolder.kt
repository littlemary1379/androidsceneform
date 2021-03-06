package com.mary.myapplication.viewholder

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import com.mary.myapplication.R
import com.mary.myapplication.bean.data.PlaneBean
import com.mary.myapplication.bean.data.RoomBean
import com.mary.myapplication.constant.Constant
import com.mary.myapplication.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

class RenderingViewHolder(context: Context, type: Int, roomBean: RoomBean) {

    companion object {
        private const val TAG = "RenderingViewHolder"
        val TYPE_3D = 0
        val TYPE_FLOOR = 1
        val TYPE_WALL = 2
    }

    var view: View = LayoutInflater.from(context).inflate(R.layout.viewholder_rendering, null)
    private var type = type

    lateinit var sceneView: SceneView

    private lateinit var transformationSystem: TransformationSystem
    private lateinit var transformableNode: TransformableNode
    private lateinit var parentsTransformableNode: TransformableNode
    private lateinit var transformableViewNode: TransformableNode

    private var maxLength = 0f

    private lateinit var floorVectorList1: MutableList<List<Vector3>>
    private lateinit var ceilingVectorList1: MutableList<List<Vector3>>
    private var vectorList1: MutableList<List<Vector3>> = mutableListOf()

    private var doorHeight = 0f
    private var windowHeight = 0f

    private var windowVectorPointList: MutableList<List<Vector3>> = mutableListOf()
    private var windowVectorSegmentList: MutableList<List<List<Vector3>>> = mutableListOf()

    private var doorVectorPointList: MutableList<List<Vector3>> = mutableListOf()
    private var doorVectorSegmentList: MutableList<List<List<Vector3>>> = mutableListOf()

    private lateinit var centerPosition: Vector3
    private lateinit var cameraPosition: Vector3
    private var cameraClip: Float = 0f
    private var cylinderDiameter = 0f
    private var textSize = 0f

    private var lastDistance: Float = 0f

    private var downX: Float = 0f
    private var downY: Float = 0f

    private var xAngle: Float = 0f
    private var yAngle: Float = 0f
    private var lastXAngle: Float = 0f
    private var lastYAngle: Float = 0f

    private var isScale: Boolean = false
    private var isFloor: Boolean = false

    private var percentageHeight: Float = 0f
    private var percentageWidth: Float = 0f
    private var percentageDoorHeight: Float = 0f
    private var percentageDoorWidth: Float = 0f

    var roomBean = roomBean
    var pointList: MutableList<Vector3> = mutableListOf()
    var segmentList: MutableList<List<Vector3>> = mutableListOf()

    private lateinit var drawType: Constant.DrawType

    init {

        findView()
        doorHeight = 15f
        windowHeight = 10f

        initPointList()

        maxLength = LocationUtil.longLength(pointList, roomBean.height)

        initVectorList(segmentList)
        initPartVectorList(roomBean.wallObjectList)

        initCenterVector(vectorList1)
        setCameraPosition(cameraPosition)

        initSceneView()

        when (type) {

            TYPE_3D -> {
                draw3Droom()
                draw3Dpart()
            }

            TYPE_FLOOR -> {

                //?????? ??? ?????? ??????
                isFloor = true

                drawFloor()
                drawFloorPart()

                ThreadUtil.startUIThread(300, Runnable {
                    quaternionXAxis90Rendering()
                })

            }

            else -> {
                drawType = Constant.DrawType.TYPE_ROOM
                drawModeling(floorVectorList1)

            }
        }

        setTransformableNode()

    }

    private fun quaternionXAxis90Rendering() {

        var xQuaternion = Quaternion.axisAngle(Vector3(0f, 0f, 0f), 0f)
        var yQuaternion = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f)

        parentsTransformableNode.worldRotation =
            Quaternion.multiply(xQuaternion, yQuaternion)
    }

    private fun findView() {
        sceneView = view.findViewById(R.id.sceneView)
    }

    fun pause() {
        sceneView.pause()
    }

    fun resume() {
        sceneView.resume()
    }

    private fun initPointList() {
        var floorBean = roomBean.floorPlaneBean
        for (i in 0 until floorBean?.pointList?.size!!) {
            if (floorBean != null) {
                pointList.add(
                    Vector3(
                        floorBean.pointList[i].x,
                        floorBean.pointList[i].y,
                        floorBean.pointList[i].z
                    )
                )
            }
        }

        for (i in 0 until floorBean?.segmentBeanList?.size!!) {
            if (floorBean != null) {
                var list: MutableList<Vector3> = mutableListOf()
                list.add(
                    Vector3(
                        floorBean.segmentBeanList[i].startPointBean?.x!!,
                        floorBean.segmentBeanList[i].startPointBean?.y!!,
                        floorBean.segmentBeanList[i].startPointBean?.z!!
                    )
                )
                list.add(
                    Vector3(
                        floorBean.segmentBeanList[i].endPointBean?.x!!,
                        floorBean.segmentBeanList[i].endPointBean?.y!!,
                        floorBean.segmentBeanList[i].endPointBean?.z!!
                    )
                )
                segmentList.add(list)
            }
        }

        DlogUtil.d(TAG, roomBean.wallObjectList[0].toJSONObject().toString())

    }

    private fun initVectorList(rawVectorList: List<List<Vector3>>) {

        floorVectorList1 = mutableListOf()
        ceilingVectorList1 = mutableListOf()

        for (i in rawVectorList.indices) {
            var cellingPointList = listOf(
                Vector3(
                    rawVectorList[i][0].x / maxLength,
                    roomBean.height / maxLength,
                    rawVectorList[i][0].z / maxLength
                ),
                Vector3(
                    rawVectorList[i][1].x / maxLength,
                    roomBean.height / maxLength,
                    rawVectorList[i][1].z / maxLength
                )
            )

            var floorPointList = listOf(
                Vector3(rawVectorList[i][0].x / maxLength, 0f, rawVectorList[i][0].z / maxLength),
                Vector3(rawVectorList[i][1].x / maxLength, 0f, rawVectorList[i][1].z / maxLength)
            )
            floorVectorList1.add(floorPointList)
            ceilingVectorList1.add(cellingPointList)
        }

        vectorList1.addAll(floorVectorList1)
        vectorList1.addAll(ceilingVectorList1)

    }

    private fun initPartVectorList(partVectorList: List<PlaneBean>) {
        for (i in partVectorList.indices) {
            var partVector = partVectorList[i]
            var pointList: MutableList<Vector3> = mutableListOf()
            var segmentList: MutableList<List<Vector3>> = mutableListOf()
            for (j in 0 until partVector.pointList.size) {
                pointList.add(
                    Vector3(
                        partVector.pointList[j].x / maxLength,
                        partVector.pointList[j].y / maxLength,
                        partVector.pointList[j].z / maxLength
                    )
                )
            }

            for (j in 0 until partVector.segmentBeanList.size) {
                var segmentPointList: MutableList<Vector3> = mutableListOf()

                segmentPointList.add(
                    Vector3(
                        partVector.segmentBeanList[j].startPointBean?.x!! / maxLength,
                        partVector.segmentBeanList[j].startPointBean?.y!! / maxLength,
                        partVector.segmentBeanList[j].startPointBean?.z!! / maxLength
                    )
                )

                segmentPointList.add(
                    Vector3(
                        partVector.segmentBeanList[j].endPointBean?.x!! / maxLength,
                        partVector.segmentBeanList[j].endPointBean?.y!! / maxLength,
                        partVector.segmentBeanList[j].endPointBean?.z!! / maxLength
                    )
                )

                segmentList.add(segmentPointList)
            }

            when (partVectorList[i].type) {
                "WINDOW" -> {
                    DlogUtil.d(TAG, "window")
                    windowVectorPointList.add(pointList)
                    windowVectorSegmentList.add(segmentList)
                }

                "DOOR" -> {
                    DlogUtil.d(TAG, "door")
                    doorVectorPointList.add(pointList)
                    doorVectorSegmentList.add(segmentList)
                }
            }
        }
    }

    private fun initCenterVector(vectorList: List<List<Vector3>>) {

        var cameraX = 0f
        var cameraY = 0f
        var cameraZ = 0f
        var maxPosition = 0f

        var minZ = vectorList[0][0].z
        var maxZ = vectorList[0][0].z
        var minX = vectorList[0][0].x
        var maxX = vectorList[0][0].x
        var minY = vectorList[0][0].y
        var maxY = vectorList[0][0].y

        for (i in vectorList.indices) {

            cameraX += vectorList[i][0].x
            cameraY += vectorList[i][0].y
            cameraZ += vectorList[i][0].z

            if (maxZ < vectorList[i][0].z)
                maxZ = vectorList[i][0].z
            else if (minZ > vectorList[i][0].z) {
                minZ = vectorList[i][0].z
            }

            if (minX > vectorList[i][0].x)
                minX = vectorList[i][0].x
            else if (maxX < vectorList[i][0].x)
                maxX = vectorList[i][0].x

            if (minY > vectorList[i][0].y)
                minY = vectorList[i][0].y
            else if (maxY < vectorList[i][0].y)
                maxY = vectorList[i][0].y

            cameraX += vectorList[i][1].x
            cameraY += vectorList[i][1].y
            cameraZ += vectorList[i][1].z

            if (maxZ < vectorList[i][1].z)
                maxZ = vectorList[i][1].z
            else if (minZ > vectorList[i][1].z) {
                minZ = vectorList[i][1].z
            }

            if (minX > vectorList[i][1].x)
                minX = vectorList[i][1].x
            else if (maxX < vectorList[i][1].x)
                maxX = vectorList[i][1].x

            if (minY > vectorList[i][1].y)
                minY = vectorList[i][1].y
            else if (maxY < vectorList[i][1].y)
                maxY = vectorList[i][1].y
        }

        cameraX /= vectorList.size * 2
        cameraY /= vectorList.size * 2
        cameraZ /= vectorList.size * 2

        centerPosition = Vector3(cameraX, cameraY, cameraZ)

        //?????? ??? ????????? ???????????? ????????? ??????
        if ((maxX - minX) < (maxZ - minZ) && (maxY - minY) < (maxZ - minZ)) {

            cylinderDiameter = (maxZ - minZ) * 2
            textSize = (maxX - minX)
            DlogUtil.d(TAG, "z??? ????????? ??????? $cylinderDiameter")
            maxPosition = maxZ - centerPosition.z


        } else if ((maxX - minX) > (maxZ - minZ) && (maxY - minY) < (maxX - minX)) {

            cylinderDiameter = (maxX - minX) * 2
            textSize = (maxZ - minZ)
            DlogUtil.d(TAG, "X??? ????????? ??????? $cylinderDiameter")
            maxPosition = maxX - centerPosition.x

        } else {

            cylinderDiameter = (maxY - minY) * 2
            textSize = (maxX - minX)
            DlogUtil.d(TAG, "Y??? ????????? ???????? $cylinderDiameter")
            maxPosition = maxY - centerPosition.y

        }

        cameraClip = maxPosition * 6

        cameraPosition =
            if (cameraZ <= 0) {
                Vector3(
                    cameraX, cameraY,
                    1.5f * maxPosition
                )
            } else {

                Vector3(
                    cameraX, cameraY,
                    cameraZ + 1.5f * maxPosition
                )
            }

    }

    private fun setCameraPosition(vector3: Vector3) {
        val camera = sceneView.scene.camera
        camera.worldPosition = vector3
        camera.farClipPlane = cameraClip
    }

    private fun initSceneView() {

        //sceneView.renderer?.setClearColor(com.google.ar.sceneform.rendering.Color(Color.WHITE))


        transformationSystem =
            TransformationSystem(view.resources.displayMetrics, FootprintSelectionVisualizer())

        parentsTransformableNode = TransformableNode(transformationSystem)
        parentsTransformableNode.setParent(sceneView.scene)

        sceneView.scene.addOnPeekTouchListener { hitTestResult, motionEvent ->

            try {
                transformationSystem.onTouch(hitTestResult, motionEvent)

                if (motionEvent.action == MotionEvent.ACTION_DOWN) {

                    downX = motionEvent.x
                    downY = motionEvent.y

                } else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                    if (motionEvent.pointerCount == 2) {
                        DlogUtil.d(TAG, "????????? 2???")

                        isScale = true;
                        transformationSystem.selectNode(parentsTransformableNode)

                    } else {
                        DlogUtil.d(TAG, "????????? 1???")

                        if (isScale) {
                            return@addOnPeekTouchListener
                        }

                        if (abs(motionEvent.x - downX) > 40 || abs(motionEvent.y - downY) > 40) {

                            if (isFloor) {
                                return@addOnPeekTouchListener
                            }

                            DlogUtil.d(TAG, "????????????")

                            //?????? ??????
                            var x: Float = motionEvent.x - downX
                            var y: Float = motionEvent.y - downY

                            var percentX: Float = x / sceneView.width * 0.5f
                            var percentY: Float = y / sceneView.height * 0.5f

                            xAngle = percentX * 360 * 0.52f + lastXAngle
                            yAngle = percentY * 360 * 0.52f + lastYAngle

                            var xQuaternion = Quaternion.axisAngle(Vector3(0f, 1f, 0f), xAngle)
                            //????????? ??????????????? ???????????? ??????
                            var yQuaternion = Quaternion.axisAngle(
                                Vector3(
                                    cos(Math.toRadians(xAngle.toDouble())).toFloat(),
                                    0f,
                                    sin(Math.toRadians(xAngle.toDouble())).toFloat()
                                ), yAngle
                            )

                            parentsTransformableNode.localRotation =
                                Quaternion.multiply(xQuaternion, yQuaternion)
                        } else {

                            return@addOnPeekTouchListener
                        }
                    }


                } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                    DlogUtil.d(TAG, "????????? ?????????~~~")
                    if (lastDistance != 0f) {
                        lastDistance = 0f
                    }

                    lastXAngle = xAngle
                    lastYAngle = yAngle

                    isScale = false

                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                DlogUtil.d(TAG, "bug")

                return@addOnPeekTouchListener
            }
        }
    }

    private fun setTransformableNode() {

        parentsTransformableNode.worldPosition = centerPosition

        transformableNode.worldPosition = centerPosition
        parentsTransformableNode.scaleController.minScale = 0.5f
        parentsTransformableNode.scaleController.maxScale = 3f

        transformableNode.rotationController.isEnabled = false
        transformableNode.translationController.isEnabled = false
        parentsTransformableNode.rotationController.isEnabled = false
        parentsTransformableNode.translationController.isEnabled = false

        parentsTransformableNode.select()
    }

    private fun draw3Droom() {
        //?????? ?????????
        drawType = Constant.DrawType.TYPE_ROOM
        drawModeling(floorVectorList1)
        drawPillar(floorVectorList1)
        drawModeling(ceilingVectorList1)

        //?????? ?????????
        drawSizeModeling(ceilingVectorList1)
    }

    private fun draw3Dpart() {
        //???, ?????? ?????????
        drawType = Constant.DrawType.TYPE_ROOM_PART
        if (doorVectorSegmentList == null && windowVectorSegmentList == null) {
            return
        } else if (doorVectorSegmentList == null) {
            drawDoorAndWindow(windowVectorSegmentList)
            return
        } else if (windowVectorSegmentList == null) {
            drawDoorAndWindow(doorVectorSegmentList)
        } else {
            drawDoorAndWindow(doorVectorSegmentList)
            drawDoorAndWindow(windowVectorSegmentList)
        }
    }

    private fun drawFloor() {
        //????????? ?????????, ???????????? ???????????? ????????? ????????? ???
        drawType = Constant.DrawType.TYPE_FLOOR
        drawModeling(floorVectorList1)
        //?????? ?????? ????????? ?????????
        drawType = Constant.DrawType.TYPE_FLOOR_MEASURE
        xzMeasureModeling(floorVectorList1)
    }

    private fun drawFloorPart() {
        drawType = Constant.DrawType.TYPE_FLOOR_DOOR
        if (doorVectorSegmentList.isNotEmpty()) {
            for (i in 0 until doorVectorSegmentList.size) {
                addLineBetweenPoints(
                    Vector3(doorVectorSegmentList[i][0][0].x, 0f, doorVectorSegmentList[i][0][0].z),
                    Vector3(doorVectorSegmentList[i][0][1].x, 0f, doorVectorSegmentList[i][0][1].z),
                    Constant.serenityHexColorCode
                )
            }
        }

        drawType = Constant.DrawType.TYPE_FLOOR_WINDOW
        if (windowVectorSegmentList.isNotEmpty()) {
            for (i in 0 until windowVectorSegmentList.size) {
                addLineBetweenPoints(
                    Vector3(
                        windowVectorSegmentList[i][0][0].x,
                        0f,
                        windowVectorSegmentList[i][0][0].z
                    ),
                    Vector3(
                        windowVectorSegmentList[i][0][1].x,
                        0f,
                        windowVectorSegmentList[i][0][1].z
                    ),
                    Constant.serenityHexColorCode
                )
            }
        }

        drawType = Constant.DrawType.TYPE_FLOOR_DOOR_MEASURE

        if (doorVectorSegmentList.isNotEmpty()) {
            for (i in 0 until doorVectorSegmentList.size) {
                xzMeasureModeling(
                    listOf(
                        Vector3(
                            doorVectorSegmentList[i][0][0].x,
                            0f,
                            doorVectorSegmentList[i][0][0].z
                        ),
                        Vector3(
                            doorVectorSegmentList[i][0][1].x,
                            0f,
                            doorVectorSegmentList[i][0][1].z
                        )
                    ),i
                )
            }
        }

        drawType = Constant.DrawType.TYPE_FLOOR_WINDOW_MEASURE

        if (windowVectorSegmentList.isNotEmpty()) {
            for (i in 0 until windowVectorSegmentList.size) {
                xzMeasureModeling(
                    listOf(
                        Vector3(
                            windowVectorSegmentList[i][0][0].x,
                            0f,
                            windowVectorSegmentList[i][0][0].z
                        ),
                        Vector3(
                            windowVectorSegmentList[i][0][1].x,
                            0f,
                            windowVectorSegmentList[i][0][1].z
                        )
                    ),i
                )
            }
        }

//        if (windowVectorSegmentList.isNotEmpty()) {
//            for (i in 0 until windowVectorSegmentList.size) {
//                xzMeasureModeling(
//                    windowVectorSegmentList[i][0]
//                )
//            }
//        }

    }

    private fun drawModeling(vectorList: List<List<Vector3>>) {

        for (i in vectorList.indices) {

            addLineBetweenPoints(
                vectorList[i][0],
                vectorList[i][1],
                Constant.gowoonwooriHexColorCode1
            )

        }
    }

    private fun drawPillar(vectorList: List<List<Vector3>>) {
        for (i in vectorList.indices) {
            addLineBetweenPoints(
                vectorList[i][0],
                Vector3(vectorList[i][0].x, roomBean.height / maxLength, vectorList[i][0].z),
                Constant.gowoonwooriHexColorCode1
            )
        }
    }

    private fun drawDoorAndWindow(partVectorList: List<List<List<Vector3>>>) {
        //draw Door
        for (i in partVectorList.indices) {
            for (j in partVectorList[i].indices) {
                addLineBetweenPoints(
                    partVectorList[i][j][0],
                    partVectorList[i][j][1],
                    Constant.gowoonwooriHexColorCode2
                )
            }

            //draw Size
            startLength(
                partVectorList[i][0][0],
                partVectorList[i][0][1],
                roomBean.wallObjectList[i].segmentBeanList[0].length,
                Constant.Direction.Horizontal
            )

            startLength(
                partVectorList[i][1][0],
                partVectorList[i][1][1],
                roomBean.wallObjectList[i].segmentBeanList[0].length,
                Constant.Direction.Horizontal
            )

            startLength(
                partVectorList[i][2][0],
                partVectorList[i][2][1],
                MathUtil.calculationLengthBetweenTwoVector(
                    partVectorList[i][1][0],
                    partVectorList[i][1][1],
                ),
                Constant.Direction.Vertical
            )

            startLength(
                partVectorList[i][0][1],
                partVectorList[i][0][0],
                MathUtil.calculationLengthBetweenTwoVector(
                    partVectorList[i][1][0],
                    partVectorList[i][1][1],
                ),
                Constant.Direction.Vertical
            )

            drawLengthLine(
                partVectorList[i][0][0],
                partVectorList[i][0][1],
                Constant.Direction.Horizontal
            )

            drawLengthLine(
                partVectorList[i][1][0],
                partVectorList[i][1][1],
                Constant.Direction.Vertical
            )
        }

    }

    private fun drawSizeModeling(vectorList: List<List<Vector3>>) {

        for (i in vectorList.indices) {

            startLength(
                vectorList[i][0],
                Vector3(vectorList[i][0].x, 0f, vectorList[i][0].z),
                roomBean.height,
                Constant.Direction.Horizontal
            )
            drawLengthLine(vectorList[i][0], vectorList[i][1], Constant.Direction.Horizontal)
        }
    }

    private fun xzMeasureModeling(vectorList: List<Vector3>, index : Int) {

        var upper = 0

        var newVector1: Vector3
        var newVector2: Vector3
        var newVector3: Vector3
        var newVector4: Vector3

        for (i in vectorList.indices) {

            var next: Int = if (i == vectorList.size - 1) {
                0
            } else {
                i + 1
            }

            var length = when (drawType) {
                Constant.DrawType.TYPE_FLOOR_MEASURE -> 0.15
                Constant.DrawType.TYPE_FLOOR_DOOR_MEASURE -> 0.4
                    Constant.DrawType.TYPE_FLOOR_WINDOW_MEASURE ->
                    if(index % 2 == 0) {
                        0.6
                    } else {
                        0.8
                    }
                else -> 0.0
            }

            //???????????? ????????? ?????? ????????? ???????????? ???????????? ??????, i=1??? ???????????? ?????????, ???????????? 2??? ???????????? ?????? ???????????? if???
            if (vectorList.size == 2 && i == 1) {
                return
            }

            var slope = MathUtil.calculationSlopeNormalVector(vectorList[i], vectorList[next])

            //upper 1 :  ???????????? upper 0 : ?????? upper -1 : ????????????
            if (slope == 0.0 || vectorList[i].z == vectorList[next].z) {

                upper = 0

                newVector1 = Vector3(vectorList[i].x, 0f, (vectorList[i].z + length).toFloat())
                newVector2 =
                    Vector3(vectorList[next].x, 0f, (vectorList[next].z + length).toFloat())
                newVector3 = Vector3(vectorList[i].x, 0f, (vectorList[i].z + length / 2).toFloat())
                newVector4 =
                    Vector3(vectorList[next].x, 0f, (vectorList[next].z + length / 2).toFloat())

            } else {

                if (vectorList[i].z < vectorList[next].z) {
                    upper = 1
                } else if (vectorList[i].z > vectorList[next].z) {
                    upper = -1
                }

                var xzlist1 =
                    MathUtil.calculationStraightLineEquation(vectorList[i], slope, length, upper)
                newVector1 = Vector3(xzlist1[0].toFloat(), 0f, xzlist1[1].toFloat())

                var xzlist2 =
                    MathUtil.calculationStraightLineEquation(vectorList[next], slope, length, upper)
                newVector2 = Vector3(xzlist2[0].toFloat(), 0f, xzlist2[1].toFloat())

                var xzlist3 =
                    MathUtil.calculationStraightLineEquation(
                        vectorList[i],
                        slope,
                        length / 2,
                        upper
                    )
                newVector3 = Vector3(xzlist3[0].toFloat(), 0f, xzlist3[1].toFloat())

                var xzlist4 =
                    MathUtil.calculationStraightLineEquation(
                        vectorList[next],
                        slope,
                        length / 2,
                        upper
                    )
                newVector4 = Vector3(xzlist4[0].toFloat(), 0f, xzlist4[1].toFloat())
            }

            addLineBetweenPoints(vectorList[i], newVector1, Constant.gowoonwooriHexColorCode1)
            addLineBetweenPoints(
                vectorList[next],
                newVector2,
                Constant.gowoonwooriHexColorCode1
            )

            addLineBetweenPoints(newVector3, newVector4, Constant.gowoonwooriHexColorCode1)
            setLengthLine(newVector3, newVector4, Constant.Direction.FLOOR)
        }
    }

    @JvmName("xzMeasureModeling1")
    private fun xzMeasureModeling(vectorList: List<List<Vector3>>) {

        var upper = 0

        var newVector1: Vector3
        var newVector2: Vector3
        var newVector3: Vector3
        var newVector4: Vector3

        for (i in vectorList.indices) {

            var length = when (drawType) {
                Constant.DrawType.TYPE_FLOOR_MEASURE -> 0.15
                Constant.DrawType.TYPE_FLOOR_DOOR_MEASURE -> 0.5
                else -> 0.0
            }

            var slope = MathUtil.calculationSlopeNormalVector(vectorList[i][0], vectorList[i][1])

            //upper 1 :  ???????????? upper 0 : ?????? upper -1 : ????????????
            if (slope == 0.0 || vectorList[i][0].z == vectorList[i][1].z) {

                upper = 0

                newVector1 = Vector3(vectorList[i][0].x, 0f, (vectorList[i][0].z + length).toFloat())
                newVector2 =
                    Vector3(vectorList[i][1].x, 0f, (vectorList[i][1].z + length).toFloat())
                newVector3 = Vector3(vectorList[i][0].x, 0f, (vectorList[i][0].z + length / 2).toFloat())
                newVector4 =
                    Vector3(vectorList[i][1].x, 0f, (vectorList[i][1].z + length / 2).toFloat())

            } else {

                if (vectorList[i][0].z < vectorList[i][1].z) {
                    upper = -1
                } else if (vectorList[i][0].z > vectorList[i][1].z) {
                    upper = 1
                }

                var xzlist1 =
                    MathUtil.calculationStraightLineEquation(vectorList[i][0], slope, length, upper)
                newVector1 = Vector3(xzlist1[0].toFloat(), 0f, xzlist1[1].toFloat())

                var xzlist2 =
                    MathUtil.calculationStraightLineEquation(vectorList[i][1], slope, length, upper)
                newVector2 = Vector3(xzlist2[0].toFloat(), 0f, xzlist2[1].toFloat())

                var xzlist3 =
                    MathUtil.calculationStraightLineEquation(
                        vectorList[i][0],
                        slope,
                        length / 2,
                        upper
                    )
                newVector3 = Vector3(xzlist3[0].toFloat(), 0f, xzlist3[1].toFloat())

                var xzlist4 =
                    MathUtil.calculationStraightLineEquation(
                        vectorList[i][1],
                        slope,
                        length / 2,
                        upper
                    )
                newVector4 = Vector3(xzlist4[0].toFloat(), 0f, xzlist4[1].toFloat())
            }

            addLineBetweenPoints(Vector3(vectorList[i][0].x, 0f, vectorList[i][0].z), newVector1, Constant.gowoonwooriHexColorCode1)
            addLineBetweenPoints(Vector3(vectorList[i][1].x, 0f, vectorList[i][1].z), newVector2, Constant.gowoonwooriHexColorCode1)

            addLineBetweenPoints(newVector3, newVector4, Constant.gowoonwooriHexColorCode1)
            setLengthLine(newVector4, newVector3, Constant.Direction.FLOOR)
        }
    }



    private fun addLineBetweenPoints(from: Vector3, to: Vector3, colorCode: String) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)

        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        // Prepare a color
        val colorCode = com.google.ar.sceneform.rendering.Color(Color.parseColor(colorCode))

        when (drawType) {
            Constant.DrawType.TYPE_ROOM, Constant.DrawType.TYPE_FLOOR -> {
                RenderingUtil.drawCylinderLine(
                    view.context,
                    colorCode,
                    0.0025f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )

            }
            Constant.DrawType.TYPE_ROOM_PART -> {

                RenderingUtil.drawCylinderLine(
                    view.context,
                    colorCode,
                    0.0015f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )
            }
            Constant.DrawType.TYPE_FLOOR_MEASURE, Constant.DrawType.TYPE_FLOOR_DOOR_MEASURE, Constant.DrawType.TYPE_FLOOR_WINDOW_MEASURE -> {

                RenderingUtil.drawCylinderLine(
                    view.context,
                    colorCode,
                    0.0005f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )
            }


            Constant.DrawType.TYPE_FLOOR_WINDOW -> {
                DlogUtil.d(TAG, "???????????????????????")
                RenderingUtil.drawTransparentCylinderLine(
                    view.context,
                    colorCode,
                    0.0040f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )
            }

            Constant.DrawType.TYPE_FLOOR_DOOR -> {
                RenderingUtil.drawDashCylinderLine(
                    view.context,
                    colorCode,
                    0.0030f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )
            }

        }
    }

    private fun addDashLineBetweenPoints(from: Vector3, to: Vector3, colorCode: String) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)

        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        // Prepare a color
        val colorCode = com.google.ar.sceneform.rendering.Color(Color.parseColor(colorCode))

        when (drawType) {
            Constant.DrawType.TYPE_ROOM, Constant.DrawType.TYPE_FLOOR -> {
                RenderingUtil.drawCylinderLine(
                    view.context,
                    colorCode,
                    0.0025f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )

            }
            Constant.DrawType.TYPE_ROOM_PART -> {

                RenderingUtil.drawCylinderLine(
                    view.context,
                    colorCode,
                    0.0015f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )
            }
            Constant.DrawType.TYPE_FLOOR_MEASURE, Constant.DrawType.TYPE_FLOOR_DOOR_MEASURE -> {

                RenderingUtil.drawCylinderLine(
                    view.context,
                    colorCode,
                    0.0005f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )
            }

            Constant.DrawType.TYPE_FLOOR_DOOR -> {
                DlogUtil.d(TAG, "???????????????????????")
                RenderingUtil.drawDashCylinderLine(
                    view.context,
                    colorCode,
                    0.0040f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )
            }

        }
    }

    private fun startLength(
        to: Vector3,
        from: Vector3,
        measure: Float,
        direction: Constant.Direction
    ) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)

        // Compute a line's length

        if (drawType == Constant.DrawType.TYPE_ROOM) {

            // Prepare a color
            val colorCode = com.google.ar.sceneform.rendering.Color(Color.parseColor("#888888"))

            if (direction == Constant.Direction.Horizontal) {

                percentageHeight = measure / maxLength * 0.1f

                //Rendering
                RenderingUtil.extendCylinderLineY(
                    view.context,
                    colorCode,
                    0.001f * cylinderDiameter,
                    percentageHeight,
                    transformableNode,
                    to
                )

            } else if (direction == Constant.Direction.Vertical) {
                percentageWidth = measure / maxLength * 0.2f

                //Rendering
                RenderingUtil.extendCylinderLineX(
                    view.context,
                    colorCode,
                    0.001f * cylinderDiameter,
                    percentageWidth,
                    transformableNode,
                    to, from
                )

            }

        } else if (drawType == Constant.DrawType.TYPE_ROOM_PART) {

            // Prepare a color
            val colorCode = com.google.ar.sceneform.rendering.Color(Color.parseColor("#888888"))

            if (direction == Constant.Direction.Horizontal) {

                // Compute a line's length
                percentageDoorHeight = measure / maxLength * 0.2f

                //Rendering
                RenderingUtil.extendCylinderLineY(
                    view.context,
                    colorCode,
                    0.0005f * cylinderDiameter,
                    percentageDoorHeight,
                    transformableNode,
                    to
                )

            } else {

                // Compute a line's length
                percentageDoorWidth = measure / maxLength * .2f
                DlogUtil.d(TAG, "percentageDoorWidth $percentageDoorWidth")

                //Rendering
                RenderingUtil.extendCylinderLineX(
                    view.context,
                    colorCode,
                    0.0005f * cylinderDiameter,
                    percentageDoorWidth,
                    transformableNode,
                    to, from
                )

            }
        }
    }

    private fun drawLengthLine(from: Vector3, to: Vector3, direction: Constant.Direction) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)

        var axisFrom: Vector3 = Vector3()
        var axisTo: Vector3 = Vector3()

        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        // Prepare a color
        val colorCode = com.google.ar.sceneform.rendering.Color(Color.parseColor("#888888"))

        //re-init axis
        if (drawType == Constant.DrawType.TYPE_ROOM) {

            if (direction == Constant.Direction.Horizontal) {
                axisFrom = Vector3(from.x, from.y + percentageHeight * 0.5f, from.z)
                axisTo = Vector3(to.x, to.y + percentageHeight * 0.5f, to.z)
            }

            //Rendering
            RenderingUtil.drawCylinderLine(
                view.context,
                colorCode,
                0.001f * cylinderDiameter,
                lineLength,
                transformableNode,
                axisFrom,
                axisTo
            )

        } else if (drawType == Constant.DrawType.TYPE_ROOM_PART) {

            if (direction == Constant.Direction.Horizontal) {
                axisFrom = Vector3(from.x, from.y + percentageDoorHeight * 0.5f, from.z)
                axisTo = Vector3(to.x, to.y + percentageDoorHeight * 0.5f, to.z)
            } else if (direction == Constant.Direction.Vertical) {
                axisFrom = Vector3(from.x, from.y, from.z)
                axisTo = Vector3(to.x, to.y, to.z)
            }

            //Rendering
            RenderingUtil.drawCylinderLine(
                view.context,
                colorCode,
                0.0005f * cylinderDiameter,
                lineLength,
                transformableNode,
                axisFrom,
                axisTo
            )

        }
        setLengthLine(from, to, direction)
    }

    private fun setLengthLine(from: Vector3, to: Vector3, direction: Constant.Direction) {

        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableViewNode = TransformableNode(transformationSystem)
        transformableViewNode.setParent(parentsTransformableNode)

        //set Text
        var list: List<Float> = listOf(
            to.x * maxLength - from.x * maxLength,
            to.y * maxLength - from.y * maxLength,
            to.z * maxLength - from.z * maxLength
        )

        var lengthText = (round(MathUtil.calculationLength(list) * 100) / 100).toString() + "m"

        //Rendering
        RenderingUtil.drawTextView(
            view.context,
            centerPosition,
            percentageHeight,
            lengthText,
            transformableNode,
            from,
            to,
            direction
        )
    }

}