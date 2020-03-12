package com.depuisletemps.beback.ui.recyclerview

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.Companion.getDifferenceDays
import com.depuisletemps.beback.utils.Utils.Companion.getLocalDateFromString
import com.depuisletemps.beback.utils.Utils.Companion.getStringFromDate
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.item_due_date
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.item_due_date_pic
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.item_loan_type
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.item_product
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.item_product_category
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.item_recipient
import org.joda.time.LocalDate


class LoanViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
    val item = itemview
    val category = itemview.item_product_category
    val product = itemview.item_product
    val recipient = itemview.item_recipient
    val loanType = itemview.item_loan_type
    val dueDate = itemview.item_due_date
    val dueDatePic = itemview.item_due_date_pic
    val utils:Utils = Utils()

    /**
     * This method populates the date into the recyclerView ViewHolder
     */
    fun updateWithLoan(loan: Loan, position: Int, context:Context, mode:String) {
        val black = ContextCompat.getColor(context, R.color.black)
        val red = ContextCompat.getColor(context, R.color.red)
        val green = ContextCompat.getColor(context, R.color.green)
        val primaryLightColor = ContextCompat.getColor(context, R.color.primaryLightColor)
        val primaryColor = ContextCompat.getColor(context, R.color.primaryColor)
        val ligthGrey =  ContextCompat.getColor(context, R.color.light_grey)
        val darkGrey =  ContextCompat.getColor(context, R.color.grey)
        val secondaryDarkColor = ContextCompat.getColor(context, R.color.secondaryDarkColor)

        if (mode == context.getString(R.string.standard)) {
            if (position % 2 == 0) item.setBackgroundColor(primaryColor)
            else item.setBackgroundColor(primaryLightColor)
        } else {
            if (position % 2 == 0) item.setBackgroundColor(darkGrey)
            else item.setBackgroundColor(ligthGrey)
        }

        category.setImageResource(utils.getIconFromCategory(loan.product_category))
        product.text = loan.product
        recipient.text = loan.recipient_id
        if (loan.type.equals(LoanType.LENDING.type)) {
            loanType.setImageResource(R.drawable.ic_loan)
            recipient.setTextColor(green)
        } else if (loan.type.equals(LoanType.BORROWING.type)) {
            loanType.setImageResource(R.drawable.ic_borrowing)
            recipient.setTextColor(red)
        }  else {
            loanType.setImageResource(R.drawable.ic_delivery)
            recipient.setTextColor(secondaryDarkColor)
        }
        if (mode == "Standard") {
            if (loan.due_date != null) {
                val due_date_Date = loan.due_date?.toDate()
                dueDate.text = getStringFromDate(due_date_Date)
            }
            if (!dueDate.text.equals("01/01/3000")) {
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
                    daysDiff < 3 -> dueDate.setTextColor(red)
                    daysDiff < 7 -> dueDate.setTextColor(secondaryDarkColor)
                    else -> dueDate.setTextColor(green)
                }
            }
        } else {
            dueDatePic.setImageResource(R.drawable.ic_checked)
            if (loan.returned_date != null) dueDate.text = getStringFromDate(loan.returned_date?.toDate())
        }

        item.setTag(loan.id)
    }
}

