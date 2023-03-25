package ma.ensaf.veryempty.activities


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import android.util.DisplayMetrics
import com.google.android.material.snackbar.Snackbar
import android.text.TextWatcher
import android.text.Editable
import android.content.Intent
import android.view.*
import ma.ensaf.veryempty.databinding.ActivityPhoneRegistrationBinding
import ma.ensaf.veryempty.databinding.PopupVerifyCodeBinding
import java.util.*

class ActivityPhoneRegistration : BaseActivity() {
    lateinit var binding: ActivityPhoneRegistrationBinding
    lateinit var popupVerifycodeBinding: PopupVerifyCodeBinding
    private lateinit var parent_view: View
    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_registration)
        parent_view = findViewById(android.R.id.content)
        initToolbar(binding.toolbar, true)
        setToolbarTitle(null)

        // verification code
        binding.buttonPhoneVerify.setOnClickListener {
            showConfirmVerificationDialog(
                activityContext
            )
        }
    }

    // show the confirm verification dialog
    private fun showConfirmVerificationDialog(activityContext: BaseActivity) {
        val verify_dialog = Dialog(activityContext)
        verify_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // binding
        popupVerifycodeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(activityContext),
            R.layout.popup_verify_code,
            null,
            false
        )
        verify_dialog.setContentView(popupVerifycodeBinding.root)
        val metrics = DisplayMetrics() //get metrics of screen
        activityContext.windowManager.defaultDisplay.getMetrics(metrics)
        val width = (metrics.widthPixels * 1.0).toInt() //set width to % of total
        Objects.requireNonNull(verify_dialog.window)?.attributes?.windowAnimations =
            R.style.DialogAnimation //style id
        verify_dialog.window!!.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        ) //set layout

        // verification code text listener
        // change focus to next edit text
        popupVerifycodeBinding.editTextVerifCode1.addTextChangedListener(textWatcher)
        popupVerifycodeBinding.editTextVerifCode2.addTextChangedListener(textWatcher)
        popupVerifycodeBinding.editTextVerifCode3.addTextChangedListener(textWatcher)
        popupVerifycodeBinding.editTextVerifCode4.addTextChangedListener(textWatcher)

        // resend code
        popupVerifycodeBinding.resendCodeTextview.setOnClickListener {
            verify_dialog.dismiss()
            Snackbar.make(parent_view, "Resending Code...", Snackbar.LENGTH_SHORT).show()
        }

        // confirm verification code
        popupVerifycodeBinding.buttonConfirmCode.setOnClickListener {
            verify_dialog.dismiss()
            ActivityRegisterDonor.start(activityContext)
        }
        verify_dialog.setCancelable(true)
        verify_dialog.show()
    }

    // listen for verification code edit text changes
    // go to next and previous on text change and deletion
    private val textWatcher: TextWatcher = object : TextWatcher {
        private var previousLength = 0
        private var backSpace = false
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            //after text changed
        }

        override fun beforeTextChanged(
            s: CharSequence, start: Int, count: Int,
            after: Int
        ) {
            previousLength = s.length
        }

        override fun afterTextChanged(editable: Editable) {
            backSpace = previousLength > editable.length
            if (editable === popupVerifycodeBinding.editTextVerifCode1.editableText) {
                if (!backSpace) popupVerifycodeBinding.editTextVerifCode2.requestFocus()
            } else if (editable === popupVerifycodeBinding.editTextVerifCode2.editableText) {
                if (backSpace) {
                    popupVerifycodeBinding.editTextVerifCode1.requestFocus()
                } else {
                    popupVerifycodeBinding.editTextVerifCode3.requestFocus()
                }
            } else if (editable === popupVerifycodeBinding.editTextVerifCode3.editableText) {
                if (backSpace) {
                    popupVerifycodeBinding.editTextVerifCode2.requestFocus()
                } else {
                    popupVerifycodeBinding.editTextVerifCode4.requestFocus()
                }
            } else if (editable === popupVerifycodeBinding.editTextVerifCode4.editableText) {
                if (backSpace) {
                    popupVerifycodeBinding.editTextVerifCode3.requestFocus()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ActivityPhoneRegistration::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}