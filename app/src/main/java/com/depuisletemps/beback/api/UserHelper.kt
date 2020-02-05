package com.depuisletemps.beback.api

import bolts.Task
import com.depuisletemps.beback.model.User
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class UserHelper {
    private val COLLECTION_NAME: String = "users"

    public fun getUsersCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }




}