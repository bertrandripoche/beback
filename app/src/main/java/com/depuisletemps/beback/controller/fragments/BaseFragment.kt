package com.depuisletemps.beback.controller.fragments

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.utils.Constant

open abstract class BaseFragment: Fragment() {
    lateinit var mMode: String

    /**
     * This method sets the color of the background of the recyclerView items
     */
    protected fun setBackgroundForRecyclerView(recyclerView: RecyclerView) {
        if (mMode == Constant.STANDARD) {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context!!, R.color.primaryLightColor))
        } else {
            recyclerView.setBackgroundColor(ContextCompat.getColor(context!!, R.color.light_grey))
        }
    }

}