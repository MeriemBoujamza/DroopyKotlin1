package ma.ensaf.veryempty.activities

import android.content.Context
import ma.ensaf.veryempty.models.Users
import ma.ensaf.veryempty.adapters.PostsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import android.widget.TextView
import android.content.Intent
import ma.ensaf.veryempty.models.Posts
import com.google.firebase.firestore.QuerySnapshot
import androidx.recyclerview.widget.LinearLayoutManager
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.gms.tasks.Task
import ma.ensaf.veryempty.databinding.ActivityPersProfileBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager
import java.util.*

class ActivityPersProfile : BaseActivity() {
    lateinit var binding: ActivityPersProfileBinding
    private lateinit var Profileuser: Users
    private  lateinit var postsAdapter: PostsAdapter
    private lateinit var database: FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pers_profile)
        preferenceManager = PreferenceManager(applicationContext)
        initToolbar(binding.toolbar, true)

        // show the data in the views
        initViews()
        setListeners()
        Profileuser = Users(
            1,
            preferenceManager.getString(Constants.KEY_NAME),
            preferenceManager.getString(Constants.KEY_IMAGE),
            preferenceManager.getString(Constants.KEY_CITY),
            preferenceManager.getString(Constants.KEY_PHONE),
            preferenceManager.getString(Constants.KEY_BLOOD_TYPE)
        )
        userPosts
    }

    private fun setListeners() {
        val EditBtn = findViewById<TextView>(R.id.pers_donor_type_text_view)
        EditBtn.setOnClickListener {
            startActivity(Intent(applicationContext, ActivityEditPersInfo::class.java))
            finish()
        }
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
            database.collection(Constants.KEY_COLLECTION_POSTS)
                .whereEqualTo(
                    Constants.KEY_POSTER_NAME,
                    preferenceManager.getString(Constants.KEY_NAME)
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
                                queryDocumentSnapshot.getString(Constants.KEY_POST_DATE),
                                queryDocumentSnapshot.getString(Constants.KEY_POST_BODY),
                                queryDocumentSnapshot.getString(Constants.KEY_POST_IMAGE)
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

    private fun initViews() {
        val donCount = findViewById<TextView>(R.id.pers_total_donations)
        val ReqCount = findViewById<TextView>(R.id.pers_total_requests)
        ReqCount.text =
            preferenceManager.getInt(Constants.KEY_COUNT_REQUESTS).toString()
        donCount.text =
            preferenceManager.getInt(Constants.KEY_COUNT_DONATIONS)
                .toString()
        binding.usernameTextView.text =
            preferenceManager.getString(Constants.KEY_NAME)
        val bytes =
            Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.persContentTop.persImageView.setImageBitmap(bitmap)
        binding.persContentTop.persLocationTextView.text =
            preferenceManager.getString(Constants.KEY_CITY)
        binding.persContentTop.persDescriptionTextView.text =
            activityContext.getString(R.string.medium_lorem_ipsum)
    }

    companion object {
        fun start(context: Context, obj: Users?) {
            val intent = Intent(context, ActivityPersProfile::class.java)
            intent.putExtra(ma.ensaf.veryempty.data.Constants.USER_EXTRA_OBJECT, obj)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}