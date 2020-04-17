package com.depuisletemps.beback.view.customview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.depuisletemps.beback.controller.fragments.LoanByObjectFragment
import com.depuisletemps.beback.controller.fragments.LoanByPersonFragment

class ViewPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val COUNT = 2

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment =
            LoanByObjectFragment()
        when (position) {
            0 -> fragment =
                LoanByObjectFragment()
            1 -> fragment =
                LoanByPersonFragment()
        }
        return fragment
    }

    override fun getCount(): Int {
        return COUNT
    }

}