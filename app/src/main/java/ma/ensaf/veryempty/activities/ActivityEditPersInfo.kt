package ma.ensaf.veryempty.activities


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Intent
import android.provider.MediaStore
import com.google.firebase.firestore.FirebaseFirestore

import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult

import android.content.Context
import android.util.Base64
import android.util.Patterns
import android.view.View
import androidx.activity.result.ActivityResult

import ma.ensaf.veryempty.databinding.ActivityEditPersInfoBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*

class ActivityEditPersInfo : BaseActivity() {
    private lateinit var binding: ActivityEditPersInfoBinding
    private lateinit var encodedImage: String
    private lateinit var preferenceManager: PreferenceManager
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

    @SuppressLint("CheckResult", "SetTextI18n", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_pers_info)
        preferenceManager = PreferenceManager(applicationContext)
        initToolbar(binding.toolbar, true)
        setToolbarTitle(null)
        //Lisetners
        initViews()
        setListeners()
        val citiesAdapter: ArrayAdapter<*> =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, cities)
        citiesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.citySpinner.adapter = citiesAdapter
        binding.citySpinner.setSelection(
            Arrays.asList(*cities).indexOf(
                preferenceManager.getString(Constants.KEY_CITY)
            ), false
        )
        binding.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    preferenceManager.putString(
                        Constants.KEY_CITY,
                        parent.getItemAtPosition(position).toString()
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initViews() {
        binding.nameInput.setText(preferenceManager.getString(Constants.KEY_NAME))
        binding.phoneInput.setText(preferenceManager.getString(Constants.KEY_PHONE))
        binding.emailInput.setText(preferenceManager.getString(Constants.KEY_EMAIL))
        binding.textAddImage.visibility = View.INVISIBLE
        val bytes =
            Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
        encodedImage = preferenceManager.getString(Constants.KEY_IMAGE)?: ""
    }

    private fun setListeners() {
        binding.UpdateBtn.setOnClickListener {
            if (isValidProfileDetails) {
                updateProfile()
            }
        }
        binding.imageProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun updateProfile() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)?: ""
        )
        documentReference.update(
            Constants.KEY_NAME, binding.nameInput.text.toString(),
            Constants.KEY_PHONE, binding.phoneInput.text.toString(),
            Constants.KEY_CITY, binding.citySpinner.selectedItem.toString(),
            Constants.KEY_EMAIL, binding.emailInput.text.toString(),
            Constants.KEY_IMAGE, encodedImage
        )
            .addOnSuccessListener { unused: Void? ->
                preferenceManager.putString(
                    Constants.KEY_NAME,
                    binding.nameInput.text.toString()
                )
                preferenceManager.putString(
                    Constants.KEY_PHONE,
                    binding.phoneInput.text.toString()
                )
                preferenceManager.putString(
                    Constants.KEY_CITY,
                    binding.citySpinner.selectedItem.toString()
                )
                preferenceManager.putString(
                    Constants.KEY_EMAIL,
                    binding.emailInput.text.toString()
                )
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage)
                loading(false)
                val intent = Intent(applicationContext, ActivityHome::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                loading(false)
                showToast("Unable to update profile", false)
            }
    }

    private val isValidProfileDetails: Boolean
         get() = if (binding.nameInput.text.toString().trim { it <= ' ' }.isEmpty()) {
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
            binding.UpdateBtn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.UpdateBtn.visibility = View.VISIBLE
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
            val intent = Intent(context, ActivityEditPersInfo::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}