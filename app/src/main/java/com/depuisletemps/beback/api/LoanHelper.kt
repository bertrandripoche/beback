package com.depuisletemps.beback.api

import com.depuisletemps.beback.model.Loan
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LoanHelper {
    private val COLLECTION_NAME: String = "loans"

    // COLLECTION REFERENCE
    fun getLoansCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    // CREATE
    fun createLoan(id:String, requestor_id:String, recipient_id:String, type:String, product:String, product_category:String, creation_date : String, due_date:String, returned_date:String): Task<Void>{
        val loanToCreate = Loan(id, requestor_id, recipient_id, type, product, product_category, creation_date, due_date, returned_date)
        return getLoansCollection().document(id).set(loanToCreate)
    }

    // GET
    fun getLoan(id: String): Task<DocumentSnapshot> {
        return getLoansCollection().document(id).get()
    }

    // UPDATE
    fun updateRecipient(id: String, recipient_id: String): Task<Void> {
        return getLoansCollection().document(id).update("recipient_id", recipient_id)
    }

    fun updateType(id: String, type: String): Task<Void> {
        return getLoansCollection().document(id).update("type", type)
    }

    fun updateProduct(id: String, product: String): Task<Void> {
        return getLoansCollection().document(id).update("product", product)
    }

    fun updateProductCategory(id: String, product_category: String): Task<Void> {
        return getLoansCollection().document(id).update("product_category", product_category)
    }

    fun updateDueDate(id: String, due_date: Date): Task<Void> {
        return getLoansCollection().document(id).update("due_date", due_date)
    }

    // DELETE
    fun deleteLoan(id: String): Task<Void> {
        return getLoansCollection().document(id).delete()
    }
}