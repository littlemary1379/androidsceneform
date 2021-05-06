package com.mary.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.*
import android.view.PixelCopy
import android.view.PixelCopy.request
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.SceneView
import com.mary.myapplication.bean.data.RoomBean
import com.mary.myapplication.customView.CustomHorizontalScrollViewDisableTouch
import com.mary.myapplication.customView.SimpleNavigationBarViewHolder
import com.mary.myapplication.util.ActivityUtil
import com.mary.myapplication.util.DisplayUtil
import com.mary.myapplication.util.DlogUtil
import com.mary.myapplication.util.PermissionCheckUtil
import com.mary.myapplication.viewholder.PopupViewHolder
import com.mary.myapplication.viewholder.RenderingViewHolder
import org.json.JSONObject
import java.io.FileOutputStream

class ListActivity : AppCompatActivity() {
    private val TAG = "ListActivity"

    private lateinit var frameLayoutNavigation: FrameLayout

    private lateinit var linearLayout3D: LinearLayout
    private lateinit var linearLayoutFloor: LinearLayout

    private lateinit var frameLayout3D: FrameLayout
    private lateinit var frameLayoutFloor: FrameLayout
    private lateinit var frameLayoutWall: FrameLayout

    private lateinit var frameLayoutShare: FrameLayout
    private lateinit var popupViewHolder: PopupViewHolder


    private lateinit var horizontalScrollView: CustomHorizontalScrollViewDisableTouch

    private var installRequest: Boolean = false

    private lateinit var renderingViewHolder3D: RenderingViewHolder
    private lateinit var renderingViewHolderFloor: RenderingViewHolder
    private lateinit var renderingViewHolderWall: RenderingViewHolder

    private lateinit var simpleNavigationBarViewHolder: SimpleNavigationBarViewHolder

    private var isNew = false
    private lateinit var roomBean: RoomBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        checkBundle()
        findView()
        setListener()

        permissionCheck()
        checkARcore()

