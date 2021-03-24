package com.mary.myapplication.util

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.TransformableNode

object RenderingUtil {

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
                model.isShadowReceiver = false
                model.isShadowCaster = true

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
                    Vector3.add(to, Vector3(to.x + length, to.y, to.z)).scaled(.5f);

                //4. set rotation
                val difference =
                    Vector3.subtract(to, Vector3(to.x + length, to.y, to.z))
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
        centerPosition : Vector3,
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

                if (direction == Constant.Direction.Horizontal) {

                    val rotationFromAToB =
                        Quaternion.lookRotation(
                            directionFromTopToBottom,
                            Vector3.up()
                        )

                    indicatorModel.worldRotation = Quaternion.multiply(
                        rotationFromAToB,
                        Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 90f)
                    )
                } else if(direction == Constant.Direction.Vertical) {

                    val rotationFromAToB =
                        Quaternion.lookRotation(
                            Vector3(0f, 0f, 0f),
                            Vector3.up()
                        )

                    indicatorModel.worldRotation = Quaternion.multiply(
                        rotationFromAToB,
                        Quaternion.axisAngle(Vector3(
                            0.0f, 0.0f, 1.0f), 270f)
                    )
                }
            }
    }
}