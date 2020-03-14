package com.depuisletemps.beback.ui.customview

import android.content.Context
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.utils.Constant

class CategoryAdapter(var context: Context, var icons: TypedArray, var categories: Array<String>, val size:String) :  BaseAdapter() {
    internal var inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return categories.size
    }

    override fun getItem(i: Int): Any? {
        return null
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {

        var v: View = when (size) {
            Constant.SMALL -> inflater.inflate(R.layout.spinner_loan_categories_small, null)
            else -> inflater.inflate(R.layout.spinner_loan_categories, null)
        }
        val icon = v.findViewById<View>(R.id.image_category) as ImageView?
        val names = v.findViewById<View>(R.id.text_category) as TextView?
        icon!!.setImageDrawable(icons.getDrawable(i))
        names!!.text = categories[i]
        return v
    }

}