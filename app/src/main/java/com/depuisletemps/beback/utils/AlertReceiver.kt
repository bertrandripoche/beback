package com.depuisletemps.beback.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.depuisletemps.beback.R
import com.depuisletemps.beback.ui.view.LoanDetailActivity


class AlertReceiver: BroadcastReceiver() {

    lateinit var mNotification: Notification

    override fun onReceive(context: Context, intent: Intent) {

        val loanId = intent.getStringExtra(Constant.LOAN_ID)
        val loanProduct = intent.getStringExtra(Constant.PRODUCT)

        val title = context.getString(R.string.app_name)
        val notif_message = context.getString(R.string.notif_message, loanProduct)

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
                .setStyle(Notification.BigTextStyle().bigText(notif_message))
                .setContentText(notif_message).build()
//             mNotification = NotificationCompat.Builder(context,Constant.CHANNEL_ID)
//                .setSmallIcon(R.drawable.icon)
//                .setContentTitle(title)
//                .setContentText(notif_message)
//                .setAutoCancel(true)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        } else {
            mNotification = Notification.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setStyle(Notification.BigTextStyle().bigText(notif_message))
                .setContentText(notif_message).build()
        }

        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManager.notify(loanId.hashCode(), mNotification)
    }
}