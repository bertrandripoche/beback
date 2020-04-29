package com.depuisletemps.beback.model.api

import com.depuisletemps.beback.utils.Constant
import com.google.firebase.firestore.FirebaseFirestore

class LoanerHelper {
    val mDb: FirebaseFirestore = FirebaseFirestore.getInstance()

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
}