package com.depuisletemps.beback.model.api

import com.depuisletemps.beback.model.User
import com.depuisletemps.beback.utils.Constant
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserHelper {
    val mDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    // COLLECTION REFERENCE
    fun getUsersCollection(): CollectionReference {
        return mDb.collection(Constant.USERS_COLLECTION)
    }

    // CHECK
    fun checkUserInDb(user: User, callback: (Boolean, Boolean) -> Unit) {
        val query = getUsersCollection().whereEqualTo(Constant.MAIL, user.mail)
        query.get()
            .addOnSuccessListener { docs ->
                var userAlreadyExists: Boolean = false
                for (document in docs) {
                    userAlreadyExists = true
                }
                callback(true, userAlreadyExists)
            }
            .addOnFailureListener {
               callback(false, false)
            }
    }

    // CREATE
    fun createUser(user: User): Task<Void> {
        return getUsersCollection().document(user.id).set(user, SetOptions.merge())
    }

    // UPDATE
    fun updateUser(user: User, callback: (Boolean) -> Unit) {
        mDb.runBatch { batch ->
            batch.set(getUsersCollection().document(user.id), user, SetOptions.merge())}
                .addOnSuccessListener { document -> callback(true)}
                .addOnFailureListener { exception -> callback(false)
                }
    }
}