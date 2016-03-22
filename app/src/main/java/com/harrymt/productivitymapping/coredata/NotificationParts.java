package com.harrymt.productivitymapping.coredata;

import android.app.Notification;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.harrymt.productivitymapping.PROJECT_GLOBALS;

/**
 * Data object that describes all the parts of a notification we want to store.
 */
public class NotificationParts {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "NotificationParts";

    // Notification parts to store.
    public int id;
    public String title;
    public String text;
    public String subText;
    public String packageName;
    public String contentInfo;

    public int icon;
    public Bitmap largeIcon;

    /**
     * Constructor.
     *
     * @param notificationID Id of notification.
     * @param pack Package name of notification.
     * @param title Title of notification.
     * @param text Text of notification.
     * @param subText Any subtext of notification.
     * @param contentInfo Any content info about the notification.
     * @param lIcon Bitmap image of the icon
     * @param icon Smaller icon
     */
    public NotificationParts(int notificationID, String pack, String title, String text, String subText, String contentInfo, Bitmap lIcon, int icon) {
        this.packageName = pack;
        this.title = title;
        this.text = text;
        this.subText = subText;
        this.contentInfo = contentInfo;
        this.largeIcon = lIcon;
        this.icon = icon;
        this.id = notificationID;
    }

    /**
     * Constructor.
     *
     * @param n Notification.
     * @param pack Package name of notification.
     */
    public NotificationParts(Notification n, String pack) {
        this.title = "";
        this.text = "";
        this.subText = "";
        this.packageName = "";
        this.largeIcon = null;

        Bundle b = n.extras;
        CharSequence titleCS = b.getCharSequence(Notification.EXTRA_TITLE); // e.g. Name of sender
        CharSequence textCS = b.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subTextCS = b.getCharSequence(Notification.EXTRA_SUB_TEXT); // Email address

        if (titleCS != null) this.title = titleCS.toString();
        if (textCS != null) this.text = textCS.toString();
        if (subTextCS != null) this.subText = subTextCS.toString();

        this.packageName = pack;
    }

    /**
     * See if this notification contains any of the packages to block.
     *
     * @param packagesToBlock String array of packages.
     * @return True if packageName exists in packagesToBlock array.
     */
    public boolean containsPackage(String[] packagesToBlock) {
        for (String aPackagesToBlock : packagesToBlock) {
            if (aPackagesToBlock.equals(this.packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks through the title, big, sub and normal text to see
     * if it contains any of the keywords to block.
     *
     * @param keywordsToBlock String array of keywords to block.
     * @return True if it does contain a keyword.
     */
    public boolean containsKeywords(String[] keywordsToBlock) {
        for (String aKeywordsToBlock : keywordsToBlock) {
            if (this.title.contains(aKeywordsToBlock) ||
                    this.subText.contains(aKeywordsToBlock) ||
                    this.text.contains(aKeywordsToBlock)) {
                return true;
            }
        }
        return false;
    }
}