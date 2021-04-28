package com.mary.myapplication.util

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

object OKHttpWrapper {

    interface OKHttpWrapperDelegate {
        fun onFailure(call: Call, e: IOException)
        fun onResponse(call: Call, response: Response)
    }

    private val TAG = "OKHttpWrapper"

    private var okHttpClient: OkHttpClient? = null


    private fun OKHttpWrapper() {}

    fun cancelCall(call: Call) {
        if (!call.isCanceled()) {
            call.cancel()
        }
    }


    @Throws(IOException::class)
    fun getStringResponse(response: Response): String? {
        val responseBody = response.body ?: return ""
        return responseBody.string()
    }

    @Throws(IOException::class)
    fun getStringResponseWithCustomHeader(response: Response): String? {
        val responseBody = response.body ?: return ""
        return responseBody.string()
    }

    fun requestGet(url: String?, okHttpWrapperDelegate: OKHttpWrapperDelegate) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val builder = Request.Builder()
        val request: Request? = url?.let { builder.get().url(it).build() }
        val call = request?.let { okHttpClient!!.newCall(it) }

        // auto  thread
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestGetWithHeader(
        url: String?,
        header: HashMap<String?, String?>,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.get().url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestGetWithAuthorizationBearer(
        url: String?,
        okHttpWrapperDelegate: OKHttpWrapperDelegate,
        token: String
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val builder = Request.Builder()
        val request: Request = builder.get().url(url!!).addHeader(
            "Authorization",
            "Bearer $token"
        ).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostWithJSON(
        url: String?,
        okHttpWrapperDelegate: OKHttpWrapperDelegate,
        jsonObject: JSONObject
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val mediaType: MediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
        val requestBody: RequestBody = jsonObject.toString().toRequestBody(mediaType)
        val builder = Request.Builder()
        val request: Request = builder.url(url!!).post(requestBody).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPost(url: String?, okHttpWrapperDelegate: OKHttpWrapperDelegate) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val mediaType: MediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
        val requestBody: RequestBody = "".toRequestBody(mediaType)
        val builder = Request.Builder()
        val request: Request = builder.url(url!!).post(requestBody).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostWithHeaderAndJSONBodyRaw(
        url: String?,
        header: HashMap<String?, String?>,
        jsonObject: JSONObject,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val mediaType: MediaType? = "application/json; charset=utf-8".toMediaType()
        val requestBody: RequestBody = jsonObject.toString().toRequestBody(mediaType)
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.url(url!!).post(requestBody).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPatchWithHeaderAndJSONBodyRaw(
        url: String?,
        header: HashMap<String?, String?>,
        jsonObject: JSONObject,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val mediaType: MediaType? = "application/json; charset=utf-8".toMediaType()
        val requestBody: RequestBody = jsonObject.toString().toRequestBody(mediaType)
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.url(url!!).patch(requestBody).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostWithHeader(
        url: String?,
        header: HashMap<String?, String?>,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val mediaType: MediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
        val requestBody: RequestBody = "".toRequestBody(mediaType)
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestDeleteWithHeader(
        url: String?,
        header: HashMap<String?, String?>,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val mediaType: MediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
        val requestBody: RequestBody = "".toRequestBody(mediaType)
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.delete(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPutWithHeader(
        url: String?,
        header: HashMap<String?, String?>,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val mediaType: MediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
        val requestBody: RequestBody = "".toRequestBody(mediaType)
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.put(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPutModelNameWithHeader(
        url: String?,
        header: HashMap<String?, String?>,
        modelId: String,
        name: String?,
        jsonObj: String?,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val requestBody: RequestBody = FormBody.Builder()
            .add("modelId", modelId)
            .add("name", name!!)
            .add("jsonObj", jsonObj!!)
            .build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.put(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostImageFile(
        url: String?,
        header: HashMap<String?, String?>,
        filePath: String,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val MEDIA_TYPE: MediaType =
            if (filePath.endsWith("png")) "image/png".toMediaType() else "image/jpeg".toMediaType()
        val file = File(filePath)
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("files", file.name, file.asRequestBody(MEDIA_TYPE))
            .build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostFormDataId(
        url: String?,
        affiliateId: Int,
        header: HashMap<String?, String?>,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val requestBody: RequestBody = FormBody.Builder()
            .add("affiliateId", affiliateId.toString())
            .build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostFormDataEmail(
        url: String?,
        header: HashMap<String?, String?>,
        email: String,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val requestBody: RequestBody = FormBody.Builder()
            .add("email", email)
            .build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostWithFormDataFiles(
        url: String?,
        header: HashMap<String?, String?>,
        imageList: List<String>,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val multipartBodyBuilder = MultipartBody.Builder()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        //        multipartBodyBuilder.addFormDataPart("affiliateId", affiliateId);
//        multipartBodyBuilder.addFormDataPart("content", content);
//        multipartBodyBuilder.addFormDataPart("rateTotal", rateTotal);
        var file: File
        for (i in imageList.indices) {
            val mediaType: MediaType =
                if (imageList[i].endsWith("png")) "image/png".toMediaType() else "image/jpeg".toMediaType()
            file = File(imageList[i])
            multipartBodyBuilder.addFormDataPart("files", file.name, file.asRequestBody(mediaType))
        }
        val requestBody: RequestBody = multipartBodyBuilder.build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostWithMultipartFormDataJSONAndFiles(
        url: String?, header: HashMap<String?, String?>,
        hashtag: String?, contents: String?,
        imageList: List<String>, okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val multipartBodyBuilder = MultipartBody.Builder()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        if (hashtag != null) {
            multipartBodyBuilder.addFormDataPart("hashtag", hashtag)
        }
        if (contents != null) {
            multipartBodyBuilder.addFormDataPart("contents", contents)
        }
        var file: File
        for (i in imageList.indices) {
            val mediaType: MediaType =
                if (imageList[i].endsWith("png")) "image/png".toMediaType() else "image/jpeg".toMediaType()
            file = File(imageList[i])
            multipartBodyBuilder.addFormDataPart("files", file.name, file.asRequestBody(mediaType))
        }
        val requestBody: RequestBody = multipartBodyBuilder.build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostWithMultipartFormDataAndCustomNameFiles(
        url: String?,
        header: HashMap<String?, String?>,
        hashtag: String?,
        contents: String?,
        imageList: List<String>,
        imageNameList: List<String?>,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val multipartBodyBuilder = MultipartBody.Builder()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        if (hashtag != null) {
            multipartBodyBuilder.addFormDataPart("hashtag", hashtag)
        }
        if (contents != null) {
            multipartBodyBuilder.addFormDataPart("contents", contents)
        }
        var file: File
        for (i in imageList.indices) {
            val mediaType: MediaType =
                if (imageList[i].endsWith("png")) "image/png".toMediaType() else "image/jpeg".toMediaType()
            file = File(imageList[i])
            multipartBodyBuilder.addFormDataPart(
                "files",
                imageNameList[i], file.asRequestBody(mediaType)
            )
        }
        val requestBody: RequestBody = multipartBodyBuilder.build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPutWithMultipartFormDataAndCustomNameFiles(
        url: String?, header: HashMap<String?, String?>,
        imageFile: String?, nickname: String?, title: String?,
        description: String?, address1: String?, address2: String?,
        extraAdder: String?, area: String?, okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val multipartBodyBuilder = MultipartBody.Builder()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        if (nickname != null) {
            multipartBodyBuilder.addFormDataPart("nickname", nickname)
        }
        if (title != null) {
            multipartBodyBuilder.addFormDataPart("title", title)
        }
        if (description != null) {
            multipartBodyBuilder.addFormDataPart("description", description)
        }
        if (address1 != null) {
            multipartBodyBuilder.addFormDataPart("address1", address1)
        }
        if (address2 != null) {
            multipartBodyBuilder.addFormDataPart("address2", address2)
        }
        if (extraAdder != null) {
            multipartBodyBuilder.addFormDataPart("extraAddr", extraAdder)
        }
        if (area != null) {
            multipartBodyBuilder.addFormDataPart("area", area)
        }
        if (imageFile != null && imageFile != "") {
            val mediaType: MediaType =
                if (imageFile.endsWith("png")) "image/png".toMediaType() else "image/jpeg".toMediaType()
            val file = File(imageFile)
            multipartBodyBuilder.addFormDataPart("files", file.name, file.asRequestBody(mediaType))
        }
        val requestBody: RequestBody = multipartBodyBuilder.build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value !=null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.put(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }


    fun requestPostWithMultipartFormDataAndCustomNameFiles(
        url: String?, header: HashMap<String?, String?>,
        hashtag: String?, contents: String?,
        imageList: List<String>, okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val multipartBodyBuilder = MultipartBody.Builder()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        if (hashtag != null) {
            multipartBodyBuilder.addFormDataPart("hashtag", hashtag)
        }
        if (contents != null) {
            multipartBodyBuilder.addFormDataPart("contents", contents)
        }
        var file: File
        for (i in imageList.indices) {
            val mediaType: MediaType =
                if (imageList[i].endsWith("png")) "image/png".toMediaType() else "image/jpeg".toMediaType()
            file = File(imageList[i])
            multipartBodyBuilder.addFormDataPart("files", file.name, file.asRequestBody(mediaType))
        }
        val requestBody: RequestBody = multipartBodyBuilder.build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostWithMultipartFiles(
        url: String?,
        header: HashMap<String?, String?>,
        imageList: List<String>,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val multipartBodyBuilder = MultipartBody.Builder()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        var file: File
        for (i in imageList.indices) {
            val mediaType: MediaType =
                if (imageList[i].endsWith("png")) "image/png".toMediaType() else "image/jpeg".toMediaType()
            file = File(imageList[i])
            multipartBodyBuilder.addFormDataPart("files", file.name, file.asRequestBody(mediaType))
        }
        val requestBody: RequestBody = multipartBodyBuilder.build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    fun requestPostFormDataModel(
        url: String?, header: HashMap<String, String>,
        name: String?, jsonObj: String?,
        filePath: String?, okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val multipartBodyBuilder = MultipartBody.Builder()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        if (name != null) {
            multipartBodyBuilder.addFormDataPart("name", name)
        }
        if (jsonObj != null) {
            multipartBodyBuilder.addFormDataPart("jsonObj", jsonObj)
        }
        if (filePath != null && filePath != "") {
            val mediaType: MediaType =
                if (filePath.endsWith("png")) "image/png".toMediaType() else "image/jpeg".toMediaType()
            val file = File(filePath)
            multipartBodyBuilder.addFormDataPart("files", file.name, file.asRequestBody(mediaType))
        }
        val requestBody: RequestBody = multipartBodyBuilder.build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }


    fun requestPostWithMultipartFormDataJSONAndFiles(
        url: String?,
        header: HashMap<String?, String?>,
        content: String?,
        rateTotal: String?,
        imageList: List<String>,
        imageWillKeepList: List<String?>,
        okHttpWrapperDelegate: OKHttpWrapperDelegate
    ) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val multipartBodyBuilder = MultipartBody.Builder()
        multipartBodyBuilder.setType(MultipartBody.FORM)
        if (content != null) {
            multipartBodyBuilder.addFormDataPart("content", content)
        }
        if (rateTotal != null) {
            multipartBodyBuilder.addFormDataPart("rateTotal", rateTotal)
        }
        if (imageWillKeepList.isEmpty()) {
            multipartBodyBuilder.addFormDataPart("imageUrl", "")
        } else {
            val stringBuilder = StringBuilder()
            for (i in imageWillKeepList.indices) {
                stringBuilder.append(imageWillKeepList[i])
                if (i < imageWillKeepList.size - 1) {
                    stringBuilder.append(",")
                }
            }
            multipartBodyBuilder.addFormDataPart("imageUrl", stringBuilder.toString())
        }
        var file: File
        for (i in imageList.indices) {
            val mediaType: MediaType =
                if (imageList[i].endsWith("png")) "image/png".toMediaType() else "image/jpeg".toMediaType()
            file = File(imageList[i])
            multipartBodyBuilder.addFormDataPart(
                "imageFile",
                file.name,
                file.asRequestBody(mediaType)
            )
        }
        val requestBody: RequestBody = multipartBodyBuilder.build()
        val builder = Request.Builder()
        for ((key, value) in header) {
            if (key != null && value != null) {
                builder.addHeader(key, value)
            }
        }
        val request: Request = builder.post(requestBody).url(url!!).build()
        val call = okHttpClient!!.newCall(request)

        // auto  thread
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    /**
     * example:
     *
     * OKHttpWrapper.getInstance().requestDownloadFile("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1564642051087&di=a5a0ecddbceeaf44e2b55d0439eaed44&imgtype=0&src=http%3A%2F%2Fen.pimg.jp%2F017%2F747%2F873%2F1%2F17747873.jpg",
     * new OKHttpWrapper.OKHttpWrapperDelegate() {
     * @Override
     * public void onFailure(@NotNull Call call, @NotNull IOException e) {
     * OKHttpWrapper.getInstance().cancelCall(call);
     * }
     *
     * @Override
     * public void onResponse(@NotNull Call call, @NotNull Response response) {
     * try {
     * OKHttpWrapper.getInstance().storageFileResponse(response, Environment.getExternalStorageDirectory().toString() + "/default.png");
     * }
     * catch (IOException e) {
     * e.printStackTrace();
     * }
     * finally {
     * OKHttpWrapper.getInstance().cancelCall(call);
     * }
     * }
     * });
     */
    fun requestDownloadFile(url: String?, okHttpWrapperDelegate: OKHttpWrapperDelegate) {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
            okHttpClient = builder.build()
        }
        val request: Request = Request.Builder().url(url!!).build()
        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                okHttpWrapperDelegate.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                okHttpWrapperDelegate.onResponse(call, response)
            }
        })
    }

    @Throws(IOException::class)
    fun storageFileResponse(response: Response, fileName: String?) {
        val responseBody = response.body ?: return
        val file = File(fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        val buf = ByteArray(2048)
        var len = 0
        var fileOutputStream: FileOutputStream? = null
        var inputStream: InputStream? = null
        try {
            inputStream = responseBody.byteStream()
            fileOutputStream = FileOutputStream(file)
            while (inputStream.read(buf).also { len = it } != -1) {
                fileOutputStream.write(buf, 0, len)
            }
            fileOutputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileOutputStream?.close()
            inputStream?.close()
        }
    }

    fun clear() {
        if (okHttpClient != null) {
            okHttpClient = null
        }
    }

}