package ma.ensaf.veryempty.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import ma.ensaf.veryempty.R;


@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    public Toolbar toolbar;
    public ProgressDialog progressDialog;
    protected BaseActivity activityContext;
    private ActionBar actionbar;

    // UUID.randomUUID() method that generates a unique identifier
    // for a specific installation.
    private static String uniqueID = null;
    private static String generatedAndroidId = null;


    @SuppressLint({"CheckResult", "HardwareIds"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityContext = BaseActivity.this;

        // stop crashes
        Thread.setDefaultUncaughtExceptionHandler((paramThread, paramThrowable) -> {
            Log.e("Error",paramThrowable.getLocalizedMessage());
        });

        // combination of Secure Android ID and Serial Number
        generatedAndroidId = Settings.Secure.getString(activityContext.getContentResolver(),
                Settings.Secure.ANDROID_ID) + Build.SERIAL;

        progressDialog = new ProgressDialog(BaseActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);

    }
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public void showProgressDialog(String title, String message,boolean isCancelleable) {
        if (!TextUtils.isEmpty(title) || !title.equals("")) {
            progressDialog.setTitle(title);
        }
        progressDialog.setCancelable(!isCancelleable);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    public void initToolbar(boolean isShowHome) {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        if (isShowHome) {
            Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
        } else {
            Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(false);
        }
        actionbar.setHomeButtonEnabled(true);
    }


    // because we are using bindings
    public void initToolbar(Toolbar toolbar, boolean isShowHome) {
        setSupportActionBar(toolbar);
        actionbar = getSupportActionBar();
        if (isShowHome) {
            Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
        } else {
            Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(false);
        }
        actionbar.setHomeButtonEnabled(true);
    }


    public void setToolbarTitle(String title) {
        actionbar.setTitle(title);
    }

    public void showAlert(String title,String message){
        AlertDialog.Builder dialogBuilder;
        dialogBuilder = new AlertDialog.Builder(activityContext);
        dialogBuilder.setTitle(title)
                .setMessage(message)
                .setNegativeButton("OK", (dialog, which) -> dialog.cancel())
                .show();
    }

    public void showToast(String text,boolean isLong){
        if(isLong){
            Toast.makeText(activityContext, text, Toast.LENGTH_LONG).show();
        }else{

            Toast.makeText(activityContext, text, Toast.LENGTH_SHORT).show();
        }
    }

    public void requestFocus(View view) {
        if (view.requestFocus()) {
            activityContext.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}

