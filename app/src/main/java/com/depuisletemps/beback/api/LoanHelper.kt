package com.depuisletemps.beback.api

import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanStatus
import com.depuisletemps.beback.utils.Constant
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoanHelper {
    val mDb: FirebaseFirestore = FirebaseFirestore.getInstance()

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
}