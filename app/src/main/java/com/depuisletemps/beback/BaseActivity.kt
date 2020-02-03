package com.depuisletemps.beback

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

open class BaseActivity: AppCompatActivity() {
    private val TAG = "BaseActivity"

    /**
     * This method returns the current logged user
     * @return a FirebaseUser object representing the logged user
     */
    open fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    /**
     * This method allows to know if a user is logged
     * @return true if there is a logged user
     */
    open fun isCurrentUserLogged(): Boolean {
        return getCurrentUser() != null
    }

}