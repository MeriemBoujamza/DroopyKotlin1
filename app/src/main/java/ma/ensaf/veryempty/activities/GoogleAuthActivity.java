package ma.ensaf.veryempty.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.utils.Constants;

public class GoogleAuthActivity extends ActivityRegister {

    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN=101;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("886575840000-dau1549iq492l679esc4pmd41f3clqgf.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mAuth = FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                showToast(e.getMessage(),false);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .whereEqualTo(Constants.KEY_EMAIL,task.getResult().getUser().getEmail())
                                    .whereEqualTo(Constants.KEY_NAME,task.getResult().getUser().getDisplayName())
                                    .get()
                                    .addOnCompleteListener(fireTask -> {
                                        if(fireTask.isSuccessful() && fireTask.getResult() != null && fireTask.getResult().getDocuments().size() > 0) {
                                            DocumentSnapshot documentSnapshot = fireTask.getResult().getDocuments().get(0);
                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                            preferenceManager.putString(Constants.KEY_PHONE, documentSnapshot.getString(Constants.KEY_PHONE));
                                            preferenceManager.putString(Constants.KEY_CITY, documentSnapshot.getString(Constants.KEY_CITY));
                                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                                            Intent intent= new Intent(getApplicationContext(),ActivityHome.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            //if not found in database we add him
                                            HashMap<String, Object> user = new HashMap<>();
                                            user.put(Constants.KEY_NAME, task.getResult().getUser().getDisplayName());
                                            user.put(Constants.KEY_EMAIL, task.getResult().getUser().getEmail());
                                            user.put(Constants.KEY_PHONE, task.getResult().getUser().getPhoneNumber());
                                            user.put(Constants.KEY_PASSWORD,"GoogleUser");
                                            user.put(Constants.KEY_CITY, "unprovided"); //we can have him select the city later
                                            //preferenceManager.putString(Constants.KEY_IMAGE, task.getResult().getUser().getPhotoUrl().toString());
                                            user.put(Constants.KEY_IMAGE, task.getResult().getUser().getPhotoUrl().toString());
                                            database.collection(Constants.KEY_COLLECTION_USERS)
                                                    .add(user)
                                                    .addOnSuccessListener(documentReference -> {
                                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                                        preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                                                        preferenceManager.putString(Constants.KEY_NAME, task.getResult().getUser().getDisplayName());
                                                        preferenceManager.putString(Constants.KEY_PHONE, task.getResult().getUser().getPhoneNumber());
                                                        preferenceManager.putString(Constants.KEY_CITY, "unprovided");
                                                        preferenceManager.putString(Constants.KEY_EMAIL, task.getResult().getUser().getEmail());
                                                        preferenceManager.putString(Constants.KEY_IMAGE, task.getResult().getUser().getPhotoUrl().toString());
                                                        Intent intent = new Intent(getApplicationContext(),ActivityHome.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                    })
                                                    .addOnFailureListener(exception -> {
                                                        showToast(exception.getMessage(),false);
                                                    });
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            showToast("signInWithCredential:failure", false);
                        }
                    }
                });
    }

}