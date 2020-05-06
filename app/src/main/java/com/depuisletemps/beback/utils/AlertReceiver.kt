package com.depuisletemps.beback.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.controller.activities.LoanDetailActivity


class AlertReceiver: BroadcastReceiver() {

    lateinit var mNotification: Notification

    override fun onReceive(context: Context, intent: Intent) {
        //On alarm reception, trigger the notification on user's phone
        val loanId = intent.getStringExtra(Constant.LOAN_ID)
        val loanProduct = intent.getStringExtra(Constant.PRODUCT)
        val loanType = intent.getStringExtra(Constant.TYPE)
        val loanRecipient = intent.getStringExtra(Constant.RECIPIENT_ID)

        val title = when (loanType) {
            Constant.LENDING -> context.resources.getString(R.string.notif_lending)
            Constant.BORROWING -> context.resources.getString(R.string.notif_borrowing)
            else -> context.resources.getString(R.string.notif_delivery)
        }
        val notifMessage = when (loanType) {
            Constant.LENDING -> context.resources.getString(R.string.notif_message_lending, loanProduct, loanRecipient)
            Constant.BORROWING -> context.resources.getString(R.string.notif_message_borrowing, loanProduct, loanRecipient)
            else -> context.resources.getString(R.string.notif_message_delivery, loanProduct, loanRecipient)
        }

        val intent = Intent(context, LoanDetailActivity::class.java)
        intent.putExtra(Constant.LOAN_ID, loanId)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotification = Notification.Builder(context, Constant.CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setStyle(Notification.BigTextStyle().bigText(notifMessage))
                .setContentText(notifMessage).build()
        } else {
            mNotification = Notification.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setStyle(Notification.BigTextStyle().bigText(notifMessage))
                .setContentText(notifMessage).build()
        }

        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManager.notify(loanId.hashCode(), mNotification)
    }
}