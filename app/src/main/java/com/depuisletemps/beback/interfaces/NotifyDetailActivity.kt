package com.depuisletemps.beback.interfaces

import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanAction

interface NotifyDetailActivity {
    fun displaySnackbar(message: String, loan: Loan, bubble: Int, points: Long, action: LoanAction)
    fun displayToast(message: String, bubble: Int)
}