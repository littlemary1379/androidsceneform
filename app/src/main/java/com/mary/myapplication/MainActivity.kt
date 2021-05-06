package com.mary.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.mary.myapplication.adapter.ModelListAdapter
import com.mary.myapplication.bean.ModelWrapperItemBean
import com.mary.myapplication.constant.Constant
import com.mary.myapplication.constant.WebConstant
import com.mary.myapplication.model.SceneformModel
import com.mary.myapplication.temp.TempPointBean
import com.mary.myapplication.temp.TempRoomBean
import com.mary.myapplication.util.DlogUtil
import com.mary.myapplication.util.RenderingUtil
import com.mary.myapplication.util.ThreadUtil
import org.json.JSONArray
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var dataSize : Int = 0
    private lateinit var tempRoomBean : TempRoomBean;
    private lateinit var db : FirebaseFirestore

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    private lateinit var modelListAdapter: ModelListAdapter
    private lateinit var selectedModelWrapperItemBeanList: MutableList<ModelWrapperItemBean>
    private val testToken =
        "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4NDkiLCJyb2xlcyI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sImlhdCI6MTYxMjQxMTQxNCwiZXhwIjoxNjQzOTQ3NDE0fQ.euw0iWIfXFFzyGse1w0jI1eY7kAMwEQ7EXV3nDpREQI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * Important!!!!!!
         * 사용자 텍스쳐 이용하는 경우 무조건 모델 로드 전 액티비티에서 실행되어야합니다
         */

        RenderingUtil.loadMaterial(this)

        findView()
        initList()
        setListener()
        load()

        //구글 파이어베이스로 웹 정보 로드용 코드
//        connectDB2()

    }

    //웹정보 로드용 코드
    private fun connectDB2() {
        db = FirebaseFirestore.getInstance()
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

    private fun findView(){
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
    }

    private fun initList(){
        modelListAdapter = ModelListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = modelListAdapter
    }

    private fun setListener() {
        swipeRefreshLayout.setOnRefreshListener {
            load()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun load(){
        if(::selectedModelWrapperItemBeanList.isInitialized) {
            if(selectedModelWrapperItemBeanList.isNotEmpty()){
                selectedModelWrapperItemBeanList.clear()
            }
        }
        SceneformModel.requestUploadModel(testToken,"","0","11", object : SceneformModel.SceneFormModelDelegate {
            override fun onResponse(response: String?) {
                if (response != null) {
                    DlogUtil.d(TAG, response)
                    if(WebConstant.getIsSuccess(response)) {
                        val jsonArray : JSONArray? = WebConstant.getList(response)

                        selectedModelWrapperItemBeanList = ArrayList()
                        var modelWrapperItemBean : ModelWrapperItemBean

                        for(i in 0 until jsonArray!!.length()) {
                            modelWrapperItemBean = ModelWrapperItemBean()
                            modelWrapperItemBean.initWithJSONObject(jsonArray.getJSONObject(i))
                            selectedModelWrapperItemBeanList.add(modelWrapperItemBean)
                        }

                        ThreadUtil.startUIThread(0, Runnable {
                            modelListAdapter.reloadItem(selectedModelWrapperItemBeanList)
                        })

                    }
                } else {
                    DlogUtil.d(TAG, "response null")
                }
            }

            override fun onException(e: Exception?) {
                e?.printStackTrace()
            }

        })

    }


}