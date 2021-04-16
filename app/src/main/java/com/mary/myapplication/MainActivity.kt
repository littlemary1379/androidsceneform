package com.mary.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.mary.myapplication.temp.TempPointBean
import com.mary.myapplication.temp.TempRoomBean
import com.mary.myapplication.util.ActivityUtil
import com.mary.myapplication.util.Constant
import com.mary.myapplication.util.DlogUtil
import com.mary.myapplication.util.RenderingUtil

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var dataSize : Int = 0
    private lateinit var tempRoomBean : TempRoomBean;
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RenderingUtil.loadMaterial(this)

        var textView : TextView = findViewById(R.id.textViewTest)

        textView.setOnClickListener {

            ActivityUtil.startNewActivityWithoutFinish(this, ListActivity::class.java)
        }

        connectDB()
        connectDB2()

    }


    private fun connectDB() {
        db = FirebaseFirestore.getInstance()
        db.collection("/data").document("two").get().addOnCompleteListener {
            if(it.isSuccessful) {
                val data = it.result
                DlogUtil.d(TAG, "성공 ${data.data}")
                tempRoomBean = TempRoomBean()
                tempRoomBean = it.result.toObject(TempRoomBean::class.java)!!
                DlogUtil.d(TAG, "캐스팅 성공 ${tempRoomBean?.startvector1}")
                tempRoomBean.init()
                DlogUtil.d(TAG, "벡터화 성공 ${tempRoomBean?.startRawVector1}")
                //Constant.vectorList = tempRoomBean.vectorList
            } else {
                DlogUtil.d(TAG, "실패")
            }
        }.addOnFailureListener {
            DlogUtil.d(TAG, "실패 $it")
        }
    }

    private fun connectDB2() {
        //db = FirebaseFirestore.getInstance()

        db.collection("/testData").get().addOnCompleteListener {
            if(it.isSuccessful) {
                val data = it.result
                DlogUtil.d(TAG, "성공 ${data.documents.size}")
                dataSize = data.documents.size
                connectionDB3()
            } else {
                DlogUtil.d(TAG, "실패")
            }
        }.addOnFailureListener {
            DlogUtil.d(TAG, "실패 $it")
        }
    }

    private fun connectionDB3(){
        for (i : Int in 0 until dataSize) {
            var docName = "vector$i"
            db.collection("/testData").document(docName).get().addOnCompleteListener {
                if(it.isSuccessful) {
                    val data = it.result
                    DlogUtil.d(TAG, "성공 $docName ${data.data}")
                    var tempPointBean = it.result.toObject(TempPointBean::class.java)!!
                    DlogUtil.d(TAG, "캐스팅 성공 ${tempPointBean.startVector}")
                    tempPointBean.init()
                    DlogUtil.d(TAG, "list ${tempPointBean.lineVector}")
                    Constant.vectorList?.add(tempPointBean.lineVector!!)
                    DlogUtil.d(TAG, "list ${Constant.vectorList}")
                }
            }
        }


    }
}