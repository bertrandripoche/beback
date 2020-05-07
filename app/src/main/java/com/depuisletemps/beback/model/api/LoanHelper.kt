package com.depuisletemps.beback.model.api

import android.app.Activity
import com.depuisletemps.beback.controller.activities.LoanPagerActivity
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.model.LoanType
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.Utils
import com.depuisletemps.beback.utils.Utils.getTimeStampFromString
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class LoanHelper {
    val mDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * This method creates the loan
     * @param loan is a Loan object
     * @param callback is a lambda returning a boolean, allowing to choose the following actions
     */
    fun createLoan(loan: Loan, callback: (Boolean, String) -> Unit) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document()
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(
            Constant.LOANERS_COLLECTION).document(loan.recipient)
        val loanerData = hashMapOf(Constant.NAME to loan.recipient)
        loan.id = loanRef.id

        mDb.runBatch { batch ->
            batch.set(loanRef,loan)
            batch.set(loanerRef,loanerData, SetOptions.merge())
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
        }.addOnCompleteListener {
           callback(true, loanRef.id)
        }.addOnFailureListener {
            callback(false, loanRef.id)
        }
    }

    /**
     * This method get a specific loan
     * @param loanId is the id of the loan to be retrieved
     * @param callback is a lambda returning a boolean, allowing to choose the following actions
     */
    fun getLoan(loanId: String, callback: (Loan?) -> Unit) {
        val docRef = mDb.collection(Constant.LOANS_COLLECTION).document(loanId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val loan = documentSnapshot.toObject(Loan::class.java)
            callback(loan)
            }
    }

    /**
     * This method get a specific loan
     * @param loanId is the id of the loan to be retrieved
     * @param callback is a lambda returning a boolean, allowing to choose the following actions
     */
    fun editLoan(loan: Loan, oldRecipient: String?, callback: (Boolean) -> Unit) {
        mDb.runBatch { batch ->
            batch.set(mDb.collection(Constant.LOANS_COLLECTION).document(loan.id), loan, SetOptions.merge())
            if (oldRecipient != null) {
                val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(oldRecipient)
                val loanerRefNew =
                    mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id)
                        .collection(Constant.LOANERS_COLLECTION)
                        .document(loan.recipient)
                val loanerData = hashMapOf(Constant.NAME to loan.recipient)

                when (loan.type) {
                    LoanType.LENDING.type -> batch.update(loanerRef,LoanType.LENDING.type,FieldValue.increment(-1))
                    LoanType.BORROWING.type -> batch.update(loanerRef,LoanType.BORROWING.type,FieldValue.increment(-1))
                    LoanType.DELIVERY.type -> batch.update(loanerRef,LoanType.DELIVERY.type,FieldValue.increment(-1))
                }
                batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))

                batch.set(loanerRefNew, loanerData, SetOptions.merge())
                batch.update(loanerRefNew, LoanStatus.PENDING.type, FieldValue.increment(+1))
                when (loan.type) {
                    LoanType.LENDING.type -> batch.update(loanerRefNew,LoanType.LENDING.type,FieldValue.increment(+1))
                    LoanType.BORROWING.type -> batch.update(loanerRefNew,LoanType.BORROWING.type,FieldValue.increment(+1))
                    LoanType.DELIVERY.type -> batch.update(loanerRefNew,LoanType.DELIVERY.type,FieldValue.increment(+1))
                }
            }
        }
            .addOnSuccessListener { document -> callback(true)}
            .addOnFailureListener { exception -> callback(false)
            }
    }

    /**
     * This method archives the loan
     * @param loan is a Loan object
     * @param callback is a lambda returning a boolean, allowing to choose the following actions
     */
    fun archiveLoan(loan: Loan, callback: (Boolean) -> Unit) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(loan.id)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan.recipient)

        val returnedDate: Timestamp = Timestamp.now()
        loan.returned_date = returnedDate

        var points = Utils.retrievePointsFromLoan(loan)

        mDb.runBatch { batch ->
            batch.update(loanRef, Constant.RETURNED_DATE, returnedDate)
            batch.update(loanRef, Constant.NOTIF, null)
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(+1))
            batch.update(loanerRef, loan.type, FieldValue.increment(-1))
            batch.update(loanerRef, Utils.reverseTypeField(loan.type), FieldValue.increment(+1))
            batch.update(loanerRef, Utils.awardsByType(loan.type), FieldValue.increment(points))
        }.addOnCompleteListener {
            callback(true)
        }.addOnFailureListener {
            callback(false)
        }
    }

    /**
     * This method unarchives the selected item
     * @param tag is a String representing the id of the loan
     * @param loan is a Loan representing the loan object
     */
    fun unarchiveLoan(loan: Loan, callback: (Boolean) -> Unit) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(loan.id)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan.recipient)
        var points = Utils.retrievePointsFromLoan(loan)

        mDb.runBatch { batch ->
            batch.update(loanRef, Constant.RETURNED_DATE, null)
            batch.update(loanRef, Constant.NOTIF, null)
            batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
            batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(-1))
            batch.update(loanerRef, loan.type, FieldValue.increment(+1))
            batch.update(loanerRef, Utils.reverseTypeField(loan.type), FieldValue.increment(-1))
            batch.update(loanerRef, Utils.awardsByType(loan.type), FieldValue.increment(-points))
        }.addOnCompleteListener {
            callback(true)
        }.addOnFailureListener {
            callback(false)
        }
    }

    /**
     * This method deletes the loan
     * @param loan is a Loan object
     * @param callback is a lambda returning a boolean, allowing to choose the following actions
     */
    fun deleteLoan(loan: Loan, points: Long, callback: (Boolean, String) -> Unit) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(loan.id)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(
            Constant.LOANERS_COLLECTION).document(loan.recipient)

        mDb.runBatch { batch ->
            batch.delete(loanRef)
            if (points <0) {
                batch.update(loanerRef, loan.type, FieldValue.increment(-1))
                batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(-1))
            } else {
                batch.update(loanerRef, Utils.reverseTypeField(loan.type), FieldValue.increment(-1))
                batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(-1))
                batch.update(loanerRef, Utils.awardsByType(loan.type), FieldValue.increment(-points))
            }

        }.addOnCompleteListener {
            callback(true, loanRef.id)
        }.addOnFailureListener {
            callback(false, loanRef.id)
        }
    }

    /**
     * This method undeletes the loan
     * @param loan is a Loan object
     * @param points is the number of points previously attributed for this loan
     * @param callback is a lambda returning a boolean, allowing to choose the following actions
     */
    fun undeleteLoan(loan: Loan, points: Long, callback: (Boolean) -> Unit) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(loan.id)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION)
            .document(loan.recipient)
        val loanerData = hashMapOf(Constant.NAME to loan.recipient)

        mDb.runBatch { batch ->
            batch.set(loanRef, loan)
            batch.set(loanerRef, loanerData, SetOptions.merge())
            if (points < 0) {
                batch.update(loanerRef, loan.type, FieldValue.increment(+1))
                batch.update(loanerRef, LoanStatus.PENDING.type, FieldValue.increment(+1))
            } else {
                batch.update(loanerRef, Utils.reverseTypeField(loan.type), FieldValue.increment(+1))
                batch.update(loanerRef, LoanStatus.ENDED.type, FieldValue.increment(+1))
                batch.update(loanerRef, Utils.awardsByType(loan.type), FieldValue.increment(points))
            }
        }.addOnCompleteListener {
            callback(true)
        }.addOnFailureListener {
            callback(false)
        }
    }

    /**
     * This method gets the product names of the loans owned by the user
     * @param userId is the id of the user
     * @param callback is a lambda returning a boolean, allowing to choose the following actions
     */
    fun getLoanNames(userId: String, callback: (Boolean, MutableList<String>?) -> Unit) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION)
        loanRef.whereEqualTo(Constant.REQUESTOR_ID, userId)
            .get()
            .addOnSuccessListener {result ->
                val listNames = mutableListOf<String>()
                for (document in result) listNames.add(document.data.getValue(Constant.PRODUCT).toString())
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
    fun getFilteredLoanFirestoreRecylerOptions(requesterId: String, mode: String, activity: Activity): FirestoreRecyclerOptions<Loan> {
        var query: Query
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION)
        if (mode == Constant.STANDARD) {
            query = loanRef.whereEqualTo(Constant.REQUESTOR_ID, requesterId)

            if ((activity as LoanPagerActivity).mFilterProduct != null)
                query = query.whereEqualTo(Constant.PRODUCT, (activity as LoanPagerActivity).mFilterProduct)
            if ((activity as LoanPagerActivity).mFilterRecipient != null)
                query = query.whereEqualTo(Constant.RECIPIENT_ID, (activity as LoanPagerActivity).mFilterRecipient)
            if ((activity as LoanPagerActivity).mFilterType != null)
                query = query.whereEqualTo(Constant.TYPE, (activity as LoanPagerActivity).mFilterType)

            query= query.whereEqualTo(Constant.RETURNED_DATE, null).orderBy(Constant.DUE_DATE, Query.Direction.ASCENDING)
        } else {
            query = loanRef.whereEqualTo(Constant.REQUESTOR_ID, requesterId)

            if ((activity as LoanPagerActivity).mFilterProduct != null)
                query = query.whereEqualTo(Constant.PRODUCT, (activity as LoanPagerActivity).mFilterProduct)
            if ((activity as LoanPagerActivity).mFilterRecipient != null)
                query = query.whereEqualTo(Constant.RECIPIENT_ID, (activity as LoanPagerActivity).mFilterRecipient)
            if ((activity as LoanPagerActivity).mFilterType != null)
                query = query.whereEqualTo(Constant.TYPE, (activity as LoanPagerActivity).mFilterType)
            query= query.whereGreaterThan(Constant.RETURNED_DATE, getTimeStampFromString(Constant.FAR_PAST_DATE)!! ).orderBy(Constant.RETURNED_DATE, Query.Direction.ASCENDING)
        }

        return FirestoreRecyclerOptions.Builder<Loan>().setQuery(query, Loan::class.java).build()
    }
}