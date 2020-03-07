package com.depuisletemps.beback.ui.view

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.ui.customview.CategoryAdapter
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.Companion.getStringFromDate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_add_loan.loan_due_date
import kotlinx.android.synthetic.main.activity_add_loan.loan_product
import kotlinx.android.synthetic.main.activity_add_loan.loan_recipient
import kotlinx.android.synthetic.main.activity_add_loan.loan_recipient_title
import kotlinx.android.synthetic.main.activity_add_loan.loan_type
import kotlinx.android.synthetic.main.activity_add_loan.loan_type_pic
import kotlinx.android.synthetic.main.activity_add_loan.mBtnCancelDate
import kotlinx.android.synthetic.main.activity_add_loan.spinner_loan_categories
import kotlinx.android.synthetic.main.activity_loan_detail.*
import kotlinx.android.synthetic.main.custom_toast.*
import kotlinx.android.synthetic.main.fragment_loan_by_object.*
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

        mBtnDelete.setOnClickListener(View.OnClickListener {
            deleteTheLoan(mLoan!!)
        })

        mBtnUnarchive.setOnClickListener(View.OnClickListener {
            unarchiveTheLoan(mLoan!!)
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
                mBtnDelete.visibility = View.VISIBLE
                mBtnUnarchive.visibility = View.VISIBLE
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
                mBtnEdit.visibility = View.VISIBLE
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
     * This method enable/disable the edit button
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
        val categories: Array<String> =
            this.resources.getStringArray(R.array.product_category)
        val loanRef = mDb.collection("loans").document(mLoan!!.id)
        val loanerRef = mDb.collection("users").document(mLoan!!.requestor_id).collection("loaners").document(mLoan!!.recipient_id)

        mDb.runBatch { batch ->
            if (categories[spinner_loan_categories.selectedItemPosition] != mProductCategory) batch.update(loanRef, "product_category", categories[spinner_loan_categories.selectedItemPosition])
            if (!loan_product.text.toString().equals("")) batch.update(loanRef, "product", loan_product.text.toString())
            if (!loan_recipient.text.toString().equals("")) {
                val loanerRefNew = mDb.collection("users").document(mLoan!!.requestor_id).collection("loaners").document(loan_recipient.text.toString())
                val loanerData = hashMapOf("name" to loan_recipient.text.toString())

                batch.update(loanRef, "recipient_id", loan_recipient.text.toString())
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
            if (loan_due_date.text.toString().equals("")) batch.update(loanRef,"due_date", Utils.getTimeStampFromString("01/01/3000"))
            if (!loan_due_date.text.toString().equals("")) batch.update(loanRef, "due_date", Utils.getTimeStampFromString(loan_due_date.text.toString()))
        }.addOnCompleteListener {
            //Toast.makeText(this, "Saved in Firestore", Toast.LENGTH_SHORT).show()
            displayCustomToast(
                getString(R.string.saved),
                R.drawable.bubble_3
            )
            startLoanPagerActivity(getString(R.string.standard))
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }
    }

    fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.backgroundTintList = tint
        } else {
            ViewCompat.setBackgroundTintList(button, tint)
        }
    }

    private fun deleteTheLoan(loan: Loan) {
        val loanRef = mDb.collection("loans").document(loan.id)
        val loanerRef = mDb.collection("users").document(loan.requestor_id).collection("loaners").document(loan.recipient_id)

        mDb.runBatch { batch ->
            batch.delete(loanRef)
            batch.update(loanerRef, reverseTypeField(loan.type), FieldValue.increment(-1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(-1))
        }.addOnCompleteListener {
            displayCustomToast(getString(R.string.deleted_message, loan.product), R.drawable.bubble_3)
//            Toast.makeText(context,  getString(R.string.deleted_message, loan.product), Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }

        Snackbar.make(activity_loan_detail, loan.product, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo)) {
                undeleteTheLoan(loan)
            }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        startLoanPagerActivity(getString(R.string.archive))
                    }
                }
            }).show()

    }

    /**
     * This method undeletes the previously detetedItem
     * @param loan is a Loan representing the loan object
     */
    private fun undeleteTheLoan(loan: Loan) {
        val loanRef = mDb.collection("loans").document(loan.id)
        val loanerRef = mDb.collection("users").document(loan.requestor_id).collection("loaners")
            .document(loan.recipient_id)
        val loanerData = hashMapOf("name" to loan.recipient_id)

        mDb.runBatch { batch ->
            batch.set(loanRef, loan)
            batch.set(loanerRef, loanerData, SetOptions.merge())
            batch.update(loanerRef, reverseTypeField(loan.type), FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(+1))
        }.addOnCompleteListener {
            displayCustomToast(
                getString(R.string.undeleted_message, loan.product),
                R.drawable.bubble_4
            )

            //Toast.makeText(context,  getString(R.string.undeleted_message, loan.product), Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }
    }

    /**
     * This method unarchives the selected item
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun unarchiveTheLoan(loan: Loan) {
        val loanRef = mDb.collection("loans").document(loan.id)
        val loanerRef = mDb.collection("users").document(loan.requestor_id).collection("loaners").document(loan.recipient_id)

        mDb.runBatch { batch ->
            batch.update(loanRef, "returned_date", null)
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(-1))
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, reverseTypeField(loan.type), FieldValue.increment(-1))
        }.addOnCompleteListener {
            if (loan.type.equals(LoanType.DELIVERY.type)) displayCustomToast(getString(R.string.not_received_message, loan.product), R.drawable.bubble_2)
            else displayCustomToast(getString(R.string.unarchived_message, loan.product), R.drawable.bubble_2)
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }

        Snackbar.make(activity_loan_detail, loan.product, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo)) {
                rearchiveTheLoan(loan)
            }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        startLoanPagerActivity(getString(R.string.standard))
                    }
                }
            }).show()
    }

    /**
     * This method archives the selected item
     * @param loan is a Loan representing the loan object
     */
    private fun rearchiveTheLoan(loan: Loan) {
        val loanRef = mDb.collection("loans").document(loan.id)
        val loanerRef = mDb.collection("users").document(loan.requestor_id).collection("loaners").document(loan.recipient_id)

        val returnedDate: Timestamp = Timestamp.now()

        mDb.runBatch { batch ->
            batch.update(loanRef, "returned_date", returnedDate)
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(+1))
            batch.update(loanerRef, loan.type, FieldValue.increment(-1))
            batch.update(loanerRef, reverseTypeField(loan.type), FieldValue.increment(+1))
        }.addOnCompleteListener {
            if (loan.type.equals(LoanType.DELIVERY.type)) displayCustomToast(getString(R.string.received_message, loan.product), R.drawable.bubble_1)
            else displayCustomToast(getString(R.string.archived_message, loan.product), R.drawable.bubble_1)
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }
    }

    /**
     * This method displays a message in a nice way
     */
    fun displayCustomToast(message: String, bubble: Int) {
        val inflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custom_toast, custom_toast_container)
        val text: TextView = layout.findViewById(R.id.text)
        text.background = ContextCompat.getDrawable(this, bubble)
        text.text = message
        with (Toast(this)) {
            setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    /**
     * This method send the opposite field, eg : Borrowing -> Ended_borrowing
     * @param type is the type of loan of the Loan object
     * @return a String which is the "opposite" status of the loan type
     */
    fun reverseTypeField(type: String): String {
        when (type) {
            LoanType.LENDING.type -> return LoanType.ENDED_LENDING.type
            LoanType.BORROWING.type -> return LoanType.ENDED_BORROWING.type
            else -> return LoanType.ENDED_DELIVERY.type
        }
    }

}