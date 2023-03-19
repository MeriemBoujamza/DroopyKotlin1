package ma.ensaf.veryempty.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.databinding.ActivityNewPostBinding;
import ma.ensaf.veryempty.databinding.ActivitySignUpBinding;
import ma.ensaf.veryempty.models.Users;
import ma.ensaf.veryempty.utils.Constants;
import ma.ensaf.veryempty.utils.PreferenceManager;

public class ActivityNewPost extends BaseActivity {

    private ActivityNewPostBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private String dtString;
    Date dt;

    public static void start(Context context) {
        Intent intent = new Intent(context, ActivityNewPost.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_post);
        preferenceManager = new PreferenceManager(getApplicationContext());
        //Lisetners
        setListeners();
        initViews();
    }

    private void initViews() {
        binding.userNameTextView.setText(preferenceManager.getString(Constants.KEY_NAME));
        dt = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dtString = dateFormat.format(dt);
        binding.postTimeTextView.setText(dtString);
        binding.userLocationTextView.setText(preferenceManager.getString(Constants.KEY_CITY));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.userImageView.setImageBitmap(bitmap);
    }

    private void setListeners() {
        binding.addImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.btnPost.setOnClickListener(v -> {
            addPost();
        });
    }

    private void addPost() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> post = new HashMap<>();
        post.put(Constants.KEY_POST_BODY, binding.etPostContent.getText().toString());

        post.put(Constants.KEY_POST_DATE, dtString);
        post.put(Constants.KEY_POST_LOCATION, preferenceManager.getString(Constants.KEY_CITY));
        post.put(Constants.KEY_POSTER_NAME, preferenceManager.getString(Constants.KEY_NAME));
        post.put(Constants.KEY_POST_IMAGE, encodedImage);
        post.put(Constants.KEY_POSTER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        post.put(Constants.KEY_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
        post.put(Constants.KEY_PHONE,preferenceManager.getString(Constants.KEY_PHONE));
        post.put(Constants.KEY_BLOOD_TYPE,"A+"); //TODO: GET LEGIT BLOODTYPE
        database.collection(Constants.KEY_COLLECTION_POSTS)
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    showToast("Post added successfully",false);
                    Intent intent = new Intent(getApplicationContext(),ActivityHome.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);


                })
                .addOnFailureListener(exception -> {
                    showToast(exception.getMessage(),false);
                });


    }

    private String encodedImage(Bitmap bitmap) {
        int previewWidth =400;
        int previewHeight =bitmap.getHeight() * previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG,90,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }
    //Getting a result from an activity (camera or gallery app in our example)
    private final ActivityResultLauncher<Intent> pickImage =  registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream= getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageView.setVisibility(View.VISIBLE);
                            binding.imageView.setImageBitmap(bitmap);
                            encodedImage = encodedImage(bitmap);
                        } catch(FileNotFoundException e) {
                            e.printStackTrace();

                        }
                    }
                }

            }
    );
}