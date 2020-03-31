package com.depuisletemps.beback.ui.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loaner
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class LoanerAdapter(options: FirestoreRecyclerOptions<Loaner>, private val context: Context, val mode: String, val requestorId: String, val filterProduct: String?, val filterType: String?): FirestoreRecyclerAdapter<Loaner, LoanerViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanerViewHolder {
        return LoanerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.loanactivity_recyclerview_item_loaner, parent, false))
    }

    override fun onBindViewHolder(holder: LoanerViewHolder, position: Int, loaner: Loaner) {
        holder.updateWithLoaner(loaner, position, context, mode, requestorId, filterProduct, filterType)
    }

}