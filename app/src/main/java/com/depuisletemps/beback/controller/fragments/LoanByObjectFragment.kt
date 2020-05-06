package com.depuisletemps.beback.controller.fragments

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.api.LoanHelper
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.controller.activities.LoanDetailActivity
import com.depuisletemps.beback.controller.activities.LoanPagerActivity
import com.depuisletemps.beback.view.recyclerview.ItemClickSupport
import com.depuisletemps.beback.view.recyclerview.LoanAdapter
import com.depuisletemps.beback.utils.NotificationManagement
import com.depuisletemps.beback.utils.Constant
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_loan_by_object.*

class LoanByObjectFragment: BaseFragment() {

    private val TAG = "LoanByObjectFragment"
    private lateinit var mAdapter: LoanAdapter
    var mUser: FirebaseUser? = null
    lateinit var mDb: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mDb = (activity as LoanPagerActivity).mDb
        mMode = (activity as LoanPagerActivity).mMode
        return inflater.inflate(R.layout.fragment_loan_by_object, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureRecyclerView()
        configureOnClickRecyclerView()
        setBackgroundForRecyclerView(fragment_loan_by_object_recycler_view)

        if (mMode == Constant.STANDARD) manageSwipeOnLoan()
    }

    /**
     * Manage swipe on item recyclerView item
     */
    private fun manageSwipeOnLoan() {
        val simpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {

            override fun onMove(recyclerView: RecyclerView,viewHolder: RecyclerView.ViewHolder,viewHolder1: RecyclerView.ViewHolder): Boolean {
                return false
            }

            /**
             * this method manages the swipes on items
             */
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val loan: Loan = mAdapter.getItem(position)

                when (direction) {
                    // Right : Delete
                    ItemTouchHelper.RIGHT -> deleteTheLoan(loan)
                    // Left : Archive
                    ItemTouchHelper.LEFT -> archiveTheLoan(loan)
                    else -> return
                }
            }

            /**
             * this method manages the layout below the item (for swipe effect)
             */
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean ) {
                val position = viewHolder.adapterPosition
                var returnMessage = ""
                if (position >= 0) {
                    val loan: Loan = mAdapter.getItem(position)
                    returnMessage = if (loan.type == (LoanType.DELIVERY.type)) getString(R.string.received)
                    else getString(R.string.returned)
                }

                RecyclerViewSwipeDecorator.Builder(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
                    .addSwipeLeftActionIcon(R.drawable.ic_archive_color)
                    .addSwipeLeftBackgroundColor(
                        ContextCompat.getColor(
                            context!!,
                            R.color.dark_green
                        )
                    )
                    .addSwipeLeftLabel(returnMessage)
                    .setSwipeLeftLabelTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
                    .setSwipeLeftLabelColor(ContextCompat.getColor(context!!, R.color.white))
                    .addSwipeRightActionIcon(R.drawable.ic_delete)
                    .addSwipeRightBackgroundColor(
                        ContextCompat.getColor(
                            context!!,
                            R.color.red
                        )
                    )
                    .addSwipeRightLabel(getString(R.string.delete))
                    .setSwipeRightLabelTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)
                    .setSwipeRightLabelColor(ContextCompat.getColor(context!!, R.color.white))
                    .create()
                    .decorate()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(fragment_loan_by_object_recycler_view)
    }

    /**
     * This method configure the recycler view for loan entries
     */
    private fun configureRecyclerView() {
        val ctx = context ?: return

        mUser = (activity as LoanPagerActivity).getCurrentUser()
        val requesterId: String = mUser?.uid ?: ""
        val loanHelper = LoanHelper()
        val firestoreRecylerOptions = loanHelper.getFilteredLoanFirestoreRecylerOptions(requesterId, mMode, activity as LoanPagerActivity)

        mAdapter = LoanAdapter(firestoreRecylerOptions, ctx, mMode)

        val orientation = resources.getInteger(R.integer.gallery_orientation)

        if (fragment_loan_by_object_recycler_view != null) {
            fragment_loan_by_object_recycler_view.setHasFixedSize(true)
            fragment_loan_by_object_recycler_view.layoutManager = LinearLayoutManager(context, orientation, false)
            fragment_loan_by_object_recycler_view.adapter = mAdapter
        }
    }

