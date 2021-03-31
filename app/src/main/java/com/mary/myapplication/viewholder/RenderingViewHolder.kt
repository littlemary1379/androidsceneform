package com.mary.myapplication.viewholder

import android.content.Context
import android.graphics.Color
import android.graphics.Point
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
import com.mary.myapplication.temp.RoomData
import com.mary.myapplication.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

class RenderingViewHolder(context: Context, type: Int) {

    companion object {
        private const val TAG = "RenderingViewHolder"
        val TYPE_3D = 0
        val TYPE_FLOOR = 1
        val TYPE_WALL = 2
    }

    var view: View = LayoutInflater.from(context).inflate(R.layout.viewholder_rendering, null)

    private lateinit var sceneView: SceneView

    private lateinit var transformationSystem: TransformationSystem
    private lateinit var transformableNode: TransformableNode
    private lateinit var parentsTransformableNode: TransformableNode
    private lateinit var transformableViewNode: TransformableNode

    private var height = 0f
    private var maxLength = 0f
    private lateinit var floorVectorList: MutableList<Vector3>
    private lateinit var ceilingVectorList: MutableList<Vector3>
    var vectorList: MutableList<Vector3> = mutableListOf()

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

    private var roomData = RoomData()
    private lateinit var drawType: Constant.DrawType

