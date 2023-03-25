package ma.ensaf.veryempty.activities


import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Intent
import android.provider.MediaStore
import com.google.firebase.firestore.FirebaseFirestore

import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult

import android.content.Context
import android.util.Base64
import android.view.View
import androidx.activity.result.ActivityResult
import com.google.firebase.firestore.DocumentReference
import ma.ensaf.veryempty.activities.ActivityNewPost
import ma.ensaf.veryempty.databinding.ActivityNewPostBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

class ActivityNewPost : BaseActivity() {
    private lateinit var binding: ActivityNewPostBinding
    private  var encodedImage: String? = null
    private lateinit var preferenceManager: PreferenceManager
    private var dtString: String? = null
    private lateinit var dt: Date
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_post)
        preferenceManager = PreferenceManager(applicationContext)
        //Lisetners
        setListeners()
        initViews()
    }

    private fun initViews() {
        binding.userNameTextView.text = preferenceManager.getString(Constants.KEY_NAME)
        dt = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
        dtString = dateFormat.format(dt)
        binding.postTimeTextView.text = dtString
        binding.userLocationTextView.text =
            preferenceManager.getString(Constants.KEY_CITY)
        val bytes =
            Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.userImageView.setImageBitmap(bitmap)
    }

    private fun setListeners() {
        binding.addImage.setOnClickListener { 
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
        binding.btnPost.setOnClickListener {  addPost() }
    }

    private fun addPost() {
        val database = FirebaseFirestore.getInstance()
        val post = HashMap<String, Any?>()
        post.put(Constants.KEY_POST_BODY, binding.etPostContent.text.toString())
        post.put(Constants.KEY_POST_DATE, dtString)
        post.put(Constants.KEY_POST_LOCATION, preferenceManager.getString(Constants.KEY_CITY))
        post.put(Constants.KEY_POSTER_NAME, preferenceManager.getString(Constants.KEY_NAME))
        post.put(Constants.KEY_POST_IMAGE, encodedImage)
        post.put(Constants.KEY_POSTER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
        post.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE))
        post.put(Constants.KEY_PHONE, preferenceManager.getString(Constants.KEY_PHONE))
        post.put(Constants.KEY_BLOOD_TYPE, "A+") //TODO: GET LEGIT BLOODTYPE
        database.collection(Constants.KEY_COLLECTION_POSTS)
            .add(post)
            .addOnSuccessListener { documentReference: DocumentReference? ->
                showToast("Post added successfully", false)
                val intent = Intent(applicationContext, ActivityHome::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { exception: Exception -> showToast(exception.message, false) }
    }

    private fun encodedImage(bitmap: Bitmap): String {
        val previewWidth = 400
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream)
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
                    binding.imageView.visibility = View.VISIBLE
                    binding.imageView.setImageBitmap(bitmap)
                    encodedImage = encodedImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, ActivityNewPost::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}