        initNavigation()
        initSceneView()
        initScreenWidth()

    }

    private fun checkBundle() {
        var bundle: Bundle? = intent.getBundleExtra(ActivityUtil.BUNDLE_KEY)
        if (bundle != null) {
            isNew = bundle.getBoolean("isNew", false)
            var roomBeanJSONObjectString = bundle.getString("roomBean", "")
            if (roomBeanJSONObjectString == "") {
                finish()
            } else {
                roomBean = RoomBean()
                var jsonObject = JSONObject(roomBeanJSONObjectString)
                roomBean.init(jsonObject)
            }
        }
    }

    private fun findView() {

        frameLayoutNavigation = findViewById(R.id.frameLayoutNavigationBar)

        linearLayout3D = findViewById(R.id.linearLayout3D)
        linearLayoutFloor = findViewById(R.id.linearLayoutFloor)

        frameLayout3D = findViewById(R.id.frameLayout3D)
        frameLayoutFloor = findViewById(R.id.frameLayoutFloor)
        frameLayoutWall = findViewById(R.id.frameLayoutWall)

        frameLayoutShare = findViewById(R.id.frameLayoutShare)

        horizontalScrollView = findViewById(R.id.horizontalScrollView)

    }

    private fun setListener() {
        linearLayout3D.setOnClickListener {
            horizontalScrollView.smoothScrollTo(frameLayout3D.x.toInt(), 0)
        }

        linearLayoutFloor.setOnClickListener {
            horizontalScrollView.smoothScrollTo(frameLayoutFloor.x.toInt(), 0)
        }

    }

    private fun initNavigation() {
        simpleNavigationBarViewHolder = SimpleNavigationBarViewHolder(this)
        simpleNavigationBarViewHolder.simpleNavigationBarViewHolderDelegate =
            object : SimpleNavigationBarViewHolder.SimpleNavigationBarViewHolderDelegate {
                override fun onLeftClick() {

                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onRightClick() {
                    share()
                }

            }

        simpleNavigationBarViewHolder.setImageViewRight(R.drawable.i_share)
        frameLayoutNavigation.addView(simpleNavigationBarViewHolder.getView())
    }

    private fun initSceneView() {

        renderingViewHolder3D = RenderingViewHolder(this, RenderingViewHolder.TYPE_3D, roomBean)
        frameLayout3D.addView(renderingViewHolder3D.view)

        renderingViewHolderFloor =
            RenderingViewHolder(this, RenderingViewHolder.TYPE_FLOOR, roomBean)
        frameLayoutFloor.addView(renderingViewHolderFloor.view)

        renderingViewHolderWall = RenderingViewHolder(this, RenderingViewHolder.TYPE_WALL, roomBean)
    }

    override fun onResume() {
        super.onResume()

        if (renderingViewHolder3D != null) {
            renderingViewHolder3D.resume()
        }

        if (renderingViewHolderFloor != null) {
            renderingViewHolderFloor.resume()
        }

        if (renderingViewHolderWall != null) {
            renderingViewHolderWall.resume()
        }

    }

    override fun onPause() {
        super.onPause()

        if (renderingViewHolder3D != null) {
            renderingViewHolder3D.pause()
        }

        if (renderingViewHolderFloor != null) {
            renderingViewHolderFloor.pause()
        }

        if (renderingViewHolderWall != null) {
            renderingViewHolderWall.pause()
        }

    }

    private fun initScreenWidth() {
        //가로 전체에서 양쪽 10 마진 주기
        val width: Int = DisplayUtil.getScreenWidthPx(this) - DisplayUtil.dipToPx(this, 10f)
        val layoutParams = LinearLayout.LayoutParams(width, width)
        frameLayout3D.layoutParams = layoutParams
        frameLayoutFloor.layoutParams = layoutParams
        frameLayoutWall.layoutParams = layoutParams
    }

    //1. permission
    private fun permissionCheck() {
        PermissionCheckUtil.checkPermission(this, arrayOf(Manifest.permission.CAMERA))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var permissionSize: Int = permissions.size

        for (i: Int in 0 until permissionSize) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permissions[i]
                ) == PackageManager.PERMISSION_DENIED
            ) {
                DlogUtil.d(TAG, "권한 미승인")
                finish()
            } else {
                DlogUtil.d(TAG, "${permissions[i]} 권한 승인")
            }
        }

    }

    //2. Create Session
    //세션을 만들기 전 AR core이 지원되는지 아닌지 확인하고 세션을 생성한다.
    private fun checkARcore() {
        try {
            when (ArCoreApk.getInstance().requestInstall(this, installRequest)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    DlogUtil.d(TAG, "AR core 설치 필요")
                    installRequest = true
                }
                ArCoreApk.InstallStatus.INSTALLED -> {
                    DlogUtil.d(TAG, "AR core 설치 미필요")
                }
            }
        } catch (e: UnavailableArcoreNotInstalledException) {
            DlogUtil.d(TAG, "ARCore 설치 필요")
        } catch (e: UnavailableUserDeclinedInstallationException) {
            DlogUtil.d(TAG, "ARCore 설치 필요")
        } catch (e: UnavailableApkTooOldException) {
            DlogUtil.d(TAG, "ARCore 업데이트 필요")
        } catch (e: UnavailableSdkTooOldException) {
            DlogUtil.d(TAG, "앱 업데이트 필요")
        } catch (e: UnavailableDeviceNotCompatibleException) {
            DlogUtil.d(TAG, "디바이스가 AR core을 지원하지 않음")
        } catch (e: Exception) {
            DlogUtil.d(TAG, "AR 세션 생성 실패")
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun share() {
        frameLayoutShare.visibility = View.VISIBLE
        popupViewHolder = PopupViewHolder(this)
        floorCapture()
        popupViewHolder.updateView(roomBean)
        frameLayoutShare.addView(popupViewHolder.view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun floorCapture() {

        var view: SceneView = renderingViewHolderFloor.sceneView

        var bitmap: Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        var location = IntArray(2)
        frameLayoutFloor.getLocationInWindow(location)

        try {
            request(view, bitmap, {
                if (it == PixelCopy.SUCCESS) {
                    DlogUtil.d(TAG, "됐나?? $bitmap")
                    setBitmap(bitmap)
                } else {
                    when (it) {
                        PixelCopy.ERROR_DESTINATION_INVALID -> {
                            DlogUtil.d(TAG, "ERROR_DESTINATION_INVALID")
                        }
                        PixelCopy.ERROR_SOURCE_INVALID -> {
                            DlogUtil.d(TAG, "ERROR_SOURCE_INVALID")
                        }
                        PixelCopy.ERROR_SOURCE_NO_DATA -> {
                            DlogUtil.d(TAG, "ERROR_SOURCE_NO_DATA")
                        }
                        PixelCopy.ERROR_TIMEOUT -> {
                            DlogUtil.d(TAG, "ERROR_TIMEOUT")
                        }
                        PixelCopy.ERROR_UNKNOWN -> {
                            DlogUtil.d(TAG, "ERROR_UNKNOWN")
                        }
                    }
                }
            }, Handler(Looper.getMainLooper()))
        } catch (e: java.lang.Exception) {
            e.message?.let { DlogUtil.d(TAG, it) }
        }

    }

    private fun setBitmap(bitmap: Bitmap){
        popupViewHolder.setImage(bitmap)
    }

}