    init {

        findView()
        height = 20f
        var doorHeight = 15f
        var windowHeight = 10f

        maxLength = LocationUtil.longLength(roomData.rawVectorList, height)

        DlogUtil.d(
            TAG,
            "가장 큰 길이 $maxLength"
        )

        initVectorList(roomData.rawVectorList)

        initCenterVector(vectorList)
        setCameraPosition(cameraPosition)

        initSceneView()

        when (type) {

            TYPE_3D -> {
                //모델 랜더링
                drawModeling(floorVectorList)
                drawPillar(floorVectorList)
                drawModeling(ceilingVectorList)

                //치수 랜더링
                drawSizeModeling(ceilingVectorList)

                //문, 창문 랜더링
                drawDoorAndWindow(roomData.rawDoorVectorList, doorHeight)
                drawDoorAndWindow(roomData.rawWindowVectorList, windowHeight)
            }

            TYPE_FLOOR -> {
                //바닥만 그리고, 그려진걸 쿼테이션 시켜서 뒤집을 것
                isFloor = true
                drawModeling(floorVectorList)
                testModeling(floorVectorList)

                //랜더링 시간 고려해서 스레드 처리
                Thread {
                    Thread.sleep(1000)
                    quaternionXAxis90Rendering()
                }.start()
            }

            else -> {
                drawModeling(floorVectorList)
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

    private fun initVectorList(rawVectorList: List<Vector3>) {

        floorVectorList = mutableListOf()
        ceilingVectorList = mutableListOf()

        for (i in rawVectorList.indices) {
            ceilingVectorList.add(
                Vector3(
                    rawVectorList[i].x / maxLength,
                    height / maxLength,
                    rawVectorList[i].z / maxLength
                )
            )
            floorVectorList.add(
                Vector3(
                    rawVectorList[i].x / maxLength,
                    0f,
                    rawVectorList[i].z / maxLength
                )
            )
        }

        vectorList.addAll(floorVectorList)
        vectorList.addAll(ceilingVectorList)
    }


    private fun initCenterVector(vectorList: List<Vector3>) {
        var cameraX = 0f
        var cameraY = 0f
        var cameraZ = 0f

        var minZ = vectorList[0].z
        var maxZ = vectorList[0].z
        var minX = vectorList[0].x
        var maxX = vectorList[0].x
        var minY = vectorList[0].y
        var maxY = vectorList[0].y

        for (i in vectorList.indices) {
            cameraX += vectorList[i].x
            cameraY += vectorList[i].y
            cameraZ += vectorList[i].z

            if (maxZ < vectorList[i].z)
                maxZ = vectorList[i].z
            else if (minZ > vectorList[i].z) {
                minZ = vectorList[i].z
            }

            if (minX > vectorList[i].x)
                minX = vectorList[i].x
            else if (maxX < vectorList[i].x)
                maxX = vectorList[i].x

            if (minY > vectorList[i].y)
                minY = vectorList[i].y
            else if (maxY < vectorList[i].y)
                maxY = vectorList[i].y
        }


        cameraX /= vectorList.size
        cameraY /= vectorList.size
        cameraZ /= vectorList.size

        centerPosition = Vector3(cameraX, cameraY, cameraZ)

        var deviceSize = Point()
        view.context.display?.getRealSize(deviceSize)
        val deviceWidth = deviceSize.x
        val deviceHeight = deviceSize.y

        if ((maxX - minX) < (maxZ - minZ) && (maxY - minY) < (maxZ - minZ)) {

            cylinderDiameter = (maxZ - minZ) * 2
            textSize = (maxX - minX)
            DlogUtil.d(TAG, "z로 맞춰야 할듯? $cylinderDiameter")

        } else if (deviceWidth / deviceHeight.toDouble() < (maxX - minX) / (maxY - minY)) {

            cylinderDiameter = (maxX - minX) * 2
            textSize = (maxZ - minZ)
            DlogUtil.d(TAG, "X로 맞춰야 할듯? $cylinderDiameter")

        } else {

            cylinderDiameter = (maxY - minY) * 2
            textSize = (maxX - minX)
            DlogUtil.d(TAG, "Y로 맞춰야 할듯?? $cylinderDiameter")

        }

        var centerLength = MathUtil.calculationLength(
            listOf(
                vectorList[2].x - centerPosition.x,
                vectorList[2].y - centerPosition.y,
                vectorList[2].z - centerPosition.z
            )
        )

        cameraClip = centerLength * 6

        cameraPosition =
            if (cameraZ <= 0) {
                Vector3(
                    cameraX, cameraY,
                    1.5f * centerLength
                )
            } else {
                Vector3(
                    cameraX, cameraY,
                    cameraZ * 2 + 1.5f * centerLength
                )
            }
    }

    private fun setCameraPosition(vector3: Vector3) {
        val camera = sceneView.scene.camera
        camera.worldPosition = vector3
        camera.farClipPlane = cameraClip
    }

    private fun initSceneView() {

        sceneView.renderer?.setClearColor(com.google.ar.sceneform.rendering.Color(Color.WHITE))
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
                        DlogUtil.d(TAG, "손가락 2개")

                        isScale = true;

                    } else {
                        DlogUtil.d(TAG, "손가락 1개")

                        if (isScale) {
                            return@addOnPeekTouchListener
                        }

                        if (abs(motionEvent.x - downX) > 40 || abs(motionEvent.y - downY) > 40) {

                            if (isFloor) {
                                return@addOnPeekTouchListener
                            }

                            DlogUtil.d(TAG, "쿼터니언")

                            //개선 필요
                            var x: Float = motionEvent.x - downX
                            var y: Float = motionEvent.y - downY

                            var percentX: Float = x / sceneView.width * 0.5f
                            var percentY: Float = y / sceneView.height * 0.5f

                            xAngle = percentX * 360 * 0.52f + lastXAngle
                            yAngle = percentY * 360 * 0.52f + lastYAngle

                            var xQuaternion = Quaternion.axisAngle(Vector3(0f, 1f, 0f), xAngle)
                            //자바의 삼각함수는 라디언만 먹음
                            var yQuaternion = Quaternion.axisAngle(
                                Vector3(
                                    cos(Math.toRadians(xAngle.toDouble())).toFloat(),
                                    0f,
                                    sin(Math.toRadians(xAngle.toDouble())).toFloat()
                                ), yAngle
                            )

                            parentsTransformableNode.localRotation =
                                Quaternion.multiply(xQuaternion, yQuaternion)
                        }
                    }


                } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                    DlogUtil.d(TAG, "손가락 뗐다고~~~")
                    if (lastDistance != 0f) {
                        lastDistance = 0f
                    }

                    lastXAngle = xAngle
                    lastYAngle = yAngle

                    isScale = false

                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setTransformableNode() {

        parentsTransformableNode.select()
        parentsTransformableNode.worldPosition = centerPosition

        transformableNode.worldPosition = centerPosition

        parentsTransformableNode.scaleController.minScale = 1f
        parentsTransformableNode.scaleController.maxScale = 3f
        parentsTransformableNode.rotationController.isEnabled = false

    }


    private fun drawModeling(vectorList: List<Vector3>) {

        drawType = Constant.DrawType.TYPE_ROOM

        for (i in vectorList.indices) {
            if (i == vectorList.size - 1) {
                addLineBetweenPoints(
                    vectorList[i],
                    vectorList[0],
                    Constant.gowoonwooriHexColorCode1
                )
            } else {
                addLineBetweenPoints(
                    vectorList[i],
                    vectorList[i + 1],
                    Constant.gowoonwooriHexColorCode1
                )
            }
        }
    }

    private fun drawPillar(vectorList: List<Vector3>) {
        for (i in vectorList.indices) {
            addLineBetweenPoints(
                vectorList[i],
                Vector3(vectorList[i].x, height / maxLength, vectorList[i].z),
                Constant.gowoonwooriHexColorCode1
            )
        }
    }

    private fun drawDoorAndWindow(doorVectorList: List<Vector3>, doorHeight: Float) {

        drawType = Constant.DrawType.TYPE_DOOR

        //draw Door
        addLineBetweenPoints(
            Vector3(
                doorVectorList[0].x / maxLength,
                doorVectorList[0].y / maxLength,
                doorVectorList[0].z / maxLength
            ),
            Vector3(
                doorVectorList[1].x / maxLength,
                doorVectorList[1].y / maxLength,
                doorVectorList[1].z / maxLength
            ),
            Constant.gowoonwooriHexColorCode2
        )
        addLineBetweenPoints(
            Vector3(
                doorVectorList[0].x / maxLength,
                (doorVectorList[0].y + doorHeight) / maxLength,
                doorVectorList[0].z / maxLength
            ),
            Vector3(
                doorVectorList[1].x / maxLength,
                (doorVectorList[1].y + doorHeight) / maxLength,
                doorVectorList[1].z / maxLength
            ),
            Constant.gowoonwooriHexColorCode2
        )
        addLineBetweenPoints(
            Vector3(
                doorVectorList[0].x / maxLength,
                doorVectorList[0].y / maxLength,
                doorVectorList[0].z / maxLength
            ),
            Vector3(
                doorVectorList[0].x / maxLength,
                (doorVectorList[0].y + doorHeight) / maxLength,
                doorVectorList[0].z / maxLength
            ),
            Constant.gowoonwooriHexColorCode2
        )

        addLineBetweenPoints(
            Vector3(
                doorVectorList[1].x / maxLength,
                doorVectorList[1].y / maxLength,
                doorVectorList[1].z / maxLength
            ),
            Vector3(
                doorVectorList[1].x / maxLength,
                (doorVectorList[1].y + doorHeight) / maxLength,
                doorVectorList[1].z / maxLength
            ),
            Constant.gowoonwooriHexColorCode2
        )

        //draw Size
        startLength(
            Vector3(
                doorVectorList[0].x / maxLength,
                (doorVectorList[0].y + doorHeight) / maxLength,
                doorVectorList[0].z / maxLength
            ),
            Vector3(
                doorVectorList[0].x / maxLength,
                doorVectorList[0].y / maxLength,
                doorVectorList[0].z / maxLength
            ), doorHeight, Constant.Direction.Horizontal
        )

        startLength(
            Vector3(
                doorVectorList[1].x / maxLength,
                (doorVectorList[1].y + doorHeight) / maxLength,
                doorVectorList[1].z / maxLength
            ), Vector3(
                doorVectorList[1].x / maxLength,
                doorVectorList[1].y / maxLength,
                doorVectorList[1].z / maxLength
            ), doorHeight, Constant.Direction.Horizontal
        )

        startLength(
            Vector3(
                doorVectorList[1].x / maxLength,
                (doorVectorList[1].y + doorHeight) / maxLength,
                doorVectorList[1].z / maxLength
            ),

            Vector3(
                doorVectorList[0].x / maxLength,
                (doorVectorList[0].y + doorHeight) / maxLength,
                doorVectorList[0].z / maxLength
            ),

            MathUtil.calculationLength(
                listOf(
                    (doorVectorList[1].x - doorVectorList[0].x),
                    (doorVectorList[1].y - doorVectorList[0].y)
                )
            ), Constant.Direction.Vertical
        )

        startLength(
            Vector3(
                doorVectorList[1].x / maxLength,
                doorVectorList[1].y / maxLength,
                doorVectorList[1].z / maxLength
            ), Vector3(
                doorVectorList[0].x / maxLength,
                doorVectorList[0].y / maxLength,
                doorVectorList[0].z / maxLength
            ),
            MathUtil.calculationLength(
                listOf(
                    (doorVectorList[1].x - doorVectorList[0].x),
                    (doorVectorList[1].y - doorVectorList[0].y)
                )
            ), Constant.Direction.Vertical
        )

        drawLengthLine(
            Vector3(
                doorVectorList[0].x / maxLength,
                (doorVectorList[0].y + doorHeight) / maxLength,
                doorVectorList[0].z / maxLength
            ), Vector3(
                doorVectorList[1].x / maxLength,
                (doorVectorList[1].y + doorHeight) / maxLength,
                doorVectorList[1].z / maxLength
            ), Constant.Direction.Horizontal
        )

        drawLengthLine(
            MathUtil.addVector(
                Vector3(
                    doorVectorList[0].x / maxLength,
                    (doorVectorList[0].y + doorHeight) / maxLength,
                    doorVectorList[0].z / maxLength
                ), Vector3(
                    doorVectorList[1].x / maxLength,
                    (doorVectorList[1].y + doorHeight) / maxLength,
                    doorVectorList[1].z / maxLength
                ), 10
            ),
            MathUtil.addVector(
                Vector3(
                    doorVectorList[0].x / maxLength,
                    doorVectorList[0].y / maxLength,
                    doorVectorList[0].z / maxLength
                ), Vector3(
                    doorVectorList[1].x / maxLength,
                    doorVectorList[1].y / maxLength,
                    doorVectorList[1].z / maxLength
                ), 10
            ),
            Constant.Direction.Vertical
        )
    }

    private fun drawSizeModeling(vectorList: List<Vector3>) {

        for (i in vectorList.indices) {
            if (i == vectorList.size - 1) {
                startLength(
                    vectorList[i],
                    Vector3(vectorList[i].x, 0f, vectorList[i].z),
                    height,
                    Constant.Direction.Horizontal
                )
                drawLengthLine(vectorList[i], vectorList[0], Constant.Direction.Horizontal)
            } else {
                startLength(
                    vectorList[i],
                    Vector3(vectorList[i].x, 0f, vectorList[i].z),
                    height,
                    Constant.Direction.Horizontal
                )
                drawLengthLine(vectorList[i], vectorList[i + 1], Constant.Direction.Horizontal)
            }
        }
    }

    private fun testModeling(vectorList: List<Vector3>) {

        drawType = Constant.DrawType.TYPE_MEASURE
        var length = 0.15
        var upper : Boolean = false

        for (i in vectorList.indices) {
            if (i == vectorList.size - 1) {
                var slope = MathUtil.calculationSlopeNormalVector(vectorList[i], vectorList[0])

                if (vectorList[i].z < vectorList[0].z) {
                    upper = true
                }

                var xzlist1 = MathUtil.calculationStraightLineEquation(vectorList[i], slope, length, upper)
                var newVector1 = Vector3(xzlist1[0].toFloat(), 0f, xzlist1[1].toFloat())

                var xzlist2 = MathUtil.calculationStraightLineEquation(vectorList[0], slope, length, upper)
                var newVector2 = Vector3(xzlist2[0].toFloat(), 0f, xzlist2[1].toFloat())

                addLineBetweenPoints(vectorList[i], newVector1, Constant.gowoonwooriHexColorCode1)
                addLineBetweenPoints(vectorList[0], newVector2, Constant.gowoonwooriHexColorCode1)

                var xzlist3 = MathUtil.calculationStraightLineEquation(vectorList[i], slope, length/2, upper)
                var newVector3 = Vector3(xzlist3[0].toFloat(), 0f, xzlist3[1].toFloat())

                var xzlist4 = MathUtil.calculationStraightLineEquation(vectorList[0], slope, length/2, upper)
                var newVector4 = Vector3(xzlist4[0].toFloat(), 0f, xzlist4[1].toFloat())

                addLineBetweenPoints(newVector3, newVector4, Constant.gowoonwooriHexColorCode1)
                setLengthLine(newVector3, newVector4, Constant.Direction.FLOOR)

            } else {
                var slope = MathUtil.calculationSlopeNormalVector(vectorList[i], vectorList[i+1])

                if (vectorList[i].z < vectorList[i+1].z) {
                    upper = true
                }

                var xzlist1 = MathUtil.calculationStraightLineEquation(vectorList[i], slope, length, upper)
                var newVector1 = Vector3(xzlist1[0].toFloat(), 0f, xzlist1[1].toFloat())

                var xzlist2 = MathUtil.calculationStraightLineEquation(vectorList[i+1], slope, length, upper)
                var newVector2 = Vector3(xzlist2[0].toFloat(), 0f, xzlist2[1].toFloat())

                addLineBetweenPoints(vectorList[i], newVector1, Constant.gowoonwooriHexColorCode1)
                addLineBetweenPoints(vectorList[i+1], newVector2, Constant.gowoonwooriHexColorCode1)

                var xzlist3 = MathUtil.calculationStraightLineEquation(vectorList[i], slope, length/2, upper)
                var newVector3 = Vector3(xzlist3[0].toFloat(), 0f, xzlist3[1].toFloat())

                var xzlist4 = MathUtil.calculationStraightLineEquation(vectorList[i+1], slope, length/2, upper)
                var newVector4 = Vector3(xzlist4[0].toFloat(), 0f, xzlist4[1].toFloat())

                addLineBetweenPoints(newVector3, newVector4, Constant.gowoonwooriHexColorCode1)
                setLengthLine(newVector3, newVector4, Constant.Direction.FLOOR)

            }
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

        if (drawType == Constant.DrawType.TYPE_ROOM) {
            RenderingUtil.drawCylinderLine(
                view.context,
                colorCode,
                0.0025f * cylinderDiameter,
                lineLength,
                transformableNode,
                from,
                to
            )

        } else if (drawType == Constant.DrawType.TYPE_DOOR) {

            RenderingUtil.drawCylinderLine(
                view.context,
                colorCode,
                0.0015f * cylinderDiameter,
                lineLength,
                transformableNode,
                from,
                to
            )
        } else if (drawType == Constant.DrawType.TYPE_MEASURE) {

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

                percentageHeight = measure / maxLength * 0.2f

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

        } else if (drawType == Constant.DrawType.TYPE_DOOR) {

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

        } else if (drawType == Constant.DrawType.TYPE_DOOR) {

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