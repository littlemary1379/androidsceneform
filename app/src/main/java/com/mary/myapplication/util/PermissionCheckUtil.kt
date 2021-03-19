package com.mary.myapplication.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionCheckUtil {
    private const val TAG = "PermissionCheckUtil"

    var CAMERA_REQUEST_CODE = 10

    fun checkPermission(activity: Activity, permission: Array<String>) {

        var requestList: MutableList<String> = mutableListOf()

        for (i: Int in permission.indices) {
            var permissionCheck = ContextCompat.checkSelfPermission(activity, permission[i])

            if (permissionCheck == PackageManager.PERMISSION_DENIED) {

                DlogUtil.d(TAG, "권한 승인 필요")
                requestList.add(permission[i])

            } else {
                DlogUtil.d(TAG, "${permission[i]} 이미 승인되어 있음.")
            }

        }

        if (!requestList.isNullOrEmpty()) {
            ActivityCompat.requestPermissions(activity, requestList.toTypedArray(), CAMERA_REQUEST_CODE)
        }
    }
}