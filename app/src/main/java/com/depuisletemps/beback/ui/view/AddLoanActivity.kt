package com.depuisletemps.beback.ui.view

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.depuisletemps.beback.R
import kotlinx.android.synthetic.main.activity_add_loan.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class AddLoanActivity: BaseActivity() {
    lateinit var type:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_loan)

        configureToolbar()
        type = getLoanType()
        setBackgroundFromType()
    }

    /**
     * This method sets the background color depending on the type of loan we do (lending, borrowing)
     */
    private fun setBackgroundFromType() {
        val greenColor = ContextCompat.getColor(this, R.color.green)
        val redColor = ContextCompat.getColor(this, R.color.red)
        if (type.equals("lend")) {
            loan_type.setBackgroundColor(greenColor)
            loan_type.text = getString(R.string.i_lend)
        } else if (type.equals("borrow")) {
            loan_type.setBackgroundColor(redColor)
            loan_type.text = getString(R.string.i_borrow)
        }
    }

    /**
     * This method gets loan type from
     */
    private fun getLoanType(): String {
        val i = intent
        return i.extras.getString("type") ?: ""
    }

    /**
     * This method configures the toolbar
     */
    private fun configureToolbar() {
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        Objects.requireNonNull(ab)!!.setDisplayHomeAsUpEnabled(true)
    }
}