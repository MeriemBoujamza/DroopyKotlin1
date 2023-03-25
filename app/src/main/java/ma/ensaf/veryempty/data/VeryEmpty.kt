package ma.ensaf.veryempty.data

import android.content.Context
import ma.ensaf.veryempty.utils.TypefaceUtil.overrideFont
import androidx.multidex.MultiDexApplication
import androidx.multidex.MultiDex
import androidx.appcompat.app.AppCompatDelegate

class VeryEmpty : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        MultiDex.install(this)
        overrideFont(applicationContext, "SERIF", "fonts/montserrat_light.ttf")
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        val TAG = VeryEmpty::class.java.simpleName

        @get:Synchronized
        var instance: VeryEmpty? = null
            private set

        /**
         * Gets the application context.
         *
         * @return the application context
         */
        val context: Context?
            get() {
                if (instance == null) {
                    instance = VeryEmpty()
                }
                return instance
            }
    }
}