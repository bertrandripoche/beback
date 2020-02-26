package com.depuisletemps.beback.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.ui.recyclerview.ItemClickSupport
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
    var mUser: FirebaseUser? = null
    var mIsLoanAlertDialogDisplayed:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        configureToolbar()
        configureRecyclerView()
        configureOnClickRecyclerView()

        mBtnAdd.setOnClickListener(View.OnClickListener {
            createLoanAlertDialog()
        })

    }

    /**
     * This method overrides the onBackPressed method to change the behavior of the Back button
     */
    override fun onBackPressed() {
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mIsLoanAlertDialogDisplayed) outState?.putBoolean("loanAlertDialogDisplayed", true)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null)
            if (savedInstanceState.getBoolean("loanAlertDialogDisplayed"))  createLoanAlertDialog()
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
        val query = mLoansRef.whereEqualTo("requestor_id", requesterId).orderBy("due_date", Query.Direction.ASCENDING)

        val options = FirestoreRecyclerOptions.Builder<Loan>().setQuery(query, Loan::class.java).build()
        mAdapter = LoanAdapter(options, this, getString(R.string.standard))

        val orientation = resources.getInteger(R.integer.gallery_orientation)

        if (this.activity_loan_recycler_view != null) {
            activity_loan_recycler_view.setHasFixedSize(true)
            activity_loan_recycler_view.layoutManager = LinearLayoutManager(applicationContext, orientation, false)
            activity_loan_recycler_view.adapter = mAdapter
        }
    }

    fun configureOnClickRecyclerView() {
        ItemClickSupport.addTo(activity_loan_recycler_view, R.layout.loanactivity_recyclerview_item_loan_swipelayout)
            .setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(
                    recyclerView: RecyclerView?,
                    position: Int,
                    v: View
                ) {
                    Toast.makeText(
                        v.context,
                        "Oh le joli test",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    /**
     * Create the loan alertDialog popup
     * @param contribution is the
     * @param rate
     * @param duration
     */
    private fun createLoanAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setCancelable(true)
        val layoutInflater = LayoutInflater.from(this)
        val dialogView = layoutInflater.inflate(R.layout.add_loan_alert_dialog, null)
        alertDialogBuilder.setView(dialogView)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        activateButtonListeners(dialogView, alertDialog)
        mIsLoanAlertDialogDisplayed = true
    }

    /**
     * Alert dialog buttons management
     */
    fun activateButtonListeners(dialogView: View, alertDialog: AlertDialog) {
        val btn_loaner = dialogView.findViewById<Button>(R.id.btn_loaner)
        btn_loaner.setOnClickListener{
            startAddLoanActivity("lend")
        }
        val btn_borrower= dialogView.findViewById<Button>(R.id.btn_borrower)
        btn_borrower.setOnClickListener{
            startAddLoanActivity("borrow")
        }
        val btn_delivery= dialogView.findViewById<Button>(R.id.btn_delivery)
        btn_delivery.setOnClickListener{
            startAddLoanActivity("delivery")
        }
        val btn_cancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        btn_cancel.setOnClickListener {
            alertDialog.cancel()
            mIsLoanAlertDialogDisplayed = false
        }
    }

    /**
     * This method starts the AddLoan activity
     */
    fun startAddLoanActivity(type: String) {
        val intent = Intent(this, AddLoanActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }

    /**
     * This method indicates when the adapter needs to start listening
     */
    override fun onStart() {
        super.onStart()
        mAdapter!!.startListening()
    }

    /**
     * This method indicates when the adapter needs to stop listening
     */
    override fun onStop() {
        super.onStop()
        mAdapter!!.stopListening()
    }


}