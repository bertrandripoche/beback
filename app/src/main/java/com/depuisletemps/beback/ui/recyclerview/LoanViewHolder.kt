package com.depuisletemps.beback.ui.recyclerview

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.Companion.getDifferenceDays
import com.depuisletemps.beback.utils.Utils.Companion.getLocalDateFromString
import com.depuisletemps.beback.utils.Utils.Companion.getStringFromDate
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.*
import org.joda.time.LocalDate

class LoanViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val item = view.item_loan_recycler_layout
    val category = view.item_product_category
    val product = view.item_product
    val recipient = view.item_recipient
    val loanType = view.item_loan_type
    val dueDate = view.item_due_date
    val dueDatePic = view.item_due_date_pic
    val utils:Utils = Utils()

    /**
     * This method populates the date into the recyclerView ViewHolder
     */
    fun updateWithLoan(loan: Loan, position: Int, context:Context) {
        val black = ContextCompat.getColor(context, R.color.black)
        val red = ContextCompat.getColor(context, R.color.red)
        val green = ContextCompat.getColor(context, R.color.green)
        val primaryLightColor = ContextCompat.getColor(context, R.color.primaryLightColor)
        val primaryColor = ContextCompat.getColor(context, R.color.primaryColor)
        val secondaryDarkColor = ContextCompat.getColor(context, R.color.secondaryDarkColor)
        if (position % 2 == 0) item?.setBackgroundColor(primaryLightColor)
        else item?.setBackgroundColor(primaryColor)

        category.setImageResource(utils.getIconFromCategory(loan.product_category))
        product.text = loan.product
        recipient.text = loan.recipient_id
        if (loan.type == "lend") {
            loanType.setImageResource(R.drawable.ic_loan)
            recipient.setTextColor(green)
        } else if (loan.type == "borrow") {
            loanType.setImageResource(R.drawable.ic_borrowing)
            recipient.setTextColor(red)
        }  else {
            loanType.setImageResource(R.drawable.ic_delivery_yellow)
            recipient.setTextColor(secondaryDarkColor)
        }
        if (loan.due_date != null) {
            val due_date_Date = loan.due_date?.toDate()
            dueDate.text = getStringFromDate(due_date_Date)
        }
        if (!dueDate.text.equals("01/01/3000")) {
            dueDatePic.visibility = View.VISIBLE
            dueDate.visibility = View.VISIBLE
        }
        else {
            dueDatePic.visibility = View.GONE
            dueDate.visibility = View.GONE
        }

        if (dueDate.text != "") {
            val dueDateLocalDate = getLocalDateFromString(dueDate.text.toString())
            val todayLocalDate = LocalDate.now()
            val daysDiff: Int = getDifferenceDays(todayLocalDate, dueDateLocalDate)

            println("DueDate : "+dueDate.text.toString()+ " - Diff : "+daysDiff)

            if (daysDiff < 0) {
                dueDate.setTextColor(black)
                dueDatePic.setImageResource(R.drawable.ic_coffin)
            } else if (daysDiff < 7) dueDate.setTextColor(red)
            else if (daysDiff < 14) dueDate.setTextColor(secondaryDarkColor)
            else dueDate.setTextColor(green)
        }
    }

}

