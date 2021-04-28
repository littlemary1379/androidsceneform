package com.mary.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mary.myapplication.R
import com.mary.myapplication.adapter.viewholder.ModelListViewHolder
import com.mary.myapplication.bean.ModelWrapperItemBean

class ModelListAdapter : RecyclerView.Adapter<ModelListViewHolder>() {

    private val modelWrapperItemBeanList : MutableList<ModelWrapperItemBean> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_modeling_list, parent, false)
        return ModelListViewHolder(view);
    }

    override fun onBindViewHolder(holder: ModelListViewHolder, position: Int) {
        val itemBean = modelWrapperItemBeanList[position]
        holder.apply {
            holder.item = itemBean
            updateView()
        }
    }

    fun reloadItem(list : List<ModelWrapperItemBean>){
        modelWrapperItemBeanList.clear()
        modelWrapperItemBeanList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return modelWrapperItemBeanList.size
    }

}