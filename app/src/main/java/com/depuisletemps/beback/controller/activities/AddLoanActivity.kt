package com.depuisletemps.beback.controller.activities

import android.Manifest
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.api.LoanHelper
import com.depuisletemps.beback.model.FieldType
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.view.customview.CategoryAdapter
import com.depuisletemps.beback.utils.*
import com.depuisletemps.beback.utils.Utils.getTimeStampFromString
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_add_loan.*
import kotlinx.android.synthetic.main.activity_add_loan.loan_due_date
import kotlinx.android.synthetic.main.activity_add_loan.loan_notif_date
import kotlinx.android.synthetic.main.activity_add_loan.loan_product
import kotlinx.android.synthetic.main.activity_add_loan.loan_recipient
import kotlinx.android.synthetic.main.activity_add_loan.loan_recipient_title
import kotlinx.android.synthetic.main.activity_add_loan.loan_type
import kotlinx.android.synthetic.main.activity_add_loan.loan_type_pic
import kotlinx.android.synthetic.main.activity_add_loan.mBtnCancelDate
import kotlinx.android.synthetic.main.activity_add_loan.mBtnCancelNotif
import kotlinx.android.synthetic.main.activity_add_loan.notif_d_day
import kotlinx.android.synthetic.main.activity_add_loan.notif_one_week
import kotlinx.android.synthetic.main.activity_add_loan.notif_three_days
import kotlinx.android.synthetic.main.activity_add_loan.spinner_loan_categories
import kotlinx.android.synthetic.main.activity_add_loan.toggle_btns
import kotlinx.android.synthetic.main.activity_loan_detail.*
import org.joda.time.LocalDate
import java.text.DecimalFormat

