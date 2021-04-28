package com.mary.myapplication.bean

import com.google.gson.JsonParser
import com.mary.myapplication.bean.data.RoomBean
import com.mary.myapplication.util.DlogUtil
import com.mary.myapplication.util.ParsingUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.net.URLDecoder

class ModelWrapperItemBean {

    private val TAG = "ModelWrapperItemBean"

    var id: Long = 0
    var userId: Long = 0
    var name = ""
    var imgUrl = ""
    var jsonObj = ""
    var isDelete = 0
    var createdAt = ""
    var createdBy: Long = 0
    var updatedAt = ""
    var updatedBy: Long = 0

    var roomBean: RoomBean? = null

    @Throws(JSONException::class)
    fun initWithJSONObject(jsonObject: JSONObject?) {
        id = jsonObject?.let { ParsingUtil.parsingLong(it, "id") }!!
        userId = ParsingUtil.parsingLong(jsonObject, "userId")
        name = ParsingUtil.parsingString(jsonObject, "name").toString()
        imgUrl = ParsingUtil.parsingString(jsonObject, "imgUrl").toString()
        jsonObj = ParsingUtil.parsingString(jsonObject, "jsonObj").toString()
        isDelete = ParsingUtil.parsingInt(jsonObject, "isDelete")
        createdAt = ParsingUtil.parsingString(jsonObject, "createdAt").toString()
        createdBy = ParsingUtil.parsingLong(jsonObject, "createdBy")
        updatedAt = ParsingUtil.parsingString(jsonObject, "updatedAt").toString()
        updatedBy = ParsingUtil.parsingLong(jsonObject, "updatedBy")
        if (jsonObj == "") {
            return
        }
        try {
            jsonObj = URLDecoder.decode(jsonObj, "UTF-8")

            DlogUtil.d(TAG, jsonObj)
            try {
                roomBean = RoomBean()
                roomBean!!.init(JSONObject(jsonObj))
                roomBean!!.thumbnailImage = imgUrl
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }
}