package com.depuisletemps.beback

import android.R
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


open class BaseActivity: AppCompatActivity() {
    private val TAG = "BaseActivity"
    protected val mDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * This method returns the current logged user
     * @return a FirebaseUser object representing the logged user
     */
    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    /**
     * This method allows to know if a user is logged
     * @return true if there is a logged user
     */
    fun isCurrentUserLogged(): Boolean {
        return getCurrentUser() != null
    }

    protected open fun onFailureListener(): OnFailureListener? {
        return OnFailureListener {
            Toast.makeText(
                applicationContext,
                "Damned, we hit an unknown error",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}