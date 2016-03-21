package com.harrymt.productivitymapping.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import com.harrymt.productivitymapping.coredata.NotificationParts;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;

/**
 * Utility class for manipulating notification.
 */
public class NotificationUtil {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "NotifiBUtil";

    // Context of app.
    Context context;

    /**
     * Constructor.
     *
     * @param c Context of app.
     */
    public NotificationUtil(Context c) {
        context = c;
    }

    /**
     * Posts the given notification.
     *
     * @param parts Notification to build.
     */
    public void postNotification(NotificationParts parts) {
        Notification notification = buildNotification(parts);
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify((int) System.currentTimeMillis(), notification);
    }

    /**
     * Builds a notification from the notification parts object.
     *
     * TODO add more parts.
     *
     * @param part NotificationPart object
     * @return The built notification
     */
    public Notification buildNotification(NotificationParts part) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(part.title);
        notificationBuilder.setContentText(part.text);
        notificationBuilder.setSubText(part.subText);
        notificationBuilder.setSmallIcon(R.drawable.ic_standard_notification);
        notificationBuilder.setAutoCancel(true);

//
//        Notification n = new Notification.Builder(context)
//                .setWhen(sbnNotification.when)
//                .setContentIntent(sbnNotification.contentIntent)
//                .setSubText(sbnNotification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
//                .setContent(sbnNotification.contentView)
//                .setContentInfo(sbnNotification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT))
//                .setContentTitle(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE))
//                .setContentText(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT))
//                .setSmallIcon(R.drawable.ic_standard_notification).build();
//

        return notificationBuilder.build();
    }
}