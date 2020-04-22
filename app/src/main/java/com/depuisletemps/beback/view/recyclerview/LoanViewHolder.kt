package com.depuisletemps.beback.view.recyclerview

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.getDifferenceDays
import com.depuisletemps.beback.utils.Utils.getLocalDateFromString
import com.depuisletemps.beback.utils.Utils.getStringFromDate
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.*
import org.joda.time.LocalDate


class LoanViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
    val item = itemview
    val category = itemview.item_product_category
    val product = itemview.item_product
    val recipient = itemview.item_recipient
    val loanType = itemview.item_loan_type
    val dueDate = itemview.item_due_date
    val dueDatePic = itemview.item_due_date_pic
    val notif = itemview.item_notif

    /**
     * This method populates the date into the recyclerView ViewHolder
     */
    fun updateWithLoan(loan: Loan, position: Int, context:Context, mode:String) {
        val black = ContextCompat.getColor(context, R.color.black)
        val red = ContextCompat.getColor(context, R.color.red)
        val green = ContextCompat.getColor(context, R.color.green)
        val secondaryDarkColor = ContextCompat.getColor(context, R.color.secondaryDarkColor)

        category.setImageDrawable(Utils.getIconFromCategory(loan.product_category, context))
        product.text = loan.product
        recipient.text = loan.recipient_id
        when {
            loan.type.equals(LoanType.LENDING.type) -> {
                loanType.setImageResource(R.drawable.ic_loan)
                recipient.setTextColor(green)
            }
            loan.type.equals(LoanType.BORROWING.type) -> {
                loanType.setImageResource(R.drawable.ic_borrowing)
                recipient.setTextColor(red)
            }
            else -> {
                loanType.setImageResource(R.drawable.ic_delivery)
                recipient.setTextColor(secondaryDarkColor)
            }
        }
        if (mode.equals(Constant.STANDARD)) {
            if (loan.due_date != null) {
                val due_date_Date = loan.due_date?.toDate()
                dueDate.text = getStringFromDate(due_date_Date)
            }
            if (dueDate.text != Constant.FAR_AWAY_DATE) {
                dueDatePic.visibility = View.VISIBLE
                dueDate.visibility = View.VISIBLE
            } else {
                dueDatePic.visibility = View.GONE
                dueDate.visibility = View.GONE
            }

            if (dueDate.text != "") {
                val dueDateLocalDate = getLocalDateFromString(dueDate.text.toString())
                val todayLocalDate = LocalDate.now()
                val daysDiff: Int = getDifferenceDays(todayLocalDate, dueDateLocalDate)

                when {
                    daysDiff < 0 -> {
                        dueDate.setTextColor(black)
                        dueDatePic.setImageResource(R.drawable.ic_coffin)
                    }
                    daysDiff < 3 -> {
                        dueDate.setTextColor(red)
                        dueDatePic.setImageResource(R.drawable.ic_due_date)
                    }
                    daysDiff < 7 -> {
                        dueDate.setTextColor(secondaryDarkColor)
                        dueDatePic.setImageResource(R.drawable.ic_due_date)
                    }
                    else -> {
                        dueDate.setTextColor(green)
                        dueDatePic.setImageResource(R.drawable.ic_due_date)
                    }
                }
            }
        } else {
            dueDatePic.setImageResource(R.drawable.ic_checked)
            if (loan.returned_date != null) dueDate.text = getStringFromDate(loan.returned_date?.toDate())
        }

        if (loan.notif != null) {
            notif.visibility = View.VISIBLE
            if (Utils.isStringDatePassed(loan.notif.toString())) notif.setImageResource(R.drawable.ic_notification_grey)
        }
        else notif.visibility = View.INVISIBLE

        item.tag = loan.id
    }
}
