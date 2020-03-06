package com.depuisletemps.beback.ui.view

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.ui.customview.CategoryAdapter
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.Companion.getStringFromDate
import com.google.android.material.floatingactionbutton.FloatingActionButton
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


class LoanDetailActivity: BaseActivity() {
    private val TAG = "LoanDetailActivity"
    lateinit var mWhat:String
    lateinit var mWho:String
    lateinit var mProductCategory:String
    lateinit var mDue:String
    var mLoan: Loan? = null
    val utils: Utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_detail)
        getLoan()

        configureToolbar()
        configureSpinner()
        configureTextWatchers()

        mBtnEdit.setOnClickListener(View.OnClickListener {
            if (isFormValid())
                editFirestoreLoan()
            else {
                Toast.makeText(applicationContext, R.string.invalid_edit_form, Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    /**
     * This method sets the background color depending on the type of loan we do (lending, borrowing)
     */
    private fun configureScreen(loan: Loan?) {
        val greenColor = ContextCompat.getColor(this, R.color.green)
        val redColor = ContextCompat.getColor(this, R.color.red)
        val yellowColor = ContextCompat.getColor(this, R.color.secondaryColor)
        val blueColor = ContextCompat.getColor(this, R.color.primaryColor)
        val blackColor = ContextCompat.getColor(this, R.color.black)
        val orangeColor = ContextCompat.getColor(this, R.color.secondaryDarkColor)
        val greyColor = ContextCompat.getColor(this, R.color.dark_grey)

        if (loan != null) {
            if (loan.type.equals(LoanType.LENDING.type)) {
                loan_type.setBackgroundColor(greenColor)
                loan_type_pic.setBackgroundColor(greenColor)
                loan_recipient_title.text = getString(R.string.whom_no_star)
                loan_type.text = getString(R.string.i_lended)
                loan_type_pic.setImageResource(R.drawable.ic_loan_black)
            } else if (loan.type.equals(LoanType.BORROWING.type)) {
                loan_type.setBackgroundColor(redColor)
                loan_type_pic.setBackgroundColor(redColor)
                loan_recipient_title.text = getString(R.string.who_no_star)
                loan_type.text = getString(R.string.i_borrowed)
                loan_type_pic.setImageResource(R.drawable.ic_borrowing_black)
            } else if (loan.type.equals(LoanType.DELIVERY.type)) {
                loan_type.setBackgroundColor(yellowColor)
                loan_type_pic.setBackgroundColor(yellowColor)
                loan_recipient_title.text = getString(R.string.who_no_star)
                loan_type.text = getString(R.string.i_wait)
                loan_recipient.hint = getString(R.string.delivery_hint)
                loan_creation_date_title.text = getString(R.string.since)
                loan_type_pic.setImageResource(R.drawable.ic_delivery_black)
            }

            if (loan.returned_date != null) {
                loan_product.keyListener = null
                loan_product.setBackgroundColor(blueColor)
                loan_recipient.keyListener = null
                loan_recipient.setBackgroundColor(blueColor)
                spinner_loan_categories.setBackgroundColor(blueColor)
                spinner_loan_categories.isEnabled = false
                mBtnPick.visibility = View.GONE
                loan_returned_date_title.visibility = View.VISIBLE
                loan_returned_date.visibility = View.VISIBLE
                loan_returned_date.text = getStringFromDate(loan.returned_date?.toDate())
                loan_product.setText(loan.product)
                loan_recipient.setText(loan.recipient_id)

                if (getStringFromDate(loan.due_date?.toDate()) == "01/01/3000") {
                    loan_due_date_title.visibility = View.GONE
                    loan_due_date.visibility = View.GONE
                } else {
                    loan_due_date.setBackgroundColor(blueColor)
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
                }

            } else {
                if (getStringFromDate(loan.due_date?.toDate()) != "01/01/3000") setPickDate(getStringFromDate(loan.due_date?.toDate()))
                loan_product.hint = loan.product
                loan_recipient.hint = loan.recipient_id
                loan_due_date.setTextColor(greyColor)
            }

            spinner_loan_categories.setSelection(utils.getIndexFromCategory(loan.product_category))
            loan_creation_date.setText(getStringFromDate(loan.creation_date?.toDate()))
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
            if (savedInstanceState.getString("dueDateSet") != "") setPickDate(savedInstanceState.getString("dueDateSet"))
    }

    /**
     * This method gets loan type from
     */
    private fun getLoan() {
        val i = intent
        val tag: String = i.extras?.getString("loanId") ?: ""
        val docRef = mDb.collection("loans").document(tag)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                mLoan = documentSnapshot.toObject(Loan::class.java)
                mProductCategory = mLoan!!.product_category
                mWhat = mLoan!!.product
                mWho = mLoan!!.recipient_id
                mDue = getStringFromDate(mLoan!!.due_date?.toDate())

                configureScreen(mLoan)
            }
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
        val categoriesIcons = resources.obtainTypedArray(R.array.product_category_icon)

        val spinner = findViewById<View>(R.id.spinner_loan_categories) as Spinner

        val categoryAdapter = CategoryAdapter(applicationContext, categoriesIcons, categories, "small")
        spinner.adapter = categoryAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setEditBtnState()
            }
        }
    }

    /**
     * Method to describe the actions to complete on text writing
     */
    val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) { // Enable-disable Floating Action Button
            setEditBtnState()
        }
    }

    /**
     * This method
     */
    fun setEditBtnState() {
        if (isFormValid()) enableFloatButton() else disableFloatButton()
    }

    /**
     * Method to configure the textWatchers on the fields which requires it
     */
    fun isFormValid(): Boolean {
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)

        return !loan_product.text.toString().equals("")
                || !loan_recipient.text.toString().equals("")
                || !loan_due_date.text.toString().equals(mDue)
                || categories[spinner_loan_categories.selectedItemPosition] != mProductCategory
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
            setEditBtnState()
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
        setEditBtnState()
    }

    /**
     * Method to configure the textWatchers on the fields which requires it
     */
     fun configureTextWatchers() {
        loan_product.addTextChangedListener(textWatcher)
        loan_recipient.addTextChangedListener(textWatcher)
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
    * This method create a loan entry in the Firebase database "loan" collection
    */
    private fun editFirestoreLoan(){
    //TODO
    }

    fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.backgroundTintList = tint
        } else {
            ViewCompat.setBackgroundTintList(button, tint)
        }
    }
}