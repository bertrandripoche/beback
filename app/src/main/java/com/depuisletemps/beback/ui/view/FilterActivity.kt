package com.depuisletemps.beback.ui.view

import android.Manifest
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.utils.Constant
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_add_loan.*
import kotlinx.android.synthetic.main.activity_add_loan.mBtnSubmit
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class FilterActivity: BaseActivity() {

    private val TAG = "FilterActivity"
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
        setContentView(R.layout.activity_filter)

        defineColors()
        configureToolbar()
        configureButtons()
        configureAutoCompleteFields()
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
     * This method configures the toolbar
     */
    private fun configureToolbar() {
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        Objects.requireNonNull(ab)!!.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * This method sets all the listeners for the buttons
     */
    private fun configureButtons() {
        mBtnSubmit.setOnClickListener(View.OnClickListener {
            if (isFormValid())
            else {
                Toast.makeText(applicationContext, R.string.invalid_form, Toast.LENGTH_LONG)
                    .show()
            }
        })

        toggle_borrowing.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                toggle_borrowing.setBackgroundResource(R.drawable.round_secondary_color_button)
                unsetToggle(toggle_lending)
            } else {
                toggle_borrowing.setBackgroundResource(R.drawable.round_grey_color_button)
            }
            if (isFormValid()) enableFloatButton() else disableFloatButton()
        })

        toggle_lending.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                toggle_lending.setBackgroundResource(R.drawable.round_secondary_color_button)
                unsetToggle(toggle_borrowing)
            } else {
                toggle_lending.setBackgroundResource(R.drawable.round_grey_color_button)
            }
            if (isFormValid()) enableFloatButton() else disableFloatButton()
        })

        disableFloatButton()
    }

    /**
     * This method unsets the toggle button
     */
    fun unsetToggle(btn: ToggleButton) {
        btn.isChecked = false
        btn.setBackgroundResource(R.drawable.round_grey_color_button)
    }

    /**
     * Method to configure the textWatchers on the fields which requires it
     */
    fun isFormValid(): Boolean {
        return !filter_product.text.toString().equals("")
                || !filter_recipient.text.toString().equals("")
                || toggle_lending.isChecked
                || toggle_borrowing.isChecked
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