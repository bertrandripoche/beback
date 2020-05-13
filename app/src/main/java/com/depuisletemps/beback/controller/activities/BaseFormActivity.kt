package com.depuisletemps.beback.controller.activities

import android.widget.AutoCompleteTextView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.utils.AutocompletionField
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_loan_detail.*

abstract class BaseFormActivity: BaseActivity() {
    private val mUser: FirebaseUser? = getCurrentUser()

    /**
     * This method disables the toggle button
     */
    fun disableToggle(btn: ToggleButton) {
        val greyColor = ContextCompat.getColor(this, R.color.dark_grey)
        val blueColor = ContextCompat.getColor(this, R.color.primaryLightColor)

        btn.isChecked = false
        btn.isClickable = false
        btn.setBackgroundColor(blueColor)
        btn.setTextColor(greyColor)
    }

    /**
     * This method unsets the toggle button
     */
    fun unsetToggle(btn: ToggleButton) {
        val lightGreyColor = ContextCompat.getColor(this, R.color.light_grey)
        val blackColor = ContextCompat.getColor(this, R.color.black)

        btn.isChecked = false
        btn.isClickable = true
        btn.setBackgroundColor(lightGreyColor)
        btn.setTextColor(blackColor)
    }

    /**
     * This method allows to set a listener on a button
     * @param btn being the button on which to set the listener
     */
    protected open fun setButtonOnClickListener(btn: ToggleButton) {
        btn.setOnCheckedChangeListener {buttonView, isChecked ->
            if (isChecked) {
                btn.setBackgroundColor(yellowColor)
                if (btn != notif_d_day && notif_d_day.isChecked) unsetToggle(notif_d_day)
                if (btn != notif_three_days && notif_three_days.isChecked) unsetToggle(
                    notif_three_days
                )
                if (btn != notif_one_week && notif_one_week.isChecked) unsetToggle(notif_one_week)
            } else btn.setBackgroundColor(lightGreyColor)
        }
    }

    /**
     * This method configures the autocomplete fields
     */
    protected fun configureAutoCompleteFields(productTextView: AutoCompleteTextView, recipientTextView: AutoCompleteTextView, withPhoneContact: Boolean, threshold: Int) {
        if (mUser != null) {
            val autocompletionField = AutocompletionField(this)

            val productToPopulate = arrayListOf<String>()
            autocompletionField.getAutocompletionProductListFromFirebase(mUser.uid, productToPopulate, productTextView, threshold)

            val nameToPopulate = arrayListOf<String>()
            if (withPhoneContact) autocompletionField.getAutocompletionListFromPhoneContactsAndFirebase(mUser.uid, nameToPopulate, recipientTextView, threshold)
            else autocompletionField.getAutocompletionNameListFromFirebase(mUser.uid, nameToPopulate, recipientTextView, threshold)
        }
    }

    /**
     * This method configures the Type title section
     */
    protected fun configureType(type: String, editMode: Boolean) {
        when (type) {
            LoanType.LENDING.type ->
                if (editMode) setTypeScreen(greenColor,R.string.whom_no_star,R.string.i_lended,R.drawable.ic_loan_black)
                else setTypeScreen(greenColor,R.string.whom,R.string.i_lended,R.drawable.ic_loan_black)
            LoanType.BORROWING.type ->
                if (editMode) setTypeScreen(redColor,R.string.who_no_star,R.string.i_borrowed,R.drawable.ic_borrowing_black)
                else setTypeScreen(redColor,R.string.who,R.string.i_borrowed,R.drawable.ic_borrowing_black)
            LoanType.DELIVERY.type -> {
                if (editMode) setTypeScreen(yellowColor,R.string.who_no_star,R.string.delivery_for,R.drawable.ic_delivery_black)
                else setTypeScreen(yellowColor,R.string.who,R.string.delivery_for,R.drawable.ic_delivery_black)
                loan_recipient.hint = getString(R.string.delivery_hint)
                if (editMode) loan_creation_date_title.text = getString(R.string.since)
            }
        }
    }

    /**
     * Sets the type title
     */
    private fun setTypeScreen(color: Int, recipient: Int, type: Int, img: Int) {
        loan_type.setBackgroundColor(color)
        loan_type_pic.setBackgroundColor(color)
        loan_recipient_title.text = getString(recipient)
        loan_type.text = getString(type)
        loan_type_pic.setImageResource(img)
    }
}