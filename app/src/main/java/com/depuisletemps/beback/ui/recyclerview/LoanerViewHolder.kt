package com.depuisletemps.beback.ui.recyclerview

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loaner
import com.depuisletemps.beback.utils.Utils
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loaner.view.*


class LoanerViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
    val item = itemview
    val name: TextView = itemview.item_loaner_name
    val flexboxLayout: FlexboxLayout = itemview.findViewById(R.id.item_flexbox)
    /**
     * This method populates the date into the recyclerView ViewHolder
     */
    fun updateWithLoaner(loaner: Loaner, position: Int, context: Context, mode:String, requestorId: String) {
        val primaryLightColor = ContextCompat.getColor(context, R.color.primaryLightColor)
        val primaryColor = ContextCompat.getColor(context, R.color.primaryColor)
        val ligthGrey =  ContextCompat.getColor(context, R.color.light_grey)
        val darkGrey =  ContextCompat.getColor(context, R.color.grey)
        var mDb: FirebaseFirestore = FirebaseFirestore.getInstance()
        val mLoansRef: CollectionReference = mDb.collection("loans")

        name.text = loaner.name

        lateinit var query: Query
        if (mode == context.getString(R.string.standard)) {
            if (position % 2 == 0) item.setBackgroundColor(primaryLightColor)
            else item.setBackgroundColor(primaryColor)
            query = mLoansRef.whereEqualTo("requestor_id", requestorId)
                .whereEqualTo("recipient_id", name.text.toString())
                .whereEqualTo("returned_date", null)
                .orderBy("due_date", Query.Direction.ASCENDING)
        } else {
            if (position % 2 == 0) item.setBackgroundColor(ligthGrey)
            else item.setBackgroundColor(darkGrey)
            query = mLoansRef.whereEqualTo("requestor_id", requestorId)
                .whereEqualTo("recipient_id", name.text.toString())
                .whereGreaterThan("returned_date", Utils.getTimeStampFromString("01/01/1970")!! )
                .orderBy("returned_date", Query.Direction.ASCENDING)
        }

        val loansHashMap:HashMap<String,String> = HashMap()
        query.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document != null) {
                        val textView = TextView(context)
                        textView.text = document.data.getValue("product").toString()
                        when (document.data.getValue("type").toString()) {
                            "lending" -> textView.setBackgroundResource(R.drawable.round_green_button)
                            "borrowing" -> textView.setBackgroundResource(R.drawable.round_red_button)
                            "delivery" -> textView.setBackgroundResource(R.drawable.round_secondary_color_button)
                        }
                        textView.setPadding(20, 20, 20, 20)
                        textView.setTypeface(null, Typeface.BOLD)
                        flexboxLayout.addView(textView)
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("You lose")
            }

    }
}