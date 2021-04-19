package com.mary.myapplication.temp

import android.content.Context
import android.graphics.Color
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory

object ARMaterial {

    private fun ARMaterial() {}

    // material
    var pointMaterial: Material? = null
    var segmentMaterial: Material? = null

    var objectPointMaterial: Material? = null
    var objectSegmentMaterial: Material? = null

    var guideNodeMaterial: Material? = null
    var guideSegmentMaterial: Material? = null

    var wallPointMaterial: Material? = null
    var wallSegmentMaterial: Material? = null

    // node shadow
    var nodeShadow = true

    fun init(context: Context?) {

        // create node material
        ARTool.createColorMaterial(context, Color.GREEN, object : ARTool.MaterialCreatedDelegate{
            override fun onCreated(material: Material?) {
                    pointMaterial = material
                    segmentMaterial = material
                    guideNodeMaterial = material
                    guideSegmentMaterial = material
                }
        })

        // create object node material
        ARTool.createColorMaterial(context, Color.BLUE, object : ARTool.MaterialCreatedDelegate {
            override fun onCreated(material: Material?) {
                objectPointMaterial = material
                objectSegmentMaterial = material
            }

        })

        MaterialFactory
            .makeOpaqueWithColor(context, com.google.ar.sceneform.rendering.Color(Color.RED))
            .thenAccept { material: Material? ->
                wallPointMaterial = material
                wallSegmentMaterial = material
            }
    }

    fun destroy() {
        pointMaterial = null
        segmentMaterial = null
        objectPointMaterial = null
        objectSegmentMaterial = null
        guideNodeMaterial = null
        guideSegmentMaterial = null
    }
}