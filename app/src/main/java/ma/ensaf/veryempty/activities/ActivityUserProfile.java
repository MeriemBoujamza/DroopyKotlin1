package ma.ensaf.veryempty.activities;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.adapters.PostsAdapter;
import ma.ensaf.veryempty.data.Constants;
import ma.ensaf.veryempty.databinding.ActivityUserProfileBinding;
import ma.ensaf.veryempty.models.CUsers;
import ma.ensaf.veryempty.models.Posts;
import ma.ensaf.veryempty.models.RowItem;
import ma.ensaf.veryempty.models.Users;

public class ActivityUserProfile extends BaseActivity {

    private static final String TAG = ActivityUserProfile.class.getSimpleName();

    ActivityUserProfileBinding binding;
    private View parent_view;
    private Users Profileuser;
    private PostsAdapter postsAdapter;
    private FirebaseFirestore database;

    private Users parsedUserObj;
    private TextView DcountTV;
    private TextView RcountTV;

    public static void start(Context context, Users obj) {
        Intent intent = new Intent(context, ActivityUserProfile.class);
        intent.putExtra(Constants.USER_EXTRA_OBJECT, obj);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);
        parent_view = findViewById(android.R.id.content);

        // deserialize object from string to object class.
        parsedUserObj = (Users) getIntent().getSerializableExtra(Constants.USER_EXTRA_OBJECT);

        if (parsedUserObj == null) {
            finish();
        }

        initToolbar(binding.toolbar,true);

        // show the data in the views
        initViews();
        setListeners();
        getUserInfo();
        //bindRecyclerView(); TODO: REMEMBER TO UNCOMMENT THIS
    }

    private void getUserPosts() {
        database = FirebaseFirestore.getInstance();
        List<Posts> posts= new ArrayList<>();
        database.collection(ma.ensaf.veryempty.utils.Constants.KEY_COLLECTION_POSTS)
                .whereEqualTo(ma.ensaf.veryempty.utils.Constants.KEY_POSTER_NAME, parsedUserObj.getName())
                .get()
                .addOnCompleteListener(task ->  {
                    if(task.isSuccessful() && task.getResult() != null) {
                        int i=0;
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            i++;

                            Posts post = new Posts(i,Profileuser
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_DATE)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_BODY)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_IMAGE)
                            );

                            posts.add(post);
                        }

                    }
                    ////////////
                    // show the posts
                    // show the list of recent activity
                    binding.recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(activityContext));
                    binding.recentActivityRecyclerView.setHasFixedSize(true);
                    binding.recentActivityRecyclerView.setNestedScrollingEnabled(false);
                    //set data and list adapter
                    postsAdapter = new PostsAdapter(activityContext, posts);
                    binding.recentActivityRecyclerView.setAdapter(postsAdapter);

                    ///////////////////

                });
        //return users;
    }

    private void getUserInfo() {
        database = FirebaseFirestore.getInstance();
        database.collection(ma.ensaf.veryempty.utils.Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(ma.ensaf.veryempty.utils.Constants.KEY_NAME, parsedUserObj.getName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int i = 0;
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            i++;
                            if (i == 1) {
                                int totalD =queryDocumentSnapshot.getDouble(ma.ensaf.veryempty.utils.Constants.KEY_COUNT_DONATIONS).intValue();
                                DcountTV = findViewById(R.id.user_total_donations);
                                RcountTV = findViewById(R.id.user_total_requests);
                                DcountTV.setText(String.valueOf(totalD));
                                RcountTV.setText(String.valueOf(queryDocumentSnapshot.getDouble(ma.ensaf.veryempty.utils.Constants.KEY_COUNT_REQUESTS).intValue()));
                                if(totalD > 3) {
                                    findViewById(R.id.heroBadge).setVisibility(View.VISIBLE);
                                } else {
                                    findViewById(R.id.heroBadge).setVisibility(View.INVISIBLE);
                                }
                                Profileuser = new Users(i, queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_NAME)
                                        , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_IMAGE)
                                        , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_CITY)
                                        , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_PHONE),
                                        queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_BLOOD_TYPE)

                                        );
                                Profileuser.setLastDonatedDate("2022-01-07"); //TODO: REMOVE THIS
                            }
                        }
                    }
                    getUserPosts();
                });

                }

    private void setListeners() {
        TextView contactBtn = findViewById(R.id.user_action_contact);
        contactBtn.setOnClickListener(v -> {
            String number =parsedUserObj.getPhoneNumber();
            Uri uri = Uri.parse("tel:"+number);
            startActivity(new Intent(Intent.ACTION_CALL, uri ));
        });
    }

    private void initViews() {
        binding.usernameTextView.setText(parsedUserObj.getName());
        byte[] bytes = Base64.decode(parsedUserObj.getImage(),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.userContentTop.userImageView.setImageBitmap(bitmap);
        //TextView totalDonationsTV = findViewById(R.id.pers_total_donations);
        //totalDonationsTV.setText();
        binding.userContentTop.userLocationTextView.setText(parsedUserObj.getLocation());
        binding.userContentTop.userDescriptionTextView.setText(activityContext.getString(R.string.medium_lorem_ipsum));
    }

 /*  private void bindRecyclerView() {
        // show the list of recent activity
        binding.recentActivityRecyclerView.setLayoutManager(new LinearLayoutManager(activityContext));
        binding.recentActivityRecyclerView.setHasFixedSize(true);
        binding.recentActivityRecyclerView.setNestedScrollingEnabled(false);
        //set data and list adapter
        postsAdapter = new PostsAdapter(activityContext, null);
        binding.recentActivityRecyclerView.setAdapter(postsAdapter);
    }

    private void generateListForRecentActivity(List<Posts> posts) {
        // Create new list of posts
        // by filtering the post related to this user
        List<Posts> newPostsList = new ArrayList<>();

        for (Posts posts1:posts) {
            String str_user_id = String.valueOf(posts1.getUser().getId());
            if (str_user_id.toLowerCase().equalsIgnoreCase(String.valueOf(parsedUserObj.getId()))) {
                newPostsList.add(posts1);
            }
        }

        // refresh the adapter
        postsAdapter.setPostsList(newPostsList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        generateListForRecentActivity(Constants.getPosts(activityContext));
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return true;
    }
}