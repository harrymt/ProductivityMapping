package com.harrymt.productivitymapping.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import com.harrymt.productivitymapping.NotificationParts;
import com.harrymt.productivitymapping.R;

/**
 * Created by harrymt on 10/03/16
 */
public class NotificationBuilderUtil {

    Context context;

    public NotificationBuilderUtil(Context c) {
        context = c;
    }

    // What we need todo, is copy over as much of the notification as we can, atm, we are only copying over title and content
    // Cant just use notification e.g. postNewNotifiation because we get Bad notification poster .. couldnt create icon
    private void postStatusBarNotification(StatusBarNotification sbn) {
        Notification sbnNotification = sbn.getNotification();

        Notification n = new Notification.Builder(context)
                .setWhen(sbnNotification.when)
                .setContentIntent(sbnNotification.contentIntent)
                .setSubText(sbnNotification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
                .setContent(sbnNotification.contentView)
                .setContentInfo(sbnNotification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT))
                .setContentTitle(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE))
                .setContentText(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT))
                .setSmallIcon(R.drawable.ic_standard_notification).build();

        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify((int) System.nanoTime(), n);
    }

    public void postNotification(NotificationParts parts) {
        postNewNotification(buildNotification(parts.title, parts.text, parts.subText));
    }

    public Notification buildNotification(String title, String text, String subText) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        notificationBuilder.setSubText(subText);
        notificationBuilder.setSmallIcon(R.drawable.ic_standard_notification);
        notificationBuilder.setAutoCancel(true);
        return notificationBuilder.build();
    }

    public void postNewNotification(Notification notification) {
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify((int) System.currentTimeMillis(), notification);
    }
}