package com.mary.myapplication.adapter.viewholder

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mary.myapplication.ListActivity
import com.mary.myapplication.R
import com.mary.myapplication.bean.ModelWrapperItemBean
import com.mary.myapplication.util.ActivityUtil
import com.mary.myapplication.util.DlogUtil

class ModelListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        private const val TAG = "ModelListViewHolder"
    }

    lateinit var item : ModelWrapperItemBean

    private lateinit var textViewName : TextView


    init {
        findView()
        setListener()
    }

    private fun findView(){
        textViewName = itemView.findViewById(R.id.textViewName)
    }

    fun updateView(){
        textViewName.text = item.name
    }

    private fun setListener(){
        itemView.setOnClickListener {
            DlogUtil.d(TAG, "클릭클릭")

            var bundle = Bundle()
            bundle.putString("roomBean", item.roomBean?.toJSONObject().toString())
            bundle.putBoolean("isNew", false)
            ActivityUtil.startNewActivityWithoutFinish(itemView.context, ListActivity::class.java, bundle)
        }
    }

}