package ma.ensaf.veryempty.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;


import java.util.concurrent.Executor;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.data.Tools;
import ma.ensaf.veryempty.databinding.ActivityRegisterBinding;
import ma.ensaf.veryempty.utils.Constants;
import ma.ensaf.veryempty.utils.PreferenceManager;

public class ActivityRegister extends BaseActivity {

    private static final String TAG = ActivityRegister.class.getSimpleName();
    ActivityRegisterBinding binding;
    protected PreferenceManager preferenceManager;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    public boolean authinfo = true;

    public static void start(Context context) {
        Intent intent = new Intent(context, ActivityRegister.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);

    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make status transparent
        Tools.setStatusBarTransparent(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        //facebook setting
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent=new Intent(getApplicationContext(),ActivityHome.class);
            startActivity(intent);
            finish();
        }
        ///

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                Toast.makeText(getApplicationContext(),"Authentication succeeded!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),ActivityHome.class));
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Log in using your ")
                .setNegativeButtonText("Use account password")
                .build();

        setListeners();
    }

    private void setListeners()  {
        binding.registerLink.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.signInLayout.setOnClickListener(v ->{
            // TODO: add already signed in condition here
            if(preferenceManager.getBoolean(Constants.LOGGEDIN_ONCE_BEFORE)) {
                biometricPrompt.authenticate(promptInfo);

            } else {
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
            }
        });
        binding.facebookBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),FacebookAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });
        binding.googleBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),GoogleAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });
    }
}
