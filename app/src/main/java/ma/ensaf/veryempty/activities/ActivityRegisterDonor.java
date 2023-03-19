package ma.ensaf.veryempty.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.databinding.ActivityRegisterDonorBinding;
import ma.ensaf.veryempty.utils.Constants;
import ma.ensaf.veryempty.utils.PreferenceManager;

public class ActivityRegisterDonor extends BaseActivity {

    private static final String TAG = ActivityRegisterDonor.class.getSimpleName();

    ActivityRegisterDonorBinding binding;
    private PreferenceManager preferenceManager;

    private View parent_view;
    boolean maleGenderPressed = false;
    boolean femaleGenderPressed = false;
    String selectedBloodType = null;
    String selectedSex = null;

    public static void start(Context context) {
        Intent intent = new Intent(context, ActivityRegisterDonor.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @SuppressLint({"CheckResult", "SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register_donor);
        parent_view = findViewById(android.R.id.content);
        preferenceManager = new PreferenceManager(getApplicationContext());
        initToolbar(binding.toolbar,true);
        setToolbarTitle(null);

        if(preferenceManager.getString(Constants.KEY_PHONE) != null) {
            binding.inputPhone.setText(preferenceManager.getString(Constants.KEY_PHONE));
        }
        binding.inputEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));

        // on click male gender
        binding.genderMaleImageView.setOnClickListener(v -> {
            // toggle button
            toggleGenderButtonStates(true);
        });

        // on click female gender
        binding.genderFemaleImageView.setOnClickListener(v -> {
            // toggle button
            toggleGenderButtonStates(false);
        });

        // listener for blood group radio groups
        binding.bloodGroupRadioGroup1.setOnCheckedChangeListener(listener);
        binding.bloodGroupRadioGroup2.setOnCheckedChangeListener(listener);
        binding.bloodGroupRadioGroup3.setOnCheckedChangeListener(listener);
        binding.bloodGroupRadioGroup4.setOnCheckedChangeListener(listener);
        binding.bloodGroupRadioGroup5.setOnCheckedChangeListener(listener);
        binding.bloodGroupRadioGroup6.setOnCheckedChangeListener(listener);
        binding.bloodGroupRadioGroup7.setOnCheckedChangeListener(listener);
        binding.bloodGroupRadioGroup8.setOnCheckedChangeListener(listener);

        // show next screen
        binding.buttonRegisterDonor.setOnClickListener(v -> {
            if(isformDetailsValid()) {
                AddDonation();

            }
        });
//        Tools.systemBarLollipopTransparent(this);
    }

    private boolean isformDetailsValid() {
        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("enter email",false);
            return false;
        } else if (binding.inputPhone.getText().toString().trim().isEmpty()) {
            showToast("enter phone",false);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("invalid email",false);
            return false;
        } else if (!Patterns.PHONE.matcher(binding.inputPhone.getText().toString()).matches()) {
            showToast("invalid phone number",false);

            return false;
        } else if (!binding.conditionsCheckbox.isChecked()) {
            showToast("please accept the terms and conditions to donate",false);
            return false;
        } else if (selectedBloodType == null || selectedSex == null) {
            showToast("select gender and blood type",false);
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading)  {
        if(isLoading) {
            binding.buttonRegisterDonor.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonRegisterDonor.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void AddDonation() {

        //TODO: check if donor donated in the last 3 months or smh then reject
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReferenceUser=database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> donation = new HashMap<>();
        donation.put(Constants.KEY_DONOR_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        donation.put(Constants.KEY_DONATION_CONTACT_EMAIL, binding.inputEmail.getText().toString());
        donation.put(Constants.KEY_DONATION_CONTACT_PHONE, binding.inputPhone.getText().toString());
        donation.put(Constants.KEY_BLOOD_TYPE, selectedBloodType);
        donation.put(Constants.KEY_GENDER, selectedSex);
        donation.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
        donation.put(Constants.KEY_CITY, preferenceManager.getString(Constants.KEY_CITY));
        donation.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        Date dt = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dtString = dateFormat.format(dt);
        donation.put(Constants.KEY_DONATION_DATETIME, dtString);
        database.collection(Constants.KEY_COLLECTION_DONATIONS)
                .add(donation)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    ///
                    documentReferenceUser.update(Constants.KEY_IS_DONOR,true,
                            Constants.KEY_COUNT_DONATIONS,preferenceManager.getInt(Constants.KEY_COUNT_DONATIONS)+1)
                            .addOnSuccessListener(unused -> {}
                                )
                            .addOnFailureListener(e -> {
                                showToast("Unable to update user info",false);
                            });

                    ///
                    preferenceManager.putString(Constants.KEY_IS_DONOR,"true");
                    preferenceManager.putString(Constants.KEY_GENDER, selectedSex);
                    preferenceManager.putString(Constants.KEY_BLOOD_TYPE, selectedBloodType);
                    preferenceManager.putInt(Constants.KEY_COUNT_DONATIONS,preferenceManager.getInt(Constants.KEY_COUNT_DONATIONS)+1);
                    preferenceManager.putString(Constants.KEY_DONATION_DATETIME,dtString );
                    Intent intent = new Intent(getApplicationContext(),ActivityPersProfile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);


                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage(),false);
                });



    }

    private void toggleGenderButtonStates(boolean isMale) {
        if(isMale){
            selectedSex = "male";
            // set male views
            binding.genderMaleImageView.setImageState(new int[] {android.R.attr.state_pressed},true);
            binding.genderMaleImageView.setColorFilter(ContextCompat.getColor(activityContext, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.genderMaleTextView.setTextColor(ContextCompat.getColor(activityContext,R.color.be_hero_dark));

            // reset female views
            binding.genderFemaleImageView.setImageState(new int[] {-android.R.attr.state_pressed},true);
            binding.genderFemaleImageView.setColorFilter(ContextCompat.getColor(activityContext, R.color.be_hero_dark_grey), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.genderFemaleTextView.setTextColor(ContextCompat.getColor(activityContext,R.color.be_hero_dark_grey));
        }else{
            selectedSex = "female";
            // reset male views
            binding.genderMaleImageView.setImageState(new int[] {-android.R.attr.state_pressed},true);
            binding.genderMaleImageView.setColorFilter(ContextCompat.getColor(activityContext, R.color.be_hero_dark_grey), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.genderMaleTextView.setTextColor(ContextCompat.getColor(activityContext,R.color.be_hero_dark_grey));
            // set female views
            binding.genderFemaleImageView.setImageState(new int[] {android.R.attr.state_pressed},true);
            binding.genderFemaleImageView.setColorFilter(ContextCompat.getColor(activityContext, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            binding.genderFemaleTextView.setTextColor(ContextCompat.getColor(activityContext,R.color.be_hero_dark));
        }
    }

    private RadioGroup.OnCheckedChangeListener listener = (group, checkedId) -> {
        switch (group.getId()){
            case R.id.blood_group_radio_group_1:
                selectedBloodType ="A+";
                resetRadios(group.getId(),R.id.blood_group_radio_group_2,R.id.blood_group_radio_group_3,R.id.blood_group_radio_group_4,R.id.blood_group_radio_group_5,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_8);
                break;
            case R.id.blood_group_radio_group_2:
                selectedBloodType ="A-";
                resetRadios(group.getId(),R.id.blood_group_radio_group_1,R.id.blood_group_radio_group_3,R.id.blood_group_radio_group_4,R.id.blood_group_radio_group_5,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_8);
                break;
            case R.id.blood_group_radio_group_3:
                selectedBloodType ="B+";
                resetRadios(group.getId(),R.id.blood_group_radio_group_1,R.id.blood_group_radio_group_2,R.id.blood_group_radio_group_4,R.id.blood_group_radio_group_5,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_8);
                break;
            case R.id.blood_group_radio_group_4:
                selectedBloodType ="B-";
                resetRadios(group.getId(),R.id.blood_group_radio_group_1,R.id.blood_group_radio_group_2,R.id.blood_group_radio_group_3,R.id.blood_group_radio_group_5,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_8);
                break;
            case R.id.blood_group_radio_group_5:
                selectedBloodType ="O+";
                resetRadios(group.getId(),R.id.blood_group_radio_group_1,R.id.blood_group_radio_group_2,R.id.blood_group_radio_group_3,R.id.blood_group_radio_group_4,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_8);
                break;
            case R.id.blood_group_radio_group_6:
                selectedBloodType ="O-";
                resetRadios(group.getId(),R.id.blood_group_radio_group_1,R.id.blood_group_radio_group_2,R.id.blood_group_radio_group_3,R.id.blood_group_radio_group_4,R.id.blood_group_radio_group_5,R.id.blood_group_radio_group_7,R.id.blood_group_radio_group_8);
                break;
            case R.id.blood_group_radio_group_7:
                selectedBloodType ="AB+";
                resetRadios(group.getId(),R.id.blood_group_radio_group_1,R.id.blood_group_radio_group_2,R.id.blood_group_radio_group_3,R.id.blood_group_radio_group_4,R.id.blood_group_radio_group_5,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_8);
                break;
            case R.id.blood_group_radio_group_8:
                selectedBloodType ="AB-";
                resetRadios(group.getId(),R.id.blood_group_radio_group_1,R.id.blood_group_radio_group_2,R.id.blood_group_radio_group_3,R.id.blood_group_radio_group_4,R.id.blood_group_radio_group_5,R.id.blood_group_radio_group_6,R.id.blood_group_radio_group_7);
                break;
        }
    };

    private void resetRadios(int checkedId, int radio_group_1, int radio_group_2, int radio_group_3, int radio_group_4, int radio_group_5
            , int radio_group_6, int radio_group_7) {

        // remove the listeners before clearing so we don't throw an exception
        ((RadioGroup)findViewById(radio_group_1)).setOnCheckedChangeListener(null);
        ((RadioGroup)findViewById(radio_group_2)).setOnCheckedChangeListener(null);
        ((RadioGroup)findViewById(radio_group_3)).setOnCheckedChangeListener(null);
        ((RadioGroup)findViewById(radio_group_4)).setOnCheckedChangeListener(null);
        ((RadioGroup)findViewById(radio_group_5)).setOnCheckedChangeListener(null);
        ((RadioGroup)findViewById(radio_group_6)).setOnCheckedChangeListener(null);
        ((RadioGroup)findViewById(radio_group_7)).setOnCheckedChangeListener(null);
        // clear all the other radion groups
        ((RadioGroup)findViewById(radio_group_1)).clearCheck();
        ((RadioGroup)findViewById(radio_group_2)).clearCheck();
        ((RadioGroup)findViewById(radio_group_3)).clearCheck();
        ((RadioGroup)findViewById(radio_group_4)).clearCheck();
        ((RadioGroup)findViewById(radio_group_5)).clearCheck();
        ((RadioGroup)findViewById(radio_group_6)).clearCheck();
        ((RadioGroup)findViewById(radio_group_7)).clearCheck();
        //reset the listeners
        ((RadioGroup)findViewById(radio_group_1)).setOnCheckedChangeListener(listener);
        ((RadioGroup)findViewById(radio_group_2)).setOnCheckedChangeListener(listener);
        ((RadioGroup)findViewById(radio_group_3)).setOnCheckedChangeListener(listener);
        ((RadioGroup)findViewById(radio_group_4)).setOnCheckedChangeListener(listener);
        ((RadioGroup)findViewById(radio_group_5)).setOnCheckedChangeListener(listener);
        ((RadioGroup)findViewById(radio_group_6)).setOnCheckedChangeListener(listener);
        ((RadioGroup)findViewById(radio_group_7)).setOnCheckedChangeListener(listener);
    }


    //TODO: add this code to other activities bash ykhdm lbackpress btn i guess
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}

