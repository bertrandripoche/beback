package com.depuisletemps.beback.controller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.model.Loaner
import com.depuisletemps.beback.controller.activities.LoanPagerActivity
import com.depuisletemps.beback.model.api.LoanerHelper
import com.depuisletemps.beback.view.recyclerview.LoanerAdapter
import com.depuisletemps.beback.utils.Constant
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_loan_by_person.*

class LoanByPersonFragment: BaseFragment() {

    private val TAG = "LoanByPersonFragment"
    private var mAdapter: LoanerAdapter? = null
    var mUser: FirebaseUser? = null
    lateinit var mDb: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mDb = (activity as LoanPagerActivity).mDb
        mMode = (activity as LoanPagerActivity).mMode

        return inflater.inflate(R.layout.fragment_loan_by_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureRecyclerView()
        setBackgroundForRecyclerView(fragment_loan_by_person_recycler_view)
    }

    /**
     * This method configure the recycler view for loan entries
     */
    private fun configureRecyclerView() {
        val ctx = context ?: return

        mUser = (activity as LoanPagerActivity).getCurrentUser()
        val requesterId: String = mUser?.uid ?: ""

        val loanerHelper = LoanerHelper()
        val firestoreRecyclerOptions = loanerHelper.getFilteredLoanerFirestoreRecylerOptions(requesterId, mMode, activity as LoanPagerActivity)
        mAdapter = LoanerAdapter(firestoreRecyclerOptions, ctx, mMode, requesterId, (activity as LoanPagerActivity).mFilterProduct, (activity as LoanPagerActivity).mFilterType)

        val orientation = resources.getInteger(R.integer.gallery_orientation)

        if (fragment_loan_by_person_recycler_view != null) {
            fragment_loan_by_person_recycler_view.setHasFixedSize(true)
            fragment_loan_by_person_recycler_view.layoutManager = LinearLayoutManager(context, orientation, false)
            fragment_loan_by_person_recycler_view.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            fragment_loan_by_person_recycler_view.adapter = mAdapter
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