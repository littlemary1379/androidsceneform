package com.mary.myapplication.bean.data

import com.google.ar.sceneform.math.Vector3
import com.mary.myapplication.util.ParsingUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class RoomBean {

    private val TAG = "RoomBean"

    // normal vector of floor
    var normalVectorOfPlane: Vector3 = Vector3()

    // floor
    var floorPlaneBean: PlaneBean? = null

    // ceiling
    var ceilingPlaneBean: PlaneBean? = null

    // room wall
    var wallList: MutableList<PlaneBean> = ArrayList<PlaneBean>()

    // object on the wall
    var wallObjectList: MutableList<PlaneBean> = ArrayList<PlaneBean>()

    // room height
    var height = 0f

    // room floor fixed y
    var floorFixedY = 0f

    var area // 면적
            = 0f
    var circumference // 둘레
            = 0f
    var wallArea // 벽면적
            = 0f
    var volume // 체적
            = 0f

    var name: String? = null
    var unit: String? = null

    var centerPoint: PointBean? = null

    var thumbnailImage: String? = null

    init {
        floorPlaneBean = PlaneBean()
        ceilingPlaneBean = PlaneBean()
        wallList.clear()
        wallObjectList.clear()
        height = 0f
        floorFixedY = 0f
        area = 0f
        circumference = 0f
        wallArea = 0f
        volume = 0f
        name = ""
        unit = ""
        thumbnailImage = ""
    }

    @Throws(JSONException::class)
    fun toJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        jsonObject.put("normalVectorOfPlaneX", normalVectorOfPlane!!.x.toString())
        jsonObject.put("normalVectorOfPlaneY", normalVectorOfPlane!!.y.toString())
        jsonObject.put("normalVectorOfPlaneZ", normalVectorOfPlane!!.z.toString())
        jsonObject.put("floor", floorPlaneBean!!.toJSONObject())
        jsonObject.put("ceiling", ceilingPlaneBean!!.toJSONObject())
        val wallArray = JSONArray()
        for (i in wallList.indices) {
            wallArray.put(wallList[i].toJSONObject())
        }
        jsonObject.put("wallArray", wallArray)
        val wallObjectArray = JSONArray()
        for (i in wallObjectList.indices) {
            wallObjectArray.put(wallObjectList[i].toJSONObject())
        }
        jsonObject.put("wallObjectArray", wallObjectArray)
        jsonObject.put("height", height.toString())
        jsonObject.put("floorFixedY", floorFixedY.toString())
        jsonObject.put("area", area.toString())
        jsonObject.put("circumference", circumference.toString())
        jsonObject.put("wallArea", wallArea.toString())
        jsonObject.put("volume", volume.toString())
        jsonObject.put("name", name)
        jsonObject.put("unit", unit)
        jsonObject.put("thumbnailImage", thumbnailImage)
        return jsonObject
    }

    @Throws(JSONException::class)
    fun init(jsonObject: JSONObject?) {
        val normalVectorOfPlaneX: Float? =
            jsonObject?.let { ParsingUtil.parsingString(it, "normalVectorOfPlaneX")?.toFloat() }
        val normalVectorOfPlaneY: Float? =
            jsonObject?.let { ParsingUtil.parsingString(it, "normalVectorOfPlaneY")?.toFloat() }
        val normalVectorOfPlaneZ: Float? =
            jsonObject?.let { ParsingUtil.parsingString(it, "normalVectorOfPlaneZ")?.toFloat() }
        normalVectorOfPlane.set(
            Vector3(
                normalVectorOfPlaneX!!,
                normalVectorOfPlaneY!!,
                normalVectorOfPlaneZ!!
            )
        )
        val floorObject: JSONObject? = ParsingUtil.parsingJSONObject(jsonObject, "floor")
        if (floorObject != null) {
            floorPlaneBean!!.init(floorObject)
        }
        val ceilingObject: JSONObject? = ParsingUtil.parsingJSONObject(jsonObject, "ceiling")
        if (ceilingObject != null) {
            ceilingPlaneBean!!.init(ceilingObject)
        }
        val wallArray: JSONArray? = ParsingUtil.parsingJSONArray(jsonObject, "wallArray")
        var planeBean: PlaneBean
        for (i in 0 until wallArray!!.length()) {
            planeBean = PlaneBean()
            planeBean.init(wallArray.getJSONObject(i))
            wallList.add(planeBean)
        }
        val wallObjectArray: JSONArray? = ParsingUtil.parsingJSONArray(jsonObject, "wallObjectArray")
        for (i in 0 until wallObjectArray!!.length()) {
            planeBean = PlaneBean()
            planeBean.init(wallObjectArray.getJSONObject(i))
            wallObjectList.add(planeBean)
        }
        height = ParsingUtil.parsingString(jsonObject, "height")!!.toFloat()
        floorFixedY = ParsingUtil.parsingString(jsonObject, "floorFixedY")!!.toFloat()
        area = ParsingUtil.parsingString(jsonObject, "area")!!.toFloat()
        circumference = ParsingUtil.parsingString(jsonObject, "circumference")!!.toFloat()
        wallArea = ParsingUtil.parsingString(jsonObject, "wallArea")!!.toFloat()
        volume = ParsingUtil.parsingString(jsonObject, "volume")!!.toFloat()
        name = ParsingUtil.parsingString(jsonObject, "name")
        unit = ParsingUtil.parsingString(jsonObject, "unit")
        thumbnailImage = ParsingUtil.parsingString(jsonObject, "thumbnailImage")
    }

    @Throws(JSONException::class)
    fun init(jsonObject: JSONObject?, cx: Float, cy: Float, cz: Float) {
        val normalVectorOfPlaneX: Float? =
            jsonObject?.let { ParsingUtil.parsingString(it, "normalVectorOfPlaneX")!!.toFloat() }
        val normalVectorOfPlaneY: Float? =
            jsonObject?.let { ParsingUtil.parsingString(it, "normalVectorOfPlaneY")!!.toFloat() }
        val normalVectorOfPlaneZ: Float? =
            jsonObject?.let { ParsingUtil.parsingString(it, "normalVectorOfPlaneZ")!!.toFloat() }
        normalVectorOfPlane!!.set(
            Vector3(
                normalVectorOfPlaneX!!,
                normalVectorOfPlaneY!!,
                normalVectorOfPlaneZ!!
            )
        )
        val floorObject: JSONObject? = ParsingUtil.parsingJSONObject(jsonObject, "floor")
        if (floorObject != null) {
            floorPlaneBean!!.init(floorObject, cx, cy, cz)
        }
        val ceilingObject: JSONObject? = ParsingUtil.parsingJSONObject(jsonObject, "ceiling")
        if (ceilingObject != null) {
            ceilingPlaneBean!!.init(ceilingObject, cx, cy, cz)
        }
        val wallArray: JSONArray? = ParsingUtil.parsingJSONArray(jsonObject, "wallArray")
        var planeBean: PlaneBean
        for (i in 0 until wallArray!!.length()) {
            planeBean = PlaneBean()
            planeBean.init(wallArray.getJSONObject(i), cx, cy, cz)
            wallList.add(planeBean)
        }
        val wallObjectArray: JSONArray? = ParsingUtil.parsingJSONArray(jsonObject, "wallObjectArray")
        for (i in 0 until wallObjectArray!!.length()) {
            planeBean = PlaneBean()
            planeBean.init(wallObjectArray.getJSONObject(i), cx, cy, cz)
            wallObjectList.add(planeBean)
        }
        height = ParsingUtil.parsingString(jsonObject, "height")!!.toFloat()
        floorFixedY = ParsingUtil.parsingString(jsonObject, "floorFixedY")!!.toFloat()
        area = ParsingUtil.parsingString(jsonObject, "area")!!.toFloat()
        circumference = ParsingUtil.parsingString(jsonObject, "circumference")!!.toFloat()
        wallArea = ParsingUtil.parsingString(jsonObject, "wallArea")!!.toFloat()
        volume = ParsingUtil.parsingString(jsonObject, "volume")!!.toFloat()
        name = ParsingUtil.parsingString(jsonObject, "name")
        unit = ParsingUtil.parsingString(jsonObject, "unit")
        thumbnailImage = ParsingUtil.parsingString(jsonObject, "thumbnailImage")
    }

    fun calculate3DModelCenterPoint() {
        var tx = 0f
        var tz = 0f
        for (i in 0 until floorPlaneBean!!.pointList.size) {
            tx += floorPlaneBean!!.pointList[i].x
            tz += floorPlaneBean!!.pointList[i].z
        }
        centerPoint = PointBean()
        centerPoint!!.x = tx / floorPlaneBean!!.pointList.size
        centerPoint!!.y = height * 0.5f
        centerPoint!!.z = tz / floorPlaneBean!!.pointList.size
    }

    fun calculate2DModelCenterPoint() {
        var tx = 0f
        var tz = 0f
        for (i in 0 until floorPlaneBean!!.pointList.size) {
            tx += floorPlaneBean!!.pointList[i].x
            tz += floorPlaneBean!!.pointList[i].z
        }
        centerPoint = PointBean()
        centerPoint!!.x = tx / floorPlaneBean!!.pointList.size
        centerPoint!!.y = 0F
        centerPoint!!.z = tz / floorPlaneBean!!.pointList.size
    }

    /**
     * 입면도
     *
     * this part need re-calculate all point
     * because all plane are in same surface
     */
