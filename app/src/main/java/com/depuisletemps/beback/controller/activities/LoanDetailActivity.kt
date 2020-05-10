package com.depuisletemps.beback.controller.activities

import android.app.DatePickerDialog
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.FieldType
import com.depuisletemps.beback.model.api.LoanHelper
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.view.customview.CategoryAdapter
import com.depuisletemps.beback.utils.NotificationManagement
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.StringUtils
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.getStringFromDate
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_loan_detail.*
import kotlinx.android.synthetic.main.activity_loan_detail.loan_due_date
import kotlinx.android.synthetic.main.activity_loan_detail.loan_due_date_title
import kotlinx.android.synthetic.main.activity_loan_detail.loan_notif_date
import kotlinx.android.synthetic.main.activity_loan_detail.loan_notif_title
import kotlinx.android.synthetic.main.activity_loan_detail.loan_product
import kotlinx.android.synthetic.main.activity_loan_detail.loan_recipient
import kotlinx.android.synthetic.main.activity_loan_detail.mBtnCancelDate
import kotlinx.android.synthetic.main.activity_loan_detail.mBtnCancelNotif
import kotlinx.android.synthetic.main.activity_loan_detail.mBtnPick
import kotlinx.android.synthetic.main.activity_loan_detail.notif_d_day
import kotlinx.android.synthetic.main.activity_loan_detail.notif_one_week
import kotlinx.android.synthetic.main.activity_loan_detail.notif_three_days
import kotlinx.android.synthetic.main.activity_loan_detail.spinner_loan_categories
import kotlinx.android.synthetic.main.activity_loan_detail.toggle_btns
import org.joda.time.LocalDate
import java.text.DecimalFormat

