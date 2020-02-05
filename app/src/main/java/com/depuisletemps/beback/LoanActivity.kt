package com.depuisletemps.beback

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseUser

class LoanActivity: BaseActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var mArchiveButton: MenuItem
    lateinit var mProfileButton: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        var user: FirebaseUser? = getCurrentUser()
        println(user)

        configureToolbar()
    }
    /**
     * This method overrides the onBackPressed method to change the behavior of the Back button
     */
    override fun onBackPressed() {

    }

    /**
     * This method creates the icons menu on the toolbar
     * @param menu is the menu to create
     * @return true
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        mArchiveButton = menu.getItem(0)
        mProfileButton = menu.getItem(1)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * This method configures the toolbar
     */
    private fun configureToolbar() {
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
    }

    /**
     * This method manages the clicks on the toolbar icons
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_archive -> {
            Toast.makeText(this,"Archive action",Toast.LENGTH_LONG).show()
            true
        }
        R.id.menu_profile ->{
            Toast.makeText(this,"Profile action",Toast.LENGTH_LONG).show()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}