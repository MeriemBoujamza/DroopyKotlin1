package ma.ensaf.veryempty.activities

import ma.ensaf.veryempty.activities.ActivityUserProfile.Companion.start
import com.google.firebase.firestore.FirebaseFirestore
import ma.ensaf.veryempty.adapters.RequestsAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import ma.ensaf.veryempty.models.Users
import com.google.firebase.firestore.QuerySnapshot
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.tasks.Task
import ma.ensaf.veryempty.databinding.ActivityRequestsBinding
import ma.ensaf.veryempty.utils.Constants
import ma.ensaf.veryempty.utils.PreferenceManager
import java.util.*

class ActivityRequests : BaseActivity() {
    lateinit var binding: ActivityRequestsBinding
    private lateinit var parent_view: View
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    private lateinit var requestsAdapter: RequestsAdapter
    @SuppressLint("CheckResult", "SetTextI18n", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_requests)
        parent_view = findViewById(android.R.id.content)
        preferenceManager = PreferenceManager(applicationContext)
        initToolbar(binding.requestsToolbar.toolbar, true)
        bindRecyclerView()
        requestsFromDb
    }

    //set data and list adapter
    //requestsAdapter.setUsersList(users);
    // show the users
    // clicking the requests list
    //return users;
    val requestsFromDb: Unit
        get() {
            database = FirebaseFirestore.getInstance()
            val users: MutableList<Users> = ArrayList()
            database.collection(Constants.KEY_COLLECTION_REQUESTS)
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful && task.result != null) {
                        var i = 0
                        for (queryDocumentSnapshot in task.result!!) {
                            i++
                            val user = Users(
                                i,
                                queryDocumentSnapshot.getString(Constants.KEY_NAME),
                                queryDocumentSnapshot.getString(Constants.KEY_IMAGE),
                                queryDocumentSnapshot.getString(Constants.KEY_CITY),
                                queryDocumentSnapshot.getString(Constants.KEY_PHONE),
                                queryDocumentSnapshot.getString(Constants.KEY_BLOOD_TYPE),
                                queryDocumentSnapshot.getString(Constants.KEY_REQUEST_DATETIME)
                            )
                            users.add(user)
                        }
                    }
                    //set data and list adapter
                    //requestsAdapter.setUsersList(users);
                    // show the users
                    binding.usersRecyclerView.layoutManager = LinearLayoutManager(activityContext)
                    binding.usersRecyclerView.setHasFixedSize(true)
                    binding.usersRecyclerView.isNestedScrollingEnabled = false
                    requestsAdapter = RequestsAdapter(activityContext, users)
                    binding.usersRecyclerView.adapter = requestsAdapter
                    // clicking the requests list
                    requestsAdapter.SetOnItemClickListener { v: View?, position: Int, obj: Users? ->
                        start(
                            activityContext,
                            obj
                        )
                    }
                    binding.requestsTopLayout.buttonRequest.setOnClickListener { v: View? ->
                        ActivityRequestBlood.start(
                            activityContext
                        )
                    }
                }
            //return users;
        }

    private fun bindRecyclerView() {}
    override fun onResume() {
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        } else {
            Snackbar.make(parent_view, item.title.toString() + " clicked", Snackbar.LENGTH_SHORT)
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_requests, menu)
        return true
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, ActivityRequests::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}