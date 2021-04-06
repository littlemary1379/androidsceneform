package com.mary.myapplication.util

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import android.view.Window
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction

object ActivityUtil {
    const val BUNDLE_KEY = "BUNDLE_KEY"

    fun startSystemIntent(packageContext: Context, intent: Intent?) {
        packageContext.startActivity(intent)
    }

    fun startSystemIntentWithResultByRequestCode(
        activity: Activity,
        intent: Intent?,
        requestCode: Int
    ) {
        activity.startActivityForResult(intent, requestCode)
    }

    fun startNewActivityWithoutFinish(packageContext: Context, cls: Class<*>?) {
        val intent = Intent(packageContext, cls)
        packageContext.startActivity(intent)
    }

    fun startNewActivityWithoutFinish(packageContext: Context, cls: Class<*>?, bundle: Bundle?) {
        val intent = Intent(packageContext, cls)
        intent.putExtra(BUNDLE_KEY, bundle)
        packageContext.startActivity(intent)
    }

    fun startNewActivityWithFinish(packageContext: Context, cls: Class<*>?) {
        val intent = Intent(packageContext, cls)
        packageContext.startActivity(intent)
        (packageContext as Activity).finish()
    }

    fun startNewActivityWithFinish(packageContext: Context, cls: Class<*>?, bundle: Bundle?) {
        val intent = Intent(packageContext, cls)
        intent.putExtra(BUNDLE_KEY, bundle)
        packageContext.startActivity(intent)
        (packageContext as Activity).finish()
    }

    fun getBundleDataFromPreActivity(activity: Activity): Bundle? {
        return activity.intent.extras
    }

    fun getActivityResultBundleDataFromPreActivity(intent: Intent): Bundle? {
        return intent.extras
    }

    fun startNewActivityWithoutFinishForResult(
        activity: Activity,
        cls: Class<*>?,
        requestCode: Int
    ) {
        val intent = Intent(activity, cls)
        activity.startActivityForResult(intent, requestCode)
    }

    fun startActivityWithTransitionAnimationWithoutFinish(activity: Activity, cls: Class<*>?) {
        val intent = Intent(activity, cls)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.startActivity(
                    intent,
                    ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
                )
            }
        }
    }

    fun startActivityWithTransitionAnimationWithFinish(activity: Activity, cls: Class<*>?) {
        val intent = Intent(activity, cls)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.startActivity(
                    intent,
                    ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
                )
                activity.finish()
            }
        }
    }

    private fun targetActivityFiringTransitionAnimation(activity: Activity): Boolean {
        return try {
            activity.window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            true
        } catch (e: Exception) {
            false
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun tagetActivitySetEnterTransitionAnimationExplode(activity: Activity) {
        if (targetActivityFiringTransitionAnimation(activity)) {
            activity.window.enterTransition = Explode()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun tagetActivitySetEnterTransitionAnimationSlide(activity: Activity) {
        if (targetActivityFiringTransitionAnimation(activity)) {
            activity.window.enterTransition = Slide()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun tagetActivitySetEnterTransitionAnimationFade(activity: Activity) {
        if (targetActivityFiringTransitionAnimation(activity)) {
            activity.window.enterTransition = Fade()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun tagetActivitySetExitTransitionAnimationExplode(activity: Activity) {
        if (targetActivityFiringTransitionAnimation(activity)) {
            activity.window.exitTransition = Explode()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun tagetActivitySetExitTransitionAnimationSlide(activity: Activity) {
        if (targetActivityFiringTransitionAnimation(activity)) {
            activity.window.exitTransition = Slide()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun tagetActivitySetExitTransitionAnimationFade(activity: Activity) {
        if (targetActivityFiringTransitionAnimation(activity)) {
            activity.window.exitTransition = Fade()
        }
    }

    fun addFragment(
        activity: FragmentActivity,
        containerViewId: Int,
        fragment: Fragment,
        withAnimation: Boolean
    ) {
        if (fragment.isAdded) {
            return
        }
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.add(containerViewId, fragment).commit()
    }

//    fun addFragmentWithTAG(
//        activity: FragmentActivity,
//        containerViewId: Int,
//        fragment: Fragment,
//        tag: String?,
//        animation: Boolean,
//        rootFragment: Fragment?
//    ) {
//        if (fragment.isAdded) {
//            return
//        }
//        val transaction = activity.supportFragmentManager.beginTransaction()
//        if (animation) {
//            transaction.setCustomAnimations(
//                R.anim.fragment_from_right,
//                R.anim.fragment_out_left,
//                R.anim.fragment_from_right,
//                R.anim.fragment_out_left
//            )
//            if (rootFragment != null) {
//                rootFragment.view!!
//                    .startAnimation(
//                        AnimationUtils.loadAnimation(
//                            activity,
//                            R.anim.view_out_litte_left
//                        )
//                    )
//            }
//        } else {
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//        }
//        transaction.add(containerViewId, fragment, tag).commitAllowingStateLoss()
//    }

    fun replaceFragmentWithTAG(
        activity: FragmentActivity,
        containerViewId: Int,
        fragment: Fragment?,
        tag: String?
    ) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(containerViewId, fragment!!, tag).commitAllowingStateLoss()
    }

//    fun removeFragment(
//        activity: FragmentActivity,
//        fragment: Fragment?,
//        animation: Boolean,
//        prevFragment: Fragment?
//    ) {
//        if (fragment == null || !fragment.isAdded) {
//            return
//        }
//        val transaction = activity.supportFragmentManager.beginTransaction()
//        if (animation) {
//            transaction.setCustomAnimations(
//                R.anim.fragment_from_right,
//                R.anim.fragment_out_left,
//                R.anim.fragment_from_right,
//                R.anim.fragment_out_left
//            )
//            if (prevFragment != null) {
//                prevFragment.view!!
//                    .startAnimation(
//                        AnimationUtils.loadAnimation(
//                            activity,
//                            R.anim.view_from_little_right
//                        )
//                    )
//            }
//        } else {
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//        }
//        transaction.remove(fragment).commit()
//    }

    /**
     * use this when activity
     * @param activity
     * @param fragment
     */
    fun hideFragment(activity: FragmentActivity, fragment: Fragment?) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.hide(fragment!!).commitAllowingStateLoss()
    }

    /**
     * use this when activity
     * @param activity
     * @param fragment
     */
    fun showFragment(activity: FragmentActivity, fragment: Fragment?) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.show(fragment!!).commitAllowingStateLoss()
    }

    fun detachFragment(activity: FragmentActivity, fragment: Fragment?) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.detach(fragment!!).commit()
    }

    fun attachFragment(activity: FragmentActivity, fragment: Fragment?) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.attach(fragment!!).commit()
    }

    fun addToBackStackFragment(
        activity: FragmentActivity,
        containerViewId: Int,
        fragment: Fragment?
    ) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(containerViewId, fragment!!)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     *
     * is activity visible
     *
     */
    private fun isForeground(activity: Activity?): Boolean {
        if (activity == null || TextUtils.isEmpty(activity.javaClass.name)) {
            return false
        }
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = am.getRunningTasks(1)
        if (list != null && list.size > 0) {
            val cpn = list[0].topActivity
            if (activity.javaClass.name == cpn!!.className) return true
        }
        return false
    }
}