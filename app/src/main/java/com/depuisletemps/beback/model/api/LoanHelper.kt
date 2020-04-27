package com.depuisletemps.beback.model.api

import com.depuisletemps.beback.R
import com.depuisletemps.beback.controller.activities.LoanDetailActivity
import com.depuisletemps.beback.interfaces.NotifyDetailActivity
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanAction
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.utils.Constant
import com.depuisletemps.beback.utils.Utils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_profile.view.*

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
            Constant.LOANERS_COLLECTION).document(loan.recipient_id)
        val loanerData = hashMapOf(Constant.NAME to loan.recipient_id)
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
     * This method archives the loan
     * @param loan is a Loan object
     * @param callback is a lambda returning a boolean, allowing to choose the following actions
     */
    fun archiveLoan(loan: Loan, callback: (Boolean) -> Unit) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(loan.id)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan.recipient_id)

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
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION).document(loan.recipient_id)
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
            Constant.LOANERS_COLLECTION).document(loan.recipient_id)

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
//            val loanDetailActivity = LoanDetailActivity()
//            loanDetailActivity.displayToast("", R.drawable.bubble_3)
        }.addOnFailureListener {
            callback(false, loanRef.id)
        }
    }

    fun undeleteLoan(loan: Loan, points: Long, callback: (Boolean) -> Unit) {
        val loanRef = mDb.collection(Constant.LOANS_COLLECTION).document(loan.id)
        val loanerRef = mDb.collection(Constant.USERS_COLLECTION).document(loan.requestor_id).collection(Constant.LOANERS_COLLECTION)
            .document(loan.recipient_id)
        val loanerData = hashMapOf(Constant.NAME to loan.recipient_id)

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

}