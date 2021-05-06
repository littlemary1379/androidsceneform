package com.mary.myapplication.viewholder

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mary.myapplication.R
import com.mary.myapplication.bean.data.RoomBean

class PopupViewHolder(context: Context) {

    private lateinit var imageViewFloor: ImageView
    private lateinit var textViewTitle : TextView
    private lateinit var textViewArea : TextView
    private lateinit var textViewCircumference : TextView
    private lateinit var textViewHeight : TextView
    private lateinit var textViewWallArea : TextView
    private lateinit var textViewWallVolume : TextView


    var view : View = LayoutInflater.from(context).inflate(R.layout.view_holder_share, null)

    init {
        findView()
    }

    private fun findView() {
        imageViewFloor = view.findViewById(R.id.imageViewFloor)
        textViewTitle = view.findViewById(R.id.textViewTitle)
        textViewArea = view.findViewById(R.id.textViewArea)
        textViewCircumference = view.findViewById(R.id.textViewCircumference)
        textViewHeight = view.findViewById(R.id.textViewHeight)
        textViewWallArea = view.findViewById(R.id.textViewWallArea)
        textViewWallVolume = view.findViewById(R.id.textViewWallVolume)
    }

    fun setImage(bitmap: Bitmap) {
        imageViewFloor.setImageBitmap(bitmap)
    }

    fun updateView(roomBean: RoomBean) {
        textViewTitle.text = roomBean.name
        textViewArea.text = roomBean.area.toString()
        textViewCircumference.text = roomBean.circumference.toString()
        textViewHeight.text = roomBean.height.toString()
        textViewWallArea.text = roomBean.wallArea.toString()
        textViewWallVolume.text = roomBean.volume.toString()
    }
}