package ma.ensaf.veryempty.activities

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import ma.ensaf.veryempty.utils.Constants
import java.util.*

@Suppress("DEPRECATION")
class GoogleAuthActivity : ActivityRegister() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("886575840000-dau1549iq492l679esc4pmd41f3clqgf.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mAuth = FirebaseAuth.getInstance()
        mAuth.currentUser?.let {
            mUser = mAuth.currentUser!!
        } ?: run {
        }

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult<ApiException>(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                showToast("Google Sign In failed", false)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
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
                                    )?: ""
                                )
                                preferenceManager.putString(
                                    Constants.KEY_PHONE, documentSnapshot.getString(
                                        Constants.KEY_PHONE
                                    )?: ""
                                )
                                preferenceManager.putString(
                                    Constants.KEY_CITY, documentSnapshot.getString(
                                        Constants.KEY_CITY
                                    )?: ""
                                )
                                preferenceManager.putString(
                                    Constants.KEY_IMAGE, documentSnapshot.getString(
                                        Constants.KEY_IMAGE
                                    )?: ""
                                )
                                preferenceManager.putString(
                                    Constants.KEY_EMAIL, documentSnapshot.getString(
                                        Constants.KEY_EMAIL
                                    )?: ""
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
                                user.put(Constants.KEY_PASSWORD, "GoogleUser")
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
                                    .addOnFailureListener { exception: Exception ->
                                        showToast(
                                            exception.message,
                                            false
                                        )
                                    }
                            }
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    showToast("signInWithCredential:failure", false)
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 101
    }
}