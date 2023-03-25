package ma.ensaf.veryempty.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.graphics.PorterDuff
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import android.widget.RadioGroup
import com.google.firebase.firestore.DocumentReference
import ma.ensaf.veryempty.databinding.ActivityRegisterDonorBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class ActivityRegisterDonor : BaseActivity() {
    lateinit  var binding: ActivityRegisterDonorBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var parent_view: View
    private var selectedBloodType: String? = null
    private var selectedSex: String? = null
    @SuppressLint("CheckResult", "SetTextI18n", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register_donor)
        parent_view = findViewById(android.R.id.content)
        preferenceManager = PreferenceManager(applicationContext)
        initToolbar(binding.toolbar, true)
        setToolbarTitle(null)
        if (preferenceManager.getString(Constants.KEY_PHONE) != null) {
            binding.inputPhone.setText(preferenceManager.getString(Constants.KEY_PHONE))
        }
        binding.inputEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL))

        // on click male gender
        binding.genderMaleImageView.setOnClickListener {
            // toggle button
            toggleGenderButtonStates(true)
        }

        // on click female gender
        binding.genderFemaleImageView.setOnClickListener {
            // toggle button
            toggleGenderButtonStates(false)
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
        binding.buttonRegisterDonor.setOnClickListener {
            if (isformDetailsValid()) {
                addDonation()
            }
        }
        //        Tools.systemBarLollipopTransparent(this);
    }

    private fun isformDetailsValid(): Boolean {
        return if (binding.inputEmail.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("enter email", false)
            false
        } else if (binding.inputPhone.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("enter phone", false)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString())
                .matches()
        ) {
            showToast("invalid email", false)
            false
        } else if (!Patterns.PHONE.matcher(binding.inputPhone.text.toString())
                .matches()
        ) {
            showToast("invalid phone number", false)
            false
        } else if (!binding.conditionsCheckbox.isChecked) {
            showToast("please accept the terms and conditions to donate", false)
            false
        } else if (selectedBloodType == null || selectedSex == null) {
            showToast("select gender and blood type", false)
            false
        } else {
            true
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonRegisterDonor.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.buttonRegisterDonor.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun addDonation() {

        //TODO: check if donor donated in the last 3 months or smh then reject
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val documentReferenceUser = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)?: ""
        )
        val donation = HashMap<String, Any?>()
        donation.put(Constants.KEY_DONOR_ID, preferenceManager.getString(Constants.KEY_USER_ID))
        donation.put(Constants.KEY_DONATION_CONTACT_EMAIL, binding.inputEmail.text.toString())
        donation.put(Constants.KEY_DONATION_CONTACT_PHONE, binding.inputPhone.text.toString())
        donation.put(Constants.KEY_BLOOD_TYPE, selectedBloodType)
        donation.put(Constants.KEY_GENDER, selectedSex)
        donation.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
        donation.put(Constants.KEY_CITY, preferenceManager.getString(Constants.KEY_CITY))
        donation.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE))
        val dt = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
        val dtString = dateFormat.format(dt)
        donation.put(Constants.KEY_DONATION_DATETIME, dtString)
        database.collection(Constants.KEY_COLLECTION_DONATIONS)
            .add(donation)
            .addOnSuccessListener { documentReference: DocumentReference? ->
                loading(false)
                ///
                documentReferenceUser.update(
                    Constants.KEY_IS_DONOR,
                    true,
                    Constants.KEY_COUNT_DONATIONS,
                    preferenceManager.getInt(Constants.KEY_COUNT_DONATIONS) + 1
                )
                    .addOnSuccessListener { unused: Void? -> }
                    .addOnFailureListener {
                        showToast(
                            "Unable to update user info",
                            false
                        )
                    }

                ///
                preferenceManager.putString(Constants.KEY_IS_DONOR, "true")
                preferenceManager.putString(Constants.KEY_GENDER, selectedSex)
                preferenceManager.putString(Constants.KEY_BLOOD_TYPE, selectedBloodType)
                preferenceManager.putInt(
                    Constants.KEY_COUNT_DONATIONS, preferenceManager.getInt(
                        Constants.KEY_COUNT_DONATIONS
                    ) + 1
                )
                preferenceManager.putString(Constants.KEY_DONATION_DATETIME, dtString)
                val intent = Intent(applicationContext, ActivityPersProfile::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { exception: Exception ->
                loading(false)
                showToast(exception.message, false)
            }
    }

    private fun toggleGenderButtonStates(isMale: Boolean) {
        if (isMale) {
            selectedSex = "male"
            // set male views
            binding.genderMaleImageView.setImageState(
                intArrayOf(android.R.attr.state_pressed),
                true
            )
            binding.genderMaleImageView.setColorFilter(
                ContextCompat.getColor(
                    activityContext,
                    R.color.white
                ), PorterDuff.Mode.MULTIPLY
            )
            binding.genderMaleTextView.setTextColor(
                ContextCompat.getColor(
                    activityContext,
                    R.color.be_hero_dark
                )
            )

            // reset female views
            binding.genderFemaleImageView.setImageState(
                intArrayOf(-android.R.attr.state_pressed),
                true
            )
            binding.genderFemaleImageView.setColorFilter(
                ContextCompat.getColor(
                    activityContext,
                    R.color.be_hero_dark_grey
                ), PorterDuff.Mode.MULTIPLY
            )
            binding.genderFemaleTextView.setTextColor(
                ContextCompat.getColor(
                    activityContext,
                    R.color.be_hero_dark_grey
                )
            )
        } else {
            selectedSex = "female"
            // reset male views
            binding.genderMaleImageView.setImageState(
                intArrayOf(-android.R.attr.state_pressed),
                true
            )
            binding.genderMaleImageView.setColorFilter(
                ContextCompat.getColor(
                    activityContext,
                    R.color.be_hero_dark_grey
                ), PorterDuff.Mode.MULTIPLY
            )
            binding.genderMaleTextView.setTextColor(
                ContextCompat.getColor(
                    activityContext,
                    R.color.be_hero_dark_grey
                )
            )
            // set female views
            binding.genderFemaleImageView.setImageState(
                intArrayOf(android.R.attr.state_pressed),
                true
            )
            binding.genderFemaleImageView.setColorFilter(
                ContextCompat.getColor(
                    activityContext,
                    R.color.white
                ), PorterDuff.Mode.MULTIPLY
            )
            binding.genderFemaleTextView.setTextColor(
                ContextCompat.getColor(
                    activityContext,
                    R.color.be_hero_dark
                )
            )
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

    //TODO: add this code to other activities bash ykhdm lbackpress btn i guess
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
        @kotlin.jvm.JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, ActivityRegisterDonor::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}