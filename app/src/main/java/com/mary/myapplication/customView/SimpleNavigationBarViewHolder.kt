package com.mary.myapplication.customView

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mary.myapplication.R
import com.mary.myapplication.util.ViewUtil

class SimpleNavigationBarViewHolder(context: Context) {

    interface SimpleNavigationBarViewHolderDelegate {
        fun onLeftClick()
        fun onRightClick()
    }

    private var view: View? = null

    private var imageViewLeft: ImageView? = null
    private var imageViewRight: ImageView? = null
    private var textViewTitle: TextView? = null
    private var viewBottomLine: View? = null

    var simpleNavigationBarViewHolderDelegate: SimpleNavigationBarViewHolderDelegate? = null

    init {
        view = ViewUtil.inflateView(context, R.layout.view_holder_simple_navigation_bar, null)
        findView()
        setListener()
    }

    private fun findView() {
        imageViewLeft = view!!.findViewById(R.id.imageViewLeft)
        imageViewRight = view!!.findViewById(R.id.imageViewRight)
        textViewTitle = view!!.findViewById(R.id.textViewTitle)
        viewBottomLine = view!!.findViewById(R.id.viewBottomLine)
    }

    private fun setListener() {
        imageViewLeft!!.setOnClickListener { view: View? -> simpleNavigationBarViewHolderDelegate!!.onLeftClick() }
        imageViewRight!!.setOnClickListener { view: View? -> simpleNavigationBarViewHolderDelegate!!.onRightClick() }
    }

    fun setImageViewLeft(resourceId: Int) {
        imageViewLeft!!.setImageResource(resourceId)
    }

    fun setImageViewRight(resourceId: Int) {
        imageViewRight!!.setImageResource(resourceId)
    }

    fun setTitle(title: String?) {
        textViewTitle!!.text = title
    }

    fun setTitle(title: Int) {
        textViewTitle!!.setText(title)
    }

    fun getView(): View? {
        return view
    }

    fun hideBottomLine() {
        viewBottomLine!!.visibility = View.GONE
    }

    fun getRightImageView(): View? {
        return imageViewRight
    }
}