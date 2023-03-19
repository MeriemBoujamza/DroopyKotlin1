package ma.ensaf.veryempty.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.databinding.ActivitySignInBinding;
import ma.ensaf.veryempty.utils.Constants;
import ma.ensaf.veryempty.utils.PreferenceManager;

public class SignInActivity extends BaseActivity {
    private static final String TAG = SignInActivity.class.getSimpleName();

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;





    public static void start(Context context) {
        Intent intent = new Intent(context, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        //////////SETTING THE PREFERENCEMANAGER
        preferenceManager = new PreferenceManager(getApplicationContext());
        
        setListeners();
    }

    private void setListeners() {
        binding.registerLink.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.signInButton.setOnClickListener(v -> {
            if(isValidSignInDetails()) {
                signIn();
            }
        });
    }

    private boolean isValidSignInDetails() {
        if (binding.emailInput.getText().toString().isEmpty()) {
            showToast("enter email",false);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailInput.getText().toString()).matches()) {
            showToast("enter valid email",false);
            return false;
        }
        else if (binding.passwordInput.getText().toString().isEmpty()) {
            showToast("enter password",false);
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading)  {
        if(isLoading) {
            binding.signInButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.signInButton.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.emailInput.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.passwordInput.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_PHONE, documentSnapshot.getString(Constants.KEY_PHONE));
                        preferenceManager.putString(Constants.KEY_CITY, documentSnapshot.getString(Constants.KEY_CITY));
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        preferenceManager.putInt(Constants.KEY_COUNT_DONATIONS,documentSnapshot.getDouble(Constants.KEY_COUNT_DONATIONS).intValue());
                        preferenceManager.putInt(Constants.KEY_COUNT_REQUESTS,documentSnapshot.getDouble(Constants.KEY_COUNT_REQUESTS).intValue());
                        Intent intent= new Intent(getApplicationContext(),ActivityHome.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable to sign in",false);
                    }
                });
    }
}