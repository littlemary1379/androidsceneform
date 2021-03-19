package com.mary.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
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

    private var lastDistance: Float = 0f

    private var downX: Float = 0f
    private var downY: Float = 0f

    private var xAngle: Float = 0f
    private var yAngle: Float = 0f
    private var lastXAngle: Float = 0f
    private var lastYAngle: Float = 0f

    private var isScale: Boolean = false
    private var scale : Float = 1f
    private var percentageHeight: Float = 0f

    private var firstVectorX : Float = 0f

    private lateinit var secondVector: Vector3
    private lateinit var thirdVector: Vector3


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findView()

        permissionCheck()
        checkARcore()

        var firstVector: Vector3 = Vector3(0f, 0f, 0f)
        secondVector = Vector3(0f, 1f, 0f)
        thirdVector= Vector3(1f, 1f, 0f)
        var fourthVector: Vector3 = Vector3(1f, 0f, 0f)

        var fifthVector: Vector3 = Vector3(0f, 0f, -1f)
        var sixthVector: Vector3 = Vector3(0f, 1f, -1f)
        var seventhVector: Vector3 = Vector3(1f, 1f, -1f)
        var eightVector: Vector3 = Vector3(1f, 0f, -1f)

        var cameraX: Float = (firstVector.x + secondVector.x + thirdVector.x + fourthVector.x) / 4
        var cameraY: Float = (firstVector.y + secondVector.y + thirdVector.y + fourthVector.y) / 4

        setCameraPosition(cameraX, cameraY, 1.5f)

        initSceneView()

        //음 일단 비프로그래밍적으로 해보고, 다음 프로그래밍으로 해야지
