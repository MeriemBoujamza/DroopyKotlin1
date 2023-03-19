package ma.ensaf.veryempty.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.databinding.ActivityPhoneRegistrationBinding;
import ma.ensaf.veryempty.databinding.PopupVerifyCodeBinding;

public class ActivityPhoneRegistration extends BaseActivity {

    private static final String TAG = ActivityPhoneRegistration.class.getSimpleName();

    ActivityPhoneRegistrationBinding binding;
    PopupVerifyCodeBinding popupVerifycodeBinding;

    private View parent_view;

    public static void start(Context context) {
        Intent intent = new Intent(context, ActivityPhoneRegistration.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @SuppressLint({"CheckResult", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_registration);
        parent_view = findViewById(android.R.id.content);

        initToolbar(binding.toolbar,true);
        setToolbarTitle(null);

        // verification code
        binding.buttonPhoneVerify.setOnClickListener(v -> {
            showConfirmVerificationDialog(activityContext);
        });
    }

    // show the confirm verification dialog
    private void showConfirmVerificationDialog(BaseActivity activityContext) {

        Dialog verify_dialog = new Dialog(activityContext);
        verify_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // binding
        popupVerifycodeBinding = DataBindingUtil.inflate(LayoutInflater.from(activityContext), R.layout.popup_verify_code, null, false);
        verify_dialog.setContentView(popupVerifycodeBinding.getRoot());

        DisplayMetrics metrics = new DisplayMetrics(); //get metrics of screen
        activityContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) (metrics.widthPixels * 1.0); //set width to % of total
        Objects.requireNonNull(verify_dialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation; //style id
        verify_dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT); //set layout

        // verification code text listener
        // change focus to next edit text
        popupVerifycodeBinding.editTextVerifCode1.addTextChangedListener(textWatcher);
        popupVerifycodeBinding.editTextVerifCode2.addTextChangedListener(textWatcher);
        popupVerifycodeBinding.editTextVerifCode3.addTextChangedListener(textWatcher);
        popupVerifycodeBinding.editTextVerifCode4.addTextChangedListener(textWatcher);

        // resend code
        popupVerifycodeBinding.resendCodeTextview.setOnClickListener(v -> {
            verify_dialog.dismiss();
            Snackbar.make(parent_view, "Resending Code...", Snackbar.LENGTH_SHORT).show();
        });

        // confirm verification code
        popupVerifycodeBinding.buttonConfirmCode.setOnClickListener(v -> {
            verify_dialog.dismiss();
            ActivityRegisterDonor.start(activityContext);
        });

        verify_dialog.setCancelable(true);
        verify_dialog.show();
    }

    // listen for verification code edit text changes
    // go to next and previous on text change and deletion
    private TextWatcher textWatcher = new TextWatcher() {

        private int previousLength;
        private boolean backSpace;
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //after text changed
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            previousLength = s.length();
        }

        @Override
        public void afterTextChanged(Editable editable) {
            backSpace = previousLength > editable.length();
            if (editable == popupVerifycodeBinding.editTextVerifCode1.getEditableText()) {
                if (!backSpace) popupVerifycodeBinding.editTextVerifCode2.requestFocus();
            }else if (editable == popupVerifycodeBinding.editTextVerifCode2.getEditableText()) {
                if (backSpace) {
                    popupVerifycodeBinding.editTextVerifCode1.requestFocus();
                }else{
                    popupVerifycodeBinding.editTextVerifCode3.requestFocus();
                }
            }else if (editable == popupVerifycodeBinding.editTextVerifCode3.getEditableText()) {
                if (backSpace) {
                    popupVerifycodeBinding.editTextVerifCode2.requestFocus();
                }else{
                    popupVerifycodeBinding.editTextVerifCode4.requestFocus();
                }
            }else if (editable == popupVerifycodeBinding.editTextVerifCode4.getEditableText()) {
                if (backSpace) {
                    popupVerifycodeBinding.editTextVerifCode3.requestFocus();
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
