package com.mary.myapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.*
import com.mary.myapplication.util.DlogUtil
import com.mary.myapplication.util.LocationUtil
import com.mary.myapplication.util.MathUtil
import com.mary.myapplication.util.PermissionCheckUtil
import kotlin.math.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var sceneView: SceneView

    private var session: Session? = null
    private var installRequest: Boolean = false

    private lateinit var transformationSystem: TransformationSystem
    private lateinit var transformableNode: TransformableNode
    private lateinit var parentsTransformableNode: TransformableNode
    private lateinit var transformableViewNode: TransformableNode


    private lateinit var anchorNode: AnchorNode

    private var height = 0f
    private var maxLength = 0f

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
    private var scale: Float = 1f
    private var percentageHeight: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findView()

        permissionCheck()
        checkARcore()


        //정육면 입방체임
        //입방체 바닥
        var rawFirstVector: Vector3 = Vector3(0f, 0f, 0f)
        var rawSecondVector = Vector3(30f, 0f, 0f)
        var rawThirdVector = Vector3(30f, 0f, -20f)
        var rawFourthVector: Vector3 = Vector3(0f, 0f, -20f)

//        var fifthVector: Vector3 = Vector3(0f, 0f, -2f)
//        var sixthVector: Vector3 = Vector3(0f, 1f, -2f)
//        var seventhVector: Vector3 = Vector3(4f, 1f, -2f)
//        var eightVector: Vector3 = Vector3(4f, 0f, -2f)

        height = 10f

        maxLength = LocationUtil.longLength(
            listOf(
                rawFirstVector,
                rawSecondVector,
                rawThirdVector,
                rawFourthVector
            ), height
        )

        DlogUtil.d(
            TAG,
            "가장 큰 길이 $maxLength"
        )


        var firstVector = Vector3(rawFirstVector.x / maxLength, 0f, rawFirstVector.z / maxLength)
        var secondVector = Vector3(rawSecondVector.x / maxLength, 0f, rawSecondVector.z / maxLength)
        var thirdVector = Vector3(rawThirdVector.x / maxLength, 0f, rawThirdVector.z / maxLength)
        var fourthVector = Vector3(rawFourthVector.x / maxLength, 0f, rawFourthVector.z / maxLength)

        var fifthVector =
            Vector3(rawFirstVector.x / maxLength, height / maxLength, rawFirstVector.z / maxLength)
        var sixthVector = Vector3(
            rawSecondVector.x / maxLength,
            height / maxLength,
            rawSecondVector.z / maxLength
        )
        var seventhVector =
            Vector3(rawThirdVector.x / maxLength, height / maxLength, rawThirdVector.z / maxLength)
        var eighthVector = Vector3(
            rawFourthVector.x / maxLength,
            height / maxLength,
            rawFourthVector.z / maxLength
        )


        var vectorList: List<Vector3> = listOf(
            firstVector,
            secondVector,
            thirdVector,
            fourthVector,
            fifthVector,
            sixthVector,
            seventhVector,
            eighthVector
        )
        initCenterVector(vectorList)

        setCameraPosition(cameraPosition)

        initSceneView()


        //모델 랜더링
        val floorVectorList: List<Vector3> =
            listOf(firstVector, secondVector, thirdVector, fourthVector)
        drawModeling(floorVectorList)
        drawPillar(floorVectorList)

        val ceilingVectorList: List<Vector3> =
            listOf(fifthVector, sixthVector, seventhVector, eighthVector)
        drawModeling(ceilingVectorList)


        //위로 올라오는 선 긋기(y축만 차이나는 선끼리 찾아 벡터로 연결), y가 높은게 to로 들어가야 선이 연결됨.
        //전체 model height에서 15% 정도 선이 올라오도록 하는게 좋지 않을까?
        //여기는 계산을 해둔 상태인데, roomBean에 높이가 있으니까 lineLength는 빼도 될거 같아 :D

//        startLength(fifthVector, sixthVector)
//        startLength(sixthVector, seventhVector)
//        startLength(seventhVector, eighthVector)
//        startLength(eighthVector, fifthVector)
//
////        //y좌표가 일치하는 선끼리 긋기.
//        drawLengthLine(fifthVector, sixthVector)
//        drawLengthLine(sixthVector, seventhVector)
//        drawLengthLine(seventhVector, eighthVector)
//        drawLengthLine(eighthVector, fifthVector)

        drawSizeModeling(ceilingVectorList)
