package com.depuisletemps.beback.ui.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


class LoanAdapter(options: FirestoreRecyclerOptions<Loan>, private val context: Context, val mode: String): FirestoreRecyclerAdapter<Loan,LoanViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        return LoanViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.loanactivity_recyclerview_item_loan, parent, false))
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int, loan: Loan) {
        holder.updateWithLoan(loan, position, context, mode)
    }

}