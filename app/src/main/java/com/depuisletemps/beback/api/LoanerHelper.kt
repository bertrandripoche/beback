package com.depuisletemps.beback.api

import com.depuisletemps.beback.api.LoanHelper.Companion.getLoansCollection
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.Loaner
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*

class LoanerHelper {
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
            lending: Int,
            borrowing: Int,
            ended_lending: Int,
            ended_borrowing: Int,
            delivery: Int,
            requestorId: String,
            recipientId: String
        ): Task<Void> {
            val loanerToCreate = Loaner(recipientId, lending, borrowing, ended_lending, ended_borrowing, delivery)
            return getLoanersCollection(requestorId).document(recipientId).set(loanerToCreate, SetOptions.merge())
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