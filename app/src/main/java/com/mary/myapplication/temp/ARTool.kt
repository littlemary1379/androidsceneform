package com.mary.myapplication.temp

import android.content.Context
import android.widget.TextView
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import java.util.function.Consumer

object ARTool {

    /**
     * create material
     */
    interface MaterialCreatedDelegate {
        fun onCreated(material: Material?)
    }

    fun createColorMaterial(
        context: Context?,
        color: Int,
        materialCreatedDelegate: MaterialCreatedDelegate
    ) {
        MaterialFactory
            .makeOpaqueWithColor(context, Color(color))
            .thenAccept { material: Material? ->
                materialCreatedDelegate.onCreated(
                    material
                )
            }
    }

    /**
     * create view render able
     */
    interface ViewRenderableCreatedDelegate {
        fun onCreated(viewRenderable: ViewRenderable?)
    }

    fun createViewRenderable(
        context: Context?,
        layoutResource: Int,
        viewRenderableCreatedDelegate: ViewRenderableCreatedDelegate,
        shadow: Boolean
    ) {
        ViewRenderable.builder()
            .setView(context, layoutResource)
            .build()
            .thenAccept { viewRenderable: ViewRenderable ->
                viewRenderable.isShadowCaster = shadow
                viewRenderable.isShadowReceiver = shadow
                viewRenderableCreatedDelegate.onCreated(viewRenderable)
            }
    }

    /**
     * update plan cloud point area
     */
    fun updatePlanRenderer(planeRenderer: PlaneRenderer) {
        planeRenderer.material.thenAccept { material: Material ->
            material.setFloat3(PlaneRenderer.MATERIAL_SPOTLIGHT_RADIUS, 1000f, 1000f, 1000f)
            material.setFloat3(
                PlaneRenderer.MATERIAL_COLOR,
                Color(1f, 1f, 1f, 1f)
            )
        }
        planeRenderer.isShadowReceiver = false

//        // Build texture sampler
//        Texture.Sampler sampler = Texture.Sampler.builder()
//                .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
//                .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
//                .setWrapMode(Texture.Sampler.WrapMode.REPEAT).build();
//
//        // Build texture with sampler
//        CompletableFuture<Texture> trigrid = Texture.builder()
//                .setSource(this, R.drawable.grid_blue)
//                .setSampler(sampler).build();
//
//        planeRenderer.getMaterial().thenAcceptBoth(trigrid, (material, texture) -> {
//            material.setTexture(PlaneRenderer.MATERIAL_TEXTURE, texture);
//            material.setFloat(PlaneRenderer.MATERIAL_SPOTLIGHT_RADIUS, 1000f);
//        });
    }

    /**
     * check plan type
     */
    fun checkPlanType(
        hitTestResultList: List<HitResult>,
        none: String?,
        wall: String?,
        ceiling: String?,
        floor: String?
    ): String? {
        for (hitResult in hitTestResultList) {
            val trackable = hitResult.trackable
            if (trackable is Plane && trackable.isPoseInPolygon(hitResult.hitPose)) {
                if (trackable.type == Plane.Type.VERTICAL) {
                    return wall // wall
                } else if (trackable.type == Plane.Type.HORIZONTAL_DOWNWARD_FACING) {
                    return ceiling // ceiling
                } else if (trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                    return floor // floor
                }
            }
        }
        return none
    }

