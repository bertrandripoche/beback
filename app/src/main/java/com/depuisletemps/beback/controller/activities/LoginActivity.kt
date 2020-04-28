package com.depuisletemps.beback.controller.activities

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.api.UserHelper
import com.depuisletemps.beback.model.User
import com.depuisletemps.beback.utils.Constant
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
        createNotificationChannel()

        buttonFacebookLogin.setOnClickListener{createSignInIntent(Constant.FB)}
        buttonGoogleLogin.setOnClickListener{createSignInIntent(Constant.GOOGLE)}
        buttonMailLogin.setOnClickListener{createSignInIntent(Constant.MAIL)}
    }

    /**
     * This method check if someone is already logged (and in such case, start the loan activity)
     */
    private fun checkLoginAndDisplayAppropriateScreen() {
        if (isCurrentUserLogged()) startLoanPagerActivity(Constant.STANDARD)
    }

    /**
     * This method creates the Sign-In intent to trigger the sign-in procedure
     */
    private fun createSignInIntent(login: String) {
        val provider: IdpConfig = when (login) {
            resources.getString(R.string.google) -> GoogleBuilder().build()
            resources.getString(R.string.facebook) -> FacebookBuilder().build()
            else -> EmailBuilder().build()
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
//                startLoanPagerActivity(Constant.STANDARD)
            } else {
                when {
                    response == null ->
                        showSnackBar( login_activity_linear_layout, getString(R.string.error_authentication_canceled))
                    response.error!!.errorCode == ErrorCodes.NO_NETWORK ->
                        showSnackBar(login_activity_linear_layout, getString(R.string.error_no_internet))
                    response.error!!.errorCode == ErrorCodes.UNKNOWN_ERROR ->
                        showSnackBar(login_activity_linear_layout, getString(R.string.error_unknown))
                    else ->
                        showSnackBar(login_activity_linear_layout, getString(R.string.error_undefined))
                }
            }
        }
    }

    /**
     * Method allowing to display a snackbar message
     * @param linearLayout is the element where the snackbar should be displayed
     * @param message is the message we want to display
     */
    private fun showSnackBar(linearLayout: LinearLayout, message: String) {
        Snackbar.make(linearLayout, message, Snackbar.LENGTH_SHORT).show()
    }

     /**
     * This method create a user entry in the Firebase database "employees" collection, only if needed
     */
    private fun checkAndCreateFirestoreUser(){
        val firebaseUser: FirebaseUser? = getCurrentUser()

        val id:String = firebaseUser?.uid ?: ""
        val mail:String = firebaseUser?.email ?: ""
        val pic:String = firebaseUser?.photoUrl.toString()
        val displayName:String = firebaseUser?.displayName ?: ""
        val firstname:String = displayName.split(" ")[0]
        val lastname:String = displayName.replace(firstname,"").trim()

         val user = User(id, mail, firstname, lastname, "", pic)
         checkUserInDb(user)
     }

    /**
     * This method checks existence of user in Firestore db
     */
    fun checkUserInDb(user: User) {
        val userHelper = UserHelper()
        userHelper.checkUserInDb(user) {result,documentExists ->
            if (result) {
                if (!documentExists) addUserInFirestore(user)
                startLoanPagerActivity(Constant.STANDARD)
            } else {
                Log.d(TAG, getString(R.string.transaction_failure))
            }
        }
    }

    /**
     * This method adds the user in Firestore
     */
    fun addUserInFirestore(user: User): Task<Void> {
        val userHelper = UserHelper()
        return userHelper.createUser(user)
    }

    /**
     * This method adds the user in Firestore
     */
    private fun createNotificationChannel() { // Needed for notification feature, need to be started first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constant.CHANNEL_ID, Constant.CHANNEL_NAME, importance)
            channel.description = Constant.CHANNEL_DESCRIPTION
            val notificationManager = getSystemService(NotificationManager::class.java)
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel)
        }
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }
}
