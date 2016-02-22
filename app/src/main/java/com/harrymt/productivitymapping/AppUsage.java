package com.harrymt.productivitymapping;

/**
 * Created by harrymt on 25/11/15
 */
public class AppUsage
{
    String packageName; long timeSpentInSeconds;

    public AppUsage(String p, long t) {
        this.packageName = p; this.timeSpentInSeconds = t;
    }
}