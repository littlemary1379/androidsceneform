package com.mary.myapplication.bean.data

import com.mary.myapplication.util.ParsingUtil
import org.json.JSONException
import org.json.JSONObject

class PointBean {

    var x = 0f
    var y = 0f
    var z = 0f

    @Throws(JSONException::class)
    fun toJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        jsonObject.put("x", x.toString())
        jsonObject.put("y", y.toString())
        jsonObject.put("z", z.toString())
        return jsonObject
    }

    @Throws(JSONException::class)
    fun init(jsonObject: JSONObject?) {
        val x: String = ParsingUtil.parsingString(jsonObject!!, "x")!!
        val y: String = ParsingUtil.parsingString(jsonObject!!, "y")!!
        val z: String = ParsingUtil.parsingString(jsonObject!!, "z")!!
        this.x = x.toFloat()
        this.y = y.toFloat()
        this.z = z.toFloat()
    }

    @Throws(JSONException::class)
    fun init(jsonObject: JSONObject?, cx: Float, cy: Float, cz: Float) {
        val x: String = ParsingUtil.parsingString(jsonObject!!, "x")!!
        val y: String = ParsingUtil.parsingString(jsonObject!!, "y")!!
        val z: String = ParsingUtil.parsingString(jsonObject!!, "z")!!
        this.x = x.toFloat() - cx
        this.y = y.toFloat() - cy
        this.z = z.toFloat() - cz
    }

    fun clear() {
        x = 0f
        y = 0f
        z = 0f
    }

}