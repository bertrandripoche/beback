package com.depuisletemps.beback.controller.activities

import android.widget.AutoCompleteTextView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.utils.AutocompletionField
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_loan_detail.*

open class BaseFormActivity: BaseActivity() {
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
}