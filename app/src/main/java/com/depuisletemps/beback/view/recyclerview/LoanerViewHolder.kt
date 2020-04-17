package com.depuisletemps.beback.view.recyclerview

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loaner
import com.depuisletemps.beback.controller.activities.LoanDetailActivity
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.Utils
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loaner.view.*


class LoanerViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
    companion object {
        var count = true
    }

    val name: TextView = itemview.item_loaner_name
    val rateIcon: ImageView = itemview.item_loaner_rate
    val flexboxLayout: FlexboxLayout = itemview.findViewById(R.id.item_flexbox)
    /**
     * This method populates the date into the recyclerView ViewHolder
     */
    fun updateWithLoaner(loaner: Loaner, position: Int, context: Context, mode:String, requestorId: String, filterProduct: String?, filterType: String?) {
        val mDb: FirebaseFirestore = FirebaseFirestore.getInstance()
        val mLoansRef: CollectionReference = mDb.collection(Constant.LOANS_COLLECTION)
        var loanCount = 0

        val primaryLightColor = ContextCompat.getColor(context, R.color.primaryLightColor)
        val primaryColor = ContextCompat.getColor(context, R.color.primaryColor)
        val ligthGrey =  ContextCompat.getColor(context, R.color.light_grey)
        val grey =  ContextCompat.getColor(context, R.color.grey)
        val endedBorrowing:Int = loaner.ended_borrowing ?: 0
        val endedLending:Int = loaner.ended_lending ?: 0
        val endedDelivery:Int = loaner.ended_delivery ?: 0
        val theirPoints:Int = loaner.their_points ?: 0
        var rate: Double = -1.0

        name.text = loaner.name

        lateinit var query: Query
        if (mode == Constant.STANDARD) {
            query = mLoansRef.whereEqualTo(Constant.REQUESTOR_ID, requestorId)
            if (filterProduct != null)
                query = query.whereEqualTo(Constant.PRODUCT, filterProduct)
            if (filterType != null)
                query = query.whereEqualTo(Constant.TYPE, filterType)
            query = query
                .whereEqualTo(Constant.RECIPIENT_ID, name.text.toString())
                .whereEqualTo(Constant.RETURNED_DATE, null)

        } else {
            query = mLoansRef.whereEqualTo(Constant.REQUESTOR_ID, requestorId)
            if (filterProduct != null)
                query = query.whereEqualTo(Constant.PRODUCT, filterProduct)
            if (filterType != null)
                query = query.whereEqualTo(Constant.TYPE, filterType)

             query = query.whereEqualTo(Constant.RECIPIENT_ID, name.text.toString())
        }

        query.get()
            .addOnSuccessListener { documents ->
                flexboxLayout.removeAllViews()
                for (document in documents) {
                    if (document != null) {
                        if (mode == Constant.ARCHIVE && document.data.getValue(Constant.RETURNED_DATE) == null) {
                        }  else {
                            val linearLayout = LinearLayout(context)
                            linearLayout.orientation = LinearLayout.HORIZONTAL
                            val linearLayoutlayoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            linearLayoutlayoutParams.setMargins(25, 15, 25, 15)
                            linearLayout.layoutParams = linearLayoutlayoutParams

                            val imageView = AppCompatImageView(context)
                            val textView = TextView(context)

                            linearLayout.addView(imageView, linearLayoutlayoutParams)
                            linearLayout.addView(textView, linearLayoutlayoutParams)
                            imageView.setBackgroundResource(R.drawable.semi_round_white_color_button)
                            imageView.setImageDrawable(Utils.getIconFromCategory(document.data.getValue(Constant.PRODUCT_CATEGORY).toString(), context))
                            val paramsImage: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            imageView.layoutParams = paramsImage

                            textView.text = document.data.getValue(Constant.PRODUCT).toString()
                            when (document.data.getValue(Constant.TYPE).toString()) {
                                Constant.LENDING -> textView.setBackgroundResource(R.drawable.semi_round_green_button)
                                Constant.BORROWING -> textView.setBackgroundResource(R.drawable.semi_round_red_button)
                                Constant.DELIVERY -> textView.setBackgroundResource(R.drawable.semi_round_yellow_color_button)
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
                                intent.putExtra(Constant.LOAN_ID, document.data.getValue(Constant.ID).toString())
                                startActivity(context,intent,null)
                            }
                            loanCount += 1
                        }
                    }
                }

                if (loanCount == 0) {

                    itemView.visibility = View.GONE
                    var params = itemView.layoutParams
                    params.height = 0
                    itemView.layoutParams = params
                } else {
//                    count = !count
//
//                    if(count) {
//                        if (mode == Constant.STANDARD) itemView.setBackgroundColor(primaryColor)
//                        else itemView.setBackgroundColor(grey)
//                    } else {
//                        if (mode == Constant.STANDARD) itemView.setBackgroundColor(primaryLightColor)
//                        else itemView.setBackgroundColor(ligthGrey)
//                    }

                    if(position %2 == 0) {
                        if (mode == Constant.STANDARD) itemView.setBackgroundColor(primaryColor)
                        else itemView.setBackgroundColor(grey)
                    } else {
                        if (mode == Constant.STANDARD) itemView.setBackgroundColor(primaryLightColor)
                        else itemView.setBackgroundColor(ligthGrey)
                    }

                    if ((endedBorrowing + endedLending + endedDelivery) != 0)
                        rate = (theirPoints / (endedBorrowing + endedLending + endedDelivery)).toDouble()
                    when {
                        rate > 3.5 -> rateIcon.setImageResource(R.drawable.ic_rate_0)
                        rate > 3.0 -> rateIcon.setImageResource(R.drawable.ic_rate_1)
                        rate > 2.5 -> rateIcon.setImageResource(R.drawable.ic_rate_2)
                        rate > 2.0 -> rateIcon.setImageResource(R.drawable.ic_rate_3)
                        rate > 1.5 -> rateIcon.setImageResource(R.drawable.ic_rate_4)
                        else -> rateIcon.setImageResource(R.drawable.ic_rate_5)
                    }
                }
            }
            .addOnFailureListener {
            }

    }

}