    /**
     * This method manages the click on an item from the recyclerView
     */
    private fun configureOnClickRecyclerView() {
        ItemClickSupport.addTo(fragment_loan_by_object_recycler_view, R.layout.loanactivity_recyclerview_item_loan)
            .setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(
                    recyclerView: RecyclerView?,
                    position: Int,
                    v: View
                ) {
                    startLoanDetailActivity(v.tag.toString())
                }
            })
    }

    /**
     * This method indicates when the adapter needs to start listening for Firestore
     */
    override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }

    /**
     * This method indicates when the adapter needs to stop listening for Firestore
     */
    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }

    /**
     * This method archives the selected item
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun archiveTheLoan(loan: Loan) {
        val loanHelper = LoanHelper()
        loanHelper.archiveLoan(loan) {result ->
            if (result) {
                if (loan.type == (LoanType.DELIVERY.type)) (activity as LoanPagerActivity).displayCustomToast(getString(R.string.received_message, loan.product), R.drawable.bubble_1, context!!)
                else (activity as LoanPagerActivity).displayCustomToast(getString(R.string.archived_message, loan.product), R.drawable.bubble_1, context!!)
                mAdapter.notifyDataSetChanged()
                NotificationManagement.stopAlarm(loan.id, loan.product, loan.type, loan.recipient_id, activity as LoanPagerActivity, context!!)
            } else {
                (activity as LoanPagerActivity).displayCustomToast(getString(R.string.error_undeleting_loan), R.drawable.bubble_3, context!!)
            }
        }

        Snackbar.make(fragment_loan_by_object_layout, loan.product,Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo)) {unarchiveTheLoan(loan)}.show()
    }

    /**
     * This method unarchives the selected item
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun unarchiveTheLoan(loan: Loan) {
        val loanHelper = LoanHelper()
        loanHelper.unarchiveLoan(loan) {result ->
            if (result) {
                if (loan.type == (LoanType.DELIVERY.type)) (activity as LoanPagerActivity).displayCustomToast(getString(R.string.not_received_message, loan.product), R.drawable.bubble_2, context!!)
                else (activity as LoanPagerActivity).displayCustomToast(getString(R.string.unarchived_message, loan.product), R.drawable.bubble_2, context!!)
                mAdapter.notifyDataSetChanged()
            } else {
                (activity as LoanPagerActivity).displayCustomToast(getString(R.string.error_undeleting_loan), R.drawable.bubble_3, context!!)
            }
        }
    }

    /**
     * This method deletes the selected item
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun deleteTheLoan(loan: Loan) {
        val loanHelper = LoanHelper()
        loanHelper.deleteLoan(loan, -1) {result, loanId ->
            if (result) {
                (activity as LoanPagerActivity).displayCustomToast(getString(R.string.deleted_message, loan.product), R.drawable.bubble_4, context!!)
                NotificationManagement.stopAlarm(loan.id, loan.product, loan.type, loan.recipient_id, activity as LoanPagerActivity, context!!)
                mAdapter.notifyDataSetChanged()
            } else {
                Log.w(TAG, getString(R.string.transaction_failure))
            }
        }

        Snackbar.make(fragment_loan_by_object_layout, loan.product,Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo)) {
                undeleteTheLoan(loan)
            }.show()
    }

    /**
     * This method undeletes the previously detetedItem
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun undeleteTheLoan(loan: Loan) {
        val loanHelper = LoanHelper()
        loanHelper.undeleteLoan(loan, -1) {result ->
            if (result) {
                (activity as LoanPagerActivity).displayCustomToast(getString(R.string.undeleted_message, loan.product), R.drawable.bubble_3, context!!)
                mAdapter.notifyDataSetChanged()
            } else {
                (activity as LoanPagerActivity).displayCustomToast(getString(R.string.error_undeleting_loan), R.drawable.bubble_3, context!!)
            }
        }
    }

    /**
     * This method starts the Loan activity
     */
    fun startLoanDetailActivity(tag: String) {
        val intent = Intent(context, LoanDetailActivity::class.java)
        intent.putExtra(Constant.LOAN_ID, tag)
        startActivity(intent)
    }

}