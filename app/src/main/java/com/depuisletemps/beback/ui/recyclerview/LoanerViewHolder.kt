package com.depuisletemps.beback.ui.recyclerview

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loaner
import com.depuisletemps.beback.ui.view.LoanDetailActivity
import com.depuisletemps.beback.utils.Utils
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.AlignSelf
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
        var utils = Utils()

        name.text = loaner.name

        lateinit var query: Query
        if (mode == context.getString(R.string.standard)) {
            if (position % 2 == 0) item.setBackgroundColor(primaryColor)
            else item.setBackgroundColor(primaryLightColor)
            query = mLoansRef.whereEqualTo("requestor_id", requestorId)
                .whereEqualTo("recipient_id", name.text.toString())
                .whereEqualTo("returned_date", null)
                .orderBy("due_date", Query.Direction.ASCENDING)
        } else {
            if (position % 2 == 0) item.setBackgroundColor(darkGrey)
            else item.setBackgroundColor(ligthGrey)
            query = mLoansRef.whereEqualTo("requestor_id", requestorId)
                .whereEqualTo("recipient_id", name.text.toString())
                .whereGreaterThan("returned_date", Utils.getTimeStampFromString("01/01/1970")!! )
                .orderBy("returned_date", Query.Direction.ASCENDING)
        }

        val loansHashMap:HashMap<String,String> = HashMap()
        query.get()
            .addOnSuccessListener { documents ->
                flexboxLayout.removeAllViews()
                for (document in documents) {
                    if (document != null) {
                        val linearLayout = LinearLayout(context)
                        linearLayout.orientation = LinearLayout.HORIZONTAL
                        val linearLayoutlayoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        linearLayoutlayoutParams.setMargins(25, 0, 25, 30)
                        linearLayout.layoutParams = linearLayoutlayoutParams

                        val imageView = AppCompatImageView(context)
                        val textView = TextView(context)

                        linearLayout.addView(imageView, linearLayoutlayoutParams)
                        linearLayout.addView(textView, linearLayoutlayoutParams)
                        imageView.setBackgroundResource(R.drawable.semi_round_white_color_button)
                        imageView.setImageResource(utils.getIconFromCategory(document.data.getValue("product_category").toString()))
                        val paramsImage: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        imageView.layoutParams = paramsImage

                        textView.text = document.data.getValue("product").toString()
                        when (document.data.getValue("type").toString()) {
                            "lending" -> textView.setBackgroundResource(R.drawable.semi_round_green_button)
                            "borrowing" -> textView.setBackgroundResource(R.drawable.semi_round_red_button)
                            "delivery" -> textView.setBackgroundResource(R.drawable.semi_round_yellow_color_button)
                        }
                        textView.setTypeface(null, Typeface.BOLD)
                        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        textView.layoutParams = params
                        textView.gravity = Gravity.CENTER_VERTICAL
                        textView.setPadding(25, 0, 25, 0)

                        flexboxLayout.addView(linearLayout)

                        linearLayout.isClickable = true
                        linearLayout.setOnClickListener{
                            val intent = Intent(context, LoanDetailActivity::class.java)
                            intent.putExtra("loanId", document.data.getValue("id").toString())
                            startActivity(context,intent,null)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("You lose")
            }

    }
}