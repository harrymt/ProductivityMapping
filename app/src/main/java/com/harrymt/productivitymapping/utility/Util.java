package com.harrymt.productivitymapping.utility;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.util.Log;

import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Various utility functions.
 */
public class Util {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "Util";

    /**
     * Check if the context (app) can listen to notifications.
     * @param c Context of app.
     * @return True if context can listen to it, false if not.
     */
    public static boolean weCanListenToNotifications(Context c) {
        String listOfEnabledNotificationListeners = Settings.Secure.getString(c.getContentResolver(), "enabled_notification_listeners");
        String ourNotificationListener = c.getApplicationContext().getPackageName();
        return listOfEnabledNotificationListeners != null &&
                listOfEnabledNotificationListeners.contains(ourNotificationListener);
    }

    /**
     * Gets a list of all the apps on a users phone.
     * @param c Context of app.
     * @return List of information about all the apps on a users phone.
     */
    public static List<ResolveInfo> getListOfAppsOnPhone(Context c) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return c.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    /**
     * Convert an epoch time to a friendly string.
     *
     * @param epochTime Time since epoch.
     * @return Time in a formatted string.
     */
    public static String convertTimeToFriendlyString(long epochTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.UK);
        return sdf.format(new Date(epochTime));
    }


    /**
     * Split a csv string to an array.
     *
     * @return String array.
     */
    public static String[] splitCSVStringToArray(String words) {
        if (words.length() == 0) return new String[] {};
        return words.split(",", -1);
    }

    /**
     * A wrapper for stats object.
     */
    public static class StatsTuple {
        public String word;
        public Integer occurrences;
    }

    /**
     * Gets an associative array from the ArrayList of string arrays that mark each unique
     * string and how many times it occurs.
     *
     * @param array Array list of string arrays
     * @return A Map with strings and occurrences.
     */
    public static Map<String, Integer> getOccurrencesFromListOfArrays(ArrayList<String[]> array) {
        Map<String, Integer> word_occurrences = new HashMap<>();

        for (String[] words : array) {
            for (String word : words) {
                if (word_occurrences.containsKey(word)) {
                    Integer number = word_occurrences.get(word);
                    number++;
                    word_occurrences.remove(word);
                    word_occurrences.put(word, number);
                } else {
                    word_occurrences.put(word, 1);
                }

            }
        }
        return word_occurrences;
    }

    /**
     * Get the most popular set from an array list of string arrays.
     *
     * @param array Array list of string arrays.
     * @return The highest word pair.
     */
    public static StatsTuple getMostPopularSetFromList(ArrayList<String[]> array) {
        Map<String, Integer> words = getOccurrencesFromListOfArrays(array);

        StatsTuple highest_word_pair = null;
        boolean start = true;
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            if(start) {
                highest_word_pair = new StatsTuple();
                highest_word_pair.word = entry.getKey();
                highest_word_pair.occurrences = entry.getValue();
                start = false;
            }
            if(highest_word_pair.occurrences < entry.getValue()) {
                highest_word_pair.word = entry.getKey();
                highest_word_pair.occurrences = entry.getValue();
            }
        }

        return highest_word_pair;
    }

    /**
     * Logs and prints the stack trace of an error.
     *
     * @param e Error to log.
     */
    public static void logError(Exception e) {
        e.printStackTrace();
        Log.e(TAG, e.getMessage());
    }

    // Array separator that the string arrays are separated by.
    // In a strange format so can easily be spotted if something goes wrong.
    static String uniqueDelimiter = "_%@%_";

    /**
     * Utility function to convert a String separated by the unqiue delimited back into a String.
     * @param str String
     * @return String[]
     */
    public static String[] stringToArray(String str)
    {
        if (str.length() == 0) return new String[] {};
        return str.split(uniqueDelimiter, -1);
    }

    /**
     * Utility function to convert a String array to a delimited separated string.
     * @param array Array to convert.
     * @return String delimited by unique delimiter.
     */
    public static String arrayToString(String[] array) {
        if (array == null || array.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        int i;

        for(i = 0; i < array.length - 1; i++) {
            sb.append(array[i]);
            sb.append(uniqueDelimiter);
        }
        sb.append(array[i]);
        return sb.toString();
    }

}