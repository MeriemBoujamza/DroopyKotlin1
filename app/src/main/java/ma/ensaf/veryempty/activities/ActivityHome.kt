package ma.ensaf.veryempty.activities


import com.google.android.material.bottomnavigation.BottomNavigationView
import ma.ensaf.veryempty.zadapters.PostsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import com.google.android.material.bottomnavigation.BottomNavigationMenuView

import android.util.TypedValue
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth

import ma.ensaf.veryempty.models.Posts
import com.google.firebase.firestore.QuerySnapshot
import ma.ensaf.veryempty.models.Users
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.google.android.gms.tasks.Task

import ma.ensaf.veryempty.databinding.ActivityHomeBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager
import java.util.*

class ActivityHome : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityHomeBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var parent_view: View
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var database: FirebaseFirestore
    private lateinit var  bottomNavigationView: BottomNavigationView
    @SuppressLint("CheckResult", "SetTextI18n", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        parent_view = findViewById(android.R.id.content)
        preferenceManager = PreferenceManager(applicationContext)
        initToolbar(binding.toolbar, false)
        //ImageView share = findViewById(R.id.action_like_image_view);
        initViews()
        setListeners()
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val iconView = menuView.getChildAt(2).findViewById<View>(R.id.settings)
        val layoutParams = iconView.layoutParams
        val displayMetrics = resources.displayMetrics
        // set your height here
        layoutParams.height =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 102f, displayMetrics)
                .toInt()
        // set your width here
        layoutParams.width =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 102f, displayMetrics)
                .toInt()
        iconView.layoutParams = layoutParams
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        postsFromDb
    }

    private fun setListeners() {
        binding.logoutBtn.setOnClickListener { signOut() }
        binding.reserachBtn.setOnClickListener {
            MapsActivity.start(Objects.requireNonNull(activityContext)) //TODO: check this out for errors
        }
    }

    private fun signOut() {
        showToast("signing out...", false)
        //preferenceManager.clear();
        preferenceManager.putBoolean(Constants.LOGGEDIN_ONCE_BEFORE, true)
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false)
        startActivity(Intent(applicationContext, ActivityRegister::class.java))
        //might generate errors
        LoginManager.getInstance().logOut()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun initViews() {
        binding.homeTitleLayout.postUpdateRipple.setOnClickListener {
            ActivityNewPost.start(
                activityContext
            )
        }

        // show all the donors
        binding.homeContentTop.buttonFindDonor.setOnClickListener {
            ActivityDonors.start(
                activityContext
            )
        }

        // show the requests
        binding.homeContentTop.buttonViewRequests.setOnClickListener {
            ActivityRequests.start(
                activityContext
            )
        }
    }//Snackbar.make(parent_view, "Share Clicked...", Snackbar.LENGTH_LONG).show();
    //return users;
// get the tag
    ////////////
    // show the posts
    //set data and list adapter

    // clicking the ask for help button
    @get:SuppressLint("NonConstantResourceId")
    val postsFromDb: Unit
        get() {
            database = FirebaseFirestore.getInstance()
            val posts: MutableList<Posts?> = ArrayList()
            database.collection(Constants.KEY_COLLECTION_POSTS)
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful && task.result != null) {
                        var i = 0
                        for (queryDocumentSnapshot in task.result!!) {
                            i++
                            val user = Users(
                                i,
                                queryDocumentSnapshot.getString(Constants.KEY_POSTER_NAME),
                                queryDocumentSnapshot.getString(Constants.KEY_IMAGE),
                                queryDocumentSnapshot.getString(Constants.KEY_POST_LOCATION),
                                queryDocumentSnapshot.getString(Constants.KEY_PHONE),
                                queryDocumentSnapshot.getString(Constants.KEY_BLOOD_TYPE),
                                queryDocumentSnapshot.getString(Constants.KEY_POST_DATE)
                            )
                            val post = Posts(
                                i,
                                user,
                                queryDocumentSnapshot.getString(Constants.KEY_POST_DATE),
                                queryDocumentSnapshot.getString(Constants.KEY_POST_BODY),
                                queryDocumentSnapshot.getString(Constants.KEY_POST_IMAGE)
                            )
                            posts.add(post)
                            posts.reverse()
                        }
                    }
                    ////////////
                    // show the posts
                    binding.postsRecyclerView.layoutManager = LinearLayoutManager(activityContext)
                    binding.postsRecyclerView.setHasFixedSize(true)
                    binding.postsRecyclerView.isNestedScrollingEnabled = false
                    //set data and list adapter
                    postsAdapter = PostsAdapter(activityContext, posts)
                    binding.postsRecyclerView.adapter = postsAdapter

                    // clicking the ask for help button
                    postsAdapter.SetOnItemClickListener { v: View, position: Int, obj: Posts? ->
                        when (v.id) {
                            R.id.action_like_image_view -> {
                                // get the tag
                                val imageTag =
                                    v.findViewById<View>(R.id.action_like_image_view).tag as String
                                if (imageTag.equals("liked")) {
                                    v.findViewById<View>(R.id.action_like_image_view).tag = "like"
                                    (v.findViewById<View>(R.id.action_like_image_view) as ImageView).setImageResource(
                                        R.drawable.ic_heart_empty
                                    )
                                } else {
                                    v.findViewById<View>(R.id.action_like_image_view).tag = "liked"
                                    (v.findViewById<View>(R.id.action_like_image_view) as ImageView).setImageResource(
                                        R.drawable.ic_heart_filled
                                    )
                                }
                            }
                            R.id.action_share_image_view -> {
                                //Snackbar.make(parent_view, "Share Clicked...", Snackbar.LENGTH_LONG).show();
                                val sharingIntent = Intent(Intent.ACTION_SEND)
                                sharingIntent.type = "text/plain"
                                val shareBody =
                                    "Hey, I think you might be interested in this Post from the Droppy App \"Link_to_Post\""
                                val shareSub = "DROPPY Post"
                                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
                                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                                startActivity(Intent.createChooser(sharingIntent, "Share using"))
                            }
                        }
                    }
                }
            //return users;
        }

    @SuppressLint("NonConstantResourceId")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.person -> {
                intent = Intent(applicationContext, ActivityPersProfile::class.java)
                startActivity(intent)
                return true
            }
            R.id.home -> {
                Toast.makeText(this, "This is Home!", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.settings -> {
                intent = Intent(applicationContext, ActivityRegisterDonor::class.java)
                startActivity(intent)
                return true
            }
        }
        return false
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ActivityHome::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}