package com.mary.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mary.myapplication.util.ActivityUtil
import com.mary.myapplication.util.RenderingUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RenderingUtil.loadMaterial(this)

        var textView : TextView = findViewById(R.id.textViewTest)
        textView.setOnClickListener {

            ActivityUtil.startNewActivityWithoutFinish(this, ListActivity::class.java)
        }

    }

}