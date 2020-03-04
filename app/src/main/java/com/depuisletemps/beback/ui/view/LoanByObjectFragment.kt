package com.depuisletemps.beback.ui.view

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.ui.recyclerview.ItemClickSupport
import com.depuisletemps.beback.ui.recyclerview.LoanAdapter
import com.depuisletemps.beback.utils.Utils.Companion.getTimeStampFromString
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.Timestamp
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_add_loan.*
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

        if (mMode == "Standard") {

            val simpleCallback = object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    viewHolder1: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

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
                    val loan: Loan = mAdapter.getItem(position)
                    lateinit var return_message: String
                    if (loan.type.equals(LoanType.DELIVERY.type)) return_message = getString(R.string.received)
                    else return_message = getString(R.string.returned)

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
                        .addSwipeLeftLabel(return_message)
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
                    super.onChildDraw(c, recyclerView!!, viewHolder!!, dX, dY, actionState, isCurrentlyActive)
                }
            }

            val itemTouchHelper = ItemTouchHelper(simpleCallback)
            itemTouchHelper.attachToRecyclerView(fragment_loan_by_object_recycler_view)
        }

    }

    private fun archiveTheLoan(tag: String, loan: Loan) {
        val loanRef = mDb.collection("loans").document(tag)
        val loanerRef = mDb.collection("users").document(loan.requestor_id).collection("loaners").document(loan.recipient_id)

        val returnedDate: Timestamp = Timestamp.now()

        mDb.runBatch { batch ->
            batch.update(loanRef, "returned_date", returnedDate)
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(+1))
            batch.update(loanerRef, loan.type, FieldValue.increment(-1))
            batch.update(loanerRef, reverseTypeField(loan.type), FieldValue.increment(+1))
        }.addOnCompleteListener {
            if (loan.type.equals(LoanType.DELIVERY.type)) Toast.makeText(context, getString(R.string.received_message, loan.product), Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, getString(R.string.archived_message, loan.product), Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }

        Snackbar.make(fragment_loan_by_object_layout, loan.product,Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo), View.OnClickListener{
                unarchiveTheLoan(tag, loan)
            }).show()
    }

    private fun unarchiveTheLoan(tag: String, loan: Loan) {
        val loanRef = mDb.collection("loans").document(tag)
        val loanerRef = mDb.collection("users").document(loan.requestor_id).collection("loaners").document(loan.recipient_id)

        mDb.runBatch { batch ->
            batch.update(loanRef, "returned_date", null)
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(-1))
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, reverseTypeField(loan.type), FieldValue.increment(-1))
        }.addOnCompleteListener {
            if (loan.type.equals(LoanType.DELIVERY.type)) Toast.makeText(context, getString(R.string.not_received_message, loan.product), Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, getString(R.string.unarchived_message, loan.product), Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }
    }

    private fun deleteTheLoan(tag: String, loan: Loan) {
        val loanRef = mDb.collection("loans").document(tag)
        val loanerRef = mDb.collection("users").document(loan.requestor_id).collection("loaners").document(loan.recipient_id)

        mDb.runBatch { batch ->
            batch.delete(loanRef)
            batch.update(loanerRef, loan.type, FieldValue.increment(-1))
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))
        }.addOnCompleteListener {
            Toast.makeText(context,  getString(R.string.deleted_message, loan.product), Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }

        Snackbar.make(fragment_loan_by_object_layout, loan.product,Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo), View.OnClickListener{
                undeleteTheLoan(tag, loan)
            }).show()
    }

    private fun undeleteTheLoan(tag:String, loan: Loan) {
        val loanRef = mDb.collection("loans").document()
        val loanerRef = mDb.collection("users").document(loan.requestor_id).collection("loaners").document(loan.recipient_id)
        val loanerData = hashMapOf("name" to loan.recipient_id)
        val loan = Loan(loanRef.id, loan.requestor_id, loan.recipient_id, loan.type, loan.product, loan.product_category, loan.creation_date, loan.due_date, loan.returned_date)

        mDb.runBatch { batch ->
            batch.set(loanRef,loan)
            batch.set(loanerRef,loanerData, SetOptions.merge())
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
        }.addOnCompleteListener {
            Toast.makeText(context,  getString(R.string.undeleted_message, loan.product), Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
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
        mLoansRef = mDb.collection("loans")
        if (mMode == getString(R.string.standard)) {
            query = mLoansRef.whereEqualTo("requestor_id", requesterId)
                .whereEqualTo("returned_date", null).orderBy("due_date", Query.Direction.ASCENDING)
        } else {
            query = mLoansRef.whereEqualTo("requestor_id", requesterId)
                .whereGreaterThan("returned_date", getTimeStampFromString("01/01/1970")!! ).orderBy("returned_date", Query.Direction.ASCENDING)
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

    private fun setBackgroundForRecyclerView() {
        if (mMode == getString(R.string.standard)) {
            fragment_loan_by_object_recycler_view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.primaryColor))
        } else {
            fragment_loan_by_object_recycler_view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.grey))
        }
    }

    fun configureOnClickRecyclerView() {
        ItemClickSupport.addTo(fragment_loan_by_object_recycler_view, R.layout.loanactivity_recyclerview_item_loan)
            .setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(
                    recyclerView: RecyclerView?,
                    position: Int,
                    v: View
                ) {
                    Toast.makeText(
                        v.context,
                        "Oh le joli test",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    /**
     * This method send the opposite field, eg : Borrowing -> Ended_borrowing
     */
    fun reverseTypeField(type: String): String {
        when (type) {
            LoanType.LENDING.type -> return LoanType.ENDED_LENDING.type
            LoanType.BORROWING.type -> return LoanType.ENDED_BORROWING.type
            else -> return LoanType.ENDED_DELIVERY.type
        }
    }

    /**
     * This method indicates when the adapter needs to start listening
     */
    override fun onStart() {
        super.onStart()
        mAdapter!!.startListening()
    }

    /**
     * This method indicates when the adapter needs to stop listening
     */
    override fun onStop() {
        super.onStop()
        mAdapter!!.stopListening()
    }
}