package com.depuisletemps.beback.ui.view

import android.os.Bundle
import com.depuisletemps.beback.R
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class AboutActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        configureToolbar()
    }

    /**
     * This method configures the toolbar
     */
    private fun configureToolbar() {
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        Objects.requireNonNull(ab)!!.setDisplayHomeAsUpEnabled(true)
    }
}