    fun createWorldNode(
        tx: Float,
        ty: Float,
        tz: Float,
        material: Material?,
        shadow: Boolean
    ): Node? {
        val modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material)
        modelRenderable.isShadowReceiver = shadow
        modelRenderable.isShadowCaster = shadow
        val node = Node()
        node.renderable = modelRenderable
        node.worldPosition = Vector3(tx, ty, tz)
        return node
    }

    fun createLocalNode(
        tx: Float,
        ty: Float,
        tz: Float,
        material: Material?,
        shadow: Boolean
    ): Node? {
        val modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material)
        modelRenderable.isShadowReceiver = shadow
        modelRenderable.isShadowCaster = shadow
        val node = Node()
        node.renderable = modelRenderable
        node.localPosition = Vector3(tx, ty, tz)
        return node
    }

    fun createAnchorNode(anchor: Anchor?, material: Material?, shadow: Boolean): AnchorNode? {
        val modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material)
        modelRenderable.isShadowReceiver = shadow
        modelRenderable.isShadowCaster = shadow
        val anchorNode = AnchorNode(anchor)
        anchorNode.renderable = modelRenderable
        return anchorNode
    }

    fun drawSegment(
        startNode: Node,
        endNode: Node,
        lineMaterial: Material?,
        shadow: Boolean
    ): Node? {
        val startVector3 = startNode.worldPosition
        val endVector3 = endNode.worldPosition
        val difference = Vector3.subtract(startVector3, endVector3)
        val directionFromTopToBottom = difference.normalized()
        val rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up())
        val lineModelRenderable = ShapeFactory.makeCube(
            Vector3(0.005f, 0.005f, difference.length()),
            Vector3.zero(),
            lineMaterial
        )
        lineModelRenderable.isShadowCaster = shadow
        lineModelRenderable.isShadowReceiver = shadow
        val lineNode = Node()
        lineNode.setParent(startNode)
        lineNode.renderable = lineModelRenderable
        lineNode.worldPosition = Vector3.add(startVector3, endVector3).scaled(0.5f)
        lineNode.worldRotation = rotationFromAToB
        return lineNode
    }

    fun removeChildFormNode(node: Node) {
        val childList = node.children
        if (!childList.isEmpty()) {
            for (i in childList.indices.reversed()) {
                childList[i].setParent(null)
            }
        }
    }

//    interface SetSegmentSizeTextViewDelegate {
//        fun onFinish(viewRenderable: ViewRenderable?, faceToCameraNode: FaceToCameraNode?)
//    }

//    fun setSegmentSizeTextView(
//        context: Context?,
//        originalLength: Float,
//        arUnit: ARConstants.ARUnit?,
//        parentNode: Node?,
//        setSegmentSizeTextViewDelegate: SetSegmentSizeTextViewDelegate?
//    ) {
//        val length: Float = MathTool.getLengthByUnit(arUnit, originalLength)
//        ViewRenderable.builder()
//            .setView(context, R.layout.view_renderable_text)
//            .build()
//            .thenAccept(Consumer { viewRenderable: ViewRenderable ->
//                val textView = viewRenderable.view as TextView
//                textView.text = String.format("%.2f", length) + " " + MathTool.getLengthUnitString(
//                    arUnit
//                )
//                viewRenderable.isShadowCaster = false
//                viewRenderable.isShadowReceiver = false
//                val faceToCameraNode = FaceToCameraNode()
//                faceToCameraNode.setParent(parentNode)
//                faceToCameraNode.setLocalRotation(
//                    Quaternion.axisAngle(
//                        Vector3(
//                            0f,
//                            1f,
//                            0f
//                        ), 0f
//                    )
//                )
//                faceToCameraNode.setLocalPosition(Vector3(0f, 0.05f, 0f))
//                faceToCameraNode.setRenderable(viewRenderable)
//                setSegmentSizeTextViewDelegate?.onFinish(viewRenderable, faceToCameraNode)
//            })
//    }

//    fun setSegmentSizeTextView(
//        context: Context?,
//        originalLength: Float,
//        arUnit: ARConstants.ARUnit?,
//        parentNode: Node?,
//        textHeight: Float,
//        setSegmentSizeTextViewDelegate: SetSegmentSizeTextViewDelegate?
//    ) {
//        val length: Float = MathTool.getLengthByUnit(arUnit, originalLength)
//        ViewRenderable.builder()
//            .setView(context, R.layout.view_renderable_text)
//            .build()
//            .thenAccept(Consumer { viewRenderable: ViewRenderable ->
//                val textView = viewRenderable.view as TextView
//                textView.text = String.format("%.2f", length) + " " + MathTool.getLengthUnitString(
//                    arUnit
//                )
//                viewRenderable.isShadowCaster = false
//                viewRenderable.isShadowReceiver = false
//                val faceToCameraNode = FaceToCameraNode()
//                faceToCameraNode.setParent(parentNode)
//                faceToCameraNode.setLocalRotation(
//                    Quaternion.axisAngle(
//                        Vector3(
//                            0f,
//                            1f,
//                            0f
//                        ), 0f
//                    )
//                )
//                faceToCameraNode.setLocalPosition(Vector3(0f, textHeight, 0f))
//                faceToCameraNode.setRenderable(viewRenderable)
//                setSegmentSizeTextViewDelegate?.onFinish(viewRenderable, faceToCameraNode)
//            })
//    }

    fun createAnchorNode(anchor: Anchor?): AnchorNode? {
        return AnchorNode(anchor)
    }

}