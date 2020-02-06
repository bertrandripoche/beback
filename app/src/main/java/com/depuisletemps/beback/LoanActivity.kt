package com.depuisletemps.beback

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query

class LoanActivity: BaseActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var mArchiveButton: MenuItem
    lateinit var mProfileButton: MenuItem
    lateinit var mLoansRef: CollectionReference
    val mUser: FirebaseUser? = getCurrentUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        configureToolbar()
    }
    /**
     * This method overrides the onBackPressed method to change the behavior of the Back button
     */
    override fun onBackPressed() {

    }

    /**
     * This method creates the icons menu on the toolbar
     * @param menu is the menu to create
     * @return true
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        mArchiveButton = menu.getItem(0)
        mProfileButton = menu.getItem(1)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * This method configures the toolbar
     */
    private fun configureToolbar() {
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
    }

    /**
     * This method manages the clicks on the toolbar icons
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_archive -> {
            Toast.makeText(this,"Archive action",Toast.LENGTH_LONG).show()
            true
        }
        R.id.menu_profile ->{
            Toast.makeText(this,"Profile action",Toast.LENGTH_LONG).show()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

//    private fun configureRecyclerView() {
//        mLoansRef = mDb.collection("loans")
//        val requestor_id: String = if (mUser != null) mUser.uid
//        val query: Query = mLoansRef.whereEqualTo("requestor_id", requestor_id)
//
//        val options: FirestoreRecyclerOptions<Attendee> =
//            FirestoreRecyclerOptions.Builder<Attendee>()
//                .setQuery(query, Attendee::class.java)
//                .build()
//        mAdapter = AttendeesAdapter(options)
//        if (mRecyclerView != null) {
//            mRecyclerView.setHasFixedSize(true)
//            mRecyclerView.setLayoutManager(LinearLayoutManager(applicationContext))
//            mRecyclerView.setAdapter(mAdapter)
//        }
//    }
}