package ma.ensaf.veryempty.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog

import android.os.Bundle
import android.os.Build
import android.text.TextUtils
import ma.ensaf.veryempty.R
import android.content.DialogInterface
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import java.util.*

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var progressDialog: ProgressDialog
    public lateinit var activityContext: BaseActivity
    private lateinit var actionbar: ActionBar
    @SuppressLint("CheckResult", "HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContext = this@BaseActivity

        // stop crashes
        Thread.setDefaultUncaughtExceptionHandler { paramThread: Thread?, paramThrowable: Throwable ->
            Log.e(
                "Error",
                paramThrowable.localizedMessage!!
            )
        }

        // combination of Secure Android ID and Serial Number
        generatedAndroidId = Settings.Secure.getString(
            activityContext!!.contentResolver,
            Settings.Secure.ANDROID_ID
        ) + Build.SERIAL
        progressDialog = ProgressDialog(this@BaseActivity)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(true)
    }


    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showProgressDialog(title: String, message: String?, isCancelleable: Boolean) {
        if (!TextUtils.isEmpty(title) || title != "") {
            progressDialog.setTitle(title)
        }
        progressDialog.setCancelable(!isCancelleable)
        progressDialog.setMessage(message)
        progressDialog.show()
    }

    fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    fun initToolbar(isShowHome: Boolean) {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionbar = supportActionBar!!
        if (isShowHome) {
            Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true)
        } else {
            Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(false)
        }
        actionbar.setHomeButtonEnabled(true)
    }

    // because we are using bindings
    fun initToolbar(toolbar: Toolbar?, isShowHome: Boolean) {
        setSupportActionBar(toolbar)
        actionbar = supportActionBar!!
        if (isShowHome) {
            Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true)
        } else {
            Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(false)
        }
        actionbar.setHomeButtonEnabled(true)
    }

    fun setToolbarTitle(title: String?) {
        actionbar.title = title
    }

    fun showAlert(title: String?, message: String?) {
        val dialogBuilder: AlertDialog.Builder
        dialogBuilder = AlertDialog.Builder(activityContext)
        dialogBuilder.setTitle(title)
            .setMessage(message)
            .setNegativeButton("OK") { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .show()
    }

    fun showToast(text: String?, isLong: Boolean) {
        if (isLong) {
            Toast.makeText(activityContext, text, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(activityContext, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun requestFocus(view: View) {
        if (view.requestFocus()) {
            activityContext!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    companion object {
        // UUID.randomUUID() method that generates a unique identifier
        // for a specific installation.
        private val uniqueID: String? = null
        private var generatedAndroidId: String? = null
    }
}