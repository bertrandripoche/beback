package com.depuisletemps.beback.controller.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.api.LoanHelper
import com.depuisletemps.beback.model.api.LoanerHelper
import com.depuisletemps.beback.utils.AutocompletionField
import com.depuisletemps.beback.utils.Constant
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_add_loan.*
import kotlinx.android.synthetic.main.activity_add_loan.mBtnSubmit
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.activity_loan_detail.*
import kotlinx.android.synthetic.main.activity_loan_detail.loan_recipient


class FilterActivity(): BaseActivity() {

    private val TAG = "FilterActivity"
    private val mUser: FirebaseUser? = getCurrentUser()

    private var mMode = Constant.STANDARD
    private var mSide = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        setOriginScreen()
        defineTheColors(this)
        configureToolbar()
        configureButtons()
        configureTextWatchers()
        configureAutoCompleteFields()
    }

    private fun setOriginScreen() {
        mMode = intent?.extras?.getString(Constant.MODE) ?: Constant.STANDARD
        mSide = intent?.extras?.getInt(Constant.SIDE) ?: 0
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun configureAutoCompleteFields() {
        if (mUser != null) {
            val autocompletionField = AutocompletionField(this)

            var nameToPopulate = arrayListOf<String>()
            autocompletionField.getAutocompletionNameListFromFirebase(mUser.uid, nameToPopulate, filter_recipient)

            var productToPopulate = arrayListOf<String>()
            autocompletionField.getAutocompletionProductListFromFirebase(mUser.uid, productToPopulate, filter_product)
        }
    }

    /**
     * This method sets all the listeners for the buttons
     */
    private fun configureButtons() {
        mBtnSubmit.setOnClickListener(View.OnClickListener {
            if (isFormValid())
                startLoanPagerActivity()
            else {
                Toast.makeText(applicationContext, R.string.invalid_filter, Toast.LENGTH_LONG)
                    .show()
            }
        })

        setToggleButtonOnClickListener(toggle_lending)
        setToggleButtonOnClickListener(toggle_borrowing)
        setToggleButtonOnClickListener(toggle_delivery)

        filter_product.setOnTouchListener(OnTouchListener { v, event ->
            filter_product.showDropDown()
            false
        })

        filter_recipient.setOnTouchListener(OnTouchListener { v, event ->
            filter_recipient.showDropDown()
            false
        })

        disableFloatButton(mBtnSubmit, this)
    }

    /**
     * This method allows to set a listener on a button
     * @param btn being the button on which to set the listener
     */
    fun setToggleButtonOnClickListener(btn: ToggleButton) {
        btn.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                btn.setBackgroundResource(R.drawable.round_secondary_color_button)
                if (btn != toggle_lending && toggle_lending.isChecked) unsetToggle(toggle_lending)
                if (btn != toggle_borrowing && toggle_borrowing.isChecked) unsetToggle(toggle_borrowing)
                if (btn != toggle_delivery && toggle_delivery.isChecked) unsetToggle(toggle_delivery)
            } else {
                btn.setBackgroundResource(R.drawable.round_grey_color_button)
            }
        setFloatBtnState(isFormValid(),mBtnSubmit, this)
        })
    }

    /**
     * Method to configure the textWatchers on the fields which requires it
     */
    private fun configureTextWatchers() {
        filter_recipient.addTextChangedListener(textWatcher)
        filter_product.addTextChangedListener(textWatcher)
    }

    /**
     * Method to describe the actions to complete on text writing
     */
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) { // Enable-disable Floating Action Button
            if (isFormValid()) enableFloatButton(mBtnSubmit, applicationContext) else disableFloatButton(mBtnSubmit, applicationContext)
        }
    }

    /**
     * Method to check if the form should be considered valid
     * @return a Boolean which states if the form is valid
     */
    private fun isFormValid(): Boolean = !filter_product.text.toString().equals("")
            || !filter_recipient.text.toString().equals("")
            || toggle_lending.isChecked
            || toggle_borrowing.isChecked
            || toggle_delivery.isChecked

    /**
     * This method starts the Loan activity
     */
    private fun startLoanPagerActivity() {
        val intent = Intent(this, LoanPagerActivity::class.java)

        val filterProduct: String? = when {
            filter_product.text.toString() != "" -> filter_product.text.toString()
                else -> null
        }
        val filterRecipient: String? = when {
            filter_recipient.text.toString() != "" -> filter_recipient.text.toString()
            else -> null
        }
        val filterType: String? = when {
            toggle_lending.isChecked -> Constant.LENDING
            toggle_borrowing.isChecked -> Constant.BORROWING
            toggle_delivery.isChecked -> Constant.DELIVERY
            else -> null
        }

        intent.putExtra(Constant.FILTER_RECIPIENT, filterRecipient)
        intent.putExtra(Constant.FILTER_PRODUCT, filterProduct)
        intent.putExtra(Constant.FILTER_TYPE, filterType)
        intent.putExtra(Constant.FILTERS, Constant.YES)
        intent.putExtra(Constant.MODE, mMode)
        intent.putExtra(Constant.SIDE, mSide)
        startActivity(intent)
    }
}