package com.depuisletemps.beback.controller.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.depuisletemps.beback.R
import com.depuisletemps.beback.view.customview.ViewPagerAdapter
import com.depuisletemps.beback.utils.Constant
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_loan_pager.*
import java.lang.reflect.Method

class LoanPagerActivity: BaseActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var mArchiveButton: MenuItem
    lateinit var mPendingButton: MenuItem
    var mFilterProduct:String? = null
    var mFilterRecipient:String? = null
    var mFilterType:String? = null
    private var mIsLoanAlertDialogDisplayed:Boolean = false
    var mMode: String = Constant.STANDARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_pager)

        configureMainToolbar()
        mMode = getLoanMode(savedInstanceState)
        configurePager()
        checkScreenSideForFilter()

        mBtnAdd.setOnClickListener{createLoanAlertDialog()}
        mBtnFilter.setOnClickListener{startFilterActivity()}
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mIsLoanAlertDialogDisplayed) outState.putBoolean(Constant.LOAN_ALERTDIALOG_DISPLAYED, true)
        outState.putString(Constant.FILTER_PRODUCT, mFilterProduct)
        outState.putString(Constant.FILTER_RECIPIENT, mFilterRecipient)
        outState.putString(Constant.FILTER_TYPE, mFilterType)
        outState.putString(Constant.MODE, mMode)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(Constant.LOAN_ALERTDIALOG_DISPLAYED)) createLoanAlertDialog()
            mFilterType = savedInstanceState.getString(Constant.FILTER_TYPE)
            mFilterRecipient = savedInstanceState.getString(Constant.FILTER_RECIPIENT)
            mFilterProduct = savedInstanceState.getString(Constant.FILTER_PRODUCT)
        }
    }

    override fun onPrepareOptionsPanel(view: View?, menu: Menu): Boolean {
        // Allows to have the icon on the toolbar sub-menu
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val m: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible", java.lang.Boolean.TYPE
                )
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: Exception) {
                Log.e(
                    javaClass.simpleName,
                    "onMenuOpened...unable to set icons for overflow menu",
                    e
                )
            }
        }
        return super.onPrepareOptionsPanel(view, menu!!)
    }

    /**
     * This method check if we come from the filter screen and set the viewpager accordingly
     */
    private fun checkScreenSideForFilter() {
        if (intent.extras?.getInt(Constant.SIDE) != 0) {
            viewPager.currentItem = 1
            intent.extras?.putInt(Constant.SIDE, 0)
        }
    }

    /**
     * This method returns the mode from the bundle
     * @return the placeId or null
     */
    private fun getLoanMode(savedInstanceState: Bundle?): String {
        if (savedInstanceState != null) return savedInstanceState.getString(Constant.MODE)!!

        if (intent.extras?.getString(Constant.MODE) != null) return intent.extras.getString(Constant.MODE)!!

        return Constant.STANDARD
    }

    /**
     * This method sets the ViewPager
     */
    private fun configurePager() {
        if (this.intent.extras != null)
            if (this.intent.extras?.getString(Constant.FILTERS) == Constant.YES) {
                if (this.intent.extras?.getString(Constant.FILTER_TYPE) != null) mFilterType = this.intent.extras.getString(Constant.FILTER_TYPE)
                if (this.intent.extras?.getString(Constant.FILTER_PRODUCT) != null) mFilterProduct = this.intent.extras.getString(Constant.FILTER_PRODUCT)
                if (this.intent.extras?.getString(Constant.FILTER_RECIPIENT) != null) mFilterRecipient = this.intent.extras.getString(Constant.FILTER_RECIPIENT)

                btn_erase_filter.visibility = View.VISIBLE

                btn_erase_filter.setOnClickListener {
                    intent.removeExtra(Constant.FILTERS)
                    btn_erase_filter.visibility = View.INVISIBLE
                    mFilterProduct = null
                    mFilterRecipient = null
                    mFilterType = null
                    val newPage = viewPager.currentItem
                    configurePager()
                    viewPager.currentItem = newPage
                }
            }

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
    private fun configureMainToolbar() {
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
        mPendingButton = menu.getItem(1)

        if (mMode == Constant.STANDARD) {
            mPendingButton.setVisible(false)
            mArchiveButton.setVisible(true)
        }
        if (mMode == getString(R.string.archive)) {
            mPendingButton.setVisible(true)
            mArchiveButton.setVisible(false)
        }

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * This method manages the clicks on the toolbar icons
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_archive -> {
            switchArchiveStandardMode()
            true
        }
        R.id.menu_pending-> {
            switchArchiveStandardMode()
            true
        }
        R.id.menu_profile ->{
            startProfileActivity()
            true
        }
        R.id.menu_about->{
            startAboutActivity()
            true
        }
        R.id.menu_logout->{
            displayCustomToast(getString(R.string.sign_out_message, getCurrentUser()!!.displayName.toString()), R.drawable.bubble_1, this)
            Snackbar.make(activity_loan_pager, R.string.logout, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo)) {
                }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onShown(transientBottomBar: Snackbar?) {
                    }

                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            signOutUserFromFirebase(this@LoanPagerActivity)
                        }
                    }
                }).show()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Switch from archive to standard / standard to archive mode
     */
    private fun switchArchiveStandardMode() {
        when (mMode) {
            Constant.STANDARD -> enableArchiveMode()
            getString(R.string.archive) -> disableArchiveMode()
        }
        displayCorrectPage()
    }

    /**
     * Configure and displays the right page
     */
    private fun displayCorrectPage() {
        if (viewPager.currentItem == 0) {
            configurePager()
            viewPager.currentItem = 0
        } else {
            configurePager()
            viewPager.currentItem = 1
        }
    }

    /**
     * Enable archive mode
     */
    fun enableArchiveMode() {
        mMode = getString(R.string.archive)
        mPendingButton.setVisible(true)
        mArchiveButton.setVisible(false)
    }

    /**
     * Disable archive mode
     */
    fun disableArchiveMode() {
        mMode = Constant.STANDARD
        mPendingButton.setVisible(false)
        mArchiveButton.setVisible(true)
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
        alertDialog.setOnCancelListener(DialogInterface.OnCancelListener {
            mIsLoanAlertDialogDisplayed = false
        })
        alertDialog.show()

        activateButtonListeners(dialogView, alertDialog)
        mIsLoanAlertDialogDisplayed = true
    }

    /**
     * Alert dialog buttons management
     */
    private fun activateButtonListeners(dialogView: View, alertDialog: AlertDialog) {
        val btnLoaner = dialogView.findViewById<Button>(R.id.btn_loaner)
        btnLoaner.setOnClickListener{
            startAddLoanActivity(Constant.LENDING)
        }
        val btnBorrower= dialogView.findViewById<Button>(R.id.btn_borrower)
        btnBorrower.setOnClickListener{
            startAddLoanActivity(Constant.BORROWING)
        }
        val btnDelivery= dialogView.findViewById<Button>(R.id.btn_delivery)
        btnDelivery.setOnClickListener{
            startAddLoanActivity(Constant.DELIVERY)
        }
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            mIsLoanAlertDialogDisplayed = false
            alertDialog.cancel()
        }
    }

    /**
     * This method starts the AddLoan activity
     */
    private fun startAddLoanActivity(type: String) {
        val intent = Intent(this, AddLoanActivity::class.java)
        intent.putExtra(Constant.TYPE, type)
        startActivity(intent)
    }

    /**
     * This method starts the Profile activity
     */
    private fun startProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    /**
     * This method starts the Profile activity
     */
    private fun startAboutActivity() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    /**
     * This method starts the Filter activity
     */
    private fun startFilterActivity() {
        val intent = Intent(this, FilterActivity::class.java)
        intent.putExtra(Constant.MODE, mMode)
        intent.putExtra(Constant.SIDE, viewPager.currentItem)
        startActivity(intent)
    }
}