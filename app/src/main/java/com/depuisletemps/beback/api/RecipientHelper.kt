package com.depuisletemps.beback.api

import com.depuisletemps.beback.api.LoanHelper.Companion.getLoansCollection
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.Loaner
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class RecipientHelper {
    companion object {
        private val COLLECTION_NAME: String = "users"
        private val SUBCOLLECTION_NAME: String = "loaners"

        // COLLECTION REFERENCE
        fun getLoanersCollection(userId: String): CollectionReference {
            return FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(userId).collection(
                SUBCOLLECTION_NAME)
        }

        // CREATE
        fun createLoaner(
            name: String,
            lending: Int,
            borrowing: Int,
            userId: String
        ): Task<DocumentReference> {
            val loanerToCreate = Loaner(name, lending, borrowing)
            return getLoanersCollection(userId).add(loanerToCreate)
        }

        // GET
        fun getLoaner(id: String, userId: String): Task<DocumentSnapshot> {
            return getLoanersCollection(userId).document(id).get()
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

        fun updateDueDate(id: String, due_date: Timestamp): Task<Void> {
            return getLoansCollection().document(id).update("due_date", due_date)
        }

        // DELETE
        fun deleteLoan(id: String): Task<Void> {
            return getLoansCollection().document(id).delete()
        }
    }
}