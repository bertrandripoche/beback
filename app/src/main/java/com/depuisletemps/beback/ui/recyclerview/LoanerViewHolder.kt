package com.depuisletemps.beback.ui.recyclerview

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loaner
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan_swipelayout.view.*
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loaner.view.*

class LoanerViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
    val item = itemview.content
    val name: TextView = itemview.item_loaner_name
    val borrowing_number: TextView = itemview.item_borrowing_number
    val lending_number: TextView = itemview.item_lending_number

    /**
     * This method populates the date into the recyclerView ViewHolder
     */
    fun updateWithLoaner(loaner: Loaner, position: Int, context: Context) {
        val primaryLightColor = ContextCompat.getColor(context, R.color.primaryLightColor)
        val primaryColor = ContextCompat.getColor(context, R.color.primaryColor)
        if (position % 2 == 0) item?.setBackgroundColor(primaryLightColor)
        else item?.setBackgroundColor(primaryColor)

        name.text = loaner.name
        borrowing_number.text = loaner.borrowing.toString()
        lending_number.text = loaner.lending.toString()
    }
}