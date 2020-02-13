package com.depuisletemps.beback.api

import com.depuisletemps.beback.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserHelper {
    companion object {
        private val COLLECTION_NAME: String = "users"

        // COLLECTION REFERENCE
        fun getUsersCollection(): CollectionReference {
            return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
        }

        // CREATE
        fun createUser(
            id: String,
            mail: String,
            firstname: String?,
            lastname: String?,
            pseudo: String?,
            pic: String?
        ): Task<Void> {
            val userToCreate = User(id, mail, firstname, lastname, pseudo, pic)
            return getUsersCollection().document(id).set(userToCreate)
        }

        // GET
        fun getUser(id: String): Task<DocumentSnapshot> {
            return getUsersCollection().document(id).get()
        }

        // UPDATE
        fun updateFirstname(id: String, firstname: String?): Task<Void> {
            return getUsersCollection().document(id).update("firstname", firstname)
        }

        fun updateLastname(id: String, lastname: String?): Task<Void> {
            return getUsersCollection().document(id).update("lastname", lastname)
        }

        fun updatePseudo(id: String, pseudo: String?): Task<Void> {
            return getUsersCollection().document(id).update("pseudo", pseudo)
        }

        fun updatePic(id: String, pic: String?): Task<Void> {
            return getUsersCollection().document(id).update("pic", pic)
        }

        // DELETE
        fun deleteUser(id: String): Task<Void> {
            return getUsersCollection().document(id).delete()
        }
    }
}