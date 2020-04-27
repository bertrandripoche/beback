package com.depuisletemps.beback.controller.activities

import android.Manifest
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.interfaces.NotifyDetailActivity
import com.depuisletemps.beback.model.api.LoanHelper
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanAction
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.view.customview.CategoryAdapter
import com.depuisletemps.beback.utils.NotificationManagement
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.getStringFromDate
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_loan_detail.*
import kotlinx.android.synthetic.main.activity_loan_detail.loan_due_date
import kotlinx.android.synthetic.main.activity_loan_detail.loan_due_date_title
import kotlinx.android.synthetic.main.activity_loan_detail.loan_notif_date
import kotlinx.android.synthetic.main.activity_loan_detail.loan_notif_title
import kotlinx.android.synthetic.main.activity_loan_detail.loan_product
import kotlinx.android.synthetic.main.activity_loan_detail.loan_recipient
import kotlinx.android.synthetic.main.activity_loan_detail.loan_recipient_title
import kotlinx.android.synthetic.main.activity_loan_detail.loan_type
import kotlinx.android.synthetic.main.activity_loan_detail.loan_type_pic
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

class LoanDetailActivity: BaseActivity() {
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
    val mUser: FirebaseUser? = getCurrentUser()
    var yellowColor: Int = 0
    var orangeColor: Int = 0
    var blueDeeperColor: Int = 0
    var blackColor: Int = 0
    var redColor: Int = 0
    var greenColor: Int = 0
    var darkGreyColor: Int = 0
    private var lightGreyColor = 0
    private var greyColor = 0
    private var blueColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_detail)
        defineColors()
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
            setEditBtnState()
            setEditFieldsTextColor()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun defineColors() {
        yellowColor = ContextCompat.getColor(this, R.color.secondaryColor)
        orangeColor = ContextCompat.getColor(this, R.color.secondaryDarkColor)
        blueDeeperColor = ContextCompat.getColor(this, R.color.primaryColor)
        darkGreyColor = ContextCompat.getColor(this, R.color.dark_grey)
        blackColor = ContextCompat.getColor(this, R.color.black)
        greenColor = ContextCompat.getColor(this, R.color.green)
        redColor = ContextCompat.getColor(this, R.color.red)
        lightGreyColor = ContextCompat.getColor(this, R.color.light_grey)
        greyColor = ContextCompat.getColor(this, R.color.dark_grey)
        blueColor = ContextCompat.getColor(this, R.color.primaryLightColor)

    }

    private fun configureButtons() {
        mBtnEdit.setOnClickListener{
            if (isFormValid())
                editFirestoreLoan()
            else {
                Toast.makeText(applicationContext, R.string.invalid_edit_form, Toast.LENGTH_LONG)
                    .show()
            }
        }

        notif_d_day.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notif_d_day.setBackgroundColor(yellowColor)
                if (notif_three_days.isChecked) unsetToggle(notif_three_days)
                if (notif_one_week.isChecked) unsetToggle(notif_one_week)
            } else {
                notif_d_day.setBackgroundColor(lightGreyColor)
            }
            setEditBtnState()
        })

        notif_three_days.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notif_three_days.setBackgroundColor(yellowColor)
                if (notif_one_week.isChecked) unsetToggle(notif_one_week)
                if (notif_d_day.isChecked) unsetToggle(notif_d_day)
            } else {
                notif_three_days.setBackgroundColor(lightGreyColor)
            }
            setEditBtnState()
        })

        notif_one_week.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notif_one_week.setBackgroundColor(yellowColor)
                if (notif_three_days.isChecked) unsetToggle(notif_three_days)
                if (notif_d_day.isChecked) unsetToggle(notif_d_day)
            } else {
                notif_one_week.setBackgroundColor(lightGreyColor)
            }
            setEditBtnState()
        })

        mBtnDelete.setOnClickListener{deleteTheLoan(mLoan!!)}
        mBtnUnarchive.setOnClickListener{unarchiveTheLoan(mLoan!!)}
    }

    /**
     * This method configuree the spinner
     */
    private fun configureSpinner() {
        val spinner = findViewById<View>(R.id.spinner_loan_categories) as Spinner

        val categoryAdapter = CategoryAdapter(applicationContext, mCategoriesIcons, mCategories, "small")
        spinner.adapter = categoryAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                setEditBtnState()
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
    fun configureTextWatchers() {
        loan_product.addTextChangedListener(textWatcher)
        loan_recipient.addTextChangedListener(textWatcher)
    }

    /**
     * This method fills the existing information for th loan
     * @param loan is the Loan object representing the current loan data
     */
    private fun configureScreen(loan: Loan?) {

        if (loan != null) {
            configureType(loan)

            if (loan.returned_date != null) {
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
                loan_recipient.setText(loan.recipient_id)

                if (getStringFromDate(loan.due_date?.toDate()) == Constant.FAR_AWAY_DATE) {
                    loan_due_date_title.visibility = View.GONE
                    loan_due_date.visibility = View.GONE
                } else {
                    loan_due_date.setTextColor(blackColor)
                    loan_due_date.text = getStringFromDate(loan.due_date?.toDate())
                    mBtnCancelDate.visibility = View.GONE
                }

                if (loan_due_date.text != "") {
                    feedback.visibility = View.VISIBLE
                    val dueDateLocalDate = Utils.getLocalDateFromString(loan_due_date.text.toString())
                    val returnedLocalDate = Utils.getLocalDateFromString(loan_returned_date.text.toString())
                    val daysDiff: Int = Utils.getDifferenceDays(returnedLocalDate, dueDateLocalDate)

                    if (daysDiff < -7) {
                        loan_returned_date.setTextColor(redColor)
                        feedback.setTextColor(redColor)
                        feedback.setBackgroundResource(R.drawable.bubble_4)
                        feedback.setText(R.string.angry)
                    }
                    else if (daysDiff < 0) {
                        loan_returned_date.setTextColor(orangeColor)
                        feedback.setTextColor(orangeColor)
                        feedback.setBackgroundResource(R.drawable.bubble_2)
                        feedback.setText(R.string.fine)
                    }
                    else {
                        loan_returned_date.setTextColor(greenColor)
                        feedback.setTextColor(greenColor)
                        feedback.setBackgroundResource(R.drawable.bubble_1)
                        feedback.setText(R.string.happy)
                    }
                } else {
                    loan_returned_date.setTextColor(greenColor)
                    feedback.setTextColor(greenColor)
                    feedback.setBackgroundResource(R.drawable.bubble_1)
                    feedback.setText(R.string.happy)
                }

                loan_notif_date.visibility = View.GONE
                loan_notif_title.visibility = View.GONE
                mBtnPick.visibility = View.GONE

            } else {
                mBtnEdit.visibility = View.VISIBLE
                if (getStringFromDate(loan.due_date?.toDate()) != Constant.FAR_AWAY_DATE && mFirstTime) setDueDate(getStringFromDate(loan.due_date?.toDate()))
                if (mFirstTime) loan_product.setText(loan.product)
                if (mFirstTime) loan_recipient.setText(loan.recipient_id)
                loan_creation_date.setTextColor(greyColor)

                when (loan.notif) {
                    null -> {
                        if (loan_due_date.text != "")
                            displayToggleButtons()
                            checkNotifBtns()
                    }
                    else -> {
                        setNotifDate(loan.notif!!)
                    }
                }
                disableFloatButton()
            }

            if (mFirstTime) spinner_loan_categories.setSelection(Utils.getIndexFromCategory(loan.product_category, this))
            else spinner_loan_categories.setSelection(Utils.getIndexFromCategory(mCurrentProductCategory, this))
            loan_creation_date.text = getStringFromDate(loan.creation_date?.toDate())

        }

        if (mUser != null) {
            val nameToPopulate = arrayListOf<String>()

            runWithPermissions(Manifest.permission.READ_CONTACTS) {
                val phones = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                while (phones!!.moveToNext()) {
                    val name =
                        phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    if (!nameToPopulate.contains(name)) nameToPopulate.add(name)
                }

                val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(mUser.uid)
                    .collection(Constant.LOANERS_COLLECTION)
                loanerRef
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) nameToPopulate.add(document.data.getValue(Constant.NAME).toString())

                        val loanRecipientNamesListAdapter = ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_dropdown_item_1line, nameToPopulate
                        )
                        loan_recipient.setAdapter(loanRecipientNamesListAdapter)
                        loan_recipient.threshold = 1
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, getString(R.string.error_getting_docs), exception)
                    }
            }
        }
    }

    /**
     * This method configures the Type title section
     */
    private fun configureType(loan: Loan) {
        when {
            loan.type.equals(LoanType.LENDING.type) -> {
                loan_type.setBackgroundColor(greenColor)
                loan_type_pic.setBackgroundColor(greenColor)
                loan_recipient_title.text = getString(R.string.whom_no_star)
                loan_type.text = getString(R.string.i_lended)
                loan_type_pic.setImageResource(R.drawable.ic_loan_black)
            }
            loan.type.equals(LoanType.BORROWING.type) -> {
                loan_type.setBackgroundColor(redColor)
                loan_type_pic.setBackgroundColor(redColor)
                loan_recipient_title.text = getString(R.string.who_no_star)
                loan_type.text = getString(R.string.i_borrowed)
                loan_type_pic.setImageResource(R.drawable.ic_borrowing_black)
            }
            loan.type.equals(LoanType.DELIVERY.type) -> {
                loan_type.setBackgroundColor(yellowColor)
                loan_type_pic.setBackgroundColor(yellowColor)
                loan_recipient_title.text = getString(R.string.who_no_star)
                loan_type.text = getString(R.string.delivery_for)
                loan_recipient.hint = getString(R.string.delivery_hint)
                loan_creation_date_title.text = getString(R.string.since)
                loan_type_pic.setImageResource(R.drawable.ic_delivery_black)
            }
        }
    }

    /**
     * Method to describe the actions to complete on text writing
     */
    val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) {
            setEditBtnState()
            setEditFieldsTextColor()
        }
    }

    /**
     * This method gets loan info
     */
    private fun getLoan() {
        val i = intent
        mLoanId = i.extras?.getString(Constant.LOAN_ID) ?: ""
        val docRef = mDb.collection(Constant.LOANS_COLLECTION).document(mLoanId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                mLoan = documentSnapshot.toObject(Loan::class.java)

                mLoan?.let {
                    mProductCategory = mLoan!!.product_category
                    mWhat = mLoan!!.product
                    mWho = mLoan!!.recipient_id
                    mDue = getStringFromDate(mLoan!!.due_date?.toDate())
                    mNotif = mLoan!!.notif
                }

                configureScreen(mLoan)
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
     * This method enable/disable the edit button
     */
    fun setEditBtnState() {
        if (isFormValid())
            enableFloatButton()
        else disableFloatButton()
    }

    /**
     * Method to tell if the form is valid
     */
    fun isFormValid(): Boolean {
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)

        if (loan_returned_date == null) {
            if (!loan_due_date.text.toString().equals(mDue) && mDue != null) loan_due_date.setTextColor(blackColor)
            else loan_due_date.setTextColor(darkGreyColor)
        }

        val currentNotif: String? = when {
            notif_d_day.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            notif_three_days.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            notif_one_week.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            loan_notif_date.text.toString() != "" -> loan_notif_date.text.toString()
            else -> null
        }

        return !loan_product.text.toString().equals(mWhat)
                || !loan_recipient.text.toString().equals(mWho)
                || (!loan_due_date.text.toString().equals(mDue) && !loan_due_date.text.toString().equals(""))
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
            setEditBtnState()
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
                getString(R.string.due) -> {
                    setDueDate(date)
                }
                getString(R.string.notif) -> {
                    setNotifDate(date)
                }
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
     * This method set a due date
     */
    private fun setNotifDate(date: String) {
        loan_notif_date.text = date
        if ((loan_notif_date.text == mNotif && loan_due_date.text == mDue) || (loan_notif_date.text == mNotif && loan_due_date.text == "" && mDue == Constant.FAR_AWAY_DATE))
            loan_notif_date.setTextColor(greyColor)
        else loan_notif_date.setTextColor(blackColor)
        loan_notif_date.setBackgroundColor(blueColor)
        mBtnCancelNotif.visibility = View.VISIBLE
    }

    /**
     * This method sets the toggle button
     */
    fun setToggle(btn: ToggleButton) {
        btn.isChecked = true
        btn.setBackgroundColor(yellowColor)
    }

    /**
     * Method to choose to display/hide the toggle notification buttons
     */
    private fun setToggleButtons() {
        if (loan_due_date.text != "" && loan_notif_date.text == "") {
            displayToggleButtons()
        } else {
            hideToggleButtons()
        }
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
        setEditBtnState()
        setToggleButtons()
        checkNotifBtns()
    }

    /**
     * Make the float button enabled
     */
    fun enableFloatButton() {
        setButtonTint(mBtnEdit, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.secondaryColor)) )
    }

    /**
     * Make the float button disabled
     */
    private fun disableFloatButton() {
        setButtonTint(mBtnEdit, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.light_grey)) )
    }

    /**
    * This method edits a loan entry in the Firebase database "loans" collection
    */
    private fun editFirestoreLoan(){
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(mLoan!!.id)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(mLoan!!.requestor_id).collection(Constant.LOANERS_COLLECTION).document(mLoan!!.recipient_id)

        val currentNotif: String? = when {
            notif_d_day.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            notif_three_days.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            notif_one_week.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            loan_notif_date.text.toString() != "" -> loan_notif_date.text.toString()
            else -> null
        }

        mDb.runBatch { batch ->
            if (categories[spinner_loan_categories.selectedItemPosition] != mProductCategory) batch.update(loanRef, "product_category", categories[spinner_loan_categories.selectedItemPosition])
            if (!loan_product.text.toString().equals("")) batch.update(loanRef, Constant.PRODUCT, loan_product.text.toString())
            if (!loan_recipient.text.toString().equals("")) {
                val loanerRefNew = mDb.collection(Constant.USERS_COLLECTION).document(mLoan!!.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan_recipient.text.toString())
                val loanerData = hashMapOf(Constant.NAME to loan_recipient.text.toString())

                if (!loan_due_date.text.toString().equals(mDue) || (currentNotif != mNotif || loan_due_date.text.toString() != mDue)) {
                    batch.update(loanRef, Constant.NOTIF, currentNotif)
                }

                batch.update(loanRef, Constant.RECIPIENT_ID, loan_recipient.text.toString())
                when (mLoan!!.type) {
                    LoanType.LENDING.type -> batch.update(loanerRef, LoanType.LENDING.type, FieldValue.increment(-1))
                    LoanType.BORROWING.type -> batch.update(loanerRef, LoanType.BORROWING.type, FieldValue.increment(-1))
                    LoanType.DELIVERY.type -> batch.update(loanerRef, LoanType.DELIVERY.type, FieldValue.increment(-1))
                }
                batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))

                batch.set(loanerRefNew,loanerData, SetOptions.merge())
                batch.update(loanerRefNew, LoanStatus.PENDING.type, FieldValue.increment(+1))
                when (mLoan!!.type) {
                    LoanType.LENDING.type -> batch.update(loanerRefNew, LoanType.LENDING.type, FieldValue.increment(+1))
                    LoanType.BORROWING.type -> batch.update(loanerRefNew, LoanType.BORROWING.type, FieldValue.increment(+1))
                    LoanType.DELIVERY.type -> batch.update(loanerRefNew, LoanType.DELIVERY.type, FieldValue.increment(+1))
                }
            }
            if (loan_due_date.text.toString().equals("")) batch.update(loanRef,Constant.DUE_DATE, Utils.getTimeStampFromString(Constant.FAR_AWAY_DATE))
            if (!loan_due_date.text.toString().equals("")) batch.update(loanRef, Constant.DUE_DATE, Utils.getTimeStampFromString(loan_due_date.text.toString()))
        }.addOnCompleteListener {
            displayCustomToast(
                getString(R.string.saved),
                R.drawable.bubble_3,
                this
            )
            if (!loan_due_date.text.toString().equals(mDue) || (currentNotif != mNotif || loan_due_date.text.toString() != mDue)) {
                NotificationManagement.stopAlarm(mLoan!!.id, mLoan!!.product, mLoan!!.type, mLoan!!.recipient_id, this, this)
                NotificationManagement.createNotification(mLoan!!.id, mLoan!!.product, mLoan!!.type, mLoan!!.recipient_id, getNotifDate(), this, this)
            }
            startLoanPagerActivity(Constant.STANDARD)
        }.addOnFailureListener { e ->
            Log.w(TAG, getString(R.string.transaction_failure), e)
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

    private fun deleteTheLoan(loan: Loan) {
        var points = Utils.retrievePointsFromLoan(loan)

        val loanHelper = LoanHelper()
        loanHelper.deleteLoan(loan, points) {result, loanId ->
            if (result) {
                displayCustomToast(getString(R.string.deleted_message, loan.product), R.drawable.bubble_3, this)
                NotificationManagement.stopAlarm(loan.id, loan.product, loan.type, loan.recipient_id, this, this)

                Snackbar.make(activity_loan_detail, loan.product, Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo)) {
                        undeleteTheLoan(loan, points)
                    }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onShown(transientBottomBar: Snackbar?) {
                        }

                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                startLoanPagerActivity(getString(R.string.archive))
                            }
                        }
                    }).show()
            } else {
                displayCustomToast(getString(R.string.error_adding_loan), R.drawable.bubble_3, this)
            }
        }

    }

    /**
     * This method undeletes the previously detetedItem
     * @param loan is a Loan representing the loan object
     */
    private fun undeleteTheLoan(loan: Loan, points: Long) {
        val loanHelper = LoanHelper()
        loanHelper.undeleteLoan(loan, points) {result ->
            if (result) {
                displayCustomToast(getString(R.string.undeleted_message, loan.product), R.drawable.bubble_4,this)
            } else {
                displayCustomToast(getString(R.string.error_undeleting_loan), R.drawable.bubble_3, this)
            }
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
                if (loan.type.equals(LoanType.DELIVERY.type)) displayCustomToast(getString(R.string.not_received_message, loan.product), R.drawable.bubble_2, this)
                else displayCustomToast(getString(R.string.unarchived_message, loan.product), R.drawable.bubble_2, this)
            } else {
                displayCustomToast(getString(R.string.error_undeleting_loan), R.drawable.bubble_3, this)
            }
        }

        Snackbar.make(activity_loan_detail, loan.product, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo)) {
                rearchiveTheLoan(loan)
            }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        startLoanPagerActivity(Constant.STANDARD)
                    }
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
                if (loan.type.equals(LoanType.DELIVERY.type)) displayCustomToast(getString(R.string.received_message, loan.product), R.drawable.bubble_1, this)
                else displayCustomToast(getString(R.string.archived_message, loan.product), R.drawable.bubble_1, this)
            } else {
                displayCustomToast(getString(R.string.error_undeleting_loan), R.drawable.bubble_3, this)
            }
        }
    }

//    override fun displayToast(message: String, bubble: Int) {
//        displayCustomToast(message, bubble, this)
//    }

//    override fun displaySnackbar(message: String, loan: Loan, bubble: Int, points: Long, action: LoanAction) {
//        Snackbar.make(activity_loan_detail, loan.product, Snackbar.LENGTH_LONG)
//            .setAction(getString(R.string.undo)) {
//                if (action == LoanAction.UNDELETE) undeleteTheLoan(loan, points)
//                else rearchiveTheLoan(loan)
//            }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
//                override fun onShown(transientBottomBar: Snackbar?) {
//                }
//
//                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
//                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
//                        startLoanPagerActivity(getString(R.string.archive))
//                    }
//                }
//            }).show()
//    }

}