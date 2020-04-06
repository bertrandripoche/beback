package com.depuisletemps.beback.ui.view

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.ui.customview.CategoryAdapter
import com.depuisletemps.beback.utils.AlertReceiver
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.StringUtils
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.getTimeStampFromString
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
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
import kotlinx.android.synthetic.main.toolbar.*
import org.joda.time.LocalDate
import java.lang.Integer.parseInt
import java.text.DecimalFormat
import java.util.*

class AddLoanActivity: BaseActivity() {
    private val TAG = "AddLoanActivity"
    lateinit var mType: String
    private val mUser: FirebaseUser? = getCurrentUser()
    private var yellowColor: Int = 0
    private var lightGreyColor: Int = 0
    private var greyColor: Int = 0
    private var blueColor: Int = 0
    private var blueDeeperColor: Int = 0
    private var blackColor: Int = 0
    private var redColor: Int = 0
    private var greenColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_loan)

        defineColors()
        configureToolbar()
        configureSpinner()
        configureTextWatchers()
        configureScreenFromType()
        configureButtons()
    }

    private fun defineColors() {
        yellowColor = ContextCompat.getColor(this, R.color.secondaryColor)
        lightGreyColor = ContextCompat.getColor(this, R.color.light_grey)
        greyColor = ContextCompat.getColor(this, R.color.grey)
        blueColor = ContextCompat.getColor(this, R.color.primaryLightColor)
        blueDeeperColor = ContextCompat.getColor(this, R.color.primaryColor)
        blackColor = ContextCompat.getColor(this, R.color.black)
        greenColor = ContextCompat.getColor(this, R.color.green)
        redColor = ContextCompat.getColor(this, R.color.red)
    }

    /**
     * This method sets all the listeners for the buttons
     */
    private fun configureButtons() {
        mBtnSubmit.setOnClickListener(View.OnClickListener {
            if (isFormValid())
                createFirestoreLoan()
            else {
                Toast.makeText(applicationContext, R.string.invalid_form, Toast.LENGTH_LONG)
                    .show()
            }
        })

        notif_d_day.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notif_d_day.setBackgroundColor(yellowColor)
                unsetToggle(notif_three_days)
                unsetToggle(notif_one_week)
            } else {
                notif_d_day.setBackgroundColor(lightGreyColor)
            }
        })

        notif_three_days.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notif_three_days.setBackgroundColor(yellowColor)
                unsetToggle(notif_one_week)
                unsetToggle(notif_d_day)
            } else {
                notif_three_days.setBackgroundColor(lightGreyColor)
            }
        })

        notif_one_week.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notif_one_week.setBackgroundColor(yellowColor)
                unsetToggle(notif_three_days)
                unsetToggle(notif_d_day)
            } else {
                notif_one_week.setBackgroundColor(lightGreyColor)
            }
        })
    }

    /**
     * This method unsets the toggle button
     */
    fun unsetToggle(btn: ToggleButton) {
        btn.isChecked = false
        btn.setBackgroundColor(lightGreyColor)
    }

    /**
     * This method disables the toggle button
     */
    fun disableToggle(btn: ToggleButton) {
        btn.isChecked = false
        btn.setBackgroundColor(blueColor)
        btn.setTextColor(greyColor)
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

        disableFloatButton()
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
     * This method configures the toolbar
     */
    private fun configureToolbar() {
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        Objects.requireNonNull(ab)!!.setDisplayHomeAsUpEnabled(true)
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
            if (isFormValid()) enableFloatButton() else disableFloatButton()
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
     * Method to configure the textWatchers on the fields which requires it
     */
    fun isFormValid(): Boolean {
        return !loan_product.text.toString().equals("") &&  !loan_recipient.text.toString().equals("")
    }

    /**
     * Make the float button enabled
     */
    fun enableFloatButton() {
        setButtonTint(mBtnSubmit, ColorStateList.valueOf(yellowColor) )
    }

    /**
     * Make the float button disabled
     */
    private fun disableFloatButton() {
        setButtonTint(mBtnSubmit, ColorStateList.valueOf(lightGreyColor) )
    }

    /**
    * This method create a loan entry in the Firebase database "loan" collection
    */
    private fun createFirestoreLoan(){
        val user: FirebaseUser? = getCurrentUser()

        val requestorId:String = user?.uid ?: ""
        val recipientId:String = StringUtils.capitalizeWord(loan_recipient.text.toString())

        val product:String = StringUtils.capitalizeWord(loan_product.text.toString())
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)

        val productCategory:String = categories[spinner_loan_categories.selectedItemPosition]
        val creationDate = Timestamp.now()
        var dueDate = loan_due_date.text.toString()
        if (dueDate == "") dueDate = Constant.FAR_AWAY_DATE
        val returnedDate = null

        val notif: String? = when {
            notif_d_day.isChecked -> Constant.NOTIF_D_DAY
            notif_three_days.isChecked -> Constant.NOTIF_THREE_DAYS
            notif_one_week.isChecked -> Constant.NOTIF_ONE_WEEK
            loan_notif_date.text.toString() != "" -> loan_notif_date.text.toString()
            else -> null
        }

        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document()
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(requestorId).collection(Constant.LOANERS_COLLECTION).document(recipientId)
        val loanerData = hashMapOf(Constant.NAME to recipientId)
        val loan = Loan(loanRef.id, requestorId, recipientId, mType, product, productCategory, creationDate, getTimeStampFromString(dueDate), notif, returnedDate)

        mDb.runBatch { batch ->
            batch.set(loanRef,loan)
            batch.set(loanerRef,loanerData, SetOptions.merge())
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
        }.addOnCompleteListener {
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
            if (notif_d_day.isChecked || notif_three_days.isChecked || notif_one_week.isChecked || loan_notif_date.text.toString() != "")
                createNotification(loanRef.id, product, mType, recipientId)
            startLoanPagerActivity(Constant.STANDARD)
        }.addOnFailureListener { e ->
            Log.w(TAG, getString(R.string.transaction_failure), e)
        }

    }

    private fun createNotification(loanId: String, loanProduct: String, loanType: String, loanRecipient: String){
        val dateNotif: LocalDate? = getNotifDate()

        if (dateNotif != null) {
            val day: String = DateFormat.format("dd", dateNotif.toDate()).toString()
            val month: String = DateFormat.format("MM", dateNotif.toDate()).toString()
            val year: String = DateFormat.format("yyyy", dateNotif.toDate()).toString()
            val monthForCalendar = parseInt(month) - 1
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, parseInt(year))
            calendar.set(Calendar.MONTH, monthForCalendar)
            calendar.set(Calendar.DAY_OF_MONTH, parseInt(day))
            calendar.set(Calendar.HOUR_OF_DAY, 13)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.AM_PM, Calendar.PM)

            startAlarm(calendar, loanId, loanProduct, loanType, loanRecipient)
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
     * This method start the notification via the alertReceiver class and alarmManager
     */
    fun startAlarm(calendar: Calendar, loanId: String, loanProduct: String, loanType: String, loanRecipient: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        intent.putExtra(Constant.LOAN_ID, loanId)
        intent.putExtra(Constant.PRODUCT, loanProduct)
        intent.putExtra(Constant.TYPE, loanType)
        intent.putExtra(Constant.RECIPIENT_ID, loanRecipient)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, loanId.hashCode(), intent, 0)
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    /**
     * Set color to float button
     */
    private fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.backgroundTintList = tint
        } else {
            ViewCompat.setBackgroundTintList(button, tint)
        }
    }

}