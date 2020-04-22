package com.depuisletemps.beback.controller.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.utils.Constant
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.custom_toast.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*


open class BaseActivity: AppCompatActivity() {
    private val TAG = "BaseActivity"
    private val SIGN_OUT_TASK = 10

    val mDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * This method configures the toolbar
     */
    fun configureToolbar() {
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        Objects.requireNonNull(ab)!!.setDisplayHomeAsUpEnabled(true)
    }

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
        intent.putExtra(Constant.MODE, mode)
        startActivity(intent)
    }

    /**
     * This method starts the Loan activity
     */
    protected fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * This method displays a message in a nice way
     */
    fun displayCustomToast(message: String, bubble: Int, context: Context) {
        val inflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custom_toast, custom_toast_container)
        val text: TextView = layout.findViewById(R.id.text)
        text.background = ContextCompat.getDrawable(context, bubble)
        text.text = message
        with (Toast(context)) {
            setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.backgroundTintList = tint
        } else {
            ViewCompat.setBackgroundTintList(button, tint)
        }
    }

    /**
     * This method disables the toggle button
     */
    fun disableToggle(btn: ToggleButton) {
        val greyColor = ContextCompat.getColor(this, R.color.dark_grey)
        val blueColor = ContextCompat.getColor(this, R.color.primaryLightColor)

        btn.isChecked = false
        btn.isClickable = false
        btn.setBackgroundColor(blueColor)
        btn.setTextColor(greyColor)
    }

    /**
     * This method unsets the toggle button
     */
    fun unsetToggle(btn: ToggleButton) {
        val lightGreyColor = ContextCompat.getColor(this, R.color.light_grey)
        val blackColor = ContextCompat.getColor(this, R.color.black)

        btn.isChecked = false
        btn.isClickable = true
        btn.setBackgroundColor(lightGreyColor)
        btn.setTextColor(blackColor)
    }

}