package com.mary.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.mary.myapplication.util.ActivityUtil
import com.mary.myapplication.util.DlogUtil
import com.mary.myapplication.util.RenderingUtil

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RenderingUtil.loadMaterial(this)

        var textView : TextView = findViewById(R.id.textViewTest)

        textView.setOnClickListener {

            ActivityUtil.startNewActivityWithoutFinish(this, ListActivity::class.java)
        }

        connectDB()

    }


    private fun connectDB() {
        val db = FirebaseFirestore.getInstance()
        db.collection("/data").document("two").get().addOnCompleteListener {
            DlogUtil.d(TAG, "성공")
        }.addOnFailureListener {
            DlogUtil.d(TAG, "실패 $it")
        }
    }
}