class LoanDetailActivity: BaseFormActivity() {
    private val TAG = "LoanDetailActivity"
    var mWhat:String = ""
    var mWho:String = ""
    var mProductCategory:String = ""
    var mDue:String = ""
    var mNotif:String? = null
    var mCurrentProductCategory:String = ""
    var mCurrentDue:String = ""
    var mLoanId: String = ""
    var mLoan: Loan? = null
    var mFirstTime: Boolean = true
    lateinit var mCategories: Array<String>
    lateinit var mCategoriesIcons: TypedArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_detail)

        defineTheColors(this)
        mCategories = this.resources.getStringArray(R.array.product_category)
        mCategoriesIcons = this.resources.obtainTypedArray(R.array.product_category_icon)

        configureButtons()
        configureToolbar()
        configureSpinner()
        configureTextWatchers()

        getLoan()
        getSavedInstanceData(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (loan_due_date.text != "") outState.putString(Constant.DUE_DATE_SET, loan_due_date.text.toString())
        else outState.putString(Constant.DUE_DATE_SET, Constant.FAR_AWAY_DATE)
        outState.putString(Constant.MWHAT, mWhat)
        outState.putString(Constant.MWHO, mWho)
        outState.putString(Constant.WHATSAVED, loan_product.text.toString())
        outState.putString(Constant.WHOSAVED, loan_recipient.text.toString())
        outState.putString(Constant.DUESAVED, mDue)
        outState.putString(Constant.PRODUCT_CATEGORYSAVED, mCategories[spinner_loan_categories.selectedItemPosition])
        outState.putString(Constant.LOAN_ID, mLoanId)
        outState.putString(Constant.NOTIF_DATE, mNotif)
    }

    private fun getSavedInstanceData(savedInstanceState: Bundle?) {
        if (savedInstanceState != null){
            mFirstTime = false
            if (savedInstanceState.getString(Constant.DUE_DATE_SET) != Constant.FAR_AWAY_DATE)
                setPickDate(savedInstanceState.getString("dueDateSet"), getString(R.string.due))
            if (savedInstanceState.getString(Constant.MWHAT) != null) mWhat = savedInstanceState.getString(Constant.MWHAT)!!
            if (savedInstanceState.getString(Constant.MWHO) != null) mWho = savedInstanceState.getString(Constant.MWHO)!!
            if (savedInstanceState.getString(Constant.WHATSAVED) != null) loan_product.setText(savedInstanceState.getString(Constant.WHATSAVED)!!)
            if (savedInstanceState.getString(Constant.WHOSAVED) != null) loan_recipient.setText(savedInstanceState.getString(Constant.WHOSAVED)!!)
            if (savedInstanceState.getString(Constant.PRODUCT_CATEGORYSAVED) != null) mCurrentProductCategory = savedInstanceState.getString(Constant.PRODUCT_CATEGORYSAVED)!!
            if (savedInstanceState.getString(Constant.DUESAVED) != null) mCurrentDue = savedInstanceState.getString(Constant.DUESAVED)!!
            if (savedInstanceState.getString(Constant.LOAN_ID) != null) mLoanId = savedInstanceState.getString(Constant.LOAN_ID)!!
            if (savedInstanceState.getString(Constant.NOTIF_DATE) != null) mNotif = savedInstanceState.getString(Constant.NOTIF_DATE)!!
            setFloatBtnState(isFormValid(),mBtnEdit, this)
            setEditFieldsTextColor()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Configure the different buttons on the activity
     */
    private fun configureButtons() {
        mBtnEdit.setOnClickListener{
            if (isFormValid()) editFirestoreLoan()
            else  Toast.makeText(applicationContext, R.string.invalid_edit_form, Toast.LENGTH_LONG).show()
        }

        setButtonOnClickListener(notif_d_day)
        setButtonOnClickListener(notif_three_days)
        setButtonOnClickListener(notif_one_week)
        setFloatBtnState(isFormValid(),mBtnEdit, this)

        mBtnDelete.setOnClickListener{deleteTheLoan(mLoan!!)}
        mBtnUnarchive.setOnClickListener{unarchiveTheLoan(mLoan!!)}
    }

    /**
     * This method configures the spinner for the product type
     */
    private fun configureSpinner() {
        val spinner = findViewById<View>(R.id.spinner_loan_categories) as Spinner

        val categoryAdapter = CategoryAdapter(applicationContext, mCategoriesIcons, mCategories, "small")
        spinner.adapter = categoryAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                setFloatBtnState(isFormValid(),mBtnEdit, applicationContext)
                if (mLoan?.returned_date == null) {
                    if (spinner.selectedView != null) {
                        val text: TextView =
                            (spinner.selectedView as ConstraintLayout).getViewById(R.id.text_category) as TextView
                        if (position != mCategories.indexOf(mProductCategory)) text.setTextColor(Color.BLACK)
                        else text.setTextColor(greyColor)
                    }
                }
            }
        }

    }

    /**
     * Method to configure the textWatchers on the fields which requires it
     */
    private fun configureTextWatchers() {
        loan_product.addTextChangedListener(textWatcher)
        loan_recipient.addTextChangedListener(textWatcher)
    }

    /**
     * This method fills the existing information for th loan
     * @param loan is the Loan object representing the current loan data
     */
    private fun configureScreen(loan: Loan?) {
        if (loan != null) {
            configureType(loan.type, true)

            if (loan.returned_date != null) {
                setArchiveScreen(loan)
                setDueDateField(loan)
                generateFeedback()
            } else {
                mBtnEdit.visibility = View.VISIBLE
                if (getStringFromDate(loan.due_date?.toDate()) != Constant.FAR_AWAY_DATE && mFirstTime) setDueDate(getStringFromDate(loan.due_date?.toDate()))
                if (mFirstTime) loan_product.setText(loan.product)
                if (mFirstTime) loan_recipient.setText(loan.recipient)
                loan_creation_date.setTextColor(greyColor)
                setNotifField(loan)
                disableFloatButton(mBtnEdit, this)
            }

            if (mFirstTime) spinner_loan_categories.setSelection(Utils.getIndexFromCategory(loan.product_category, this))
            else spinner_loan_categories.setSelection(Utils.getIndexFromCategory(mCurrentProductCategory, this))
            loan_creation_date.text = getStringFromDate(loan.creation_date?.toDate())
        }

        configureAutoCompleteFields(loan_product,loan_recipient, true,2)
    }

    /**
     * Correctly displays the notif field
     */
    private fun setNotifField(loan: Loan) {
        when (loan.notif) {
            null -> {
                if (loan_due_date.text != "")
                    displayToggleButtons()
                checkNotifBtns()
            }
            else ->  setNotifDate(loan.notif!!)
        }
    }

    /**
     * Choose the appropriate feedback to display for ended loans
     */
    private fun generateFeedback() {
        if (loan_due_date.text != "") {
            feedback.visibility = View.VISIBLE
            val dueDateLocalDate = Utils.getLocalDateFromString(loan_due_date.text.toString())
            val returnedLocalDate = Utils.getLocalDateFromString(loan_returned_date.text.toString())
            val daysDiff: Int = Utils.getDifferenceDays(returnedLocalDate, dueDateLocalDate)

            when {
                daysDiff < -7 -> displayEvaluationMessage(redColor, R.drawable.bubble_4, R.string.angry)
                daysDiff < 0 -> displayEvaluationMessage(orangeColor, R.drawable.bubble_2, R.string.fine)
                else -> displayEvaluationMessage(greenColor, R.drawable.bubble_1, R.string.happy)
            }
        } else displayEvaluationMessage(greenColor, R.drawable.bubble_1, R.string.happy)
    }

    /**
     * Correctly displays the due date field
     */
    private fun setDueDateField(loan: Loan) {
        if (getStringFromDate(loan.due_date?.toDate()) == Constant.FAR_AWAY_DATE) {
            loan_due_date_title.visibility = View.GONE
            loan_due_date.visibility = View.GONE
        } else {
            loan_due_date.setTextColor(blackColor)
            loan_due_date.text = getStringFromDate(loan.due_date?.toDate())
            mBtnCancelDate.visibility = View.GONE
        }
    }

    /**
     * Displays the required feedback for returned loans
     */
    private fun displayEvaluationMessage(color: Int, img: Int, text: Int) {
        loan_returned_date.setTextColor(color)
        feedback.setTextColor(color)
        feedback.setBackgroundResource(img)
        feedback.setText(text)
    }

    /**
     * Set the screen for returned loan
     * @param loan is a Loan object representing the current described loan
     */
    private fun setArchiveScreen(loan: Loan) {
        mBtnDelete.visibility = View.VISIBLE
        mBtnUnarchive.visibility = View.VISIBLE
        loan_product.keyListener = null
        loan_product.setBackgroundColor(blueDeeperColor)
        loan_recipient.keyListener = null
        loan_recipient.setBackgroundColor(blueDeeperColor)
        spinner_loan_categories.setBackgroundColor(blueDeeperColor)
        spinner_loan_categories.isEnabled = false
        mBtnPick.visibility = View.GONE
        loan_returned_date_title.visibility = View.VISIBLE
        loan_returned_date.visibility = View.VISIBLE
        loan_returned_date.text = getStringFromDate(loan.returned_date?.toDate())
        loan_product.setText(loan.product)
        loan_recipient.setText(loan.recipient)
        loan_notif_date.visibility = View.GONE
        loan_notif_title.visibility = View.GONE
        mBtnPick.visibility = View.GONE
    }

    /**
     * Method to describe the actions to complete on text writing
     */
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) {
            setFloatBtnState(isFormValid(),mBtnEdit, applicationContext)
            setEditFieldsTextColor()
        }
    }

    /**
     * This method gets loan info
     */
    private fun getLoan() {
        val i = intent
        mLoanId = i.extras?.getString(Constant.LOAN_ID) ?: ""
        val loanHelper = LoanHelper()
        loanHelper.getLoan(mLoanId) {loan ->
            loan?.let {
                mLoan = loan
                mProductCategory = loan.product_category
                mWhat = loan.product
                mWho = loan.recipient
                mDue = getStringFromDate(loan.due_date?.toDate())
                mNotif = loan.notif
            }
            configureScreen(loan)
        }
    }

    /**
     * This method enable/disable the edit button
     */
    fun setEditFieldsTextColor() {
        if (loan_product.text.toString() != mWhat || mLoan?.returned_date != null) loan_product.setTextColor(blackColor) else loan_product.setTextColor(greyColor)
        if (loan_recipient.text.toString() != mWho || mLoan?.returned_date != null) loan_recipient.setTextColor(blackColor) else loan_recipient.setTextColor(greyColor)
        if (loan_due_date.text != mDue || mLoan?.returned_date != null) loan_due_date.setTextColor(blackColor) else loan_due_date.setTextColor(greyColor)
    }

    /**
     * Method to tell if the form is valid
     */
    fun isFormValid(): Boolean {
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)

        if (loan_returned_date == null) {
            if (loan_due_date.text.toString() != (mDue)) loan_due_date.setTextColor(blackColor)
            else loan_due_date.setTextColor(darkGreyColor)
        }

        val currentNotif: String? = when {
            notif_d_day.isChecked -> if (loan_due_date.text.toString() != "") Utils.getStringFromLocalDate(getNotifDate()) else null
            notif_three_days.isChecked -> if (loan_due_date.text.toString() != "") Utils.getStringFromLocalDate(getNotifDate()) else null
            notif_one_week.isChecked -> if (loan_due_date.text.toString() != "") Utils.getStringFromLocalDate(getNotifDate()) else null
            loan_notif_date.text.toString() != "" -> loan_notif_date.text.toString()
            else -> null
        }

        return loan_product.text.toString() != (mWhat)
                || loan_recipient.text.toString() != (mWho)
                || (loan_due_date.text.toString() != (mDue) && loan_due_date.text.toString() != (""))
                || (mDue != Constant.FAR_AWAY_DATE && loan_due_date.text.toString() == "")
                || categories[spinner_loan_categories.selectedItemPosition] != mProductCategory
                || currentNotif != mNotif
    }

    /**
     * This method displays the DatePicker
     */
    fun clickDataPicker(view: View) {
        val df = DecimalFormat("00")
        val today = LocalDate.now()
        val year:Int = today.year
        val month:Int = today.monthOfYear-1
        val day = today.dayOfMonth

        val btn: String = when (view.id) {
            R.id.mBtnPick -> getString(R.string.due)
            else -> getString(R.string.notif)
        }

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            setPickDate(getString(R.string.due_date, df.format(dayOfMonth), df.format(monthOfYear+1), year), btn)
            setFloatBtnState(isFormValid(),mBtnEdit, this)
        }, year, month, day)

        dpd.datePicker.minDate = System.currentTimeMillis()
        dpd.show()
    }

    /**
     * This method set the date picked in the accurate field
     */
    private fun setPickDate(date: String?, btn: String) {
        if (date != null) {
            when (btn) {
                getString(R.string.due) ->  setDueDate(date)
                getString(R.string.notif) -> setNotifDate(date)
            }
            unsetToggle(notif_d_day)
            unsetToggle(notif_three_days)
            unsetToggle(notif_one_week)
            setToggleButtons()
            checkNotifBtns()
        }
    }

    /**
     * This method set a due date
     */
    private fun setDueDate(date: String) {
        loan_due_date.text = date
        loan_due_date.setBackgroundColor(blueColor)
        if (loan_due_date.text == mDue) loan_due_date.setTextColor(greyColor)
        else loan_due_date.setTextColor(blackColor)
        mBtnCancelDate.visibility = View.VISIBLE
    }

    /**
     * This method set a notif date
     */
    private fun setNotifDate(date: String) {
        loan_notif_date.text = date
        if (loan_notif_date.text == mNotif) loan_notif_date.setTextColor(greyColor)
        else loan_notif_date.setTextColor(blackColor)
        loan_notif_date.setBackgroundColor(blueColor)
        mBtnCancelNotif.visibility = View.VISIBLE
    }

    /**
     * Method to choose to display/hide the toggle notification buttons
     */
    private fun setToggleButtons() {
        if (loan_due_date.text != "" && loan_notif_date.text == "") displayToggleButtons()
        else hideToggleButtons()
        enableNotifBtn()
    }

    /**
     * Method to display the toggle notification buttons
     */
    private fun displayToggleButtons() {
        loan_notif_date.visibility = View.INVISIBLE
        toggle_btns.visibility = View.VISIBLE
    }

    /**
     * Method to display the toggle notification buttons
     */
    private fun hideToggleButtons() {
        loan_notif_date.visibility = View.VISIBLE
        toggle_btns.visibility = View.INVISIBLE
    }

    /**
     * Method to check if the notifs buttons should be switched off
     */
    private fun checkNotifBtns() {
        if (loan_due_date.text != "") {
            val dueDate = Utils.getLocalDateFromString(loan_due_date.text.toString())
            val today = LocalDate.now()
            if (Utils.getDifferenceDays(today, dueDate) < 1) disableToggle(notif_d_day)
            if (Utils.getDifferenceDays(today, dueDate) < 4) disableToggle(notif_three_days)
            if (Utils.getDifferenceDays(today, dueDate) < 8) disableToggle(notif_one_week)
        }
    }

    /**
     * This methods disables the notif button
     */
    private fun enableNotifBtn() {
        unsetToggle(notif_d_day)
        unsetToggle(notif_three_days)
        unsetToggle(notif_one_week)
    }

    /**
     * This method empties the due date field
     */
    fun cancelDate(view: View) {
        val btn: String = when (view.id) {
            R.id.mBtnCancelDate -> getString(R.string.due)
            else -> getString(R.string.notif)
        }

        when (btn) {
            getString(R.string.due) -> {
                loan_due_date.text = ""
                loan_due_date.setBackgroundColor(blueDeeperColor)
                mBtnCancelDate.visibility = View.GONE
            }
            getString(R.string.notif) -> {
                loan_notif_date.text = ""
                loan_notif_date.setBackgroundColor(blueDeeperColor)
                mBtnCancelNotif.visibility = View.GONE
            }
        }
        setFloatBtnState(isFormValid(),mBtnEdit, this)
        setToggleButtons()
        checkNotifBtns()
    }

    /**
    * This method edits a loan entry in the Firebase database "loans" collection
    */
    private fun editFirestoreLoan(){
        val newLoan = mLoan!!
        val categories: Array<String> = this.resources.getStringArray(R.array.product_category)

        val currentNotif: String? = when {
            notif_d_day.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            notif_three_days.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            notif_one_week.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            loan_notif_date.text.toString() != "" -> loan_notif_date.text.toString()
            else -> null
        }

        if (categories[spinner_loan_categories.selectedItemPosition] != mProductCategory) newLoan.product_category = categories[spinner_loan_categories.selectedItemPosition]
        if (loan_recipient.text.toString() != mWho) newLoan.recipient = StringUtils.capitalizeWords(loan_recipient.text.toString(), FieldType.NAME)
        if (loan_product.text.toString() != mWhat) newLoan.product= StringUtils.capitalizeWords(loan_product.text.toString(), FieldType.PRODUCT)
        if (loan_due_date.text.toString() == "") newLoan.due_date = Utils.getTimeStampFromString(Constant.FAR_AWAY_DATE)
        if (loan_due_date.text.toString() != "") newLoan.due_date = Utils.getTimeStampFromString(loan_due_date.text.toString())
        if (loan_due_date.text.toString() != mDue || (currentNotif != mNotif || loan_due_date.text.toString() != mDue)) newLoan.notif = currentNotif
        val oldRecipient = when (loan_recipient.text.toString()) {
                mWho -> null
                else -> mWho
        }

        val loanHelper = LoanHelper()
        loanHelper.editLoan(newLoan,oldRecipient) {result ->
            if (result) {
                displayCustomToast(getString(R.string.saved),R.drawable.bubble_3,this)
                if (currentNotif != mNotif) {
                    NotificationManagement.stopAlarm(mLoan!!.id, mLoan!!.product, mLoan!!.type, mLoan!!.recipient, this, this)
                    NotificationManagement.createNotification(newLoan.id, newLoan.product, newLoan.type, newLoan.recipient, getNotifDate(), this, this)
                }
                startLoanPagerActivity(Constant.STANDARD)
            } else displayCustomToast(getString(R.string.transaction_failure), R.drawable.bubble_3, this)
        }
    }

    /**
     * This method allows to get the notif date from screen
     * @return the LocalDate correponsding to the notif date
     */
    private fun getNotifDate(): LocalDate {
        return if (loan_notif_date.text.toString() != "") Utils.getLocalDateFromString(loan_notif_date.text.toString())
        else {
            val returnDate: LocalDate = Utils.getLocalDateFromString(loan_due_date.text.toString())
            when {
                notif_three_days.isChecked -> returnDate.minusDays(3)
                notif_one_week.isChecked -> returnDate.minusDays(7)
                else -> returnDate
            }
        }
    }

    /**
     * This method deletes the item
     * @param loan is a Loan representing the loan object
     */
    private fun deleteTheLoan(loan: Loan) {
        var points = Utils.retrievePointsFromLoan(loan)

        val loanHelper = LoanHelper()
        loanHelper.deleteLoan(loan, points) {result, loanId ->
            if (result) {
                displayCustomToast(getString(R.string.deleted_message, loan.product), R.drawable.bubble_3, this)
                NotificationManagement.stopAlarm(loan.id, loan.product, loan.type, loan.recipient, this, this)

                Snackbar.make(activity_loan_detail, loan.product, Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo)) {
                        undeleteTheLoan(loan, points)
                    }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onShown(transientBottomBar: Snackbar?) {}
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) startLoanPagerActivity(getString(R.string.archive))
                        }
                    }).show()
            } else displayCustomToast(getString(R.string.error_adding_loan), R.drawable.bubble_3, this)
        }
    }

    /**
     * This method undeletes the previously detetedItem
     * @param loan is a Loan representing the loan object
     *  @param points is the number of points affected to the deleted loan
     */
    private fun undeleteTheLoan(loan: Loan, points: Long) {
        val loanHelper = LoanHelper()
        loanHelper.undeleteLoan(loan, points) {result ->
            if (result) displayCustomToast(getString(R.string.undeleted_message, loan.product), R.drawable.bubble_4,this)
            else displayCustomToast(getString(R.string.error_undeleting_loan), R.drawable.bubble_3, this)
        }
    }

    /**
     * This method unarchives the selected item
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun unarchiveTheLoan(loan: Loan) {
        val loanHelper = LoanHelper()
        loanHelper.unarchiveLoan(loan) {result ->
            if (result) {
                if (loan.type == (LoanType.DELIVERY.type)) displayCustomToast(getString(R.string.not_received_message, loan.product), R.drawable.bubble_2, this)
                else displayCustomToast(getString(R.string.unarchived_message, loan.product), R.drawable.bubble_2, this)
            } else displayCustomToast(getString(R.string.error_undeleting_loan), R.drawable.bubble_3, this)
        }

        Snackbar.make(activity_loan_detail, loan.product, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo)) {
                rearchiveTheLoan(loan)
            }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {}
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) startLoanPagerActivity(Constant.STANDARD)
                }
            }).show()
    }

    /**
     * This method archives the selected item
     * @param loan is a Loan representing the loan object
     */
    private fun rearchiveTheLoan(loan: Loan) {
        val loanHelper = LoanHelper()
        loanHelper.archiveLoan(loan) {result ->
            if (result) {
                if (loan.type == (LoanType.DELIVERY.type)) displayCustomToast(getString(R.string.received_message, loan.product), R.drawable.bubble_1, this)
                else displayCustomToast(getString(R.string.archived_message, loan.product), R.drawable.bubble_1, this)
            } else displayCustomToast(getString(R.string.error_undeleting_loan), R.drawable.bubble_3, this)
        }
    }

    /**
     * This method allows to set a listener on a button
     * @param btn being the button on which to set the listener
     */
    override fun setButtonOnClickListener(btn: ToggleButton) {
        super.setButtonOnClickListener(btn)
        setFloatBtnState(isFormValid(),mBtnEdit, this)
    }
}