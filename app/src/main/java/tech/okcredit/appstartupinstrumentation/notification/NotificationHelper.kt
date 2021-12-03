package tech.okcredit.appstartupinstrumentation.notification

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import tech.okcredit.appstartupinstrumentation.R

object NotificationHelper {

    const val channelIdAlert = "Slot-Alert"
    const val channelNameAlert = "SlotAlert"

    fun createNotification(
        context: Context,
        title: String,
        desc: String,
        openActivityClass: Class<*>?
    ) {
        val builder = NotificationCompat.Builder(context, channelIdAlert)
            .setContentTitle(title)
            .setContentText(desc)
            .setTicker("Covid Vaccine")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVibrate(longArrayOf(1000))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(notificationPendingIntent(openActivityClass, context))
            .setVisibility(VISIBILITY_PUBLIC)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                context,
                channelIdAlert,
                channelNameAlert,
                NotificationCompat.PRIORITY_MAX
            ).also {
                builder.setChannelId(it.id)
            }
        }
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, builder.build())
    }

    private fun notificationPendingIntent(
        activityClass: Class<*>?,
        context: Context
    ): PendingIntent {
        val intent = Intent(context, activityClass).apply {
            data = Uri.parse("https://staging.xyz.app/home")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, intent, 0)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        name: String,
        priority: Int
    ): NotificationChannel {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_UNKNOWN)
            .build()

        val mChannel = NotificationChannel(
            channelId,
            name,
            priority
        )

        mChannel.description = "Channel for important notifications"
        mChannel.enableLights(true)
        mChannel.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            audioAttributes
        )
        mChannel.lightColor = Color.RED
        mChannel.enableVibration(true)
        mChannel.setShowBadge(true)
        mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        return mChannel
    }
}
