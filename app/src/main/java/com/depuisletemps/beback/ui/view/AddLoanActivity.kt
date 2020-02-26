package com.depuisletemps.beback.ui.view

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.api.LoanHelper
import com.depuisletemps.beback.api.LoanerHelper
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.Loaner
import com.depuisletemps.beback.ui.customview.CategoryAdapter
import com.depuisletemps.beback.utils.Utils.Companion.getTimeStampFromString
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_add_loan.*
import kotlinx.android.synthetic.main.toolbar.*
import org.joda.time.LocalDate
import java.text.DecimalFormat
import java.util.*


class AddLoanActivity: BaseActivity() {
    private val TAG = "AddLoanActivity"
    lateinit var mType:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_loan)

        configureToolbar()
        configureSpinner()
        configureTextWatchers()
        configureScreenFromType()

        mBtnSubmit.setOnClickListener(View.OnClickListener {
            if (isFormValid())
                createFirestoreLoan()
            else {
                Toast.makeText(applicationContext, R.string.invalid_form, Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    /**
     * This method sets the background color depending on the type of loan we do (lending, borrowing)
     */
    private fun configureScreenFromType() {
        mType = getLoanType()
        val greenColor = ContextCompat.getColor(this, R.color.green)
        val redColor = ContextCompat.getColor(this, R.color.red)
        val yellowColor = ContextCompat.getColor(this, R.color.secondaryColor)
        if (mType.equals("lend")) {
            loan_type.setBackgroundColor(greenColor)
            loan_recipient_title.text = getString(R.string.whom)
            loan_type.text = getString(R.string.i_lend)
        } else if (mType.equals("borrow")) {
            loan_type.setBackgroundColor(redColor)
            loan_recipient_title.text = getString(R.string.who)
            loan_type.text = getString(R.string.i_borrow)
        } else if (mType.equals("delivery")) {
            loan_type.setBackgroundColor(yellowColor)
            loan_recipient_title.text = getString(R.string.who)
            loan_type.text = getString(R.string.i_wait)
            loan_recipient.hint = getString(R.string.delivery_hint)
        }
        disableFloatButton()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (loan_due_date.text != "") outState.putString("dueDateSet", loan_due_date.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null)
            if (savedInstanceState.getString("dueDateSet") != "")  setPickDate(savedInstanceState.getString("dueDateSet"))
    }

    /**
     * This method gets loan type from
     */
    private fun getLoanType(): String {
        val i = intent
        return i.extras?.getString("type") ?: ""
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

        val categoryAdapter = CategoryAdapter(applicationContext, categories_icons, categories)
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
        val year:Int = today.year
        val month:Int = today.monthOfYear-1
        val day = today.dayOfMonth

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            setPickDate(getString(R.string.due_date, df.format(dayOfMonth), df.format(monthOfYear+1), year))
        }, year, month, day)
        dpd.datePicker.minDate = System.currentTimeMillis()
        dpd.show()
    }

    /**
     * This method set the date picked in the accurate field
     */
    private fun setPickDate(date: String?) {
        if (date != null) {
            loan_due_date.text = date
            val primaryLightColor = ContextCompat.getColor(this, R.color.primaryLightColor)
            loan_due_date.setBackgroundColor(primaryLightColor)
            mBtnCancelDate.visibility = View.VISIBLE
        }
    }

    /**
     * This method empties the due date field
     */
    fun cancelDate(view: View) {
        loan_due_date.text = ""
        val primaryColor = ContextCompat.getColor(this, R.color.primaryColor)
        loan_due_date.setBackgroundColor(primaryColor)
        mBtnCancelDate.visibility = View.GONE
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
        if (dueDate == "") dueDate = "01/01/3000"
        val returnedDate = null

        val loan = Loan(requestorId, recipientId, mType, product, productCategory, creationDate, getTimeStampFromString(dueDate), returnedDate)
        val loaner = Loaner(recipientId, null, null, null, null, null)
//        addLoanInFirestore(requestorId, recipientId, mType, product, productCategory, creationDate, getTimeStampFromString(dueDate), returnedDate)
//        addLoanerInFirestore(requestorId, recipientId, mType)
//        startLoanPagerActivity(getString(R.string.standard))

        val loanRef = mDb.collection("loans").document()
        val loanerRef = mDb.collection("users").document(requestorId).collection("loaners").document(recipientId)
        val data = hashMapOf("name" to recipientId)

        mDb.runBatch { batch ->
            batch.set(loanRef,loan)
            batch.set(loanerRef,data, SetOptions.merge())
            when (mType) {
                "lend" -> batch.update(loanerRef, "lending", FieldValue.increment(1))
                "borrow" -> batch.update(loanerRef, "borrowing", FieldValue.increment(1))
                "delivery" -> batch.update(loanerRef, "delivery", FieldValue.increment(1))
            }
        }.addOnCompleteListener {
            Toast.makeText(this, "Saved in Firestore", Toast.LENGTH_SHORT).show()
            startLoanPagerActivity(getString(R.string.standard))
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }

    }

    /**
     * This method adds the loan in Firestore
     */
    fun addLoanInFirestore(requestorId:String, recipientId:String, mType:String, product:String, productCategory:String, creationDate:Timestamp, dueDate:Timestamp?, returnedDate:Timestamp?): Task<DocumentReference> {
        return LoanHelper.createLoan(requestorId, recipientId, mType, product, productCategory, creationDate, dueDate, returnedDate)
    }

    /**
     * This method adds the loaner in Firestore
     */
    fun addLoanerInFirestore(requestorId:String, recipientId: String, type: String): Task<Void> {
        return when (type) {
            "lend" -> LoanerHelper.createLoaner(1, 0, 0, 0, 0, requestorId, recipientId)
            "borrow" -> LoanerHelper.createLoaner(0, 1, 0, 0, 0, requestorId, recipientId)
            "delivery" -> LoanerHelper.createLoaner(0, 0, 0, 0, 1, requestorId, recipientId)
            else -> LoanerHelper.createLoaner(0, 0, 0, 0, 0, requestorId, recipientId)
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