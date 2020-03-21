package com.depuisletemps.beback.ui.view

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanAward
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.ui.recyclerview.ItemClickSupport
import com.depuisletemps.beback.ui.recyclerview.LoanAdapter
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.Companion.getStringFromDate
import com.depuisletemps.beback.utils.Utils.Companion.getTimeStampFromString
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.Timestamp
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.custom_toast.*
import kotlinx.android.synthetic.main.fragment_loan_by_object.*

class LoanByObjectFragment: Fragment() {

    private val TAG = "LoanByObjectFragment"
    lateinit var mLoansRef: CollectionReference
    lateinit private var mAdapter: LoanAdapter
    var mUser: FirebaseUser? = null
    lateinit var mDb: FirebaseFirestore
    lateinit var mMode: String

    companion object {
        fun newInstance(): LoanByObjectFragment {
            return LoanByObjectFragment()
        }
    }

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
        setBackgroundForRecyclerView()

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
            points = getPoints(daysDiff).toLong()
        }

        mDb.runBatch { batch ->
            batch.update(loanRef, Constant.RETURNED_DATE, returnedDate)
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(+1))
            batch.update(loanerRef, loan.type, FieldValue.increment(-1))
            batch.update(loanerRef, reverseTypeField(loan.type), FieldValue.increment(+1))
            batch.update(loanerRef, awardsByType(loan.type), FieldValue.increment(points))
        }.addOnCompleteListener {
            if (loan.type.equals(LoanType.DELIVERY.type)) displayCustomToast(getString(R.string.received_message, loan.product), R.drawable.bubble_1)
            else displayCustomToast(getString(R.string.archived_message, loan.product), R.drawable.bubble_1)
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
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(-1))
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, reverseTypeField(loan.type), FieldValue.increment(-1))
            batch.update(loanerRef, awardsByType(loan.type), FieldValue.increment(-points))
        }.addOnCompleteListener {
            if (loan.type.equals(LoanType.DELIVERY.type)) displayCustomToast(getString(R.string.not_received_message, loan.product), R.drawable.bubble_2)
            else displayCustomToast(getString(R.string.unarchived_message, loan.product), R.drawable.bubble_2)
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
            displayCustomToast(getString(R.string.deleted_message, loan.product), R.drawable.bubble_4)
            mAdapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Log.w(TAG, getString(R.string.transaction_failure), e)
        }

        Snackbar.make(fragment_loan_by_object_layout, loan.product,Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo), View.OnClickListener{
                undeleteTheLoan(loan)
            }).show()
    }

    /**
     * This method undeletes the previously detetedItem
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    private fun undeleteTheLoan(loan: Loan) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document()
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan.recipient_id)
        val loanerData = hashMapOf(Constant.NAME to loan.recipient_id)

        mDb.runBatch { batch ->
            batch.set(loanRef,loan)
            batch.set(loanerRef,loanerData, SetOptions.merge())
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
        }.addOnCompleteListener {
            displayCustomToast(getString(R.string.undeleted_message, loan.product), R.drawable.bubble_3)
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

        val query: Query
        mLoansRef = mDb.collection(Constant.LOANS_COLLECTION)
        if (mMode == getString(R.string.standard)) {
            query = mLoansRef.whereEqualTo(Constant.REQUESTOR_ID, requesterId)
                .whereEqualTo(Constant.RETURNED_DATE, null).orderBy(Constant.DUE_DATE, Query.Direction.ASCENDING)
        } else {
            query = mLoansRef.whereEqualTo(Constant.REQUESTOR_ID, requesterId)
                .whereGreaterThan(Constant.RETURNED_DATE, getTimeStampFromString(Constant.FAR_PAST_DATE)!! ).orderBy(Constant.RETURNED_DATE, Query.Direction.ASCENDING)
        }

        val options = FirestoreRecyclerOptions.Builder<Loan>().setQuery(query, Loan::class.java).build()
        mAdapter = LoanAdapter(options, ctx, mMode)

        val orientation = resources.getInteger(R.integer.gallery_orientation)

        if (fragment_loan_by_object_recycler_view != null) {
            fragment_loan_by_object_recycler_view.setHasFixedSize(true)
            fragment_loan_by_object_recycler_view.layoutManager = LinearLayoutManager(context, orientation, false)
            fragment_loan_by_object_recycler_view.adapter = mAdapter
        }

    }

    /**
     * This method sets the color of the background of the recyclerView items
     */
    private fun setBackgroundForRecyclerView() {
        if (mMode == getString(R.string.standard)) {
            fragment_loan_by_object_recycler_view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.primaryColor))
        } else {
            fragment_loan_by_object_recycler_view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.grey))
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
     * This method returns the opposite field, eg : Borrowing -> Ended_borrowing
     * @param type is the type of loan of the Loan object
     * @return a String which is the "opposite" status of the loan type
     */
    private fun reverseTypeField(type: String): String {
        return when (type) {
            LoanType.LENDING.type -> LoanType.ENDED_LENDING.type
            LoanType.BORROWING.type -> LoanType.ENDED_BORROWING.type
            else -> LoanType.ENDED_DELIVERY.type
        }
    }

    /**
     * This method returns the opposite field, eg : Borrowing -> Ended_borrowing
     * @param type is the type of loan of the Loan object
     * @return a String which is the "opposite" status of the loan type
     */
    private fun awardsByType(type: String): String {
        return when (type) {
            LoanType.BORROWING.type -> LoanAward.MINE.type
            else -> LoanAward.THEIR.type
        }
    }

    /**
     * This method returns the number of points given to user (for borrowing) or recipient (for lending and delivery)
     * @param daysDiff is the difference of days between returned date and due date
     * @return a Int which is the number of points to attribute
     */
    private fun getPoints(daysDiff: Int): Int {
        return when {
            daysDiff > 30 -> 4
            daysDiff > 7 -> 3
            daysDiff >= 0 -> 2
            else -> 1
        }
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
     * This method displays a message in a nice way
     */
    fun displayCustomToast(message: String, bubble: Int) {
        val inflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custom_toast, custom_toast_container)
        val text: TextView = layout.findViewById(R.id.text)
        text.background = ContextCompat.getDrawable(context!!, bubble)
        text.text = message
        with (Toast(context)) {
            setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
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