//
//        //치수표기
//        setLengthLine(fifthVector, sixthVector)
//        setLengthLine(sixthVector, seventhVector)
//        setLengthLine(seventhVector, eighthVector)
//        setLengthLine(eighthVector, fifthVector)

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
        cameraPosition = Vector3(
            cameraX, cameraY,
            (centerPosition.z) + centerPosition.x * 2.5f
        )

        cameraClip = MathUtil.calculationLength(
            listOf(
                centerPosition.x - vectorList[0].x,
                centerPosition.y - vectorList[0].y,
                centerPosition.z - vectorList[0].z
            )
        ) * 6


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

        if (cameraZ <= 0) {
            cameraPosition = Vector3(
                cameraX, cameraY,
                1f
            )
        } else {
            cameraPosition = Vector3(
                cameraX, cameraY,
                cameraZ * 2 + 1f
            )
        }

    }

    private fun setCameraPosition(x: Float, y: Float, z: Float) {
        val camera = sceneView.scene.camera
        camera.worldPosition = Vector3(x, y, z)
    }

    private fun setCameraPosition(vector3: Vector3) {
        val camera = sceneView.scene.camera
        camera.worldPosition = vector3
        //근데 이거 값을 어떻게 적용하지?;;
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

                            xAngle = percentX * 360 * 0.4f + lastXAngle
                            yAngle = percentY * 360 * 0.4f + lastYAngle

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

        sceneView.scene.addOnUpdateListener {

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

        for (i in vectorList.indices) {
            if (i == vectorList.size - 1) {
                addLineBetweenPoints(vectorList[i], vectorList[0])
            } else {
                addLineBetweenPoints(vectorList[i], vectorList[i + 1])
            }
        }
    }

    private fun drawPillar(vectorList: List<Vector3>) {
        for (i in vectorList.indices) {
            addLineBetweenPoints(
                vectorList[i],
                Vector3(vectorList[i].x, height / maxLength, vectorList[i].z)
            )
        }
    }

    private fun drawSizeModeling(vectorList: List<Vector3>) {

        for (i in vectorList.indices) {
            if (i == vectorList.size - 1) {
                startLength(vectorList[i], vectorList[0])
                drawLengthLine(vectorList[i], vectorList[0])
                setLengthLine(vectorList[i], vectorList[0])
            } else {
                startLength(vectorList[i], vectorList[i + 1])
                drawLengthLine(vectorList[i], vectorList[i + 1])
                setLengthLine(vectorList[i], vectorList[i + 1])
            }
        }
    }

    private fun addLineBetweenPoints(from: Vector3, to: Vector3) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)


        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        // Prepare a color
        val colorOrange = Color(android.graphics.Color.parseColor("#FFC3CE"))

        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(this, colorOrange)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    0.0025f * cylinderDiameter, lineLength,
                    Vector3(0f, 0f, 0f), material
                )
                model.isShadowReceiver = false
                model.isShadowCaster = true

                // 3. make node
                val node = Node()
                node.renderable = model

                node.setParent(transformableNode)
                node.worldPosition = Vector3.add(to, from).scaled(.5f);


                //4. set rotation
                val difference = Vector3.subtract(to, from)
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
    }

    private fun startLength(from: Vector3, to: Vector3) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)

        // Compute a line's length
        percentageHeight = height / maxLength * 0.2f

        // Prepare a color
        val colorOrange = Color(android.graphics.Color.parseColor("#1B1B1B"))

        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(this, colorOrange)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    0.0020f * cylinderDiameter, percentageHeight,
                    Vector3(0f, 0f, 0f), material
                )
                model.isShadowReceiver = false
                model.isShadowCaster = true

                // 3. make node
                val node = Node()
                node.renderable = model
                node.setParent(transformableNode)
                node.worldPosition =
                    Vector3.add(to, Vector3(to.x, to.y + percentageHeight, to.z)).scaled(.5f);


            }
    }

    private fun drawLengthLine(from: Vector3, to: Vector3) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.
        transformableNode = TransformableNode(transformationSystem)
        transformableNode.setParent(parentsTransformableNode)

        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        //re-init axis
        var axisFrom = Vector3(from.x, from.y + percentageHeight * 0.5f, from.z)
        var axisTo = Vector3(to.x, to.y + percentageHeight * 0.5f, to.z)
        DlogUtil.d(TAG, "axixFrom.x : ${axisFrom.y} // axixFrom.x : ${axisTo.y}")
        DlogUtil.d(TAG, "percentageHeight : $percentageHeight")

        // Prepare a color
        val colorOrange = Color(android.graphics.Color.parseColor("#1B1B1B"))

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
                    LinearLayout.LayoutParams((150 * textSize).toInt(), (70 * textSize).toInt())
//                layoutParam.width = (50 * textSize).toInt()
                linearLayout.layoutParams = layoutParam

                var list: List<Float> = listOf(
                    to.x * maxLength - from.x * maxLength,
                    to.y * maxLength - from.y * maxLength,
                    to.z * maxLength - from.z * maxLength
                )
                textView.text = MathUtil.calculationLength(list).toString() + "m"

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
            if (session == null) {
                when (ArCoreApk.getInstance().requestInstall(this, installRequest)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        DlogUtil.d(TAG, "AR core 설치 필요")
                        installRequest = true
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        DlogUtil.d(TAG, "AR core 설치 미필요")
                        createSession()
                    }
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

    private fun createSession() {
        DlogUtil.d(TAG, "세션 생성")

        if (session == null) {
            session = Session(this)
        }

        //necessary : config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        val config = Config(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL

        session?.configure(config)

    }

    private fun motionEventDistance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }
}