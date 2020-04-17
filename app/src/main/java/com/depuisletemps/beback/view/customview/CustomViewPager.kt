package com.depuisletemps.beback.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


class CustomViewPager(context: Context?, attrs: AttributeSet?) :
    ViewPager(context!!, attrs) {
    private var checked = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (checked) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (checked) {
            super.onInterceptTouchEvent(event)
        } else false
    }

    fun setPagingEnabled(checked: Boolean) {
        this.checked = checked
    }

}