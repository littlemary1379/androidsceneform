package com.mary.myapplication.util

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.mary.myapplication.constant.Constant
import com.mary.myapplication.util.event.ESSArrow
import com.mary.myapplication.util.event.EventCenter
import java.util.function.Consumer


object RenderingUtil {

    private const val TAG = "RenderingUtil"

    var repeatingMaterial: Material? = null
    private lateinit var texture: Texture

    fun loadMaterial(context: Context) {
        DlogUtil.d(TAG, "랜더링이 비엇나? ???????????????????????")

        val sampler = Texture.Sampler.builder()
            .setMinFilter(Texture.Sampler.MinFilter.NEAREST_MIPMAP_NEAREST)
            .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
            .setWrapModeR(Texture.Sampler.WrapMode.REPEAT)
            .setWrapModeS(Texture.Sampler.WrapMode.REPEAT)
            .setWrapModeT(Texture.Sampler.WrapMode.REPEAT)
            .build()

        Texture.builder().setSource { context.assets.open("textures/line_texture.png") }
            .setSampler(sampler)
            .build()
            .thenAccept {
                texture = it
            }

        ModelRenderable.builder()
            .setSource(context, com.mary.myapplication.R.raw.material_holder)
            .build()
            .thenAccept(Consumer { modelRenderable: ModelRenderable ->
                repeatingMaterial = modelRenderable.material
                DlogUtil.d(TAG, "해치웠나? ㅇㅇ")
                EventCenter.instance.sendEvent(ESSArrow.LOAD_MODELIING_FINISH, this, null)
            }).exceptionally {
                DlogUtil.d(TAG, "해치웠나? ㄴㄴ")

                return@exceptionally null
            }

    }


    fun drawCylinderLine(
        context: Context,
        lineColor: Color,
        radius: Float,
        length: Float,
        parentNode: TransformableNode,
        from: Vector3,
        to: Vector3
    ) {
        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(context, lineColor)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    radius, length,
                    Vector3(0f, 0f, 0f), material
                )


                val light = Light.builder(Light.Type.FOCUSED_SPOTLIGHT)
                    .setShadowCastingEnabled(false)
                    .setIntensity(0f)
                    .build()

                // 3. make node
                val node = Node()
                node.renderable = model

                node.setParent(parentNode)
                node.light = light
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

    fun drawDashCylinderLine(
        context: Context,
        lineColor: Color,
        radius: Float,
        length: Float,
        parentNode: TransformableNode,
        from: Vector3,
        to: Vector3
    ) {

        val lengthCM: Float = length * 100

        DlogUtil.d(TAG, "오잉또잉? ${texture} : $lengthCM")

        val colorCode = com.google.ar.sceneform.rendering.Color(android.graphics.Color.parseColor("#DDFFFFFF"))

        repeatingMaterial?.setFloat("repeat_x", lengthCM / 10)
        repeatingMaterial?.setFloat("repeat_y", lengthCM / 10)
        repeatingMaterial?.setTexture("texture", texture)
        repeatingMaterial?.setFloat ( "alphaFactor", .5f);

        MaterialFactory.makeTransparentWithColor(context, colorCode)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    radius, length,
                    Vector3(0f, 0f, 0f), repeatingMaterial
                )

                // 3. make node
                val node = Node()
                node.renderable = model

                node.setParent(parentNode)
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

    fun drawTransparentCylinderLine(
        context: Context,
        lineColor: Color,
        radius: Float,
        length: Float,
        parentNode: TransformableNode,
        from: Vector3,
        to: Vector3
    ) {
        // 1. make a material by the color
        MaterialFactory.makeTransparentWithColor(context, lineColor)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    radius, length,
                    Vector3(0f, 0f, 0f), material
                )


                val light = Light.builder(Light.Type.FOCUSED_SPOTLIGHT)
                    .setShadowCastingEnabled(false)
                    .setIntensity(0f)
                    .build()

                // 3. make node
                val node = Node()
                node.renderable = model

                node.setParent(parentNode)
                node.light = light
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

    fun extendCylinderLineY(
        context: Context,
        lineColor: Color,
        radius: Float,
        length: Float,
        parentNode: TransformableNode,
        to: Vector3
    ) {
        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(context, lineColor)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    radius, length,
                    Vector3(0f, 0f, 0f), material
                )
                model.isShadowReceiver = false
                model.isShadowCaster = true

