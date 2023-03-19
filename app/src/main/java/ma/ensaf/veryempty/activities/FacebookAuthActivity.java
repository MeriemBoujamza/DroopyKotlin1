package ma.ensaf.veryempty.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;

import ma.ensaf.veryempty.utils.Constants;

public class FacebookAuthActivity extends ActivityRegister {

    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","email"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        showToast("before handle is success",false);
                        handleFacebookAccessToken(loginResult.getAccessToken());

                    }

                    @Override
                    public void onCancel() {
                        // App code
                        showToast("cancel",false);

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        showToast("error",false);

                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        showToast(credential.toString(),false);
        showToast("inside handle",false);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showToast("no",false);

                        if (task.isSuccessful()) {
                            showToast("is success",false);

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
                                            user.put(Constants.KEY_PASSWORD,"facebookUser");
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
                                                        showToast("failed lol",false);
                                                    });
                                        }
                                    });

                        } else {
                            // If sign in fails, display a message to the user.
                            showToast("sign in failed",false);
                        }
                    }
                });
    }


}