package com.depuisletemps.beback.ui.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener

/**
 * Class used to manage the recyclerView clicks
 */
class ItemClickSupport private constructor(
    private val mRecyclerView: RecyclerView,
    private val mItemID: Int
) {
    private var mOnItemClickListener: OnItemClickListener? = null
    private val mOnClickListener =
        View.OnClickListener { v ->
            if (mOnItemClickListener != null) {
                val holder = mRecyclerView.getChildViewHolder(v)
                mOnItemClickListener!!.onItemClicked(
                    mRecyclerView,
                    holder.adapterPosition,
                    v
                )
            }
        }

    fun setOnItemClickListener(listener: OnItemClickListener?): ItemClickSupport {
        mOnItemClickListener = listener
        return this
    }

    interface OnItemClickListener {
        fun onItemClicked(
            recyclerView: RecyclerView?,
            position: Int,
            v: View
        )
    }

    companion object {
        fun addTo(view: RecyclerView, itemID: Int): ItemClickSupport {
            var support = view.getTag(itemID) as ItemClickSupport?
            if (support == null) {
                support = ItemClickSupport(view, itemID)
            }
            return support
        }
    }

    init {
        mRecyclerView.setTag(mItemID, this)
        val attachListener: OnChildAttachStateChangeListener =
            object : OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    if (mOnItemClickListener != null) {
                        view.setOnClickListener(mOnClickListener)
                    }
                }

                override fun onChildViewDetachedFromWindow(view: View) {}
            }
        mRecyclerView.addOnChildAttachStateChangeListener(attachListener)
    }
}