package com.mary.myapplication.bean.data

import org.json.JSONException
import org.json.JSONObject

class SegmentBean {

    var startPointBean: PointBean? = null
    var endPointBean: PointBean? = null

    var length = 0f

    fun SegmentBean() {
        startPointBean = PointBean()
        endPointBean = PointBean()
        length = 0f
    }

    @Throws(JSONException::class)
    fun toJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        jsonObject.put("startPoint", startPointBean!!.toJSONObject())
        jsonObject.put("endPoint", endPointBean!!.toJSONObject())
        jsonObject.put("length", length.toString())
        return jsonObject
    }

    @Throws(JSONException::class)
    fun init(jsonObject: JSONObject) {
        startPointBean!!.init(jsonObject.getJSONObject("startPoint"))
        endPointBean!!.init(jsonObject.getJSONObject("endPoint"))
        length = jsonObject.getString("length").toFloat()
    }

    @Throws(JSONException::class)
    fun init(jsonObject: JSONObject, cx: Float, cy: Float, cz: Float) {
        startPointBean!!.init(jsonObject.getJSONObject("startPoint"), cx, cy, cz)
        endPointBean!!.init(jsonObject.getJSONObject("endPoint"), cx, cy, cz)
        length = jsonObject.getString("length").toFloat()
    }

    fun clear() {
        if (startPointBean != null) {
            startPointBean!!.clear()
            startPointBean = null
        }
        if (endPointBean != null) {
            endPointBean!!.clear()
            endPointBean = null
        }
    }


}