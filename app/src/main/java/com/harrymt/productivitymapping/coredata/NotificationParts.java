package com.harrymt.productivitymapping.coredata;

import android.app.Notification;
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
    public String bigText;
    public String subText;
    public String packageName;

    /**
     * Constructor.
     *
     * @param notificationID Id of notification.
     * @param pack Package name of notification.
     */
    public NotificationParts(int notificationID, String pack) {
        this(pack);
        id = notificationID;
    }

    /**
     * Constructor.
     *
     * @param pack Package name of notification
     */
    public NotificationParts(String pack) {
        packageName = pack;
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
        this.bigText = "";
        this.subText = "";
        this.packageName = "";
        Bundle b = n.extras;
        CharSequence titleCS = b.getCharSequence(Notification.EXTRA_TITLE); // e.g. Name of sender
        CharSequence textCS = b.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence bigTextCS = b.getCharSequence(Notification.EXTRA_BIG_TEXT); // Content of email
        CharSequence subTextCS = b.getCharSequence(Notification.EXTRA_SUB_TEXT); // Email address

        if (titleCS != null) this.title = titleCS.toString();
        if (textCS != null) this.text = textCS.toString();
        if (bigTextCS != null) this.bigText = bigTextCS.toString();
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
                    this.bigText.contains(aKeywordsToBlock) ||
                    this.subText.contains(aKeywordsToBlock) ||
                    this.text.contains(aKeywordsToBlock)) {
                return true;
            }
        }
        return false;
    }
}