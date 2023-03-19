package ma.ensaf.veryempty.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.adapters.PostsAdapter;
import ma.ensaf.veryempty.databinding.ActivityHomeBinding;
import ma.ensaf.veryempty.models.Posts;
import ma.ensaf.veryempty.models.Users;
import ma.ensaf.veryempty.utils.Constants;
import ma.ensaf.veryempty.utils.PreferenceManager;

public class ActivityHome extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = ActivityHome.class.getSimpleName();

    ActivityHomeBinding binding;
    private PreferenceManager preferenceManager;
    private View parent_view;
    private PostsAdapter postsAdapter;
    private FirebaseFirestore database;
    BottomNavigationView bottomNavigationView;
    public static void start(Context context) {
        Intent intent = new Intent(context, ActivityHome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @SuppressLint({"CheckResult", "SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        parent_view = findViewById(android.R.id.content);
        preferenceManager = new PreferenceManager(getApplicationContext());
        initToolbar(binding.toolbar,false);
        //ImageView share = findViewById(R.id.action_like_image_view);
        initViews();
        setListeners();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        final View iconView = menuView.getChildAt(2).findViewById(R.id.settings);
        final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        // set your height here
        layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 102, displayMetrics);
        // set your width here
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 102, displayMetrics);
        iconView.setLayoutParams(layoutParams);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        getPostsFromDb();
    }

    private void setListeners() {

        binding.logoutBtn.setOnClickListener(v -> signOut());
        binding.reserachBtn.setOnClickListener(v -> {
            MapsActivity.start(activityContext);
        });

    }

    private void signOut() {
        showToast("signing out...",false);
        //preferenceManager.clear();
        preferenceManager.putBoolean(Constants.LOGGEDIN_ONCE_BEFORE,true);
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,false);

        startActivity(new Intent(getApplicationContext(),ActivityRegister.class));
        //might generate errors
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        finish();

    }

    private void initViews() {



        binding.homeTitleLayout.postUpdateRipple.setOnClickListener(v -> {
            ActivityNewPost.start(activityContext);
        });

        // show all the donors
        binding.homeContentTop.buttonFindDonor.setOnClickListener(v -> {
            ActivityDonors.start(activityContext);
        });

        // show the requests
        binding.homeContentTop.buttonViewRequests.setOnClickListener(v -> {
            ActivityRequests.start(activityContext);
        });


    }

    public void getPostsFromDb() {
        database = FirebaseFirestore.getInstance();
        List<Posts> posts= new ArrayList<>();

        database.collection(ma.ensaf.veryempty.utils.Constants.KEY_COLLECTION_POSTS)
                .get()
                .addOnCompleteListener(task ->  {
                    if(task.isSuccessful() && task.getResult() != null) {
                        int i=0;
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            i++;
                            Users user = new Users(i,queryDocumentSnapshot.getString(Constants.KEY_POSTER_NAME)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_IMAGE)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_LOCATION)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_PHONE),
                                    queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_BLOOD_TYPE),
                                    queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_DATE));

                            Posts post = new Posts(i,user
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_DATE)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_BODY)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_IMAGE)
                                   );

                            posts.add(post);
                            Collections.reverse(posts);
                        }
                    }
                   ////////////
                    // show the posts
                    binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(activityContext));
                    binding.postsRecyclerView.setHasFixedSize(true);
                    binding.postsRecyclerView.setNestedScrollingEnabled(false);
                    //set data and list adapter
                    postsAdapter = new PostsAdapter(activityContext, posts);
                    binding.postsRecyclerView.setAdapter(postsAdapter);

                    // clicking the ask for help button
                    postsAdapter.SetOnItemClickListener((v, position, obj) -> {
                        switch (v.getId()){
                            case R.id.action_like_image_view:
                                // get the tag
                                String imageTag = (String) v.findViewById(R.id.action_like_image_view).getTag();
                                if(imageTag.equalsIgnoreCase("liked")){
                                    v.findViewById(R.id.action_like_image_view).setTag("like");
                                    ((ImageView)v.findViewById(R.id.action_like_image_view)).setImageResource(R.drawable.ic_heart_empty);
                                }else{
                                    v.findViewById(R.id.action_like_image_view).setTag("liked");
                                    ((ImageView)v.findViewById(R.id.action_like_image_view)).setImageResource(R.drawable.ic_heart_filled);
                                }
                                break;
                            case R.id.action_share_image_view:
                                //Snackbar.make(parent_view, "Share Clicked...", Snackbar.LENGTH_LONG).show();
                                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                String shareBody = "Hey, I think you might be interested in this Post from the Droppy App \"Link_to_Post\"";
                                String shareSub = "DROPPY Post";
                                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                startActivity(Intent.createChooser(sharingIntent, "Share using"));
                                break;
                /*case R.id.action_comment_image_view:
                    Snackbar.make(parent_view, "Comment Clicked...", Snackbar.LENGTH_LONG).show();
                    break;*/
                        }
                    });
                    ///////////////////

                });
        //return users;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.person:
                intent = new Intent(getApplicationContext(), ActivityPersProfile.class);
                startActivity(intent);
                return true;

            case R.id.home:
                Toast.makeText(this, "This is Home!", Toast.LENGTH_LONG).show();
                return true;

            case R.id.settings:
                intent = new Intent(getApplicationContext(), ActivityRegisterDonor.class);
                startActivity(intent);
                return true;
        }
        return false;
    }
}
