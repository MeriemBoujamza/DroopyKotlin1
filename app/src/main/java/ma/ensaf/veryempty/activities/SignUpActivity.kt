package ma.ensaf.veryempty.activities


import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import android.content.Intent
import android.provider.MediaStore
import com.google.firebase.firestore.FirebaseFirestore

import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Patterns
import android.view.View
import androidx.activity.result.ActivityResult
import com.google.firebase.firestore.DocumentReference
import ma.ensaf.veryempty.databinding.ActivitySignUpBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private var encodedImage: String? = null
    private var preferenceManager: PreferenceManager? = null
    private var cities = arrayOf(
        "Select City",
        "Casablanca",
        "Fes",
        "Meknes",
        "Rabat",
        "Tangier",
        "Oujda",
        "Marrakech",
        "Tetuan",
        "Laayoune"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        preferenceManager = PreferenceManager(applicationContext)
        //setting the toolbar
        initToolbar(binding.toolbar, true)
        setToolbarTitle(null)

        //Lisetners
        setListeners()
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
                    Toast.makeText(
                        activityContext,
                        parent.getItemAtPosition(position).toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setListeners() {
        binding.registerLink.setOnClickListener { SignInActivity.start(activityContext) }
        binding.signUpBtn.setOnClickListener {
            if (isValidSignUpDetails) {
                signUp()
            }
        }
        binding.imageProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun signUp() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user = HashMap<String, Any?>()
        user[Constants.KEY_NAME] = binding.nameInput.text.toString()
        user[Constants.KEY_EMAIL] = binding.emailInput.text.toString()
        user[Constants.KEY_PASSWORD] = binding.passwordInput.text.toString()
        user[Constants.KEY_PHONE] = binding.phoneInput.text.toString()
        user[Constants.KEY_CITY] = binding.citySpinner.selectedItem.toString()
        user[Constants.KEY_IS_REQUESTER] = false
        user[Constants.KEY_IS_DONOR] = false
        user[Constants.KEY_COUNT_DONATIONS] = 0
        user[Constants.KEY_COUNT_REQUESTS] = 0
        user[Constants.LAST_DONATION_DATE] = "none"
        user[Constants.KEY_IMAGE] = encodedImage
        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener { documentReference: DocumentReference ->
                loading(false)
                preferenceManager!!.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager!!.putString(Constants.KEY_USER_ID, documentReference.id)
                preferenceManager!!.putString(
                    Constants.KEY_NAME,
                    binding.nameInput.text.toString()
                )
                preferenceManager!!.putString(
                    Constants.KEY_EMAIL,
                    binding.emailInput.text.toString()
                )
                preferenceManager!!.putString(
                    Constants.KEY_PHONE,
                    binding.phoneInput.text.toString()
                )
                preferenceManager!!.putString(
                    Constants.KEY_CITY,
                    binding.citySpinner.selectedItem.toString()
                )
                preferenceManager!!.putString(Constants.KEY_IMAGE, encodedImage)?: ""
                preferenceManager!!.putBoolean(Constants.KEY_IS_DONOR, false)
                preferenceManager!!.putBoolean(Constants.KEY_IS_REQUESTER, false)
                preferenceManager!!.putInt(Constants.KEY_COUNT_DONATIONS, 0)
                preferenceManager!!.putInt(Constants.KEY_COUNT_REQUESTS, 0)
                preferenceManager!!.putString(Constants.KEY_DONATION_DATETIME, "none")
                val intent = Intent(applicationContext, ActivityHome::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { exception: Exception ->
                loading(false)
                showToast(exception.message, false)
            }
    }

    private val isValidSignUpDetails: Boolean
         get() = if (encodedImage == null) {
            showToast("select an image", false)
            false
        } else if (binding.nameInput.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("enter name", false)
            false
        } else if (binding.emailInput.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("enter email", false)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailInput.text.toString())
                .matches()
        ) {
            showToast("enter valid email", false)
            false
        } else if (binding.passwordInput.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("enter password", false)
            false
        } else if (binding.phoneInput.text.toString().trim { it <= ' ' }.isEmpty() ||
            !Patterns.PHONE.matcher(binding.phoneInput.text.toString()).matches()
        ) {
            showToast("enter valid phone number", false)
            false
        } else if (binding.citySpinner.selectedItemPosition == 0) {
            showToast("select a city", false)
            false
        } else {
            true
        }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.signUpBtn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.signUpBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun encodedImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    //Getting a result from an activity (camera or gallery app in our example)
    private val pickImage = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val imageUri = result.data!!.data
                try {
                    val inputStream = contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageProfile.setImageBitmap(bitmap)
                    binding.textAddImage.visibility = View.GONE
                    encodedImage = encodedImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SignUpActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}