package com.depuisletemps.beback.model.api

import android.app.Activity
import com.depuisletemps.beback.controller.activities.LoanPagerActivity
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.model.Loaner
import com.depuisletemps.beback.utils.Constant
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LoanerHelper {
    val mDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * This method gets the recipient names from the loans owned by the user
     * @param userId is the id of the user
     * @param callback is a lambda returning a boolean, allowing to choose the following actions
     */
    fun getLoanersNames(userId: String, callback: (Boolean, MutableList<String>?) -> Unit) {
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(userId)
            .collection(Constant.LOANERS_COLLECTION)
        loanerRef
            .get()
            .addOnSuccessListener {result ->
                val listNames = mutableListOf<String>()
                for (document in result) listNames.add(document.data.getValue(Constant.NAME).toString())

                callback(true,listNames)
            }
            .addOnFailureListener { exception ->
                callback(false,null)
            }
    }

    /**
     * This method allows to get the required options for RecyclerView for "by person" screen
     * @param requesterId is the id of the user
     * @param mode is the current mode, standard or archive
     * @param activity is the activity where the recyclerView resides
     * @return a FirestoreRecyclerOptions object required for the adapter
     */
    fun getFilteredLoanerFirestoreRecylerOptions(requesterId: String, mode: String, activity: Activity): FirestoreRecyclerOptions<Loaner> {
        var query: Query
        val loanersRef = mDb.collection(Constant.USERS_COLLECTION).document(requesterId).collection(Constant.LOANERS_COLLECTION)
        query = loanersRef
        if (mode == Constant.STANDARD) {
            if ((activity as LoanPagerActivity).mFilterRecipient != null)
                query = query.whereEqualTo(Constant.NAME, (activity).mFilterRecipient)
            query = query.whereGreaterThanOrEqualTo(LoanStatus.PENDING.type, 1).orderBy(LoanStatus.PENDING.type, Query.Direction.DESCENDING)
        } else {
            if ((activity as LoanPagerActivity).mFilterRecipient != null)
                query = query.whereEqualTo(Constant.NAME, (activity).mFilterRecipient)
            query = query.whereGreaterThanOrEqualTo(LoanStatus.ENDED.type, 1).orderBy(LoanStatus.ENDED.type, Query.Direction.DESCENDING)
        }

        return FirestoreRecyclerOptions.Builder<Loaner>().setQuery(query, Loaner::class.java).build()
    }
}