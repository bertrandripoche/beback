package com.depuisletemps.beback.ui.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.ui.recyclerview.LoanAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_loan.*

class LoanActivity: BaseActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var mArchiveButton: MenuItem
    lateinit var mProfileButton: MenuItem
    lateinit var mLoansRef: CollectionReference
    private var mAdapter: LoanAdapter? = null
    private var mLoanList: List<Loan>? = null
    var mUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        configureToolbar()

        configureRecyclerView()

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

    /**
     * This method configure the recycler view for loan entries
     */
    private fun configureRecyclerView() {
        mUser = getCurrentUser()
        val requesterId: String = mUser?.uid ?: ""
        mLoansRef = mDb.collection("loans")
        val query: Query = mLoansRef.whereEqualTo("requestor_id", requesterId)

        val options = FirestoreRecyclerOptions.Builder<Loan>().setQuery(query, Loan::class.java).build()
        mAdapter = LoanAdapter(options)

        if (activity_loan_recycler_view != null) {
            activity_loan_recycler_view.setHasFixedSize(true)
            activity_loan_recycler_view.setLayoutManager(LinearLayoutManager(applicationContext))
            activity_loan_recycler_view.setAdapter(mAdapter)
        }
    }
}