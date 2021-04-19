package com.mary.myapplication.bean.data

import com.mary.myapplication.util.ParsingUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class PlaneBean {
    var pointList: MutableList<PointBean> = ArrayList<PointBean>()
    var segmentBeanList: MutableList<SegmentBean> = ArrayList<SegmentBean>()

    var type: String? = null
    var objectOnIndex = 0

    fun PlaneBean() {
        pointList.clear()
        segmentBeanList.clear()
        type = ""
        objectOnIndex = -1
    }

    fun clear() {
        for (i in 0 until pointList.size - 1) {
            pointList[i].clear()
        }
        pointList.clear()
        for (i in 0 until segmentBeanList.size - 1) {
            segmentBeanList[i].clear()
        }
        segmentBeanList.clear()
        type = ""
        objectOnIndex = -1
    }

    @Throws(JSONException::class)
    fun toJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        val pointArray = JSONArray()
        for (i in pointList.indices) {
            pointArray.put(pointList[i].toJSONObject())
        }
        jsonObject.put("pointArray", pointArray)
        val segmentArray = JSONArray()
        for (i in segmentBeanList.indices) {
            segmentArray.put(segmentBeanList[i].toJSONObject())
        }
        jsonObject.put("segmentArray", segmentArray)
        jsonObject.put("type", type)
        jsonObject.put("objectOnIndex", objectOnIndex)
        return jsonObject
    }

    @Throws(JSONException::class)
    fun init(jsonObject: JSONObject) {
        val pointArray: JSONArray = ParsingUtil.parsingJSONArray(jsonObject, "pointArray")!!
        val segmentArray: JSONArray = ParsingUtil.parsingJSONArray(jsonObject, "segmentArray")!!
        var pointBean: PointBean
        for (i in 0 until pointArray.length()) {
            pointBean = PointBean()
            pointBean.init(pointArray.getJSONObject(i))
            pointList.add(pointBean)
        }
        var segmentBean: SegmentBean
        for (i in 0 until segmentArray.length()) {
            segmentBean = SegmentBean()
            segmentBean.init(segmentArray.getJSONObject(i))
            segmentBeanList.add(segmentBean)
        }
        type = jsonObject.getString("type")
        objectOnIndex = jsonObject.getInt("objectOnIndex")
    }

    @Throws(JSONException::class)
    fun init(jsonObject: JSONObject, cx: Float, cy: Float, cz: Float) {
        val pointArray: JSONArray = ParsingUtil.parsingJSONArray(jsonObject, "pointArray")!!
        val segmentArray: JSONArray = ParsingUtil.parsingJSONArray(jsonObject, "segmentArray")!!
        var pointBean: PointBean
        for (i in 0 until pointArray.length()) {
            pointBean = PointBean()
            pointBean.init(pointArray.getJSONObject(i), cx, cy, cz)
            pointList.add(pointBean)
        }
        var segmentBean: SegmentBean
        for (i in 0 until segmentArray.length()) {
            segmentBean = SegmentBean()
            segmentBean.init(segmentArray.getJSONObject(i), cx, cy, cz)
            segmentBeanList.add(segmentBean)
        }
        type = jsonObject.getString("type")
        objectOnIndex = jsonObject.getInt("objectOnIndex")
    }

}