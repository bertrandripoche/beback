package com.depuisletemps.beback.utils

import android.Manifest
import android.R
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.depuisletemps.beback.model.api.LoanHelper
import com.depuisletemps.beback.model.api.LoanerHelper
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions

class AutocompletionField(val context: Context) {

    /**
     * Gets phone contacts list plus recipient names list from Firebase
     */
    fun getAutocompletionListFromPhoneContactsAndFirebase(userId: String, list: ArrayList<String>, acTextView: AutoCompleteTextView, threshold: Int) {
        context.runWithPermissions(Manifest.permission.READ_CONTACTS) {
            val phones = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            while (phones!!.moveToNext()) {
                val name =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                if (!list.contains(name)) list.add(name)
            }

            getAutocompletionNameListFromFirebase(userId, list, acTextView, threshold)
        }
    }

    /**
     * Gets recipient names list from Firebase
     */
    fun getAutocompletionNameListFromFirebase(userId: String, list: ArrayList<String>, acTextView: AutoCompleteTextView, threshold: Int) {
        val loanerHelper = LoanerHelper()
        loanerHelper.getLoanersNames(userId) { result, names ->
            if (result) {
                if (names!!.isNotEmpty()) {
                    for (name in names)
                        if (!list.contains(name)) list.add(name)
                    val loanRecipientNamesListAdapter = ArrayAdapter<String>(
                        context,
                        R.layout.simple_dropdown_item_1line,
                        list
                    )
                    acTextView.setAdapter(loanRecipientNamesListAdapter)
                    acTextView.threshold = threshold
                }
            } else Log.d(Constant.AUTOCOMPLETIONCLASS, context.resources.getString(com.depuisletemps.beback.R.string.error_getting_docs), null)
        }
    }

    /**
     * Gets product names list from Firebase
     */
    fun getAutocompletionProductListFromFirebase(userId: String, list: ArrayList<String>, acTextView: AutoCompleteTextView, threshold: Int) {
        val loanHelper = LoanHelper()
        loanHelper.getLoanNames(userId) { result, names ->
            if (result) {
                if (names!!.isNotEmpty()) {
                    for (name in names)
                        if (!list.contains(name)) list.add(name)
                    val loanRecipientNamesListAdapter = ArrayAdapter<String>(
                        context,
                        R.layout.simple_dropdown_item_1line,
                        list
                    )
                    acTextView.setAdapter(loanRecipientNamesListAdapter)
                    acTextView.threshold = threshold
                }
            } else Log.d(Constant.AUTOCOMPLETIONCLASS, context.resources.getString(com.depuisletemps.beback.R.string.error_getting_docs), null)
        }
    }
}