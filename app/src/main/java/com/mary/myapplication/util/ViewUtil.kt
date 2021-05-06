package com.mary.myapplication.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

object ViewUtil {
    fun inflateView(context: Context?, resource: Int, viewGroup: ViewGroup?): View? {
        return LayoutInflater.from(context).inflate(resource, viewGroup)
    }
}