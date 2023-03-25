package ma.ensaf.veryempty.activities

import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import com.facebook.login.LoginManager
import com.facebook.FacebookCallback
import com.facebook.login.LoginResult
import com.facebook.FacebookException
import android.content.Intent
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import ma.ensaf.veryempty.utils.Constants
import java.util.*

class FacebookAuthActivity : ActivityRegister() {
    private lateinit  var callbackManager: CallbackManager
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        mAuth = FirebaseAuth.getInstance()
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    showToast("before handle is success", false)
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    // App code
                    showToast("cancel", false)
                }

                override fun onError(exception: FacebookException) {
                    // App code
                    showToast("error", false)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        showToast(credential.toString(), false)
        showToast("inside handle", false)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showToast("no", false)
                if (task.isSuccessful) {
                    showToast("is success", false)
                    val database = FirebaseFirestore.getInstance()
                    database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(
                            Constants.KEY_EMAIL, task.result.user!!
                                .email
                        )
                        .whereEqualTo(
                            Constants.KEY_NAME, task.result.user!!
                                .displayName
                        )
                        .get()
                        .addOnCompleteListener { fireTask: Task<QuerySnapshot?> ->
                            if (fireTask.isSuccessful && fireTask.result != null && fireTask.result!!
                                    .documents.size > 0
                            ) {
                                val documentSnapshot = fireTask.result!!.documents[0]
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                                preferenceManager.putString(
                                    Constants.KEY_USER_ID,
                                    documentSnapshot.id
                                )
                                preferenceManager.putString(
                                    Constants.KEY_NAME, documentSnapshot.getString(
                                        Constants.KEY_NAME
                                    )
                                )
                                preferenceManager.putString(
                                    Constants.KEY_PHONE, documentSnapshot.getString(
                                        Constants.KEY_PHONE
                                    )
                                )
                                preferenceManager.putString(
                                    Constants.KEY_CITY, documentSnapshot.getString(
                                        Constants.KEY_CITY
                                    )
                                )
                                preferenceManager.putString(
                                    Constants.KEY_IMAGE, documentSnapshot.getString(
                                        Constants.KEY_IMAGE
                                    )
                                )
                                preferenceManager.putString(
                                    Constants.KEY_EMAIL, documentSnapshot.getString(
                                        Constants.KEY_EMAIL
                                    )
                                )
                                val intent = Intent(applicationContext, ActivityHome::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            } else {
                                //if not found in database we add him
                                val user = HashMap<String, Any?>()
                                user.put(
                                    Constants.KEY_NAME, task.result.user!!
                                        .displayName
                                )
                                user.put(
                                    Constants.KEY_EMAIL, task.result.user!!
                                        .email
                                )
                                user.put(
                                    Constants.KEY_PHONE, task.result.user!!
                                        .phoneNumber
                                )
                                user.put(Constants.KEY_PASSWORD, "facebookUser")
                                user.put(
                                    Constants.KEY_CITY,
                                    "unprovided"
                                ) //we can have him select the city later
                                //preferenceManager.putString(Constants.KEY_IMAGE, task.getResult().getUser().getPhotoUrl().toString());
                                user.put(
                                    Constants.KEY_IMAGE, task.result.user!!
                                        .photoUrl.toString()
                                )
                                database.collection(Constants.KEY_COLLECTION_USERS)
                                    .add(user)
                                    .addOnSuccessListener { documentReference: DocumentReference ->
                                        preferenceManager.putBoolean(
                                            Constants.KEY_IS_SIGNED_IN, true
                                        )
                                        preferenceManager.putString(
                                            Constants.KEY_USER_ID,
                                            documentReference.id
                                        )
                                        preferenceManager.putString(
                                            Constants.KEY_NAME, task.result.user!!
                                                .displayName
                                        )
                                        preferenceManager.putString(
                                            Constants.KEY_PHONE, task.result.user!!
                                                .phoneNumber
                                        )
                                        preferenceManager.putString(
                                            Constants.KEY_CITY,
                                            "unprovided"
                                        )
                                        preferenceManager.putString(
                                            Constants.KEY_EMAIL, task.result.user!!
                                                .email
                                        )
                                        preferenceManager.putString(
                                            Constants.KEY_IMAGE, task.result.user!!
                                                .photoUrl.toString()
                                        )
                                        val intent =
                                            Intent(applicationContext, ActivityHome::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { _: Exception? ->
                                        showToast(
                                            "failed lol",
                                            false
                                        )
                                    }
                            }
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    showToast("sign in failed", false)
                }
            }
    }
}