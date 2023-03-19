package ma.ensaf.veryempty.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.databinding.ActivitySignUpBinding;
import ma.ensaf.veryempty.utils.Constants;
import ma.ensaf.veryempty.utils.PreferenceManager;

public class SignUpActivity extends BaseActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();
    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    String[] cities = {"Select City","Casablanca", "Fes", "Meknes", "Rabat", "Tangier", "Oujda","Marrakech","Tetuan","Laayoune"};

    public static void start(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        preferenceManager = new PreferenceManager(getApplicationContext());
        //setting the toolbar
        initToolbar(binding.toolbar,true);
        setToolbarTitle(null);

        //Lisetners
        setListeners();
        // cities spinner
        ArrayAdapter citiesAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, cities);
        citiesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.citySpinner.setAdapter(citiesAdapter);
        binding.citySpinner.setSelection(0,false);
        binding.citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    Toast.makeText(activityContext,parent.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setListeners() {
        binding.registerLink.setOnClickListener(v -> SignInActivity.start(activityContext));
        binding.signUpBtn.setOnClickListener(v -> {
            if(isValidSignUpDetails()) {
                signUp();
            }
        });
        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.nameInput.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.emailInput.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.passwordInput.getText().toString());
        user.put(Constants.KEY_PHONE, binding.phoneInput.getText().toString());
        user.put(Constants.KEY_CITY, binding.citySpinner.getSelectedItem().toString());
        user.put(Constants.KEY_IS_REQUESTER, false);
        user.put(Constants.KEY_IS_DONOR, false);
        user.put(Constants.KEY_COUNT_DONATIONS, 0);
        user.put(Constants.KEY_COUNT_REQUESTS, 0);
        user.put(Constants.LAST_DONATION_DATE, "none");
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,binding.nameInput.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, binding.emailInput.getText().toString());
                    preferenceManager.putString(Constants.KEY_PHONE, binding.phoneInput.getText().toString());
                    preferenceManager.putString(Constants.KEY_CITY, binding.citySpinner.getSelectedItem().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    preferenceManager.putBoolean(Constants.KEY_IS_DONOR,false);
                    preferenceManager.putBoolean(Constants.KEY_IS_REQUESTER,false);
                    preferenceManager.putInt(Constants.KEY_COUNT_DONATIONS,0);
                    preferenceManager.putInt(Constants.KEY_COUNT_REQUESTS,0);
                    preferenceManager.putString(Constants.KEY_DONATION_DATETIME,"none");
                    Intent intent = new Intent(getApplicationContext(),ActivityHome.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);


                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage(),false);
                });

    }

    private boolean isValidSignUpDetails() {
        if(encodedImage == null) {
            showToast("select an image",false);
            return false;
        } else if (binding.nameInput.getText().toString().trim().isEmpty()) {
            showToast("enter name",false);
            return false;
        } else if (binding.emailInput.getText().toString().trim().isEmpty()) {
            showToast("enter email",false);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailInput.getText().toString()).matches()) {
            showToast("enter valid email",false);
            return false;
        } else if (binding.passwordInput.getText().toString().trim().isEmpty()) {
            showToast("enter password",false);
            return false;
        } else if (binding.phoneInput.getText().toString().trim().isEmpty() ||
                !Patterns.PHONE.matcher(binding.phoneInput.getText().toString()).matches()) {
            showToast("enter valid phone number",false);
            return false;
        } else if (binding.citySpinner.getSelectedItemPosition() == 0) {
            showToast("select a city",false);
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading)  {
        if(isLoading) {
            binding.signUpBtn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.signUpBtn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private String encodedImage(Bitmap bitmap) {
        int previewWidth =150;
        int previewHeight =bitmap.getHeight() * previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }
    //Getting a result from an activity (camera or gallery app in our example)
    private final ActivityResultLauncher<Intent> pickImage =  registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream= getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        } catch(FileNotFoundException e) {
                            e.printStackTrace();

                        }
                    }
                }

            }
    );



}