package com.depuisletemps.beback.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.ui.recyclerview.ItemClickSupport
import com.depuisletemps.beback.ui.recyclerview.LoanAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.depuisletemps.beback.utils.Utils.Companion.getTimeStampFromString
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_loan_by_object.*
import kotlinx.android.synthetic.main.fragment_loan_by_person.*

class LoanByObjectFragment: Fragment() {

    private val TAG = "LoanByObjectFragment"
    lateinit var mLoansRef: CollectionReference
    private var mAdapter: LoanAdapter? = null
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
        ItemClickSupport.addTo(fragment_loan_by_object_recycler_view, R.layout.loanactivity_recyclerview_item_loan_swipelayout)
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