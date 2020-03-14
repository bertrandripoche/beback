package com.depuisletemps.beback.ui.view

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.mBtnEdit
import kotlinx.android.synthetic.main.custom_toast.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class ProfileActivity: BaseActivity() {
    private val TAG = "ProfileActivity"

    var mFirstTime: Boolean = true
    var mUserFb: FirebaseUser? = null
    lateinit var mUser: User
    var mFirst: String = ""
    var mLast: String = ""
    var mPseudo: String = ""
    var mMail: String = ""
    var mId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        configureToolbar()
        getSavedInstanceData(savedInstanceState)

        getUserInfos()

        configureTextWatchers()

        mBtnEdit.setOnClickListener{
            if (isFormValid())
                editFirestoreUser(mId)
            else {
                Toast.makeText(applicationContext, R.string.invalid_edit_profile_form, Toast.LENGTH_LONG)
                    .show()
            }
        }

        mBtnLogout.setOnClickListener {
            displayCustomToast(getString(R.string.sign_out_message, firstname.text.toString()), R.drawable.bubble_1)
            Snackbar.make(activity_profile, R.string.logout, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo)) {
                }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onShown(transientBottomBar: Snackbar?) {
                    }

                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            signOutUserFromFirebase(this@ProfileActivity)
                        }
                    }
                }).show()
        }
    }

    fun getSavedInstanceData(savedInstanceState: Bundle?) {
        if (savedInstanceState != null){
            firstname.setText(savedInstanceState.getString("first"))
            lastname.setText(savedInstanceState.getString("last")!!)
            pseudo.setText(savedInstanceState.getString("pseudo")!!)
            mail.text = savedInstanceState.getString("mail")!!
            mFirst = savedInstanceState.getString("mFirst")!!
            mLast = savedInstanceState.getString("mLast")!!
            mPseudo = savedInstanceState.getString("mPseudo")!!
            mMail = savedInstanceState.getString("mail")!!
            mFirstTime = false
            setEditBtnState()
            setEditFieldsTextColor()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("mFirst", mFirst)
        outState.putString("mLast", mLast)
        outState.putString("mPseudo", mPseudo)
        outState.putString("first", firstname.text.toString())
        outState.putString("last", lastname.text.toString())
        outState.putString("pseudo", pseudo.text.toString())
        outState.putString("mail", mMail)
    }

    /**
     * This method gets user
     */
    private fun getUserInfos() {
        mUserFb = getCurrentUser()
        val docRef = mDb.collection("users").document(mUserFb!!.uid)

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                mUser = documentSnapshot.toObject(User::class.java)!!
                mFirst = documentSnapshot.toObject(User::class.java)?.firstname.toString()
                mLast = documentSnapshot.toObject(User::class.java)?.lastname.toString()
                mPseudo = documentSnapshot.toObject(User::class.java)?.pseudo.toString()
                mMail = documentSnapshot.toObject(User::class.java)?.mail.toString()
                mId = documentSnapshot.toObject(User::class.java)?.id.toString()
                if (mFirstTime) configureScreen()
            }
    }

    /**
     * This method configures the toolbar
     */
    private fun configureToolbar() {
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        Objects.requireNonNull(ab)!!.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Method to configure the textWatchers on the fields which requires it
     */
    fun configureTextWatchers() {
        firstname.addTextChangedListener(textWatcher)
        lastname.addTextChangedListener(textWatcher)
        pseudo.addTextChangedListener(textWatcher)
    }

    /**
     * This method fills the existing information for the user
     */
    private fun configureScreen() {
        firstname.setText(mFirst)
        lastname.setText(mLast)
        pseudo.setText(mPseudo)
        mail.text = mMail

        disableFloatButton()
    }

    /**
     * Method to describe the actions to complete on text writing
     */
    val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) {
            // Enable-disable Floating Action Button
            setEditBtnState()
            setEditFieldsTextColor()
        }
    }

    /**
     * This method enable/disable the edit button
     */
    fun setEditBtnState() {
        if (isFormValid()) {
            enableFloatButton()
        } else {
            disableFloatButton()
        }
    }

    /**
     * This method enable/disable the edit button
     */
    fun setEditFieldsTextColor() {
        val blackColor = ContextCompat.getColor(this, R.color.black)
        val greyColor = ContextCompat.getColor(this, R.color.grey)

        if (firstname.text.toString() != mFirst) firstname.setTextColor(blackColor) else firstname.setTextColor(greyColor)
        if (lastname.text.toString() != mLast) lastname.setTextColor(blackColor) else lastname.setTextColor(greyColor)
        if (pseudo.text.toString() != mPseudo) pseudo.setTextColor(blackColor) else pseudo.setTextColor(greyColor)
    }

    /**
     * Method to configure the textWatchers on the fields which requires it
     */
    private fun isFormValid(): Boolean {

        return ((firstname.text.toString() != mFirst
                || lastname.text.toString() != mLast
                || pseudo.text.toString() != mPseudo)
                && firstname.text.toString() != ""
                && lastname.text.toString() != "")
    }

    /**
     * Make the float button enabled
     */
    private fun enableFloatButton() {
        setButtonTint(mBtnEdit, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.secondaryColor)) )
    }

    /**
     * Make the float button disabled
     */
    private fun disableFloatButton() {
        setButtonTint(mBtnEdit, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.light_grey)) )
    }

    /**
    * This method edits a user entry in the Firebase database "users" collection
    */
    private fun editFirestoreUser(id: String){
        val userRef = mDb.collection("users").document(id)

        mUser.firstname = firstname.text.toString()
        mUser.lastname = lastname.text.toString()
        mUser.pseudo = pseudo.text.toString()

        mDb.runBatch { batch ->
            batch.set(userRef, mUser, SetOptions.merge())
        }.addOnCompleteListener {
            displayCustomToast(
                getString(R.string.saved),
                R.drawable.bubble_3
            )
            startLoanPagerActivity(getString(R.string.standard))
        }.addOnFailureListener { e ->
            Log.w(TAG, "Transaction failure.", e)
        }

    }

    private fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.backgroundTintList = tint
        } else {
            ViewCompat.setBackgroundTintList(button, tint)
        }
    }

    /**
     * This method displays a message in a nice way
     */
    fun displayCustomToast(message: String, bubble: Int) {
        val inflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custom_toast, custom_toast_container)
        val text: TextView = layout.findViewById(R.id.text)
        text.background = ContextCompat.getDrawable(this, bubble)
        text.text = message
        with (Toast(this)) {
            setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

}