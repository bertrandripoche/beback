package com.depuisletemps.beback.view.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.utils.Constant
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


class LoanAdapter(options: FirestoreRecyclerOptions<Loan>, private val context: Context, val mode: String): FirestoreRecyclerAdapter<Loan,LoanViewHolder>(options) {
    val primaryLightColor = ContextCompat.getColor(context, R.color.primaryLightColor)
    val primaryColor = ContextCompat.getColor(context, R.color.primaryColor)
    val ligthGrey =  ContextCompat.getColor(context, R.color.light_grey)
    val grey =  ContextCompat.getColor(context, R.color.grey)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        return LoanViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.loanactivity_recyclerview_item_loan, parent, false))
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int, loan: Loan) {
        holder.updateWithLoan(loan, position, context, mode)
        if(position %2 == 0) {
            if (mode == Constant.STANDARD) holder.itemView.setBackgroundColor(primaryColor)
            else holder.itemView.setBackgroundColor(grey)
        } else {
            if (mode == Constant.STANDARD) holder.itemView.setBackgroundColor(primaryLightColor)
            else holder.itemView.setBackgroundColor(ligthGrey)
        }
    }

}