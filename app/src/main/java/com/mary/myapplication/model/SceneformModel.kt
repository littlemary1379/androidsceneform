package com.mary.myapplication.model

import com.mary.myapplication.constant.WebConstant
import com.mary.myapplication.util.OKHttpWrapper
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import java.util.*

object SceneformModel {

    private val TAG = "SceneFormModel"

    interface SceneFormModelDelegate {
        fun onResponse(response: String?)
        fun onException(e: Exception?)
    }


    fun requestUploadModel(
        token: String,
        keyword: String?,
        offset: String?,
        limitNo: String?,
        sceneFormModelDelegate: SceneFormModelDelegate
    ) {
        val url: String = WebConstant.getSearchModelUrl(keyword, offset, limitNo)!!
        val hashMap = HashMap<String?, String?>()
        hashMap["X-AUTH-TOKEN"] = token
        OKHttpWrapper.requestGetWithHeader(
            url,
            hashMap,
            object : OKHttpWrapper.OKHttpWrapperDelegate {
                override fun onFailure(call: Call, e: IOException) {
                    sceneFormModelDelegate.onException(e)
                    OKHttpWrapper.cancelCall(call)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseString: String? =
                            OKHttpWrapper.getStringResponse(response)
                        sceneFormModelDelegate.onResponse(responseString)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        OKHttpWrapper.cancelCall(call)
                    }
                }
            })
    }
}