package com.mary.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.*
import com.mary.myapplication.customView.CustomHorizontalScrollViewDisableTouch
import com.mary.myapplication.util.DisplayUtil
import com.mary.myapplication.util.DlogUtil
import com.mary.myapplication.util.PermissionCheckUtil
import com.mary.myapplication.viewholder.RenderingViewHolder

class ListActivity : AppCompatActivity() {
    private val TAG = "ListActivity"

    private lateinit var linearLayout3D: LinearLayout
    private lateinit var linearLayoutFloor: LinearLayout

    private lateinit var frameLayout3D: FrameLayout
    private lateinit var frameLayoutFloor: FrameLayout
    private lateinit var frameLayoutWall: FrameLayout

    private lateinit var horizontalScrollView: CustomHorizontalScrollViewDisableTouch

    private var installRequest: Boolean = false

    private lateinit var renderingViewHolder3D: RenderingViewHolder
    private lateinit var renderingViewHolderFloor: RenderingViewHolder
    private lateinit var renderingViewHolderWall: RenderingViewHolder



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        findView()
        setListener()

        permissionCheck()
        checkARcore()

        initSceneView()
        initScreenWidth()

    }


    private fun findView() {
        linearLayout3D = findViewById(R.id.linearLayout3D)
        linearLayoutFloor = findViewById(R.id.linearLayoutFloor)

        frameLayout3D = findViewById(R.id.frameLayout3D)
        frameLayoutFloor = findViewById(R.id.frameLayoutFloor)
        frameLayoutWall = findViewById(R.id.frameLayoutWall)

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

    private fun initSceneView() {


        renderingViewHolder3D = RenderingViewHolder(this, RenderingViewHolder.TYPE_3D)
        frameLayout3D.addView(renderingViewHolder3D.view)

        renderingViewHolderFloor = RenderingViewHolder(this, RenderingViewHolder.TYPE_FLOOR)
        frameLayoutFloor.addView(renderingViewHolderFloor.view)

        renderingViewHolderWall = RenderingViewHolder(this, RenderingViewHolder.TYPE_WALL)
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


}