//        addPoint(firstVector)
//        addPoint(secondVector)
//        addPoint(thirdVector)
//        addPoint(fourthVector)
//
//        addPoint(fifthVector)
//        addPoint(sixthVector)
//        addPoint(seventhVector)
//        addPoint(eightVector)


        //선 긋기
        addLineBetweenPoints(firstVector, secondVector)
        addLineBetweenPoints(secondVector, thirdVector)
        addLineBetweenPoints(thirdVector, fourthVector)
        addLineBetweenPoints(fourthVector, firstVector)

        addLineBetweenPoints(fifthVector, sixthVector)
        addLineBetweenPoints(sixthVector, seventhVector)
        addLineBetweenPoints(seventhVector, eightVector)
        addLineBetweenPoints(eightVector, fifthVector)

        addLineBetweenPoints(firstVector, fifthVector)
        addLineBetweenPoints(secondVector, sixthVector)
        addLineBetweenPoints(seventhVector, thirdVector)
        addLineBetweenPoints(fourthVector, eightVector)

        //위로 올라오는 선 긋기(y축만 차이나는 선끼리 찾아 벡터로 연결), y가 높은게 to로 들어가야 선이 연결됨.
        //전체 model height에서 15% 정도 선이 올라오도록 하는게 좋지 않을까?
        //여기는 계산을 해둔 상태인데, roomBean에 높이가 있으니까 lineLength는 빼도 될거 같아 :D
        startLength(firstVector, secondVector)
        startLength(fourthVector, thirdVector)
        startLength(fifthVector, sixthVector)
        startLength(eightVector, seventhVector)

        //y좌표가 일치하는 선끼리 긋기.

        drawLengthLine(secondVector, thirdVector)
        drawLengthLine(seventhVector, thirdVector)
        drawLengthLine(secondVector, sixthVector)
        drawLengthLine(sixthVector, seventhVector)

        //치수표기
        setLengthLine(secondVector, thirdVector)
        setLengthLine(seventhVector, thirdVector)
        setLengthLine(secondVector, sixthVector)
        setLengthLine(sixthVector, seventhVector)




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

    private fun setCameraPosition(x: Float, y: Float, z: Float) {
        val camera = sceneView.scene.camera
        camera.worldPosition = Vector3(x, y, z)
    }

    private fun initSceneView() {

        sceneView.renderer?.setClearColor(Color(android.graphics.Color.WHITE))
        transformationSystem =
            TransformationSystem(resources.displayMetrics, FootprintSelectionVisualizer())

        parentsTransformableNode = TransformableNode(transformationSystem)
        parentsTransformableNode.setParent(sceneView.scene)

        //parentsTransformableNode.worldPosition = Vector3(0.1f, 0.1f, -0.6f)

        sceneView.scene.addOnPeekTouchListener { hitTestResult, motionEvent ->


            try {


                transformationSystem.selectionVisualizer.removeSelectionVisual(transformableViewNode)

                transformationSystem.onTouch(hitTestResult, motionEvent)

                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    DlogUtil.d(TAG, "이거니?")

                    downX = motionEvent.x
                    downY = motionEvent.y

                } else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                    if (motionEvent.pointerCount == 2) {

                        isScale = true;

                    } else {
                        DlogUtil.d(TAG, "손가락 1개")

                        if (isScale) {
                            return@addOnPeekTouchListener
                        }

                        if (abs(motionEvent.x - downX) > 40 || Math.abs(motionEvent.y - downY) > 40) {

                            DlogUtil.d(TAG, "쿼터니언")

                            //개선 필요
                            var x: Float = motionEvent.x - downX
                            var y: Float = motionEvent.y - downY

                            var percentX: Float = x / sceneView.width * 0.5f
                            var percentY: Float = y / sceneView.height * 0.5f

                            xAngle = percentX * 360 * 0.25f + lastXAngle
                            yAngle = percentY * 360 * 0.25f + lastYAngle

                            var xQuaternion = Quaternion.axisAngle(Vector3(0f, 1f, 0f), xAngle)
                            var yQuaternion = Quaternion.axisAngle(Vector3(-cos(xAngle.toDouble()).toFloat(), 0f, sin(xAngle.toDouble()).toFloat()), yAngle)
                            var y1Quaternion = Quaternion.axisAngle(Vector3(1f, 0f, 0f), yAngle)
                            var y2Quaternion = Quaternion.axisAngle(Vector3(0f, 0f,-1f), yAngle)
                            var y3Quaternion = Quaternion.axisAngle(Vector3(-1f, 0f, 0f), yAngle)
//                            DlogUtil.d(TAG, xAngle)
//                            DlogUtil.d(TAG, "cos : "+cos(xAngle.toDouble()).toFloat())
//                            DlogUtil.d(TAG, "sin : "+sin(xAngle.toDouble()).toFloat())
//                            if(xAngle > -45f) {
//                                DlogUtil.d(TAG, "-45 보다 큼")
//                                parentsTransformableNode.worldRotation =
//                                    Quaternion.multiply(xQuaternion, y1Quaternion)
//                            } else if(xAngle > -135f && xAngle < -45f){
//                                DlogUtil.d(TAG, "-45 이하 -135이상")
//                                parentsTransformableNode.worldRotation =
//                                    Quaternion.multiply(xQuaternion, y2Quaternion)
//                            }else if(xAngle > -225f && xAngle < -135f){
//                                DlogUtil.d(TAG, "-45 이하 -135이상")
//                                parentsTransformableNode.worldRotation =
//                                    Quaternion.multiply(xQuaternion, y3Quaternion)
//                                Quaternion.
//                            }

                            transformableNode.localRotation = Quaternion.multiply(xQuaternion, y1Quaternion)
                            //parentsTransformableNode.worldRotation = Quaternion.multiply(xQuaternion, yQuaternion)



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

                //DlogUtil.d(TAG, "xaxis : ${transformableNode.worldPosition.x}")


            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        sceneView.scene.addOnUpdateListener {

        }

    }


    private fun setTransformableNode() {

        parentsTransformableNode.select()
        parentsTransformableNode.worldPosition = Vector3(0.5f, 0.5f, -0.5f)
        transformableNode.worldPosition = Vector3(0.5f, 0.5f, -0.5f)

        parentsTransformableNode.localScale = Vector3(1f, 1f, 1f)

        var scaleController =
        ScaleController(parentsTransformableNode, transformationSystem.pinchRecognizer)
        scaleController.minScale=1f
        scaleController.maxScale=4f

        var renderScaleController =
            ScaleController(transformableNode, transformationSystem.pinchRecognizer)
        renderScaleController.maxScale = 4f
        renderScaleController.minScale = 1f

        var viewScaleController =
            ScaleController(transformableViewNode, transformationSystem.pinchRecognizer)
        viewScaleController.maxScale = 1.1f
        viewScaleController.minScale = 1f

        parentsTransformableNode.apply {
            scaleController.isEnabled = true
            rotationController.isEnabled = false
            translationController.isEnabled = false
        }

        transformableNode.apply {
            renderScaleController.isEnabled = true
            rotationController.isEnabled = false
            translationController.isEnabled = false
        }

        transformableViewNode.apply {
            viewScaleController.isEnabled = false
            rotationController.isEnabled = false
            translationController.isEnabled = false
        }


    }

    //Modeling Test Code
    private fun addPoint(from: Vector3) {

        DlogUtil.d(TAG, "????????????????????????????????")

        transformableNode = TransformableNode(transformationSystem)

        transformableNode.select()
        transformableNode.setParent(sceneView.scene)
        transformableNode.worldPosition = Vector3(0.1f, 0.1f, -0.6f)

        val color = Color(1f, 0f, 0f)

//        ViewRenderable.builder()
//            .setView(this, R.layout.test_layout)
//            .build()
//            .thenAccept {
//
//                val textViewX: TextView = it.view.findViewById(R.id.textViewX)
//                textViewX.text = "X : ${from.x}"
//                val textViewY: TextView = it.view.findViewById(R.id.textViewY)
//                textViewY.text = "Y : ${from.y}"
//                val textViewZ: TextView = it.view.findViewById(R.id.textViewZ)
//                textViewZ.text = "Z : ${from.z}"
//                val indicatorModel = Node()
//                indicatorModel.setParent(anchorNode)
//                indicatorModel.renderable = it
//                indicatorModel.worldPosition = Vector3(from.x, from.y + 0.02f, from.z)
//            }

        MaterialFactory.makeOpaqueWithColor(this, color)
            .thenAccept { material: Material? ->
                // The sphere is in local coordinate space, so make the center 0,0,0
                val sphere: Renderable = ShapeFactory.makeSphere(
                    0.01f, Vector3.zero(),
                    material
                )


                sphere.isShadowCaster = false
                sphere.isShadowReceiver = false
                val indicatorModel = Node()
                indicatorModel.setParent(transformableNode)
                indicatorModel.renderable = sphere
                indicatorModel.worldPosition = Vector3(from.x, from.y, from.z)



            }
    }


    private fun addLineBetweenPoints(from: Vector3, to: Vector3) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.

        transformableNode = TransformableNode(transformationSystem)

        //transformableNode.select()
        transformableNode.setParent(parentsTransformableNode)
        //transformableNode.worldPosition = Vector3(0.1f, 0.1f, -0.6f)

        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        // Prepare a color
        val colorOrange = Color(android.graphics.Color.parseColor("#FFC3CE"))

        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(this, colorOrange)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    0.0025f, lineLength,
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

        //transformableNode.select()
        transformableNode.setParent(parentsTransformableNode)
        //transformableNode.worldPosition = Vector3(0.1f, 0.1f, -0.6f)

        // Compute a line's length
        percentageHeight = Vector3.subtract(from, to).length() * 0.15f


        // Prepare a color
        val colorOrange = Color(android.graphics.Color.parseColor("#1B1B1B"))

        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(this, colorOrange)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    0.0025f, percentageHeight,
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

        //transformableNode.select()
        transformableNode.setParent(parentsTransformableNode)
        //transformableNode.worldPosition = Vector3(0.5f, 0.5f, -0.5f)

        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        //re-init axis
        var axisFrom = Vector3(from.x, from.y + percentageHeight * 0.5f, from.z)
        var axisTo = Vector3(to.x, to.y + percentageHeight * 0.5f, to.z)

        // Prepare a color
        val colorOrange = Color(android.graphics.Color.parseColor("#1B1B1B"))

        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(this, colorOrange)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    0.0025f, lineLength,
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
    }


    private fun setLengthLine(from: Vector3, to: Vector3) {
        // Node that is automatically positioned in world space based on the ARCore Anchor.

        transformableViewNode = TransformableNode(transformationSystem)
        transformableViewNode.setParent(parentsTransformableNode)

        transformableViewNode.worldPosition = Vector3(0.5f, 0.5f, -0.5f)


        // Compute a line's length
        val lineLength = Vector3.subtract(from, to).length()

        //re-init axis
        var axisFrom = Vector3(from.x, from.y + percentageHeight * 0.5f, from.z)
        var axisTo = Vector3(to.x, to.y + percentageHeight * 0.5f, to.z)


        //transformableViewNode.worldPosition = Vector3((axisFrom.x+axisTo.x)/2, (axisFrom.y+axisTo.y)/2, (axisFrom.z+axisTo.z)/2)

        ViewRenderable.builder()
            .setView(this, R.layout.layout_t_length)
            .build()
            .thenAccept {

                val indicatorModel = Node()
                indicatorModel.setParent(transformableViewNode)
                indicatorModel.renderable = it
                indicatorModel.localPosition = Vector3((axisFrom.x+axisTo.x)/2-0.5f, ((axisFrom.y+axisTo.y)/2-0.5f+ lineLength*0.05).toFloat(), (axisFrom.z+axisTo.z)/2+0.5f)

                var textView : TextView = it.view.findViewById(R.id.textViewX)
                textView.width = 120
                var list : List<Float> = listOf(to.x-from.x, to.y-from.y, to.z-from.z)
                textView.text = MathUtil.calculationLength(list).toString() + "m"
                indicatorModel.localScale = Vector3(2f/scale, 2f/scale, 2f/scale)
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