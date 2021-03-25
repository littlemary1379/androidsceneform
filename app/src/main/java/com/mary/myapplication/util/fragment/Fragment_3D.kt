package com.mary.myapplication.util.fragment

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import com.mary.myapplication.R
import com.mary.myapplication.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin


class Fragment_3D : Fragment() {

    companion object {
        private const val TAG = "Fragment_3D"
    }

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
    private var percentageHeight: Float = 0f
    private var percentageWidth: Float = 0f
    private var percentageDoorHeight: Float = 0f
    private var percentageDoorWidth: Float = 0f

    private lateinit var drawType: Constant.DrawType

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_3d, null)
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    override fun onResume() {
        super.onResume()
        try {
            sceneView.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        findView()

        //정육면 입방체임
        //입방체 바닥
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
        height = 20f

        var rawFirstDoorVector = Vector3(10f, 0f, 0f)
        var rawSecondDoorVector = Vector3(18f, 0f, 0f)
        var doorHeight = 15f
        var rawDoorVectorList = listOf(rawFirstDoorVector, rawSecondDoorVector)

        var rawFirstWindowVector = Vector3(24f, 5f, -32f)
        var rawSecondWindowVector = Vector3(21f, 5f, -38f)
        var windowHeight = 10f
        var rawWindowVectorList = listOf(rawFirstWindowVector, rawSecondWindowVector)

        maxLength = LocationUtil.longLength(rawVectorList, height)

        DlogUtil.d(
            Companion.TAG,
            "가장 큰 길이 $maxLength"
        )

        initVectorList(rawVectorList)

        initCenterVector(vectorList)
        setCameraPosition(cameraPosition)

        initSceneView()

        //모델 랜더링
        drawModeling(floorVectorList)
        drawPillar(floorVectorList)
        drawModeling(ceilingVectorList)

        //위로 올라오는 선 긋기(y축만 차이나는 선끼리 찾아 벡터로 연결), y가 높은게 to로 들어가야 선이 연결됨.
        //전체 model height에서 15% 정도 선이 올라오도록 하는게 좋지 않을까?
        //여기는 계산을 해둔 상태인데, roomBean에 높이가 있으니까 lineLength는 빼도 될거 같아 :D
        drawSizeModeling(ceilingVectorList)

        //만약 창문이나 문이 있을 경우, 먼저 모델링 좌표를 가지고 와서 동일하게 좌표를 조절하고, 랜더링 하면 될듯 ㅇㅁ... ?!
        drawDoorAndWindow(rawDoorVectorList, doorHeight)
        drawDoorAndWindow(rawWindowVectorList, windowHeight)

        setTransformableNode()


    }

    private fun findView() {
        sceneView = view!!.findViewById(R.id.sceneView)
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
        context!!.display?.getRealSize(deviceSize)
        val deviceWidth = deviceSize.x
        val deviceHeight = deviceSize.y

        if ((maxX - minX) < (maxZ - minZ) && (maxY - minY) < (maxZ - minZ)) {

            DlogUtil.d(TAG, "z로 맞춰야 할듯?")
            cylinderDiameter = (maxZ - minZ) * 2
            textSize = (maxX - minX)

        } else if (deviceWidth / deviceHeight.toDouble() < (maxX - minX) / (maxY - minY)) {
            DlogUtil.d(TAG, deviceWidth / deviceHeight.toDouble())
            DlogUtil.d(TAG, (maxX - minX) / (maxY - minY))
            DlogUtil.d(TAG, "X로 맞춰야 할듯?")
            cylinderDiameter = (maxX - minX) * 2
            textSize = (maxZ - minZ)


        } else {
            DlogUtil.d(TAG, deviceWidth / deviceHeight.toDouble())
            DlogUtil.d(TAG, (maxX - minX) / (maxY - minY))
            DlogUtil.d(TAG, "Y로 맞춰야 할듯??")
            cylinderDiameter = (maxY - minY) * 2
            textSize = (maxX - minX)
        }

        var centerLength = MathUtil.calculationLength(
            listOf(
                vectorList[2].x - centerPosition.x,
                vectorList[2].y - centerPosition.y,
                vectorList[2].z - centerPosition.z
            )
        )

        cameraClip = centerLength * 6

        DlogUtil.d(TAG, "centerLength : $centerLength")

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
            TransformationSystem(resources.displayMetrics, FootprintSelectionVisualizer())

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

        parentsTransformableNode.scaleController.minScale = 0.5f
        parentsTransformableNode.scaleController.maxScale = 2f

    }


    private fun drawModeling(vectorList: List<Vector3>) {
        DlogUtil.d(TAG, " ???????????? ")

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

    private fun addLineBetweenPoints(from: Vector3, to: Vector3, colorCode: String) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)


        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        // Prepare a color
        val colorCode = com.google.ar.sceneform.rendering.Color(Color.parseColor(colorCode))

        if(drawType == Constant.DrawType.TYPE_ROOM) {
            context?.let {
                RenderingUtil.drawCylinderLine(
                    it,
                    colorCode,
                    0.0025f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    from,
                    to
                )
            }
        } else if(drawType == Constant.DrawType.TYPE_DOOR) {
            context?.let {
                RenderingUtil.drawCylinderLine(
                    it,
                    colorCode,
                    0.0015f * cylinderDiameter,
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

                percentageHeight = measure / maxLength * 0.2f

                //Rendering
                context?.let {
                    RenderingUtil.extendCylinderLineY(
                        it,
                        colorCode,
                        0.001f * cylinderDiameter,
                        percentageHeight,
                        transformableNode,
                        to
                    )
                }
            } else if (direction == Constant.Direction.Vertical) {
                percentageWidth = measure / maxLength * 0.2f

                //Rendering
                context?.let {
                    RenderingUtil.extendCylinderLineX(
                        it,
                        colorCode,
                        0.001f * cylinderDiameter,
                        percentageWidth,
                        transformableNode,
                        to, from
                    )
                }
            }

        } else if (drawType == Constant.DrawType.TYPE_DOOR) {

            // Prepare a color
            val colorCode = com.google.ar.sceneform.rendering.Color(Color.parseColor("#888888"))

            if (direction == Constant.Direction.Horizontal) {

                // Compute a line's length
                percentageDoorHeight = measure / maxLength * 0.2f

                //Rendering
                context?.let {
                    RenderingUtil.extendCylinderLineY(
                        it,
                        colorCode,
                        0.0005f * cylinderDiameter,
                        percentageDoorHeight,
                        transformableNode,
                        to
                    )
                }

            } else {

                // Compute a line's length
                percentageDoorWidth = measure / maxLength * .2f
                DlogUtil.d(TAG, "percentageDoorWidth $percentageDoorWidth")

                //Rendering
                context?.let {
                    RenderingUtil.extendCylinderLineX(
                        it,
                        colorCode,
                        0.0005f * cylinderDiameter,
                        percentageDoorWidth,
                        transformableNode,
                        to, from
                    )
                }

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
            context?.let {
                RenderingUtil.drawCylinderLine(
                    it,
                    colorCode,
                    0.001f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    axisFrom,
                    axisTo
                )
            }

        } else if (drawType == Constant.DrawType.TYPE_DOOR) {

            if (direction == Constant.Direction.Horizontal) {
                axisFrom = Vector3(from.x, from.y + percentageDoorHeight * 0.5f, from.z)
                axisTo = Vector3(to.x, to.y + percentageDoorHeight * 0.5f, to.z)

            } else if (direction == Constant.Direction.Vertical) {
                DlogUtil.d(TAG, "어엥?")
                axisFrom = Vector3(from.x, from.y, from.z)
                axisTo = Vector3(to.x, to.y, to.z)


            }

            //Rendering
            context?.let {
                RenderingUtil.drawCylinderLine(
                    it,
                    colorCode,
                    0.0005f * cylinderDiameter,
                    lineLength,
                    transformableNode,
                    axisFrom,
                    axisTo
                )
            }

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

        if (direction == Constant.Direction.Horizontal) {
            //Rendering
            context?.let {
                RenderingUtil.drawTextView(
                    it,
                    centerPosition,
                    percentageHeight,
                    lengthText,
                    transformableNode,
                    from,
                    to,
                    Constant.Direction.Horizontal
                )
            }
        } else if (direction == Constant.Direction.Vertical) {
            //Rendering
            context?.let {
                RenderingUtil.drawTextView(
                    it,
                    centerPosition,
                    percentageHeight,
                    lengthText,
                    transformableNode,
                    from,
                    to,
                    Constant.Direction.Vertical
                )
            }
        }
    }

}