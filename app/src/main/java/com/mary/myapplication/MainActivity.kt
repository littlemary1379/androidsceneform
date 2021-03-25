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
import com.google.ar.sceneform.SceneView
import com.mary.myapplication.util.DlogUtil
import com.mary.myapplication.util.PermissionCheckUtil
import com.mary.myapplication.util.fragment.Fragment3D
import com.mary.myapplication.util.fragment.FragmentFloor

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var linearLayout3D: LinearLayout
    private lateinit var linearLayoutFloor: LinearLayout
    private lateinit var frameLayoutContainer: FrameLayout

    private var installRequest: Boolean = false

    private var fragment = "3D"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findView()
        setListener()

        permissionCheck()
        checkARcore()

        supportFragmentManager.beginTransaction().add(R.id.frameLayoutContainer, Fragment3D().newInstance()).commit()
    }

    private fun findView() {
        linearLayout3D = findViewById(R.id.linearLayout3D)
        linearLayoutFloor = findViewById(R.id.linearLayoutFloor)
        frameLayoutContainer = findViewById(R.id.frameLayoutContainer)
    }

    private fun setListener(){
        linearLayout3D.setOnClickListener {
            DlogUtil.d(TAG, "3D")
            if(fragment == "3D") {
                return@setOnClickListener
            }
            fragment = "3D"
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, Fragment3D().newInstance()).commit()
        }

        linearLayoutFloor.setOnClickListener {
            DlogUtil.d(TAG, "floor")
            if(fragment == "floor") {
                return@setOnClickListener
            }
            fragment = "floor"
            supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContainer, FragmentFloor()).commit()
        }


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