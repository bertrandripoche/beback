package com.depuisletemps.beback

import android.os.Bundle
import com.google.firebase.auth.FirebaseUser

class LoanActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        var user: FirebaseUser? = getCurrentUser()
        println(user)
    }
}