class AddLoanActivity: BaseActivity() {
    private val TAG = "AddLoanActivity"
    lateinit var mType: String
    private val mUser: FirebaseUser? = getCurrentUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_loan)

        defineTheColors(this)
        configureToolbar()
        configureSpinner()
        configureTextWatchers()
        configureScreenFromType()
        configureButtons()
    }

    /**
     * This method sets all the listeners for the buttons
     */
    private fun configureButtons() {
        mBtnSubmit.setOnClickListener(View.OnClickListener {
            if (isFormValid()) createFirestoreLoan()
            else Toast.makeText(applicationContext, R.string.invalid_form, Toast.LENGTH_LONG).show()
        })

        setButtonOnClickListener(notif_d_day)
        setButtonOnClickListener(notif_three_days)
        setButtonOnClickListener(notif_one_week)
    }

    /**
     * This method sets the background color depending on the type of loan we do (lending, borrowing)
     */
    private fun configureScreenFromType() {
        mType = getLoanType()
        if (mType.equals(LoanType.LENDING.type)) {
            loan_type.setBackgroundColor(greenColor)
            loan_type_pic.setBackgroundColor(greenColor)
            loan_recipient_title.text = getString(R.string.whom)
            loan_type.text = getString(R.string.i_lend)
            loan_type_pic.setImageResource(R.drawable.ic_loan_black)
        } else if (mType.equals(LoanType.BORROWING.type)) {
            loan_type.setBackgroundColor(redColor)
            loan_type_pic.setBackgroundColor(redColor)
            loan_recipient_title.text = getString(R.string.who)
            loan_type.text = getString(R.string.i_borrow)
            loan_type_pic.setImageResource(R.drawable.ic_borrowing_black)
        } else if (mType.equals(LoanType.DELIVERY.type)) {
            loan_type.setBackgroundColor(yellowColor)
            loan_type_pic.setBackgroundColor(yellowColor)
            loan_recipient_title.text = getString(R.string.who)
            loan_type.text = getString(R.string.i_wait)
            loan_recipient.hint = getString(R.string.delivery_hint)
            loan_type_pic.setImageResource(R.drawable.ic_delivery_black)
        }

        if (mUser != null) {
            var nameToPopulate = arrayListOf<String>()

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

        disableFloatButton(mBtnSubmit, this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (loan_due_date.text != "") outState.putString(
            Constant.DUE_DATE_SET,
            loan_due_date.text.toString()
        )
        if (loan_notif_date.text != "") outState.putString(
            Constant.NOTIF_DATE,
            loan_notif_date.text.toString()
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.getString(Constant.DUE_DATE_SET) != "")
                setPickDate(
                    savedInstanceState.getString(Constant.DUE_DATE_SET), getString(R.string.due)
                )
            if (savedInstanceState.getString(Constant.NOTIF_DATE) != "")
                setPickDate(
                    savedInstanceState.getString(Constant.NOTIF_DATE), getString(R.string.notif)
                )
        }
    }

    /**
     * This method gets loan type from
     */
    private fun getLoanType(): String {
        val i = intent
        return i.extras?.getString(Constant.TYPE) ?: ""
    }

    /**
     * This method configuree the spinner
     */
    private fun configureSpinner() {
        val categories: Array<String> = this.resources.getStringArray(R.array.product_category)
        val categoriesIcons = this.resources.obtainTypedArray(R.array.product_category_icon)

        val spinner = findViewById<View>(R.id.spinner_loan_categories) as Spinner

        val categoryAdapter = CategoryAdapter(applicationContext, categoriesIcons, categories, "")
        spinner.adapter = categoryAdapter
    }

    /**
     * Method to describe the actions to complete on text writing
     */
    val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) { // Enable-disable Floating Action Button
            setFloatBtnState(isFormValid(),mBtnSubmit, applicationContext)
            //if (isFormValid()) enableFloatButton(mBtnSubmit,applicationContext) else disableFloatButton(mBtnSubmit, applicationContext)
        }
    }

    /**
     * This method displays the DatePicker
     */
    fun clickDataPicker(view: View) {
        val df = DecimalFormat("00")
        val today = LocalDate.now()
        val year: Int = today.year
        val month: Int = today.monthOfYear - 1
        val day = today.dayOfMonth

        val btn: String = when (view.id) {
            R.id.mBtnPick -> getString(R.string.due)
            else -> getString(R.string.notif)
        }

        val dpd = DatePickerDialog(this,DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                setPickDate(getString(R.string.due_date, df.format(dayOfMonth),df.format(monthOfYear + 1),year), btn)}, year, month,day)
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
                    loan_due_date.text = date
                    loan_due_date.setBackgroundColor(blueColor)
                    mBtnCancelDate.visibility = View.VISIBLE
                }
                getString(R.string.notif) -> {
                    loan_notif_date.text = date
                    loan_notif_date.setBackgroundColor(blueColor)
                    mBtnCancelNotif.visibility = View.VISIBLE
                }
            }
            unsetToggle(notif_d_day)
            unsetToggle(notif_three_days)
            unsetToggle(notif_one_week)
            setToggleButton()
            if (loan_due_date.text.toString() != "") checkNotifBtns()
        }
    }

    /**
     * Method to check if the notifs buttons should be switched off
     */
    private fun checkNotifBtns() {
        val dueDate = Utils.getLocalDateFromString(loan_due_date.text.toString())
        val today = LocalDate.now()
        if (Utils.getDifferenceDays(today, dueDate) < 0) disableToggle(notif_d_day)
        if (Utils.getDifferenceDays(today, dueDate) < 4) disableToggle(notif_three_days)
        if (Utils.getDifferenceDays(today, dueDate) < 8) disableToggle(notif_one_week)
    }

    /**
     * This methods disables the three days notif button
     */
    private fun enableNotifBtn() {
        notif_one_week.isClickable = true
        notif_one_week.setBackgroundColor(lightGreyColor)
        notif_one_week.setTextColor(blackColor)
        notif_three_days.isClickable = true
        notif_three_days.setBackgroundColor(lightGreyColor)
        notif_three_days.setTextColor(blackColor)
    }

    /**
     * This method empties the date field (due or notif)
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
        setToggleButton()
    }

    /**
     * Method to display/hide the toggle notification buttons
     */
    private fun setToggleButton() {
        if (loan_due_date.text != "" && loan_notif_date.text == "") {
            loan_notif_date.visibility = View.INVISIBLE
            toggle_btns.visibility = View.VISIBLE
        } else {
            loan_notif_date.visibility = View.VISIBLE
            toggle_btns.visibility = View.INVISIBLE
        }
        enableNotifBtn()
    }

    /**
     * Method to configure the textWatchers on the fields which requires it
     */
     fun configureTextWatchers() {
        loan_product.addTextChangedListener(textWatcher)
        loan_recipient.addTextChangedListener(textWatcher)
    }

    /**
     * Method to check if the form should be considered valid
     * @return a Boolean which states if the form is valid
     */
    fun isFormValid(): Boolean {
        return !loan_product.text.toString().equals("") &&  !loan_recipient.text.toString().equals("")
    }

    /**
    * This method create a loan entry in the Firebase database "loan" collection
    */
    private fun createFirestoreLoan(){
        val user: FirebaseUser? = getCurrentUser()

        val requestorId:String = user?.uid ?: ""
        val recipientId:String = StringUtils.capitalizeWords(loan_recipient.text.toString(), FieldType.NAME)

        val product:String = StringUtils.capitalizeWords(loan_product.text.toString(), FieldType.PRODUCT)
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)

        val productCategory:String = categories[spinner_loan_categories.selectedItemPosition]
        val creationDate = Timestamp.now()
        var dueDate = loan_due_date.text.toString()
        if (dueDate == "") dueDate = Constant.FAR_AWAY_DATE
        val returnedDate = null

        val notif: String? = when {
            notif_d_day.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            notif_three_days.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            notif_one_week.isChecked -> Utils.getStringFromLocalDate(getNotifDate())
            loan_notif_date.text.toString() != "" -> loan_notif_date.text.toString()
            else -> null
        }
        val test = getTimeStampFromString(dueDate)
        val loan = Loan("",requestorId, recipientId, mType, product, productCategory, creationDate, getTimeStampFromString(dueDate), notif, returnedDate)

        val loanHelper = LoanHelper()
        loanHelper.createLoan(loan) {result, loanId ->
            if (result) {
                displayCustomToast(getString(R.string.saved), R.drawable.bubble_3, this)
                notif?.let{NotificationManagement.createNotification(loanId, product, mType, recipientId, getNotifDate(), this, this)}
                startLoanPagerActivity(Constant.STANDARD)
            } else {
                displayCustomToast(getString(R.string.error_adding_loan), R.drawable.bubble_3, this)
            }
        }
    }

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
     * This method allows to set a listener on a button
     * @param btn being the button on which to set the listener
     */
    private fun setButtonOnClickListener(btn: ToggleButton) {
        btn.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                btn.setBackgroundColor(yellowColor)
                if (btn != notif_d_day && notif_d_day.isChecked) unsetToggle(notif_d_day)
                if (btn != notif_three_days && notif_three_days.isChecked) unsetToggle(notif_three_days)
                if (btn != notif_one_week && notif_one_week.isChecked) unsetToggle(notif_one_week)
            } else {
                btn.setBackgroundColor(lightGreyColor)
            }
        })
    }

}