package com.depuisletemps.beback.ui.recyclerview

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.utils.Utils
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loan.view.*

class LoanViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val item = view.item_loan_recycler_layout
    val category = view.item_product_category
    val product = view.item_product
    val recipient = view.item_recipient
    val loanType = view.item_loan_type
    val returnDate = view.item_return_date
    val utils:Utils = Utils()

    fun updateWithLoan(loan: Loan, position: Int) {
        if (position % 2 == 0) item?.setBackgroundColor(Color.parseColor("e9eff3"))
        else item?.setBackgroundColor(Color.parseColor("84b1d4"))
        category.setImageResource(utils.getIconFromCategory(loan.product_category))
        product.text = loan.product
        if (loan.type == "lend") loanType.setImageResource(R.drawable.ic_loan)
        else loanType.setImageResource(R.drawable.ic_borrowing)
        returnDate.text = loan.returned_date.toString()
    }
}

