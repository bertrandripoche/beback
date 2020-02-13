package com.depuisletemps.beback.ui.view

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.api.LoanHelper
import com.depuisletemps.beback.api.UserHelper
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.User
import com.depuisletemps.beback.ui.customview.CategoryAdapter
import com.depuisletemps.beback.utils.Utils
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import kotlinx.android.synthetic.main.activity_add_loan.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class AddLoanActivity: BaseActivity() {
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
        if (mType.equals("lend")) {
            loan_type.setBackgroundColor(greenColor)
            loan_recipient_title.text = getString(R.string.whom)
            loan_type.text = getString(R.string.i_lend)
        } else if (mType.equals("borrow")) {
            loan_type.setBackgroundColor(redColor)
            loan_recipient_title.text = getString(R.string.who)
            loan_type.text = getString(R.string.i_borrow)
        }
        disableFloatButton()
    }

    /**
     * This method gets loan type from
     */
    private fun getLoanType(): String {
        val i = intent
        return i.extras.getString("type") ?: ""
    }

    /**
     * This method configures the toolbar
     */
    private fun configureToolbar() {
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        Objects.requireNonNull(ab)!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun configureSpinner() {
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)
        val categories_icons = resources.obtainTypedArray(R.array.product_category_icon)

        val spinner = findViewById<View>(R.id.spinner_loan_categories) as Spinner

        val categoryAdapter = CategoryAdapter(applicationContext, categories_icons, categories)
        spinner.adapter = categoryAdapter
    }

    /**
     * Method to describe the actions to do on text writing
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
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            loan_return_date.text = getString(R.string.return_date, dayOfMonth, monthOfYear+1, year)
            val primaryLightColor = ContextCompat.getColor(this, R.color.primaryLightColor)
            loan_return_date.setBackgroundColor(primaryLightColor)
            mBtnCancelDate.visibility = View.VISIBLE
        }, year, month, day)
        dpd.datePicker.minDate = System.currentTimeMillis()
        dpd.show()
    }

    /**
     * This method empties the return date field
     */
    fun cancelDate(view: View) {
        loan_return_date.text = getString(R.string.return_date_hint)
        val primaryColor = ContextCompat.getColor(this, R.color.primaryColor)
        loan_return_date.setBackgroundColor(primaryColor)
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
        println("Loan product : " + loan_product.text.toString())
        println("Loan recipient : "+ loan_recipient.text.toString())
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
    fun disableFloatButton() {
        setButtonTint(mBtnSubmit, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.light_grey)) )
    }

    /**
    * This method create a user entry in the Firebase database "employees" collection, only if needed (thanks to merge option)
    */
    private fun createFirestoreLoan(){
        val user: FirebaseUser? = getCurrentUser()

        val requestor_id:String = user?.uid ?: ""
        val recipient_id:String = loan_recipient.text.toString()

        val product:String = loan_product.text.toString()
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)

        val product_category:String = categories[spinner_loan_categories.selectedItemPosition]
        val creation_date = Utils.getTodayDate()
        val due_date = loan_return_date.text.toString()
        val returned_date = ""

        addLoanInFirestore(requestor_id, recipient_id, mType, product, product_category, creation_date, due_date, returned_date)
        startLoanActivity()
    }

    /**
     * This method adds the user in Firestore
     */
    fun addLoanInFirestore(requestor_id:String, recipient_id:String, mType:String, product:String, product_category:String, creation_date:String, due_date:String, returned_date:String): Task<DocumentReference> {
        return LoanHelper.createLoan(requestor_id, recipient_id, mType, product, product_category, creation_date, due_date, returned_date)
    }

    fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.backgroundTintList = tint
        } else {
            ViewCompat.setBackgroundTintList(button, tint)
        }
    }
}