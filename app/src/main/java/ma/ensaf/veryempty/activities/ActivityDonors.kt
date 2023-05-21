package ma.ensaf.veryempty.activities

import com.google.firebase.firestore.FirebaseFirestore
import ma.ensaf.veryempty.models.Users
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R
import androidx.recyclerview.widget.LinearLayoutManager
import ma.ensaf.veryempty.models.UsersListItem
import ma.ensaf.veryempty.models.RowItem
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.tasks.Task
import ma.ensaf.veryempty.models.HeaderItem
import com.google.firebase.firestore.QuerySnapshot

import com.google.android.material.snackbar.Snackbar
import ma.ensaf.veryempty.zadapters.DonorsAdapter
import ma.ensaf.veryempty.databinding.ActivityDonorsBinding
import ma.ensaf.veryempty.utils.Constants
import java.util.*

class ActivityDonors : BaseActivity() {
    private lateinit var database: FirebaseFirestore
     lateinit var binding: ActivityDonorsBinding
    private lateinit var parent_view: View
    private lateinit var donorsAdapter: DonorsAdapter
    private var usersList: List<Users>? = null
    @SuppressLint("CheckResult", "SetTextI18n", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_donors)
        parent_view = findViewById(android.R.id.content)
        initToolbar(binding.donorsToolbar.toolbar, true)
        bindRecyclerView()
        usersList = donorsFromDb
    }

    private fun bindRecyclerView() {
        // show the list of donors
        binding.donorsRecyclerView.layoutManager = LinearLayoutManager(activityContext)
        binding.donorsRecyclerView.setHasFixedSize(true)
        binding.donorsRecyclerView.isNestedScrollingEnabled = false
        //set data and list adapter
        donorsAdapter = DonorsAdapter(activityContext, null) //items null??
        binding.donorsRecyclerView.adapter = donorsAdapter

        // clicking the ask for help button
        donorsAdapter.SetOnItemClickListener { v: View?, position: Int, obj: UsersListItem ->
            val number = (obj as RowItem).users?.phoneNumber
            val uri = Uri.parse("tel:$number")
            startActivity(Intent(Intent.ACTION_CALL, uri))
        }
    }

    private fun groupDataIntoHashMap(usersList: List<Users>) {
        val groupedHashMap = LinkedHashMap<String, Set<Users>>()
        var list: MutableSet<Users>
        for (userObj in usersList) {
            val hashMapKey = userObj.lastDonatedDate
            if (groupedHashMap.containsKey(hashMapKey)) {
                // The key is already in the HashMap; add the pojo object
                // against the existing key.
                Objects.requireNonNull(groupedHashMap[hashMapKey])?.toMutableSet()?.add(userObj)

            } else {
                // The key is not there in the HashMap; create a new key-value pair
                list = LinkedHashSet()
                list.add(userObj)
                groupedHashMap.put(hashMapKey!!, list)
            }
        }

        //Generate list from map
        generateListFromMap(groupedHashMap)
    }

    private fun generateListFromMap(groupedHashMap: LinkedHashMap<String, Set<Users>>) {
        // We linearly add every item into the consolidatedList.
        val consolidatedList: MutableList<UsersListItem> = ArrayList()
        for (date in groupedHashMap.keys) {
            val headerItem = HeaderItem()
            headerItem.date = date
            consolidatedList.add(headerItem)
            for (userModel in Objects.requireNonNull(groupedHashMap[date])!!) {
                val rowItem = RowItem()
                rowItem.users = userModel
                consolidatedList.add(rowItem)
            }
        }
        donorsAdapter.setUsersListItemList(consolidatedList)
    }

    override fun onResume() {
        super.onResume()
        //groupDataIntoHashMap(usersList);
        //donorsAdapter.notifyDataSetChanged();
    }

    val donorsFromDb: List<Users>
        get() {
            database = FirebaseFirestore.getInstance()
            val users: MutableList<Users> = ArrayList()
            database.collection(Constants.KEY_COLLECTION_DONATIONS)
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
                                queryDocumentSnapshot.getString(Constants.KEY_DONATION_CONTACT_PHONE),
                                queryDocumentSnapshot.getString(Constants.KEY_BLOOD_TYPE),
                                queryDocumentSnapshot.getString(Constants.KEY_DONATION_DATETIME)
                            )
                            users.add(user)
                        }
                    }
                    groupDataIntoHashMap(users)
                    binding.donorsCardView.becomeDonor.setOnClickListener {
                        ActivityRegisterDonor.start(
                            activityContext
                        )
                    }
                }
            return users
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
        menuInflater.inflate(R.menu.menu_activity_donors, menu)
        return true
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, ActivityDonors::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}