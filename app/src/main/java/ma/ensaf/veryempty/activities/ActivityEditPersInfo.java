package ma.ensaf.veryempty.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.databinding.ActivityEditPersInfoBinding;
import ma.ensaf.veryempty.databinding.ActivityRegisterDonorBinding;
import ma.ensaf.veryempty.databinding.ActivitySignUpBinding;
import ma.ensaf.veryempty.utils.Constants;
import ma.ensaf.veryempty.utils.PreferenceManager;

public class ActivityEditPersInfo extends BaseActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();
    private ActivityEditPersInfoBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    String[] cities = {"Select City","Casablanca", "Fes", "Meknes", "Rabat", "Tangier", "Oujda","Marrakech","Tetuan","Laayoune"};

    public static void start(Context context) {
        Intent intent = new Intent(context, ActivityEditPersInfo.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @SuppressLint({"CheckResult", "SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_pers_info);
        preferenceManager = new PreferenceManager(getApplicationContext());

        initToolbar(binding.toolbar,true);
        setToolbarTitle(null);
        //Lisetners
        initViews();
        setListeners();
        ArrayAdapter citiesAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, cities);
        citiesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.citySpinner.setAdapter(citiesAdapter);
        binding.citySpinner.setSelection(Arrays.asList(cities).indexOf(preferenceManager.getString(Constants.KEY_CITY)),false);
        binding.citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    preferenceManager.putString(Constants.KEY_CITY,parent.getItemAtPosition(position).toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void initViews() {
        binding.nameInput.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.phoneInput.setText(preferenceManager.getString(Constants.KEY_PHONE));
        binding.emailInput.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.textAddImage.setVisibility(View.INVISIBLE);
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);
    }

    private void setListeners() {
        binding.UpdateBtn.setOnClickListener(v -> {
            if(isValidProfileDetails()) {
                updateProfile();
            }
        });

        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void updateProfile() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_NAME,binding.nameInput.getText().toString(),
                Constants.KEY_PHONE,binding.phoneInput.getText().toString(),
                Constants.KEY_CITY,binding.citySpinner.getSelectedItem().toString(),
                Constants.KEY_EMAIL,binding.emailInput.getText().toString(),
                Constants.KEY_IMAGE,encodedImage)
                .addOnSuccessListener(unused -> {
                    preferenceManager.putString(Constants.KEY_NAME, binding.nameInput.getText().toString());
                    preferenceManager.putString(Constants.KEY_PHONE, binding.phoneInput.getText().toString());
                    preferenceManager.putString(Constants.KEY_CITY, binding.citySpinner.getSelectedItem().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, binding.emailInput.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    loading(false);
                    Intent intent= new Intent(getApplicationContext(),ActivityHome.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showToast("Unable to update profile",false);
                });
    }

    private boolean isValidProfileDetails() {
        if (binding.nameInput.getText().toString().trim().isEmpty()) {
            showToast("enter name",false);
            return false;
        } else if (binding.emailInput.getText().toString().trim().isEmpty()) {
            showToast("enter email",false);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailInput.getText().toString()).matches()) {
            showToast("enter valid email",false);
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
            binding.UpdateBtn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.UpdateBtn.setVisibility(View.VISIBLE);
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