                // 3. make node
                val node = Node()
                node.renderable = model
                node.setParent(parentNode)
                node.worldPosition =
                    Vector3.add(to, Vector3(to.x, to.y + length, to.z)).scaled(.5f);

            }
    }

    fun extendCylinderLineX(
        context: Context,
        lineColor: Color,
        radius: Float,
        length: Float,
        parentNode: TransformableNode,
        to: Vector3,
        from: Vector3
    ) {
        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(context, lineColor)
            .thenAccept { material: Material? ->
                // 2. make a model by the material
                val model = ShapeFactory.makeCylinder(
                    radius, length,
                    Vector3(0f, 0f, 0f), material
                )
                model.isShadowReceiver = false
                model.isShadowCaster = true

                // 3. make node
                val node = Node()
                node.renderable = model
                node.setParent(parentNode)
                node.worldPosition =
                    Vector3.add(to, MathUtil.addVector(from, to, 20)).scaled(.5f);
                DlogUtil.d(TAG, "extendCylinderLineX : ${MathUtil.addVector(from, to, 20).x}")

                //4. set rotation
                val difference =
                    Vector3.subtract(to, from)
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

    fun drawTextView(
        context: Context,
        centerPosition: Vector3,
        height: Float,
        text: String,
        parentNode: TransformableNode,
        from: Vector3,
        to: Vector3,
        direction: Constant.Direction
    ) {
        ViewRenderable.builder()
            .setView(context, com.mary.myapplication.R.layout.layout_t_length)
            .build()
            .thenAccept {
                val indicatorModel = Node()
                indicatorModel.setParent(parentNode)
                indicatorModel.renderable = it
                indicatorModel.localPosition = Vector3(
                    (from.x + to.x) / 2 - centerPosition.x,
                    ((from.y + to.y) / 2 + height * 0.3).toFloat() - centerPosition.y,
                    (from.z + to.z) / 2 - centerPosition.z
                )

                var textView: TextView = it.view.findViewById(com.mary.myapplication.R.id.textViewX)

                var linearLayout: LinearLayout =
                    it.view.findViewById(com.mary.myapplication.R.id.linearLayout)
                var layoutParam: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(250, 70)

                linearLayout.layoutParams = layoutParam
                textView.text = text

                //4. set rotation
                val difference = Vector3.subtract(to, from)
                val directionFromTopToBottom = difference.normalized()

                when (direction) {

                    Constant.Direction.Horizontal -> {

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

                    Constant.Direction.Vertical -> {

                        val rotationFromAToB =
                            Quaternion.lookRotation(
                                Vector3(0f, 0f, 0f),
                                Vector3.up()
                            )

                        indicatorModel.worldRotation = Quaternion.multiply(
                            rotationFromAToB,
                            Quaternion.axisAngle(Vector3(0.0f, 0.0f, 1.0f), 270f)
                        )

                    }

                    Constant.Direction.FLOOR -> {

                        when {
                            to.z > from.z -> {

                                var rotationFromAToB = Quaternion.lookRotation(
                                    directionFromTopToBottom,
                                    Vector3.left()
                                )

                                indicatorModel.worldRotation = Quaternion.multiply(
                                    rotationFromAToB,
                                    Quaternion.axisAngle(Vector3(0f, 1f, 0f), 90f)
                                )
                            }
                            to.z < from.z -> {
                                var rotationFromAToB = Quaternion.lookRotation(
                                    directionFromTopToBottom,
                                    Vector3.right()
                                )

                                indicatorModel.worldRotation = Quaternion.multiply(
                                    rotationFromAToB,
                                    Quaternion.axisAngle(Vector3(0f, 1f, 0f), 90f)
                                )

                            }
                            else -> {
                                var rotationFromAToB = Quaternion.lookRotation(
                                    Vector3(0f, 0f, 0f),
                                    Vector3.zero()
                                )

                                indicatorModel.worldRotation = Quaternion.multiply(
                                    rotationFromAToB,
                                    Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f)
                                )
                            }

                        }


                    }
                }
            }
    }
}