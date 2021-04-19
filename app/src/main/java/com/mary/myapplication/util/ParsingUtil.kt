package com.mary.myapplication.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object ParsingUtil {
    interface JSONObjectIterator {
        @Throws(JSONException::class)
        fun onIterator(key: Any?, value: Any?)
    }

    interface JSONObjectIteratorWithIndex {
        fun onIterator(index: Int, key: Any?, value: Any?)
    }

    @Throws(JSONException::class)
    fun jsonObjectIterator(jsonObject: JSONObject, jsonObjectIterator: JSONObjectIterator) {
        val iterator: Iterator<*> = jsonObject.keys()
        while (iterator.hasNext()) {
            val key = iterator.next() as String
            val value = jsonObject[key]
            jsonObjectIterator.onIterator(key, value)
        }
    }

    @Throws(JSONException::class)
    fun jsonObjectIteratorWithIndex(
        jsonObject: JSONObject,
        jsonObjectIteratorWithIndex: JSONObjectIteratorWithIndex
    ) {
        var index = 0
        val iterator: Iterator<*> = jsonObject.keys()
        while (iterator.hasNext()) {
            val key = iterator.next() as String
            val value = jsonObject[key]
            jsonObjectIteratorWithIndex.onIterator(index, key, value)
            index++
        }
    }

    @Throws(JSONException::class)
    fun parsingStringList(jsonObject: JSONObject, key: String?): List<String?>? {
        if (!jsonObject.has(key)) {
            return ArrayList()
        }
        return if (jsonObject.isNull(key)) {
            ArrayList()
        } else {
            val list: MutableList<String?> = ArrayList()
            val jsonArray = jsonObject.getJSONArray(key)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        }
    }

    @Throws(JSONException::class)
    fun parsingString(jsonObject: JSONObject, key: String?): String? {
        if (!jsonObject.has(key)) {
            return ""
        }
        return if (jsonObject.isNull(key)) {
            ""
        } else {
            val result = jsonObject.getString(key)
            result ?: ""
        }
    }

    @Throws(JSONException::class)
    fun parsingBoolean(jsonObject: JSONObject, key: String?): Boolean {
        if (!jsonObject.has(key)) {
            return false
        }
        return if (jsonObject.isNull(key)) {
            false
        } else {
            jsonObject.getBoolean(key)
        }
    }

    @Throws(JSONException::class)
    fun parsingInt(jsonObject: JSONObject, key: String?): Int {
        if (!jsonObject.has(key)) {
            return 0
        }
        return if (jsonObject.isNull(key)) {
            0
        } else {
            jsonObject.getInt(key)
        }
    }

    @Throws(JSONException::class)
    fun parsingDouble(jsonObject: JSONObject, key: String?): Double {
        if (!jsonObject.has(key)) {
            return 0.0
        }
        return if (jsonObject.isNull(key)) {
            0.0
        } else {
            jsonObject.getDouble(key)
        }
    }

    @Throws(JSONException::class)
    fun parsingLong(jsonObject: JSONObject, key: String?): Long {
        if (!jsonObject.has(key)) {
            return 0
        }
        return if (jsonObject.isNull(key)) {
            0
        } else {
            jsonObject.getLong(key)
        }
    }

    @Throws(JSONException::class)
    fun parsingJSONObject(jsonObject: JSONObject, key: String?): JSONObject? {
        if (!jsonObject.has(key)) {
            return JSONObject()
        }
        return if (jsonObject.isNull(key)) {
            JSONObject()
        } else {
            jsonObject.getJSONObject(key)
        }
    }

    @Throws(JSONException::class)
    fun parsingJSONArray(jsonObject: JSONObject, key: String?): JSONArray? {
        if (!jsonObject.has(key)) {
            return JSONArray()
        }
        return if (jsonObject.isNull(key)) {
            JSONArray()
        } else {
            jsonObject.getJSONArray(key)
        }
    }

}