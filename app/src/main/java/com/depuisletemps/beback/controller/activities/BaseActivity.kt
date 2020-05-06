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


open abstract class BaseActivity: AppCompatActivity() {
    private val TAG = "BaseActivity"

    protected var orangeColor: Int = 0
    protected var yellowColor: Int = 0
    protected var blueDeeperColor: Int = 0
    protected var blackColor: Int = 0
    protected var redColor: Int = 0
    protected var greenColor: Int = 0
    protected var lightGreyColor = 0
    protected var greyColor = 0
    protected var darkGreyColor = 0
    protected var blueColor = 0

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
                this.updateUIAfterRESTRequestsCompleted(Constant.SIGN_OUT_TASK, activity)
            )
    }

    open fun updateUIAfterRESTRequestsCompleted(origin: Int, activity: Activity): OnSuccessListener<Void?> {
        return OnSuccessListener {
            when (origin) {
                Constant.SIGN_OUT_TASK -> {
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
    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * Declare the colors to be available anywhere
     */
    protected fun defineTheColors(context: Context) {
        lightGreyColor = ContextCompat.getColor(context, R.color.light_grey)
        greyColor = ContextCompat.getColor(context, R.color.dark_grey)
        blueColor = ContextCompat.getColor(context, R.color.primaryLightColor)
        yellowColor = ContextCompat.getColor(context, R.color.secondaryColor)
        blueDeeperColor = ContextCompat.getColor(context, R.color.primaryColor)
        blackColor = ContextCompat.getColor(context, R.color.black)
        greenColor = ContextCompat.getColor(context, R.color.green)
        redColor = ContextCompat.getColor(context, R.color.red)
        orangeColor = ContextCompat.getColor(this, R.color.secondaryDarkColor)
        darkGreyColor = ContextCompat.getColor(this, R.color.dark_grey)
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

    /**
     * Sets the background color of the FloatActionButton
     */
    private fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) button.backgroundTintList = tint
        else ViewCompat.setBackgroundTintList(button, tint)
    }

    /**
     * Make the float button enabled
     */
    private fun enableFloatButton(btn: FloatingActionButton, context: Context) {
        setButtonTint(btn, ColorStateList.valueOf(ContextCompat.getColor(context,R.color.secondaryColor)) )
    }

    /**
     * Make the float button disabled
     */
    protected fun disableFloatButton(btn: FloatingActionButton, context: Context) {
        setButtonTint(btn, ColorStateList.valueOf(ContextCompat.getColor(context,R.color.light_grey)) )
    }

    /**
     * This method enables/disables the edit button
     */
    fun setFloatBtnState(validForm: Boolean, btn: FloatingActionButton, context: Context) {
        if (validForm) enableFloatButton(btn, context)
        else disableFloatButton(btn, context)
    }
}