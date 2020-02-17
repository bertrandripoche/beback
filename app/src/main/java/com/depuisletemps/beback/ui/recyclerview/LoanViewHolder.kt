package com.depuisletemps.beback.ui.recyclerview

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.utils.Utils
import kotlinx.android.synthetic.main.activity_add_loan.*
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.*

class LoanViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val item = view.item_loan_recycler_layout
    val category = view.item_product_category
    val product = view.item_product
    val recipient = view.item_recipient
    val loanType = view.item_loan_type
    val dueDate = view.item_due_date
    val dueDatePic = view.item_due_date_pic
    val utils:Utils = Utils()

    fun updateWithLoan(loan: Loan, position: Int, context:Context) {
        val red = ContextCompat.getColor(context, R.color.red)
        val green = ContextCompat.getColor(context, R.color.green)
        val primaryLightColor = ContextCompat.getColor(context, R.color.primaryLightColor)
        val primaryColor = ContextCompat.getColor(context, R.color.primaryColor)
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
            loanType.setImageResource(R.drawable.ic_delivery_black)
            recipient.setTextColor(red)
        }
        dueDate.text = loan.due_date.toString()
        if (!dueDate.text.equals("")) {
            dueDatePic.visibility = View.VISIBLE
            dueDate.visibility = View.VISIBLE
        }
        else {
            dueDatePic.visibility = View.GONE
            dueDate.visibility = View.GONE
        }
    }
}

