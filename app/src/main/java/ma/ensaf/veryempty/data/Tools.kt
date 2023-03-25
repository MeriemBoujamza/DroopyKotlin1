package ma.ensaf.veryempty.data

import android.os.Build
import android.app.Activity
import android.graphics.Color
import android.view.WindowManager
import ma.ensaf.veryempty.R
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import java.lang.NumberFormatException

object Tools {
    private val aPIVersison: Float
        get() {
            var f: Float
            try {
                f = Build.VERSION.RELEASE.substring(0, 1).toFloat()
            } catch (e: NumberFormatException) {
                f = 1.0f
                Log.e("", "Error on getting API version " + e.message)
            }
            return f
        }

    // Android Completely transparent Status Bar?
    fun setStatusBarTransparent(activity: Activity) {

        if (Build.VERSION.SDK_INT >= 19) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            activity.window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    fun systemBarLollipop(act: Activity) {
        if (aPIVersison >= 5.0) {
            val window = act.window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = act.resources.getColor(R.color.colorPrimaryDark)
            }
        }
    }

    fun systemBarLollipopTransparent(act: Activity) {
        if (aPIVersison >= 5.0) {
            val window = act.window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = act.resources.getColor(android.R.color.transparent)
            }
        }
    }

    fun setStatusBarColor(color: Int, act: Activity) {
        if (aPIVersison >= 5.0) {
            val window = act.window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = color
            }
        }
    }

    fun getGridSpanCount(activity: Activity): Int {
        val display = activity.windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val cellWidth = activity.resources.getDimension(R.dimen.recycler_item_size)
        return Math.round(screenWidth / cellWidth)
    }
}