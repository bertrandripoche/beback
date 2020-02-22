package com.depuisletemps.beback.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.depuisletemps.beback.R
import com.depuisletemps.beback.ui.customview.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_loan_pager.*


class LoanPagerActivity: BaseActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var mArchiveButton: MenuItem
    lateinit var mProfileButton: MenuItem
    var mIsLoanAlertDialogDisplayed:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_pager)

        configureToolbar()
        configurePager()

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
     * This method sets the ViewPager
     */
    private fun configurePager() {
        if (viewPager != null) {
            val adapter = ViewPagerAdapter(this.supportFragmentManager)
            viewPager.adapter = adapter
            viewPager.setPagingEnabled(false)
        }

        tabLayout.setupWithViewPager(viewPager)
        val imageResId = intArrayOf(R.drawable.ic_things, R.drawable.ic_people)

        for (i in 0 until imageResId.size) {
            val view: View = layoutInflater.inflate(R.layout.custom_tab, null)
            val tab = tabLayout.getTabAt(i)
            view.findViewById<View>(R.id.icon).setBackgroundResource(imageResId.get(i))
            if (tab != null) tab.customView = view
        }
    }

    /**
     * This method configures the toolbar
     */
    private fun configureToolbar() {
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
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
     * This method manages the clicks on the toolbar icons
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_archive -> {
            Toast.makeText(this,"Archive action", Toast.LENGTH_LONG).show()
            true
        }
        R.id.menu_profile ->{
            Toast.makeText(this,"Profile action", Toast.LENGTH_LONG).show()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
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
}