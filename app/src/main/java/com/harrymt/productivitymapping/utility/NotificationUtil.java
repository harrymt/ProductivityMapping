package com.harrymt.productivitymapping.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;

import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.coredata.NotificationParts;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;

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
     * @param part NotificationPart object
     * @return The built notification
     */
    public Notification buildNotification(NotificationParts part) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(part.title);
        notificationBuilder.setContentText(part.text);
        notificationBuilder.setSubText(part.subText);
        notificationBuilder.setContentInfo(part.contentInfo);
        notificationBuilder.setLargeIcon(part.largeIcon);

        // TODO set the original notification icon
        // It is not possible to change this to the resource icon of the app,
        // but it is possible to look up the app using the package name and get the icon.
        notificationBuilder.setSmallIcon(R.drawable.ic_standard_notification);
        notificationBuilder.setAutoCancel(true);

//        Notification n = new Notification.Builder(context)
//                .setWhen(sbnNotification.when)
//                .setContentIntent(sbnNotification.contentIntent)
//                .setSubText(sbnNotification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
//                .setContent(sbnNotification.contentView)
//                .setContentInfo(sbnNotification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT))
//                .setContentTitle(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE))
//                .setContentText(sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT))
//                .setSmallIcon(R.drawable.ic_standard_notification)
//                .build();

        return notificationBuilder.build();
    }


    /**
     * Get a bitmap from another package.
     *
     * @param c Context of this app.
     * @param resourceID Resource of file.
     * @param packageName Package.
     * @return Bitmap from other package, or null if not found.
     */
    public static Bitmap getBitmapFromAnotherPackage(Context c, int resourceID, String packageName) {
        try {
            Drawable myDrawable = c.getPackageManager().getResourcesForApplication(packageName)
                    .getDrawable(resourceID);

            if(myDrawable != null) {
                // Convert icon to db friendly string.
                BitmapDrawable s = (BitmapDrawable) myDrawable;
                return s.getBitmap();
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}