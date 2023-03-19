package ma.ensaf.veryempty.data;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import ma.ensaf.veryempty.utils.TypefaceUtil;

public class VeryEmpty extends MultiDexApplication {

    public static final String TAG = VeryEmpty.class.getSimpleName();
    private static VeryEmpty mInstance;

    /**
     * Gets the application context.
     *
     * @return the application context
     */
    public static Context getContext() {
        if (mInstance == null) {
            mInstance = new VeryEmpty();
        }
        return mInstance;
    }

    public static synchronized VeryEmpty getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        MultiDex.install(this);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/montserrat_light.ttf");

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}

