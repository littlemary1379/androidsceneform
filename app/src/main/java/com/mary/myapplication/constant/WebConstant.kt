package com.mary.myapplication.constant

import com.mary.myapplication.util.ParsingUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class WebConstant {

    companion object {

        val DOMAIN = "http://13.124.112.44"

        @Throws(JSONException::class)
        fun getIsSuccess(response: String?): Boolean {
            val jsonObject = JSONObject(response)
            return ParsingUtil.parsingBoolean(jsonObject, "success")
        }

        @Throws(JSONException::class)
        fun getCode(response: String?): Int {
            val jsonObject = JSONObject(response)
            return ParsingUtil.parsingInt(jsonObject, "code")
        }

        @Throws(JSONException::class)
        fun getMessage(response: String?): String? {
            val jsonObject = JSONObject(response)
            return ParsingUtil.parsingString(jsonObject, "msg")
        }

        @Throws(JSONException::class)
        fun getData(response: String?): JSONObject? {
            val jsonObject = JSONObject(response)
            return ParsingUtil.parsingJSONObject(jsonObject, "data")
        }

        @Throws(JSONException::class)
        fun getList(response: String?): JSONArray? {
            val jsonObject = JSONObject(response)
            return ParsingUtil.parsingJSONArray(jsonObject, "list")
        }


        fun getUploadModelUrl(): String? {
            return String.format("%s/v1/model/new", DOMAIN)
        }

        fun getSearchModelUrl(keyWord: String?, offset: String?, limitNo: String?): String? {
            return String.format(
                "%s/v1/search/model?keyWord=%s&offset=%s&limitNo=%s",
                DOMAIN,
                keyWord,
                offset,
                limitNo
            )
        }

        fun getDeleteModelUrl(modelId: String?): String? {
            return String.format("%s/v1/model/delete?modelId=%s", DOMAIN, modelId)
        }

        fun getUpdateModelNameUrl(): String? {
            return String.format("%s/v1/model/modify", DOMAIN)
        }
    }



}