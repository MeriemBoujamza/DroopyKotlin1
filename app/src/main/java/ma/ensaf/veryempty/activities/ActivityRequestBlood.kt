package ma.ensaf.veryempty.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.google.firebase.firestore.FirebaseFirestore

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import com.google.firebase.firestore.DocumentReference
import ma.ensaf.veryempty.databinding.ActivityRequestBloodBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class ActivityRequestBlood : BaseActivity() {
    private var selectedBloodType: String? = null
    private var preferenceManager: PreferenceManager? = null
    private var selectedPersonType: String? = null
    private var selectedCity: String? = null
    lateinit  var binding: ActivityRequestBloodBinding
    private var persons = arrayOf(
        "Select Person Type",
        "Friend",
        "Family",
        "Relative",
        "Patient",
        "Work Colleague",
        "Anonymous"
    )
    private var cities = arrayOf(
        "Select City",
        "Casablanca",
        "Fes",
        "Meknes",
        "Rabat",
        "Tangier",
        "Oujda",
        "Marrkech",
        "Tetuan",
        "Laayoune"
    )
    private var parent_view: View? = null
    @SuppressLint("CheckResult", "SetTextI18n", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_request_blood)
        parent_view = findViewById(android.R.id.content)
        preferenceManager = PreferenceManager(applicationContext)
        initToolbar(binding.toolbar, true)
        setToolbarTitle(null)

        // requesting for spinner
        val personsAdapter: ArrayAdapter<*> =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, persons)
        personsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.requestForSpinner.adapter = personsAdapter
        binding.requestForSpinner.setSelection(0, false)
        binding.requestForSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (position != 0) {
                        selectedPersonType = parent.getItemAtPosition(position).toString()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // cities spinner
        val citiesAdapter: ArrayAdapter<*> =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, cities)
        citiesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.citySpinner.adapter = citiesAdapter
        binding.citySpinner.setSelection(0, false)
        binding.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    selectedCity = parent.getItemAtPosition(position).toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // listener for blood group radio groups
        binding.bloodGroupRadioGroup1.setOnCheckedChangeListener(listener)
        binding.bloodGroupRadioGroup2.setOnCheckedChangeListener(listener)
        binding.bloodGroupRadioGroup3.setOnCheckedChangeListener(listener)
        binding.bloodGroupRadioGroup4.setOnCheckedChangeListener(listener)
        binding.bloodGroupRadioGroup5.setOnCheckedChangeListener(listener)
        binding.bloodGroupRadioGroup6.setOnCheckedChangeListener(listener)
        binding.bloodGroupRadioGroup7.setOnCheckedChangeListener(listener)
        binding.bloodGroupRadioGroup8.setOnCheckedChangeListener(listener)

        // show next screen
        binding.buttonRequestBlood.setOnClickListener { v: View? -> submitRequest() }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonRequestBlood.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.buttonRequestBlood.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun submitRequest() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val documentReferenceUser = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager!!.getString(Constants.KEY_USER_ID)?: ""
        )
        val request = HashMap<String, Any?>()
        request.put(
            Constants.KEY_REQUESTER_ID,
            preferenceManager!!.getString(Constants.KEY_USER_ID)
        )
        request.put(Constants.KEY_EMAIL, preferenceManager!!.getString(Constants.KEY_EMAIL))
        request.put(Constants.KEY_PHONE, preferenceManager!!.getString(Constants.KEY_PHONE))
        request.put(Constants.KEY_BLOOD_TYPE, selectedBloodType)
        request.put(Constants.KEY_REQ_PERSON_TYPE, selectedPersonType)
        request.put(Constants.KEY_NAME, preferenceManager!!.getString(Constants.KEY_NAME))
        request.put(Constants.KEY_CITY, selectedCity)
        request.put(Constants.KEY_IMAGE, preferenceManager!!.getString(Constants.KEY_IMAGE))
        val dt = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
        val dtString = dateFormat.format(dt)
        request.put(Constants.KEY_REQUEST_DATETIME, dtString)
        database.collection(Constants.KEY_COLLECTION_REQUESTS)
            .add(request)
            .addOnSuccessListener { documentReference: DocumentReference? ->
                loading(false)
                ///
                documentReferenceUser.update(
                    Constants.KEY_IS_REQUESTER,
                    true,
                    Constants.KEY_COUNT_REQUESTS,
                    preferenceManager!!.getInt(Constants.KEY_COUNT_REQUESTS) + 1
                )
                    .addOnSuccessListener { unused: Void? -> }
                    .addOnFailureListener {
                        showToast(
                            "Unable to update user info for request",
                            false
                        )
                    }

                ///
                preferenceManager!!.putString(Constants.KEY_IS_REQUESTER, "true")
                preferenceManager!!.putInt(
                    Constants.KEY_COUNT_REQUESTS, preferenceManager!!.getInt(
                        Constants.KEY_COUNT_REQUESTS
                    ) + 1
                )
                preferenceManager!!.putString(Constants.KEY_REQUEST_DATETIME, dtString)
                val intent = Intent(applicationContext, ActivityRequests::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { exception: Exception ->
                loading(false)
                showToast(exception.message, false)
            }
    }

    @SuppressLint("NonConstantResourceId")
    private val listener = RadioGroup.OnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
        when (group.id) {
            R.id.blood_group_radio_group_1 -> {
                selectedBloodType = "A+"
                resetRadios(
                    group.id,
                    R.id.blood_group_radio_group_2,
                    R.id.blood_group_radio_group_3,
                    R.id.blood_group_radio_group_4,
                    R.id.blood_group_radio_group_5,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_8
                )
            }
            R.id.blood_group_radio_group_2 -> {
                selectedBloodType = "A-"
                resetRadios(
                    group.id,
                    R.id.blood_group_radio_group_1,
                    R.id.blood_group_radio_group_3,
                    R.id.blood_group_radio_group_4,
                    R.id.blood_group_radio_group_5,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_8
                )
            }
            R.id.blood_group_radio_group_3 -> {
                selectedBloodType = "B+"
                resetRadios(
                    group.id,
                    R.id.blood_group_radio_group_1,
                    R.id.blood_group_radio_group_2,
                    R.id.blood_group_radio_group_4,
                    R.id.blood_group_radio_group_5,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_8
                )
            }
            R.id.blood_group_radio_group_4 -> {
                selectedBloodType = "B-"
                resetRadios(
                    group.id,
                    R.id.blood_group_radio_group_1,
                    R.id.blood_group_radio_group_2,
                    R.id.blood_group_radio_group_3,
                    R.id.blood_group_radio_group_5,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_8
                )
            }
            R.id.blood_group_radio_group_5 -> {
                selectedBloodType = "O+"
                resetRadios(
                    group.id,
                    R.id.blood_group_radio_group_1,
                    R.id.blood_group_radio_group_2,
                    R.id.blood_group_radio_group_3,
                    R.id.blood_group_radio_group_4,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_8
                )
            }
            R.id.blood_group_radio_group_6 -> {
                selectedBloodType = "O-"
                resetRadios(
                    group.id,
                    R.id.blood_group_radio_group_1,
                    R.id.blood_group_radio_group_2,
                    R.id.blood_group_radio_group_3,
                    R.id.blood_group_radio_group_4,
                    R.id.blood_group_radio_group_5,
                    R.id.blood_group_radio_group_7,
                    R.id.blood_group_radio_group_8
                )
            }
            R.id.blood_group_radio_group_7 -> {
                selectedBloodType = "AB+"
                resetRadios(
                    group.id,
                    R.id.blood_group_radio_group_1,
                    R.id.blood_group_radio_group_2,
                    R.id.blood_group_radio_group_3,
                    R.id.blood_group_radio_group_4,
                    R.id.blood_group_radio_group_5,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_8
                )
            }
            R.id.blood_group_radio_group_8 -> {
                selectedBloodType = "AB-"
                resetRadios(
                    group.id,
                    R.id.blood_group_radio_group_1,
                    R.id.blood_group_radio_group_2,
                    R.id.blood_group_radio_group_3,
                    R.id.blood_group_radio_group_4,
                    R.id.blood_group_radio_group_5,
                    R.id.blood_group_radio_group_6,
                    R.id.blood_group_radio_group_7
                )
            }
        }
    }

    private fun resetRadios(
        checkedId: Int,
        radio_group_1: Int,
        radio_group_2: Int,
        radio_group_3: Int,
        radio_group_4: Int,
        radio_group_5: Int,
        radio_group_6: Int,
        radio_group_7: Int
    ) {

        // remove the listeners before clearing so we don't throw an exception
        (findViewById<View>(radio_group_1) as RadioGroup).setOnCheckedChangeListener(null)
        (findViewById<View>(radio_group_2) as RadioGroup).setOnCheckedChangeListener(null)
        (findViewById<View>(radio_group_3) as RadioGroup).setOnCheckedChangeListener(null)
        (findViewById<View>(radio_group_4) as RadioGroup).setOnCheckedChangeListener(null)
        (findViewById<View>(radio_group_5) as RadioGroup).setOnCheckedChangeListener(null)
        (findViewById<View>(radio_group_6) as RadioGroup).setOnCheckedChangeListener(null)
        (findViewById<View>(radio_group_7) as RadioGroup).setOnCheckedChangeListener(null)
        // clear all the other radion groups
        (findViewById<View>(radio_group_1) as RadioGroup).clearCheck()
        (findViewById<View>(radio_group_2) as RadioGroup).clearCheck()
        (findViewById<View>(radio_group_3) as RadioGroup).clearCheck()
        (findViewById<View>(radio_group_4) as RadioGroup).clearCheck()
        (findViewById<View>(radio_group_5) as RadioGroup).clearCheck()
        (findViewById<View>(radio_group_6) as RadioGroup).clearCheck()
        (findViewById<View>(radio_group_7) as RadioGroup).clearCheck()
        //reset the listeners
        (findViewById<View>(radio_group_1) as RadioGroup).setOnCheckedChangeListener(listener)
        (findViewById<View>(radio_group_2) as RadioGroup).setOnCheckedChangeListener(listener)
        (findViewById<View>(radio_group_3) as RadioGroup).setOnCheckedChangeListener(listener)
        (findViewById<View>(radio_group_4) as RadioGroup).setOnCheckedChangeListener(listener)
        (findViewById<View>(radio_group_5) as RadioGroup).setOnCheckedChangeListener(listener)
        (findViewById<View>(radio_group_6) as RadioGroup).setOnCheckedChangeListener(listener)
        (findViewById<View>(radio_group_7) as RadioGroup).setOnCheckedChangeListener(listener)
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
            val intent = Intent(context, ActivityRequestBlood::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}