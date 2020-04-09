package com.depuisletemps.beback.ui.view

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
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.ui.recyclerview.ItemClickSupport
import com.depuisletemps.beback.ui.recyclerview.LoanAdapter
import com.depuisletemps.beback.utils.NotificationManagement
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.getStringFromDate
import com.depuisletemps.beback.utils.Utils.getTimeStampFromString
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_loan_by_object.*

class LoanByObjectFragment: BaseFragment() {

    private val TAG = "LoanByObjectFragment"
    lateinit var mLoansRef: CollectionReference
    lateinit private var mAdapter: LoanAdapter
    var mUser: FirebaseUser? = null
    lateinit var mDb: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
        mDb = (activity as LoanPagerActivity).mDb
        mMode = (activity as LoanPagerActivity).mMode
        return inflater.inflate(R.layout.fragment_loan_by_object, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureRecyclerView()
        configureOnClickRecyclerView()
        setBackgroundForRecyclerView(fragment_loan_by_object_recycler_view)

        if (mMode == Constant.STANDARD) {

            val simpleCallback = object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    viewHolder1: RecyclerView.ViewHolder
                ): Boolean {
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
                        ItemTouchHelper.RIGHT -> deleteTheLoan(viewHolder.itemView.tag.toString(), loan)

                        // Left : Archive
                        ItemTouchHelper.LEFT -> archiveTheLoan(viewHolder.itemView.tag.toString(), loan)

                        else -> return
                    }
                }

