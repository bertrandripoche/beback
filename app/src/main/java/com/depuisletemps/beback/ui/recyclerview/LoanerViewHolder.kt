package com.depuisletemps.beback.ui.recyclerview

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loaner
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loaner.view.*

class LoanerViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
    val item = itemview
    val name: TextView = itemview.item_loaner_name
    val borrowing_number: TextView = itemview.item_borrowing_number
    val lending_number: TextView = itemview.item_lending_number
    val ended_borrowing_number: TextView = itemview.item_ended_borrowing_number
    val ended_lending_number: TextView = itemview.item_ended_lending_number
    val delivery: TextView = itemview.item_delivery_number
    /**
     * This method populates the date into the recyclerView ViewHolder
     */
    fun updateWithLoaner(loaner: Loaner, position: Int, context: Context, mode:String) {
        val primaryLightColor = ContextCompat.getColor(context, R.color.primaryLightColor)
        val primaryColor = ContextCompat.getColor(context, R.color.primaryColor)
        val ligthGrey =  ContextCompat.getColor(context, R.color.light_grey)
        val darkGrey =  ContextCompat.getColor(context, R.color.grey)

        if (mode == context.getString(R.string.standard)) {
            if (position % 2 == 0) item.setBackgroundColor(primaryLightColor)
            else item.setBackgroundColor(primaryColor)
        } else {
            if (position % 2 == 0) item.setBackgroundColor(ligthGrey)
            else item.setBackgroundColor(darkGrey)
        }

        name.text = loaner.name

        if (loaner.borrowing != null) borrowing_number.text = loaner.borrowing.toString() else borrowing_number.text = "0"
        if (loaner.lending != null) lending_number.text = loaner.lending.toString() else lending_number.text = "0"
        if (loaner.ended_borrowing != null) ended_borrowing_number.text = loaner.ended_borrowing.toString() else ended_borrowing_number.text = "0"
        if (loaner.ended_lending != null) ended_lending_number.text = loaner.ended_lending.toString() else ended_lending_number.text = "0"
        if (loaner.delivery != null) delivery.text = loaner.delivery.toString() else delivery.text = "0"
    }
}