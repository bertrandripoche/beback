package com.depuisletemps.beback.ui.recyclerview

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
import com.depuisletemps.beback.ui.view.LoanDetailActivity
import com.depuisletemps.beback.ui.view.LoanPagerActivity
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.Utils
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.loanactivity_recyclerview_item_loaner.view.*


class LoanerViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
    val item = itemview
    val name: TextView = itemview.item_loaner_name
    val rateIcon: ImageView = itemview.item_loaner_rate
    val flexboxLayout: FlexboxLayout = itemview.findViewById(R.id.item_flexbox)
    /**
     * This method populates the date into the recyclerView ViewHolder
     */
    fun updateWithLoaner(loaner: Loaner, position: Int, context: Context, mode:String, requestorId: String, filterProduct: String?, filterType: String?) {
        val primaryLightColor = ContextCompat.getColor(context, R.color.primaryLightColor)
        val primaryColor = ContextCompat.getColor(context, R.color.primaryColor)
        val ligthGrey =  ContextCompat.getColor(context, R.color.light_grey)
        val darkGrey =  ContextCompat.getColor(context, R.color.grey)
        var mDb: FirebaseFirestore = FirebaseFirestore.getInstance()
        val mLoansRef: CollectionReference = mDb.collection(Constant.LOANS_COLLECTION)
        var utils = Utils()

        name.text = loaner.name

        val endedBorrowing:Int = loaner.ended_borrowing ?: 0
        val endedLending:Int = loaner.ended_lending ?: 0
        val endedDelivery:Int = loaner.ended_delivery ?: 0
        val theirPoints:Int = loaner.their_points ?: 0
        var rate: Double = -1.0
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

        lateinit var query: Query
        if (mode == context.getString(R.string.standard)) {
            if (position % 2 == 0) item.setBackgroundColor(primaryLightColor)
            else item.setBackgroundColor(primaryColor)

            query = mLoansRef.whereEqualTo(Constant.REQUESTOR_ID, requestorId)
            if (filterProduct != null)
                query = query.whereEqualTo(Constant.PRODUCT, filterProduct)
            if (filterType != null)
                query = query.whereEqualTo(Constant.TYPE, filterType)
            query = query
                .whereEqualTo(Constant.RECIPIENT_ID, name.text.toString())
                .whereEqualTo(Constant.RETURNED_DATE, null)
                .orderBy(Constant.DUE_DATE, Query.Direction.ASCENDING)

        } else {
            if (position % 2 == 0) item.setBackgroundColor(ligthGrey)
            else item.setBackgroundColor(darkGrey)

            query = mLoansRef.whereEqualTo(Constant.REQUESTOR_ID, requestorId)
                .whereEqualTo("type", "borrowing")
                .whereGreaterThan(Constant.RETURNED_DATE, Utils.getTimeStampFromString(Constant.FAR_PAST_DATE)!! )
                .orderBy(Constant.RETURNED_DATE, Query.Direction.ASCENDING)
//            if (filterProduct != null)
//                query = query.whereEqualTo(Constant.PRODUCT, filterProduct)
//            if (filterType != null)
//                query = query.whereEqualTo(Constant.TYPE, filterType)



//            query = mLoansRef
//                .whereEqualTo("requestor_id", "FLCVGKJ69OS2Awi9WcQmEM7bl6Q2")
//                .whereGreaterThan(Constant.RETURNED_DATE, Utils.getTimeStampFromString(Constant.FAR_PAST_DATE)!!)
//                .orderBy(Constant.RETURNED_DATE, Query.Direction.ASCENDING)

        }

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
                        linearLayoutlayoutParams.setMargins(25, 15, 25, 15)
                        linearLayout.layoutParams = linearLayoutlayoutParams

                        val imageView = AppCompatImageView(context)
                        val textView = TextView(context)

                        linearLayout.addView(imageView, linearLayoutlayoutParams)
                        linearLayout.addView(textView, linearLayoutlayoutParams)
                        imageView.setBackgroundResource(R.drawable.semi_round_white_color_button)
                        imageView.setImageResource(utils.getIconFromCategory(document.data.getValue(Constant.PRODUCT_CATEGORY).toString()))
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
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("You lose")
            }

    }
}