package com.depuisletemps.beback.controller.activities

import android.os.Bundle
import com.depuisletemps.beback.R

class AboutActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        configureToolbar()
    }
}