                /**
                 * this method manages the layout below the item (for swipe effect)
                 */
                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean ) {
                    val position = viewHolder.adapterPosition
                    var returnMessage: String = ""
                    if (position >= 0) {
                        val loan: Loan = mAdapter.getItem(position)
                        if (loan.type.equals(LoanType.DELIVERY.type)) returnMessage =
                            getString(R.string.received)
                        else returnMessage = getString(R.string.returned)
                    }

                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
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

    }

    /**
     * This method archives the selected item
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun archiveTheLoan(tag: String, loan: Loan) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(tag)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan.recipient_id)

        val returnedDate: Timestamp = Timestamp.now()

        var points: Long = 1
        if (getStringFromDate(loan.due_date?.toDate()) != Constant.FAR_AWAY_DATE) {
            val dueDateLocalDate = Utils.getLocalDateFromString(getStringFromDate(loan.due_date?.toDate()))
            val returnedLocalDate = Utils.getLocalDateFromString(getStringFromDate(returnedDate.toDate()))
            val daysDiff: Int = Utils.getDifferenceDays(dueDateLocalDate,returnedLocalDate)
            points = Utils.getPoints(daysDiff).toLong()
        }

        mDb.runBatch { batch ->
            batch.update(loanRef, Constant.RETURNED_DATE, returnedDate)
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(+1))
            batch.update(loanerRef, loan.type, FieldValue.increment(-1))
            batch.update(loanerRef, Utils.reverseTypeField(loan.type), FieldValue.increment(+1))
            batch.update(loanerRef, Utils.awardsByType(loan.type), FieldValue.increment(points))
        }.addOnCompleteListener {
            if (loan.type.equals(LoanType.DELIVERY.type)) (activity as LoanPagerActivity).displayCustomToast(getString(R.string.received_message, loan.product), R.drawable.bubble_1, context!!)
            else (activity as LoanPagerActivity).displayCustomToast(getString(R.string.archived_message, loan.product), R.drawable.bubble_1, context!!)
            NotificationManagement.stopAlarm(loan.id, loan.product, loan.type, loan.recipient_id, activity as LoanPagerActivity, context!!)

            mAdapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Log.w(TAG, getString(R.string.transaction_failure), e)
        }

        Snackbar.make(fragment_loan_by_object_layout, loan.product,Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo), View.OnClickListener{
                unarchiveTheLoan(tag, loan, points)
            }).show()
    }

    /**
     * This method unarchives the selected item
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun unarchiveTheLoan(tag: String, loan: Loan, points: Long) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(tag)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan.recipient_id)

        mDb.runBatch { batch ->
            batch.update(loanRef, Constant.RETURNED_DATE, null)
            batch.update(loanRef, Constant.NOTIF, null)
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(-1))
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, Utils.reverseTypeField(loan.type), FieldValue.increment(-1))
            batch.update(loanerRef, Utils.awardsByType(loan.type), FieldValue.increment(-points))
        }.addOnCompleteListener {
            if (loan.type.equals(LoanType.DELIVERY.type))             (activity as LoanPagerActivity).displayCustomToast(getString(R.string.not_received_message, loan.product), R.drawable.bubble_2, context!!)
            else (activity as LoanPagerActivity).displayCustomToast(getString(R.string.unarchived_message, loan.product), R.drawable.bubble_2, context!!)
            mAdapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Log.w(TAG, getString(R.string.transaction_failure), e)
        }
    }

    /**
     * This method deletes the selected item
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun deleteTheLoan(tag: String, loan: Loan) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(tag)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan.recipient_id)

        mDb.runBatch { batch ->
            batch.delete(loanRef)
            batch.update(loanerRef, loan.type, FieldValue.increment(-1))
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))
        }.addOnCompleteListener {
            (activity as LoanPagerActivity).displayCustomToast(getString(R.string.deleted_message, loan.product), R.drawable.bubble_4, context!!)
            NotificationManagement.stopAlarm(loan.id, loan.product, loan.type, loan.recipient_id, activity as LoanPagerActivity, context!!)
            mAdapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Log.w(TAG, getString(R.string.transaction_failure), e)
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
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(loan.id)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan.recipient_id)
        val loanerData = hashMapOf(Constant.NAME to loan.recipient_id)

        mDb.runBatch { batch ->
            batch.set(loanRef,loan)
            batch.set(loanerRef,loanerData, SetOptions.merge())
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
        }.addOnCompleteListener {
            (activity as LoanPagerActivity).displayCustomToast(getString(R.string.undeleted_message, loan.product), R.drawable.bubble_3, context!!)
            mAdapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Log.w(TAG, getString(R.string.transaction_failure), e)
        }
    }

    /**
     * This method configure the recycler view for loan entries
     */
    private fun configureRecyclerView() {
        val ctx = context ?: return

        mUser = (activity as LoanPagerActivity).getCurrentUser()
        val requesterId: String = mUser?.uid ?: ""

        var query: Query
        mLoansRef = mDb.collection(Constant.LOANS_COLLECTION)
        if (mMode == Constant.STANDARD) {
            query = mLoansRef.whereEqualTo(Constant.REQUESTOR_ID, requesterId)

            if ((activity as LoanPagerActivity).mFilterProduct != null)
                query = query.whereEqualTo(Constant.PRODUCT, (activity as LoanPagerActivity).mFilterProduct)
            if ((activity as LoanPagerActivity).mFilterRecipient != null)
                query = query.whereEqualTo(Constant.RECIPIENT_ID, (activity as LoanPagerActivity).mFilterRecipient)
            if ((activity as LoanPagerActivity).mFilterType != null)
                query = query.whereEqualTo(Constant.TYPE, (activity as LoanPagerActivity).mFilterType)

            query= query.whereEqualTo(Constant.RETURNED_DATE, null).orderBy(Constant.DUE_DATE, Query.Direction.ASCENDING)
        } else {
            query = mLoansRef.whereEqualTo(Constant.REQUESTOR_ID, requesterId)

            if ((activity as LoanPagerActivity).mFilterProduct != null)
                query = query.whereEqualTo(Constant.PRODUCT, (activity as LoanPagerActivity).mFilterProduct)
            if ((activity as LoanPagerActivity).mFilterRecipient != null)
                query = query.whereEqualTo(Constant.RECIPIENT_ID, (activity as LoanPagerActivity).mFilterRecipient)
            if ((activity as LoanPagerActivity).mFilterType != null)
                query = query.whereEqualTo(Constant.TYPE, (activity as LoanPagerActivity).mFilterType)
            query= query.whereGreaterThan(Constant.RETURNED_DATE, getTimeStampFromString(Constant.FAR_PAST_DATE)!! ).orderBy(Constant.RETURNED_DATE, Query.Direction.ASCENDING)
        }

        val options = FirestoreRecyclerOptions.Builder<Loan>().setQuery(query, Loan::class.java).build()
        mAdapter = LoanAdapter(options, ctx, mMode)

        val orientation = resources.getInteger(R.integer.gallery_orientation)

        if (fragment_loan_by_object_recycler_view != null) {
            fragment_loan_by_object_recycler_view.setHasFixedSize(true)
            fragment_loan_by_object_recycler_view.layoutManager = LinearLayoutManager(context, orientation, false)
            //fragment_loan_by_object_recycler_view.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            fragment_loan_by_object_recycler_view.adapter = mAdapter
        }
    }

    /**
     * This method manages the click on an item from the recyclerView
     */
    fun configureOnClickRecyclerView() {
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
     * This method indicates when the adapter needs to start listening
     */
    override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }

    /**
     * This method indicates when the adapter needs to stop listening
     */
    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
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