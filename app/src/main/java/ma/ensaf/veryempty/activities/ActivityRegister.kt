package ma.ensaf.veryempty.activities

import androidx.biometric.BiometricPrompt.PromptInfo
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import ma.ensaf.veryempty.data.Tools
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import android.content.Intent
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.biometric.BiometricPrompt

import ma.ensaf.veryempty.databinding.ActivityRegisterBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager
import java.util.concurrent.Executor

open class ActivityRegister : BaseActivity() {
    lateinit var  binding: ActivityRegisterBinding
    protected lateinit var preferenceManager: PreferenceManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private var promptInfo: PromptInfo? = null
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // make status transparent
        Tools.setStatusBarTransparent(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        //facebook setting
        preferenceManager = PreferenceManager(applicationContext)
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val intent = Intent(applicationContext, ActivityHome::class.java)
            startActivity(intent)
            finish()
        }
        ///
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this,
            executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                    startActivity(Intent(applicationContext, SignInActivity::class.java))
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(applicationContext, ActivityHome::class.java))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    startActivity(Intent(applicationContext, SignInActivity::class.java))
                }
            })
        promptInfo = PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your ")
            .setNegativeButtonText("Use account password")
            .build()
        setListeners()
    }

    private fun setListeners() {
        binding.registerLink.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext, SignUpActivity::class.java
                )
            )
        }
        binding.signInLayout.setOnClickListener {
            // TODO: add already signed in condition here
            if (preferenceManager.getBoolean(Constants.LOGGEDIN_ONCE_BEFORE)) {
                biometricPrompt.authenticate(promptInfo!!)
            } else {
                startActivity(Intent(applicationContext, SignInActivity::class.java))
            }
        }
        binding.facebookBtn.setOnClickListener {
            val intent = Intent(applicationContext, FacebookAuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }
        binding.googleBtn.setOnClickListener {
            val intent = Intent(applicationContext, GoogleAuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ActivityRegister::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}