package ma.ensaf.veryempty.activities

import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import android.content.Intent
import android.util.Patterns
import android.view.View
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

import ma.ensaf.veryempty.databinding.ActivitySignInBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager

class SignInActivity : BaseActivity() {
    private var binding: ActivitySignInBinding? = null
    private var preferenceManager: PreferenceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        //////////SETTING THE PREFERENCEMANAGER
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    private fun setListeners() {
        binding!!.registerLink.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext, SignUpActivity::class.java
                )
            )
        }
        binding!!.signInButton.setOnClickListener {
            if (isValidSignInDetails) {
                signIn()
            }
        }
    }

    private val isValidSignInDetails: Boolean
        get() = if (binding!!.emailInput.text.toString().isEmpty()) {
            showToast("enter email", false)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding!!.emailInput.text.toString())
                .matches()
        ) {
            showToast("enter valid email", false)
            false
        } else if (binding!!.passwordInput.text.toString().isEmpty()) {
            showToast("enter password", false)
            false
        } else {
            true
        }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding!!.signInButton.visibility = View.INVISIBLE
            binding!!.progressBar.visibility = View.VISIBLE
        } else {
            binding!!.signInButton.visibility = View.VISIBLE
            binding!!.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun signIn() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding!!.emailInput.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding!!.passwordInput.text.toString())
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                if (task.isSuccessful && task.result != null && task.result!!
                        .documents.size > 0
                ) {
                    val documentSnapshot = task.result!!.documents[0]
                    preferenceManager!!.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager!!.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManager!!.putString(
                        Constants.KEY_NAME,
                        documentSnapshot.getString(Constants.KEY_NAME)?: ""
                    )
                    preferenceManager!!.putString(
                        Constants.KEY_PHONE,
                        documentSnapshot.getString(Constants.KEY_PHONE)?: ""
                    )
                    preferenceManager!!.putString(
                        Constants.KEY_CITY,
                        documentSnapshot.getString(Constants.KEY_CITY)?: ""
                    )
                    preferenceManager!!.putString(
                        Constants.KEY_EMAIL,
                        documentSnapshot.getString(Constants.KEY_EMAIL)?: ""
                    )
                    preferenceManager!!.putString(
                        Constants.KEY_IMAGE,
                        documentSnapshot.getString(Constants.KEY_IMAGE)?: ""
                    )
                    preferenceManager!!.putInt(
                        Constants.KEY_COUNT_DONATIONS,
                        documentSnapshot.getDouble(Constants.KEY_COUNT_DONATIONS)!!
                            .toInt()
                    )
                    preferenceManager!!.putInt(
                        Constants.KEY_COUNT_REQUESTS,
                        documentSnapshot.getDouble(Constants.KEY_COUNT_REQUESTS)!!
                            .toInt()
                    )
                    val intent = Intent(applicationContext, ActivityHome::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    loading(false)
                    showToast("Unable to sign in", false)
                }
            }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}