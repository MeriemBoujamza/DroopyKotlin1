package ma.ensaf.veryempty.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.adapters.RequestsAdapter;
import ma.ensaf.veryempty.data.Constants;
import ma.ensaf.veryempty.databinding.ActivityRequestsBinding;
import ma.ensaf.veryempty.models.Users;
import ma.ensaf.veryempty.utils.PreferenceManager;

public class ActivityRequests extends BaseActivity {

    private static final String TAG = ActivityRequests.class.getSimpleName();

    ActivityRequestsBinding binding;
    private View parent_view;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private RequestsAdapter requestsAdapter;
    List<Users>  usersList;

    public static void start(Context context) {
        Intent intent = new Intent(context, ActivityRequests.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @SuppressLint({"CheckResult", "SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_requests);
        parent_view = findViewById(android.R.id.content);
        preferenceManager = new PreferenceManager(getApplicationContext());
        initToolbar(binding.requestsToolbar.toolbar,true);
        bindRecyclerView();
        getRequestsFromDb();
    }
    public void getRequestsFromDb() {
        database = FirebaseFirestore.getInstance();
        List<Users> users= new ArrayList<>();
        database.collection(ma.ensaf.veryempty.utils.Constants.KEY_COLLECTION_REQUESTS)
                .get()
                .addOnCompleteListener(task ->  {
                    if(task.isSuccessful() && task.getResult() != null) {
                        int i=0;
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            i++;
                            Users user = new Users(i,queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_NAME)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_IMAGE)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_CITY)
                                    , queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_PHONE),
                                    queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_BLOOD_TYPE),
                                    queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_REQUEST_DATETIME));
                            users.add(user);
                        }
                    }
                    //set data and list adapter
                    //requestsAdapter.setUsersList(users);
                    // show the users
                    binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(activityContext));
                    binding.usersRecyclerView.setHasFixedSize(true);
                    binding.usersRecyclerView.setNestedScrollingEnabled(false);
                    requestsAdapter = new RequestsAdapter(activityContext,users);
                    binding.usersRecyclerView.setAdapter(requestsAdapter);
                    // clicking the requests list
                    requestsAdapter.SetOnItemClickListener((v, position, obj) -> {
                        ActivityUserProfile.start(activityContext,obj);
                    });
                    binding.requestsTopLayout.buttonRequest.setOnClickListener(v -> {
                        ActivityRequestBlood.start(activityContext);
                    });

                });
        //return users;
    }
    private void bindRecyclerView() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }else{
            Snackbar.make(parent_view, item.getTitle()+" clicked", Snackbar.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_requests, menu);
        return true;
    }
}
