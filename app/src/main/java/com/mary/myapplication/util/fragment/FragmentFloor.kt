package com.mary.myapplication.util.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mary.myapplication.R
import com.mary.myapplication.util.DlogUtil

class FragmentFloor : Fragment(){

    companion object {
        private const val TAG = "FragmentFloor"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DlogUtil.d(TAG, " ?????????????????????????????????? ")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DlogUtil.d(TAG, " ?????????????????????????????????? ")
        return inflater.inflate(R.layout.fragment_floor, null)
    }


}