//    fun createWallModel(): RoomBean? {
//        val wallRoomBean = RoomBean()
//        //wallRoomBean.height = height
//
//        // calculate floor
//        val floorPointList: MutableList<PointBean> = ArrayList<PointBean>()
//        var pointBean = PointBean()
//        pointBean.x = floorPlaneBean!!.pointList[0].x
//        pointBean.y = floorPlaneBean!!.pointList[0].y
//        pointBean.z = 0f
//        floorPointList.add(pointBean)
//        var floorSegmentLength: Float = pointBean.x
//        for (i in 0 until floorPlaneBean!!.segmentBeanList.size) {
//            floorSegmentLength += floorPlaneBean!!.segmentBeanList[i].length
//            pointBean = PointBean()
//            pointBean.x = floorSegmentLength
//            pointBean.y = floorPlaneBean!!.pointList[i].y
//            pointBean.z = 0F
//            floorPointList.add(pointBean)
//        }
//        //wallRoomBean.floorPlaneBean.pointList.addAll(floorPointList)
//
//
//        // calculate ceiling
//        val ceilingPointList: MutableList<PointBean> = ArrayList<PointBean>()
//        pointBean = PointBean()
//        pointBean.x = ceilingPlaneBean.pointList.get(0).x
//        pointBean.y = ceilingPlaneBean.pointList.get(0).y
//        pointBean.z = 0f
//        ceilingPointList.add(pointBean)
//        var ceilingSegmentLength: Float = pointBean.x
//        for (i in 0 until ceilingPlaneBean!!.segmentBeanList.size) {
//            ceilingSegmentLength += ceilingPlaneBean!!.segmentBeanList[i].length
//            pointBean = PointBean()
//            pointBean.x = ceilingSegmentLength
//            pointBean.y = ceilingPlaneBean!!.pointList[i].y
//            pointBean.z = 0f
//            ceilingPointList.add(pointBean)
//        }
//        //wallRoomBean.ceilingPlaneBean.pointList.addAll(ceilingPointList)
//
//
//        // calculate segment length
//        var segmentBean: SegmentBean
//        for (i in 0 until wallRoomBean.floorPlaneBean.pointList.size() - 1) {
//            segmentBean = SegmentBean()
//            segmentBean.startPointBean = wallRoomBean.floorPlaneBean.pointList.get(i)
//            segmentBean.endPointBean = wallRoomBean.floorPlaneBean.pointList.get(i + 1)
//            segmentBean.length = floorPlaneBean!!.segmentBeanList.get(i).length
//            wallRoomBean.floorPlaneBean.segmentBeanList.add(segmentBean)
//        }
//        for (i in 0 until wallRoomBean.ceilingPlaneBean.pointList.size() - 1) {
//            segmentBean = SegmentBean()
//            segmentBean.startPointBean = wallRoomBean.ceilingPlaneBean.pointList.get(i)
//            segmentBean.endPointBean = wallRoomBean.ceilingPlaneBean.pointList.get(i + 1)
//            segmentBean.length = ceilingPlaneBean.segmentBeanList.get(i).length
//            wallRoomBean.ceilingPlaneBean.segmentBeanList.add(segmentBean)
//        }
//
//        // calculate wall object
//        val wallObjectPlaneBeanList: MutableList<PlaneBean> = ArrayList<PlaneBean>()
//        var wallObjectPlaneBean: PlaneBean
//        for (i in wallObjectList.indices) {
//            val wallObject: PlaneBean = wallObjectList[i]
//            val wall: PlaneBean = wallList[wallObject.objectOnIndex]
//            wallObjectPlaneBean = PlaneBean()
//            wallObjectPlaneBean.objectOnIndex = wallObject.objectOnIndex
//            val wallObjectPointList: MutableList<PointBean> = ArrayList<PointBean>()
//            var objectPointBean: PointBean
//            for (j in 0 until wallObject.pointList.size()) {
//
//                // get the index of wall object's object of reference point
//                // we need get index that left bottom point of plane
//                // because this part is draw start from left bottom point
////                int leftBottomPointIndex = getIndexOfLeftBottomPointOnThePlane(wall.pointList);
//                val leftBottomPointIndex = 0
//                val distanceOfTwoPointByXZ: Float = MathTool.getLengthOfTwoNode2D(
//                    wallObject.pointList.get(j).x,
//                    wallObject.pointList.get(j).z,
//                    wall.pointList.get(leftBottomPointIndex).x,
//                    wall.pointList.get(leftBottomPointIndex).z
//                )
//
////                ILog.iLogDebug(TAG, distanceOfTwoPointByXZ);
//                val distanceOfTwoPointByY: Float =
//                    wallObject.pointList.get(j).y - wall.pointList.get(leftBottomPointIndex).y
//                objectPointBean = PointBean()
//                objectPointBean.x = distanceOfTwoPointByXZ
//                objectPointBean.y = distanceOfTwoPointByY
//                objectPointBean.z = 0
//                wallObjectPointList.add(objectPointBean)
//            }
//            wallObjectPlaneBean.pointList.addAll(wallObjectPointList)
//            wallObjectPlaneBeanList.add(wallObjectPlaneBean)
//        }
//        wallRoomBean.wallObjectList.addAll(wallObjectPlaneBeanList)
//        return wallRoomBean
//    }

    fun calculateWallCenterPoint() {
        var length = 0f
        for (i in 0 until floorPlaneBean!!.segmentBeanList.size) {
            length += floorPlaneBean!!.segmentBeanList[i].length
        }
        centerPoint = PointBean()
        centerPoint!!.x = length * 0.5f
        centerPoint!!.y = height * 0.5f
        centerPoint!!.z = 0f
    }

    private fun getIndexOfLeftBottomPointOnThePlane(list: List<PointBean>): Int {
        val tempPointBean = PointBean()
        var x = 0f
        var y = 0f
        var z = 0f
        for (i in list.indices) {
            if (x >= list[i].x) {
                x = list[i].x
            }
            if (y >= list[i].y) {
                y = list[i].y
            }
            if (z >= list[i].z) {
                z = list[i].z
            }
        }
        tempPointBean.x = x
        tempPointBean.y = y
        tempPointBean.z = z
        var index = 0
        for (i in list.indices) {
            if (tempPointBean.x === list[i].x && tempPointBean.y === list[i].y && tempPointBean.z === list[i].z) {
                index = i
                break
            }
        }
        return index
    }

    fun clear() {
        for (i in wallObjectList.indices) {
            wallObjectList[i].clear()
        }
        wallObjectList.clear()
        for (i in wallList.indices) {
            wallList[i].clear()
        }
        wallList.clear()
        if (ceilingPlaneBean != null) {
            ceilingPlaneBean!!.clear()
            ceilingPlaneBean = null
        }
        if (floorPlaneBean != null) {
            floorPlaneBean!!.clear()
            floorPlaneBean = null
        }
        //normalVectorOfPlane = null
        height = 0f
        floorFixedY = 0f
        area = 0f
        circumference = 0f
        wallArea = 0f
        volume = 0f
        name = null
        unit = null
        thumbnailImage = null
    }

}