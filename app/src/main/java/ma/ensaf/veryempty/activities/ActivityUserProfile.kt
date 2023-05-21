package ma.ensaf.veryempty.activities

import android.content.Context
import ma.ensaf.veryempty.models.Users
import ma.ensaf.veryempty.zadapters.PostsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import ma.ensaf.veryempty.models.Posts
import com.google.firebase.firestore.QuerySnapshot
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.tasks.Task
import ma.ensaf.veryempty.data.Constants
import ma.ensaf.veryempty.databinding.ActivityUserProfileBinding
import java.util.*

class ActivityUserProfile : BaseActivity() {
    lateinit var binding: ActivityUserProfileBinding
    private lateinit  var parent_view: View
    private var Profileuser: Users? = null
    private var postsAdapter: PostsAdapter? = null
    private lateinit var database: FirebaseFirestore
    private var parsedUserObj: Users? = null
    private lateinit var DcountTV: TextView
    private lateinit var RcountTV: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)
        parent_view = findViewById(android.R.id.content)

        // deserialize object from string to object class.
        parsedUserObj = intent.getSerializableExtra(Constants.USER_EXTRA_OBJECT) as Users?
        if (parsedUserObj == null) {
            finish()
        }
        initToolbar(binding.toolbar, true)

        // show the data in the views
        initViews()
        setListeners()
        userInfo
        //bindRecyclerView(); TODO: REMEMBER TO UNCOMMENT THIS
    }

    ////////////
    // show the posts
    // show the list of recent activity
    //set data and list adapter
    //return users;
    private val userPosts: Unit
         get() {
            database = FirebaseFirestore.getInstance()
            val posts: MutableList<Posts> = ArrayList()
            database.collection(ma.ensaf.veryempty.utils.Constants.KEY_COLLECTION_POSTS)
                .whereEqualTo(
                    ma.ensaf.veryempty.utils.Constants.KEY_POSTER_NAME,
                    parsedUserObj!!.name
                )
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful && task.result != null) {
                        var i = 0
                        for (queryDocumentSnapshot in task.result!!) {
                            i++
                            val post = Posts(
                                i,
                                Profileuser,
                                queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_DATE),
                                queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_BODY),
                                queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_POST_IMAGE)
                            )
                            posts.add(post)
                        }
                    }
                    ////////////
                    // show the posts
                    // show the list of recent activity
                    binding.recentActivityRecyclerView.layoutManager =
                        LinearLayoutManager(activityContext)
                    binding.recentActivityRecyclerView.setHasFixedSize(true)
                    binding.recentActivityRecyclerView.isNestedScrollingEnabled = false
                    //set data and list adapter
                    postsAdapter = PostsAdapter(activityContext, posts)
                    binding.recentActivityRecyclerView.adapter = postsAdapter
                }
            //return users;
        }

    //TODO: REMOVE THIS
    private val userInfo: Unit
         get() {
            database = FirebaseFirestore.getInstance()
            database.collection(ma.ensaf.veryempty.utils.Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(ma.ensaf.veryempty.utils.Constants.KEY_NAME, parsedUserObj!!.name)
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful && task.result != null) {
                        var i = 0
                        for (queryDocumentSnapshot in task.result!!) {
                            i++
                            if (i == 1) {
                                val totalD =
                                    queryDocumentSnapshot.getDouble(ma.ensaf.veryempty.utils.Constants.KEY_COUNT_DONATIONS)!!
                                        .toInt()
                                DcountTV = findViewById(R.id.user_total_donations)
                                RcountTV = findViewById(R.id.user_total_requests)
                                DcountTV.setText(totalD.toString())
                                RcountTV.setText(
                                    queryDocumentSnapshot.getDouble(ma.ensaf.veryempty.utils.Constants.KEY_COUNT_REQUESTS)!!
                                        .toInt().toString()
                                )
                                if (totalD > 3) {
                                    findViewById<View>(R.id.heroBadge).visibility = View.VISIBLE
                                } else {
                                    findViewById<View>(R.id.heroBadge).visibility = View.INVISIBLE
                                }
                                Profileuser = Users(
                                    i,
                                    queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_NAME),
                                    queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_IMAGE),
                                    queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_CITY),
                                    queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_PHONE),
                                    queryDocumentSnapshot.getString(ma.ensaf.veryempty.utils.Constants.KEY_BLOOD_TYPE)
                                )
                                Profileuser!!.lastDonatedDate = "2022-01-07" //TODO: REMOVE THIS
                            }
                        }
                    }
                    userPosts
                }
        }

    private fun setListeners() {
        val contactBtn = findViewById<TextView>(R.id.user_action_contact)
        contactBtn.setOnClickListener { v: View? ->
            val number = parsedUserObj!!.phoneNumber
            val uri = Uri.parse("tel:$number")
            startActivity(Intent(Intent.ACTION_CALL, uri))
        }
    }

    private fun initViews() {
        binding.usernameTextView.text = parsedUserObj!!.name
        val bytes = Base64.decode(parsedUserObj!!.image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.userContentTop.userImageView.setImageBitmap(bitmap)
        // TextView totalDonationsTV = findViewById(R.id.pers_total_donations);
        //totalDonationsTV.setText();
        binding.userContentTop.userLocationTextView.text = parsedUserObj!!.location
        binding.userContentTop.userDescriptionTextView.text =
            activityContext.getString(R.string.medium_lorem_ipsum)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun start(context: Context, obj: Users?) {
            val intent = Intent(context, ActivityUserProfile::class.java)
            intent.putExtra(Constants.USER_EXTRA_OBJECT, obj)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}