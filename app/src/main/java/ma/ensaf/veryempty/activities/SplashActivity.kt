package ma.ensaf.veryempty.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import android.os.CountDownTimer
import android.view.View
import android.view.Window

import ma.ensaf.veryempty.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    var binding: ActivitySplashBinding? = null
    private var parent_view: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        parent_view = findViewById(android.R.id.content)

        // timer task
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                binding?.loadingProgress?.visibility = View.GONE
                // Start login activity
                ActivityWelcomeScreen.start(this@SplashActivity)
                // close splash activity
                finish()
            }
        }.start()
    }

}