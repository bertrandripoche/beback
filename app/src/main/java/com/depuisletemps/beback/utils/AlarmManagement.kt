package com.depuisletemps.beback.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object AlarmManagement {
    /**
     * This method stop the notification and clear the shared preferences
     */
    fun stopAlarm(loanId: String, loanProduct: String, loanType: String, loanRecipient: String, activity: Activity, context: Context) {
        val alarmManager = activity!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlertReceiver::class.java)
        intent.putExtra(Constant.LOAN_ID, loanId)
        intent.putExtra(Constant.PRODUCT, loanProduct)
        intent.putExtra(Constant.TYPE, loanType)
        intent.putExtra(Constant.RECIPIENT_ID, loanRecipient)
        val pendingIntent = PendingIntent.getBroadcast(context, loanId.hashCode(), intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}