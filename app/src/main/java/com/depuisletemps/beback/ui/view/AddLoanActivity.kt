package com.depuisletemps.beback.ui.view

import android.Manifest
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
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
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.Utils.Companion.getTimeStampFromString
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_add_loan.*
import kotlinx.android.synthetic.main.activity_add_loan.loan_due_date
import kotlinx.android.synthetic.main.activity_add_loan.loan_product
import kotlinx.android.synthetic.main.activity_add_loan.loan_recipient
import kotlinx.android.synthetic.main.activity_add_loan.loan_recipient_title
import kotlinx.android.synthetic.main.activity_add_loan.loan_type
import kotlinx.android.synthetic.main.activity_add_loan.loan_type_pic
import kotlinx.android.synthetic.main.activity_add_loan.mBtnCancelDate
import kotlinx.android.synthetic.main.activity_add_loan.spinner_loan_categories
import kotlinx.android.synthetic.main.activity_loan_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import org.joda.time.LocalDate
import java.text.DecimalFormat
import java.util.*


class AddLoanActivity: BaseActivity() {
    private val TAG = "AddLoanActivity"
    lateinit var mType: String
    val mUser: FirebaseUser? = getCurrentUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_loan)

        configureToolbar()
        configureSpinner()
        configureTextWatchers()
        configureScreenFromType()
        configureButtons()
    }

    private fun configureButtons() {
        val yellowColor = ContextCompat.getColor(this, R.color.secondaryColor)
        val greyColor = ContextCompat.getColor(this, R.color.light_grey)

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
                unsetToggle(notif_one_day)
                unsetToggle(notif_one_week)
            } else {
                notif_d_day.setBackgroundColor(greyColor)
            }
        })

        notif_one_day.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notif_one_day.setBackgroundColor(yellowColor)
                unsetToggle(notif_one_week)
                unsetToggle(notif_d_day)
            } else {
                notif_one_day.setBackgroundColor(greyColor)
            }
        })

        notif_one_week.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                notif_one_week.setBackgroundColor(yellowColor)
                unsetToggle(notif_one_day)
                unsetToggle(notif_d_day)
            } else {
                notif_one_week.setBackgroundColor(greyColor)
            }
        })
    }

    /**
     * This method unsets the toggle button
     */
    fun unsetToggle(btn: ToggleButton) {
        val greyColor = ContextCompat.getColor(this, R.color.light_grey)

        btn.isChecked = false
        btn.setBackgroundColor(greyColor)
    }

    /**
     * This method sets the background color depending on the type of loan we do (lending, borrowing)
     */
    private fun configureScreenFromType() {
        mType = getLoanType()
        val greenColor = ContextCompat.getColor(this, R.color.green)
        val redColor = ContextCompat.getColor(this, R.color.red)
        val yellowColor = ContextCompat.getColor(this, R.color.secondaryColor)
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
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)
        val categories_icons = resources.obtainTypedArray(R.array.product_category_icon)

        val spinner = findViewById<View>(R.id.spinner_loan_categories) as Spinner

        val categoryAdapter = CategoryAdapter(applicationContext, categories_icons, categories, "")
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

        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                setPickDate(
                    getString(
                        R.string.due_date,
                        df.format(dayOfMonth),
                        df.format(monthOfYear + 1),
                        year
                    ), btn
                )
            },
            year,
            month,
            day
        )
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
                    val primaryLightColor = ContextCompat.getColor(this, R.color.primaryLightColor)
                    loan_due_date.setBackgroundColor(primaryLightColor)
                    mBtnCancelDate.visibility = View.VISIBLE
                }
                getString(R.string.notif) -> {
                    loan_notif_date.text = date
                    val primaryLightColor = ContextCompat.getColor(this, R.color.primaryLightColor)
                    loan_notif_date.setBackgroundColor(primaryLightColor)
                    mBtnCancelNotif.visibility = View.VISIBLE
                }
            }
            setToggleButton()
        }
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
                val primaryColor = ContextCompat.getColor(this, R.color.primaryColor)
                loan_due_date.setBackgroundColor(primaryColor)
                mBtnCancelDate.visibility = View.GONE
            }
            getString(R.string.notif) -> {
                loan_notif_date.text = ""
                val primaryColor = ContextCompat.getColor(this, R.color.primaryColor)
                loan_notif_date.setBackgroundColor(primaryColor)
                mBtnCancelNotif.visibility = View.GONE
            }
        }
        setToggleButton()
    }

    /**
     * Method to display/hide the toggle notification buttons
     */
    private fun setToggleButton() {
        if (loan_due_date.text != "" && loan_notif_date.text == "")
        {
            loan_notif_date.visibility = View.INVISIBLE
            toggle_btns.visibility = View.VISIBLE
        } else {
            loan_notif_date.visibility = View.VISIBLE
            toggle_btns.visibility = View.INVISIBLE
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
     * Method to configure the textWatchers on the fields which requires it
     */
    fun isFormValid(): Boolean {
        return !loan_product.text.toString().equals("") &&  !loan_recipient.text.toString().equals("")
    }

    /**
     * Make the float button enabled
     */
    fun enableFloatButton() {
        setButtonTint(mBtnSubmit, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.secondaryColor)) )
    }

    /**
     * Make the float button disabled
     */
    private fun disableFloatButton() {
        setButtonTint(mBtnSubmit, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.light_grey)) )
    }

    /**
    * This method create a loan entry in the Firebase database "loan" collection
    */
    private fun createFirestoreLoan(){
        val user: FirebaseUser? = getCurrentUser()

        val requestorId:String = user?.uid ?: ""
        val recipientId:String = loan_recipient.text.toString()

        val product:String = loan_product.text.toString()
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)

        val productCategory:String = categories[spinner_loan_categories.selectedItemPosition]
        val creationDate = Timestamp.now()
        var dueDate = loan_due_date.text.toString()
        if (dueDate == "") dueDate = Constant.FAR_AWAY_DATE
        val returnedDate = null

        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document()
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(requestorId).collection(Constant.LOANERS_COLLECTION).document(recipientId)
        val loanerData = hashMapOf(Constant.NAME to recipientId)
        val loan = Loan(loanRef.id, requestorId, recipientId, mType, product, productCategory, creationDate, getTimeStampFromString(dueDate), returnedDate)

        mDb.runBatch { batch ->
            batch.set(loanRef,loan)
            batch.set(loanerRef,loanerData, SetOptions.merge())
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
        }.addOnCompleteListener {
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
            startLoanPagerActivity(getString(R.string.standard))
        }.addOnFailureListener { e ->
            Log.w(TAG, getString(R.string.transaction_failure), e)
        }

    }

    fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.backgroundTintList = tint
        } else {
            ViewCompat.setBackgroundTintList(button, tint)
        }
    }

}