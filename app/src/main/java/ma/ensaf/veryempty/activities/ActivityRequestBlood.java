package ma.ensaf.veryempty.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.databinding.ActivityRequestBloodBinding;
import ma.ensaf.veryempty.utils.Constants;
import ma.ensaf.veryempty.utils.PreferenceManager;


public class ActivityRequestBlood extends BaseActivity {

    private static final String TAG = ActivityRequestBlood.class.getSimpleName();

    private String selectedBloodType;
    private PreferenceManager preferenceManager;
    private String selectedPersonType;
    private String selectedCity;
    ActivityRequestBloodBinding binding;

    String[] persons = {"Select Person Type","Friend", "Family", "Relative", "Patient", "Work Colleague", "Anonymous"};

    String[] cities = {"Select City","Casablanca", "Fes", "Meknes", "Rabat", "Tangier", "Oujda","Marrkech","Tetuan","Laayoune"};

    private View parent_view;

    public static void start(Context context) {
        Intent intent = new Intent(context, ActivityRequestBlood.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @SuppressLint({"CheckResult", "SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_request_blood);
        parent_view = findViewById(android.R.id.content);
        preferenceManager = new PreferenceManager(getApplicationContext());

        initToolbar(binding.toolbar,true);
        setToolbarTitle(null);

        // requesting for spinner
        ArrayAdapter personsAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, persons);
        personsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.requestForSpinner.setAdapter(personsAdapter);
        binding.requestForSpinner.setSelection(0,false);
        binding.requestForSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    selectedPersonType =parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // cities spinner
        ArrayAdapter citiesAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, cities);
        citiesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.citySpinner.setAdapter(citiesAdapter);
        binding.citySpinner.setSelection(0,false);
        binding.citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    selectedCity = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
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
        binding.buttonRequestBlood.setOnClickListener(v -> {
            submitRequest();
        });
    }
    private void loading(Boolean isLoading)  {
        if(isLoading) {
            binding.buttonRequestBlood.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonRequestBlood.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void submitRequest() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReferenceUser=database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> request = new HashMap<>();
        request.put(Constants.KEY_REQUESTER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        request.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
        request.put(Constants.KEY_PHONE,  preferenceManager.getString(Constants.KEY_PHONE));
        request.put(Constants.KEY_BLOOD_TYPE, selectedBloodType);
        request.put(Constants.KEY_REQ_PERSON_TYPE, selectedPersonType);
        request.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
        request.put(Constants.KEY_CITY, selectedCity);
        request.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        Date dt = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dtString = dateFormat.format(dt);
        request.put(Constants.KEY_REQUEST_DATETIME, dtString);
        database.collection(Constants.KEY_COLLECTION_REQUESTS)
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    ///
                    documentReferenceUser.update(Constants.KEY_IS_REQUESTER,true,
                            Constants.KEY_COUNT_REQUESTS,preferenceManager.getInt(Constants.KEY_COUNT_REQUESTS)+1)
                            .addOnSuccessListener(unused -> {}
                            )
                            .addOnFailureListener(e -> {
                                showToast("Unable to update user info for request",false);
                            });

                    ///
                    preferenceManager.putString(Constants.KEY_IS_REQUESTER,"true");
                    preferenceManager.putInt(Constants.KEY_COUNT_REQUESTS,preferenceManager.getInt(Constants.KEY_COUNT_REQUESTS)+1);
                    preferenceManager.putString(Constants.KEY_REQUEST_DATETIME,dtString );
                    Intent intent = new Intent(getApplicationContext(),ActivityRequests.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);


                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage(),false);
                });

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
