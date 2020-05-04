package com.depuisletemps.beback.controller.activities

import android.widget.CompoundButton
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.depuisletemps.beback.R
import kotlinx.android.synthetic.main.activity_loan_detail.*

open class BaseFormActivity: BaseActivity() {

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
        btn.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                btn.setBackgroundColor(yellowColor)
                if (btn != notif_d_day && notif_d_day.isChecked) unsetToggle(notif_d_day)
                if (btn != notif_three_days && notif_three_days.isChecked) unsetToggle(
                    notif_three_days
                )
                if (btn != notif_one_week && notif_one_week.isChecked) unsetToggle(notif_one_week)
            } else btn.setBackgroundColor(lightGreyColor)
        })
    }
}