package com.mary.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.*
import com.mary.myapplication.util.*
import kotlin.math.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var sceneView: SceneView

    private var installRequest: Boolean = false

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
    private var percentageDoorHeight: Float = 0f
    private var percentageDoorWidth: Float = 0f

    private var drawType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findView()

        permissionCheck()
        checkARcore()


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

        maxLength = LocationUtil.longLength(rawVectorList, height)

        DlogUtil.d(
            TAG,
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
        drawDoor(rawDoorVectorList, doorHeight)

        setTransformableNode()

    }

    private fun findView() {
        sceneView = findViewById(R.id.sceneView)
    }

    override fun onResume() {
        super.onResume()

        if (sceneView == null) {
            return
        }

        try {
            sceneView.resume()

        } catch (e: CameraNotAvailableException) {
            DlogUtil.d(TAG, e)
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
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
        display?.getRealSize(deviceSize)
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

        sceneView.renderer?.setClearColor(Color(android.graphics.Color.WHITE))
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

        drawType = "TYPE_ROOM"

        for (i in vectorList.indices) {
            if (i == vectorList.size - 1) {
                addLineBetweenPoints(vectorList[i], vectorList[0], Constant.pinkHexColorCode)
            } else {
                addLineBetweenPoints(vectorList[i], vectorList[i + 1], Constant.pinkHexColorCode)
            }
        }
    }

    private fun drawPillar(vectorList: List<Vector3>) {
        for (i in vectorList.indices) {
            addLineBetweenPoints(
                vectorList[i],
                Vector3(vectorList[i].x, height / maxLength, vectorList[i].z),
                Constant.pinkHexColorCode
            )
        }
    }

    private fun drawDoor(doorVectorList: List<Vector3>, doorHeight: Float) {

        drawType = "TYPE_DOOR"

        //draw Door
        addLineBetweenPoints(
            Vector3(
                doorVectorList[0].x / maxLength,
                0f,
                doorVectorList[0].z / maxLength
            ),
            Vector3(doorVectorList[1].x / maxLength, 0f, doorVectorList[1].z / maxLength),
            Constant.serenityHexColorCode
        )
        addLineBetweenPoints(
            Vector3(
                doorVectorList[0].x / maxLength,
                doorHeight / maxLength,
                doorVectorList[0].z / maxLength
            ),
            Vector3(
                doorVectorList[1].x / maxLength,
                doorHeight / maxLength,
                doorVectorList[1].z / maxLength
            ),
            Constant.serenityHexColorCode
        )
        addLineBetweenPoints(
            Vector3(
                doorVectorList[0].x / maxLength,
                0f,
                doorVectorList[0].z / maxLength
            ),
            Vector3(
                doorVectorList[0].x / maxLength,
                doorHeight / maxLength,
                doorVectorList[0].z / maxLength
            ),
            Constant.serenityHexColorCode
        )

        addLineBetweenPoints(
            Vector3(
                doorVectorList[1].x / maxLength,
                0f,
                doorVectorList[1].z / maxLength
            ),
            Vector3(
                doorVectorList[1].x / maxLength,
                doorHeight / maxLength,
                doorVectorList[1].z / maxLength
            ),
            Constant.serenityHexColorCode
        )

        //draw Size
        startHorizontalLength(
            Vector3(
                doorVectorList[0].x / maxLength,
                doorHeight / maxLength,
                doorVectorList[0].z / maxLength
            ), doorHeight
        )

        startHorizontalLength(
            Vector3(
                doorVectorList[1].x / maxLength,
                doorHeight / maxLength,
                doorVectorList[1].z / maxLength
            ), doorHeight
        )

        startVerticalLength(
            Vector3(
                doorVectorList[1].x / maxLength,
                doorHeight / maxLength,
                doorVectorList[1].z / maxLength
            ),
            MathUtil.calculationLength(
                listOf(
                    (doorVectorList[1].x - doorVectorList[0].x),
                    (doorVectorList[1].y - doorVectorList[0].y)
                )
            )
        )

        startVerticalLength(
            Vector3(
                doorVectorList[1].x / maxLength,
                0f,
                doorVectorList[1].z / maxLength
            ),
            MathUtil.calculationLength(
                listOf(
                    (doorVectorList[1].x - doorVectorList[0].x),
                    (doorVectorList[1].y - doorVectorList[0].y)
                )
            )
        )
    }

    private fun drawSizeModeling(vectorList: List<Vector3>) {

        for (i in vectorList.indices) {
            if (i == vectorList.size - 1) {
                startHorizontalLength(vectorList[i], height)
                drawLengthLine(vectorList[i], vectorList[0])
                setLengthLine(vectorList[i], vectorList[0])
            } else {
                startHorizontalLength(vectorList[i], height)
                drawLengthLine(vectorList[i], vectorList[i + 1])
                setLengthLine(vectorList[i], vectorList[i + 1])
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
        val colorCode = Color(android.graphics.Color.parseColor(colorCode))

        RenderingUtil.drawCylinderLine(this, colorCode, 0.0025f * cylinderDiameter, lineLength, transformableNode, from, to)
    }

    private fun startHorizontalLength(to: Vector3, height: Float) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)

        // Compute a line's length

        if (drawType == "TYPE_ROOM") {

            percentageHeight = height / maxLength * 0.2f

            // Prepare a color
            val colorCode = Color(android.graphics.Color.parseColor("#888888"))

            //Rendering
            RenderingUtil.extendCylinderLineY(this, colorCode, 0.0015f * cylinderDiameter, percentageHeight, transformableNode, to)

        } else if (drawType == "TYPE_DOOR") {

            percentageDoorHeight = height / maxLength * 0.2f

            // Prepare a color
            val colorCode = Color(android.graphics.Color.parseColor("#888888"))

            //Rendering
            RenderingUtil.extendCylinderLineY(this, colorCode, 0.0015f * cylinderDiameter, percentageDoorHeight, transformableNode, to)

        }
    }

    private fun startVerticalLength(to: Vector3, width: Float) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)



        // todo
        if (drawType == "TYPE_ROOM") {

            // Compute a line's length
            percentageDoorWidth = width / maxLength * 0.2f

            // Prepare a color
            val colorCode = Color(android.graphics.Color.parseColor("#888888"))

            //Rendering
            RenderingUtil.extendCylinderLineX(this, colorCode, 0.0015f * cylinderDiameter, percentageDoorWidth, transformableNode, to)

        } else if (drawType == "TYPE_DOOR") {

            // Compute a line's length
            percentageDoorWidth = width / maxLength * 0.2f

            // Prepare a color
            val colorCode = Color(android.graphics.Color.parseColor("#888888"))

            //Rendering
            RenderingUtil.extendCylinderLineX(this, colorCode, 0.0015f * cylinderDiameter, percentageDoorWidth, transformableNode, to)
        }
    }

    private fun drawLengthLine(from: Vector3, to: Vector3) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)

        var axisFrom: Vector3 = Vector3()
        var axisTo: Vector3 = Vector3()

        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        //re-init axis
        if (drawType == "TYPE_ROOM") {
            axisFrom = Vector3(from.x, from.y + percentageHeight * 0.5f, from.z)
            axisTo = Vector3(to.x, to.y + percentageHeight * 0.5f, to.z)
        } else if (drawType == "TYPE_DOOR") {
            axisFrom = Vector3(from.x + percentageDoorWidth * 0.5f, from.y, from.z)
            axisTo = Vector3(to.x + percentageDoorWidth * 0.5f, to.y, to.z)
        }

        // Prepare a color
        val colorOrange = Color(android.graphics.Color.parseColor("#888888"))

        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(this, colorOrange)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    0.0020f * cylinderDiameter, lineLength,
                    Vector3(0f, 0f, 0f), material
                )
                model.isShadowReceiver = false
                model.isShadowCaster = true

                // 3. make node
                val node = Node()
                node.renderable = model
                node.setParent(transformableNode)
                node.worldPosition = Vector3.add(axisTo, axisFrom).scaled(.5f);


                //4. set rotation
                val difference = Vector3.subtract(axisTo, axisFrom)
                val directionFromTopToBottom = difference.normalized()
                val rotationFromAToB =
                    Quaternion.lookRotation(
                        directionFromTopToBottom,
                        Vector3.up()
                    )
                node.worldRotation = Quaternion.multiply(
                    rotationFromAToB,
                    Quaternion.axisAngle(Vector3(1.0f, 0.0f, 0.0f), 90f)
                )

            }

        setLengthLine(from, to)
    }


    private fun setLengthLine(from: Vector3, to: Vector3) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.

        transformableViewNode = TransformableNode(transformationSystem)
        //transformableViewNode.localPosition = centerPosition
        //DlogUtil.d(TAG, centerPosition)
        transformableViewNode.setParent(parentsTransformableNode)


        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        //re-init axis
        var axisFrom = Vector3(from.x, from.y, from.z)
        var axisTo = Vector3(to.x, to.y, to.z)


        //transformableViewNode.worldPosition = Vector3((axisFrom.x+axisTo.x)/2, (axisFrom.y+axisTo.y)/2, (axisFrom.z+axisTo.z)/2)

        ViewRenderable.builder()
            .setView(this, R.layout.layout_t_length)
            .build()
            .thenAccept {
                val indicatorModel = Node()
                indicatorModel.setParent(transformableViewNode)
                indicatorModel.renderable = it
                indicatorModel.worldPosition = Vector3(
                    (from.x + to.x) / 2,
                    ((from.y + to.y) / 2 + lineLength * 0.05).toFloat(),
                    (from.z + to.z) / 2
                )

                var textView: TextView = it.view.findViewById(R.id.textViewX)
                textView.textSize = 16f * textSize

                var linearLayout: LinearLayout = it.view.findViewById(R.id.linearLayout)
                var layoutParam: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams((200 * textSize).toInt(), (70 * textSize).toInt())
//                layoutParam.width = (50 * textSize).toInt()
                linearLayout.layoutParams = layoutParam

                var list: List<Float> = listOf(
                    to.x * maxLength - from.x * maxLength,
                    to.y * maxLength - from.y * maxLength,
                    to.z * maxLength - from.z * maxLength
                )
                textView.text =
                    (round(MathUtil.calculationLength(list) * 100) / 100).toString() + "m"

                //4. set rotation
                val difference = Vector3.subtract(axisTo, axisFrom)
                val directionFromTopToBottom = difference.normalized()

                val rotationFromAToB =
                    Quaternion.lookRotation(
                        directionFromTopToBottom,
                        Vector3.up()
                    )
                indicatorModel.worldRotation = Quaternion.multiply(
                    rotationFromAToB,
                    Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 90f)
                )
            }

    }


    //1. permission
    private fun permissionCheck() {
        PermissionCheckUtil.checkPermission(this, arrayOf(Manifest.permission.CAMERA))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var permissionSize: Int = permissions.size

        for (i: Int in 0 until permissionSize) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permissions[i]
                ) == PackageManager.PERMISSION_DENIED
            ) {
                DlogUtil.d(TAG, "권한 미승인")
                finish()
            } else {
                DlogUtil.d(TAG, "${permissions[i]} 권한 승인")
            }
        }

    }

    //2. Create Session
    //세션을 만들기 전 AR core이 지원되는지 아닌지 확인하고 세션을 생성한다.
    private fun checkARcore() {
        try {
            when (ArCoreApk.getInstance().requestInstall(this, installRequest)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    DlogUtil.d(TAG, "AR core 설치 필요")
                    installRequest = true
                }
                ArCoreApk.InstallStatus.INSTALLED -> {
                    DlogUtil.d(TAG, "AR core 설치 미필요")
                }
            }
        } catch (e: UnavailableArcoreNotInstalledException) {
            DlogUtil.d(TAG, "ARCore 설치 필요")
        } catch (e: UnavailableUserDeclinedInstallationException) {
            DlogUtil.d(TAG, "ARCore 설치 필요")
        } catch (e: UnavailableApkTooOldException) {
            DlogUtil.d(TAG, "ARCore 업데이트 필요")
        } catch (e: UnavailableSdkTooOldException) {
            DlogUtil.d(TAG, "앱 업데이트 필요")
        } catch (e: UnavailableDeviceNotCompatibleException) {
            DlogUtil.d(TAG, "디바이스가 AR core을 지원하지 않음")
        } catch (e: Exception) {
            DlogUtil.d(TAG, "AR 세션 생성 실패")
            e.printStackTrace()
        }
    }

    private fun motionEventDistance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }
}