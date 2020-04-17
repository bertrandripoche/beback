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
import androidx.core.content.ContextCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.utils.Constant
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_add_loan.mBtnSubmit
import kotlinx.android.synthetic.main.activity_filter.*


class FilterActivity: BaseActivity() {

    private val TAG = "FilterActivity"
    private val mUser: FirebaseUser? = getCurrentUser()

    private var mMode = Constant.STANDARD
    private var mSide = 0

    private var yellowColor: Int = 0
    private var blueDeeperColor: Int = 0
    private var blackColor: Int = 0
    private var redColor: Int = 0
    private var greenColor: Int = 0
    private var lightGreyColor = 0
    private var greyColor = 0
    private var blueColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        setOriginScreen()
        defineColors()
        configureToolbar()
        configureButtons()
        configureTextWatchers()
        configureAutoCompleteFields()
    }

    private fun setOriginScreen() {
        mMode = intent.extras.getString(Constant.MODE)!!
        mSide = intent.extras!!.getInt(Constant.SIDE)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun configureAutoCompleteFields() {
        if (mUser != null) {
            val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(mUser.uid).collection(Constant.LOANERS_COLLECTION)

            var nameToPopulate = arrayListOf<String>()
            loanerRef
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) nameToPopulate.add(document.data.getValue(Constant.NAME).toString())

                    val filterRecipientNamesListAdapter = ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_dropdown_item_1line, nameToPopulate
                    )
                    filter_recipient.setAdapter(filterRecipientNamesListAdapter)
                    filter_recipient.threshold = 1
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, getString(R.string.error_getting_docs), exception)
                }

            val loanRef = mDb.collection(Constant.LOANS_COLLECTION)
            var productToPopulate = arrayListOf<String>()
            loanRef.whereEqualTo(Constant.REQUESTOR_ID, mUser.uid)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) productToPopulate.add(document.data.getValue(Constant.PRODUCT).toString())

                    val filterRecipientNamesListAdapter = ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_dropdown_item_1line, productToPopulate
                    )
                    filter_product.setAdapter(filterRecipientNamesListAdapter)
                    filter_product.threshold = 1
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, getString(R.string.error_getting_docs), exception)
                }
        }
    }

    private fun defineColors() {
        lightGreyColor = ContextCompat.getColor(this, R.color.light_grey)
        greyColor = ContextCompat.getColor(this, R.color.dark_grey)
        blueColor = ContextCompat.getColor(this, R.color.primaryLightColor)
        yellowColor = ContextCompat.getColor(this, R.color.secondaryColor)
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
                startLoanPagerActivity()
            else {
                Toast.makeText(applicationContext, R.string.invalid_filter, Toast.LENGTH_LONG)
                    .show()
            }
        })

        toggle_borrowing.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                toggle_borrowing.setBackgroundResource(R.drawable.round_secondary_color_button)
                unsetToggle(toggle_lending)
                unsetToggle(toggle_delivery)
            } else {
                toggle_borrowing.setBackgroundResource(R.drawable.round_grey_color_button)
            }
            if (isFormValid()) enableFloatButton() else disableFloatButton()
        })

        toggle_lending.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                toggle_lending.setBackgroundResource(R.drawable.round_secondary_color_button)
                unsetToggle(toggle_borrowing)
                unsetToggle(toggle_delivery)
            } else {
                toggle_lending.setBackgroundResource(R.drawable.round_grey_color_button)
            }
            if (isFormValid()) enableFloatButton() else disableFloatButton()
        })

        toggle_delivery.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                toggle_delivery.setBackgroundResource(R.drawable.round_secondary_color_button)
                unsetToggle(toggle_borrowing)
                unsetToggle(toggle_lending)
            } else {
                toggle_delivery.setBackgroundResource(R.drawable.round_grey_color_button)
            }
            if (isFormValid()) enableFloatButton() else disableFloatButton()
        })

        filter_product.setOnTouchListener(OnTouchListener { v, event ->
            filter_product.showDropDown()
            false
        })

        filter_recipient.setOnTouchListener(OnTouchListener { v, event ->
            filter_recipient.showDropDown()
            false
        })

        disableFloatButton()
    }

    /**
     * Method to configure the textWatchers on the fields which requires it
     */
    fun configureTextWatchers() {
        filter_recipient.addTextChangedListener(textWatcher)
        filter_product.addTextChangedListener(textWatcher)
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
     * Method to check if the form should be considered valid
     * @return a Boolean which states if the form is valid
     */
    fun isFormValid(): Boolean {
        return !filter_product.text.toString().equals("")
                || !filter_recipient.text.toString().equals("")
                || toggle_lending.isChecked
                || toggle_borrowing.isChecked
                || toggle_delivery.isChecked
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
     * This method starts the Loan activity
     */
    fun startLoanPagerActivity() {
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