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
import com.depuisletemps.beback.utils.Constant
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
        configureButtons()
    }

    fun configureButtons() {
        mBtnEdit.setOnClickListener{
            if (isFormValid())
                editFirestoreUser(mId)
            else {
                Toast.makeText(applicationContext, R.string.invalid_edit_profile_form, Toast.LENGTH_LONG)
                    .show()
            }
        }

    }

    fun getSavedInstanceData(savedInstanceState: Bundle?) {
        if (savedInstanceState != null){
            firstname.setText(savedInstanceState.getString(Constant.FIRST))
            lastname.setText(savedInstanceState.getString(Constant.LAST)!!)
            pseudo.setText(savedInstanceState.getString(Constant.PSEUDO)!!)
            mail.text = savedInstanceState.getString(Constant.MAIL)!!
            mFirst = savedInstanceState.getString(Constant.MFIRST)!!
            mLast = savedInstanceState.getString(Constant.MLAST)!!
            mPseudo = savedInstanceState.getString(Constant.MPSEUDO)!!
            mMail = savedInstanceState.getString(Constant.MAIL)!!
            mFirstTime = false
            setEditBtnState()
            setEditFieldsTextColor()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constant.MFIRST, mFirst)
        outState.putString(Constant.MLAST, mLast)
        outState.putString(Constant.MPSEUDO, mPseudo)
        outState.putString(Constant.FIRST, firstname.text.toString())
        outState.putString(Constant.LAST, lastname.text.toString())
        outState.putString(Constant.PSEUDO, pseudo.text.toString())
        outState.putString(Constant.MAIL, mMail)
    }

    /**
     * This method gets user
     */
    private fun getUserInfos() {
        mUserFb = getCurrentUser()
        val docRef = mDb.collection(Constant.USERS_COLLECTION).document(mUserFb!!.uid)

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
    private val textWatcher: TextWatcher = object : TextWatcher {
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
        val userRef = mDb.collection(Constant.USERS_COLLECTION).document(id)

        mUser.firstname = firstname.text.toString()
        mUser.lastname = lastname.text.toString()
        mUser.pseudo = pseudo.text.toString()

        mDb.runBatch { batch ->
            batch.set(userRef, mUser, SetOptions.merge())
        }.addOnCompleteListener {
            displayCustomToast(getString(R.string.saved), R.drawable.bubble_3,this)
            startLoanPagerActivity(Constant.STANDARD)
        }.addOnFailureListener { e ->
            Log.w(TAG, getString(R.string.transaction_failure), e)
        }

    }

    private fun setButtonTint(button: FloatingActionButton, tint: ColorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.backgroundTintList = tint
        } else {
            ViewCompat.setBackgroundTintList(button, tint)
        }
    }

}