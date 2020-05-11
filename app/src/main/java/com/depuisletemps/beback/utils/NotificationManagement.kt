package com.depuisletemps.beback.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import org.joda.time.LocalDate
import java.util.*

object NotificationManagement {
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

    /**
     * This method start the notification via the alertReceiver class and alarmManager
     */
    private fun startAlarm(calendar: Calendar, loanId: String, loanProduct: String, loanType: String, loanRecipient: String, activity: Activity, context: Context) {
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        intent.putExtra(Constant.LOAN_ID, loanId)
        intent.putExtra(Constant.PRODUCT, loanProduct)
        intent.putExtra(Constant.TYPE, loanType)
        intent.putExtra(Constant.RECIPIENT_ID, loanRecipient)
        val pendingIntent = PendingIntent.getBroadcast(context, loanId.hashCode(), intent, 0)
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun createNotification(loanId: String, loanProduct: String, loanType: String, loanRecipient: String, dateNotif: LocalDate, activity: Activity, context: Context){
        if (Utils.getDifferenceDays(LocalDate.now(), dateNotif) >= 0) {
            val day: String = DateFormat.format("dd", dateNotif.toDate()).toString()
            val month: String = DateFormat.format("MM", dateNotif.toDate()).toString()
            val year: String = DateFormat.format("yyyy", dateNotif.toDate()).toString()
            val monthForCalendar = Integer.parseInt(month) - 1
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, Integer.parseInt(year))
            calendar.set(Calendar.MONTH, monthForCalendar)
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day))
            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.AM_PM, Calendar.AM)

            startAlarm(calendar, loanId, loanProduct, loanType, loanRecipient, activity, context)
        }
    }
}