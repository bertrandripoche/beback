package com.depuisletemps.beback.ui.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.depuisletemps.beback.R
import com.depuisletemps.beback.api.UserHelper
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*


class LoginActivity : BaseActivity() {

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        checkLoginAndDisplayAppropriateScreen()

        buttonFacebookLogin.setOnClickListener{createSignInIntent("facebook")}
        buttonGoogleLogin.setOnClickListener{createSignInIntent("google")}
        buttonMailLogin.setOnClickListener{createSignInIntent("mail")}
    }

    /**
     * This method check if someone is already logged (and in such case, start the loan activity)
     */
    fun checkLoginAndDisplayAppropriateScreen() {
        if (isCurrentUserLogged()) startLoanPagerActivity()
    }

    /**
     * This method creates the Sign-In intent to trigger the sign-in procedure
     */
    fun createSignInIntent(login: String) {
        val provider: IdpConfig
        provider = if (login == resources.getString(R.string.google)) {
            GoogleBuilder().build()
        } else if (login == resources.getString(R.string.facebook)) {
            FacebookBuilder().build()
        } else {
            EmailBuilder().build()
        }

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(provider))
                .build(),
            RC_SIGN_IN
        )
    }

    /**
     * This method manages the action once the login procedure finished - well or not
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                checkAndCreateFirestoreUser()
                startLoanActivity()
            } else {
                if (response == null) {
                    showSnackBar( login_activity_linear_layout, getString(R.string.error_authentication_canceled)
                    )
                } else if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    showSnackBar(login_activity_linear_layout, getString(R.string.error_no_internet))
                } else if (response.error!!.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(login_activity_linear_layout, getString(R.string.error_unknown))
                } else {
                    showSnackBar(login_activity_linear_layout, getString(R.string.error_undefined))
                }
            }
        }
    }

    /**
     * Method allowing to display a snackbar message
     * @param coordinatorLayout is the element where the snackbar should be displayed
     * @param message is the message we want to display
     */
    fun showSnackBar(linearLayout: LinearLayout, message: String) {
        Snackbar.make(linearLayout, message, Snackbar.LENGTH_SHORT).show()
    }

     /**
     * This method create a user entry in the Firebase database "employees" collection, only if needed
     */
    private fun checkAndCreateFirestoreUser(){
        val user: FirebaseUser? = getCurrentUser()

        val id:String = user?.uid ?: ""
        val mail:String = user?.email ?: ""
        val pic:String = user?.photoUrl.toString() ?: ""
        val displayName:String = user?.displayName ?: ""
        val firstname:String = displayName.split(" ")[0]
        val lastname:String = displayName.split(" ")[1]

         addUserInFirestore(id, mail, firstname, lastname, "", pic)
     }

    /**
     * This method adds the user in Firestore
     */
    fun addUserInFirestore(id: String, mail:String, firstname: String, lastname:String, pseudo:String, pic:String): Task<Void> {
        return UserHelper.createUser(id, mail, firstname, lastname, pseudo, pic)
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }
}
