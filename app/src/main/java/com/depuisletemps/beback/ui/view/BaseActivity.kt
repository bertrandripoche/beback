package com.depuisletemps.beback.ui.view

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.depuisletemps.beback.R
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


open class BaseActivity: AppCompatActivity() {
    private val TAG = "BaseActivity"
    private val SIGN_OUT_TASK = 10

    val mDb: FirebaseFirestore = FirebaseFirestore.getInstance()

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

    /**
     * This method logout the user from Firebase
     * @param activity is the Activity
     */
    open fun signOutUserFromFirebase(activity: Activity) {
        AuthUI.getInstance()
            .signOut(activity!!)
            .addOnSuccessListener(
                activity,
                this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK, activity)
            )
    }

    open fun updateUIAfterRESTRequestsCompleted(
        origin: Int,
        activity: Activity
    ): OnSuccessListener<Void?> {
        return OnSuccessListener {
            when (origin) {
                SIGN_OUT_TASK -> {
                    startLoginActivity()
                    activity.finish()
                }
                else -> {}
            }
        }
    }

    protected open fun onFailureListener(): OnFailureListener? {
        return OnFailureListener {
            Toast.makeText(
                applicationContext,
                getString(R.string.unknown_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * This method starts the Loan activity
     */
    protected fun startLoanPagerActivity(mode: String) {
        val intent = Intent(this, LoanPagerActivity::class.java)
        intent.putExtra(getString(R.string.mode), mode)
        startActivity(intent)
    }

    /**
     * This method starts the Loan activity
     */
    protected fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}