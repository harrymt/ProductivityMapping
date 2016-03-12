package com.harrymt.productivitymapping;

/**
 * Study session
 */
public class Session {

    public int zoneId;
    public int startTime;
    public int stopTime;
    public int productivityPercentage;

    public Session() { }

    public Session(int zone_id, int start_time, int stop_time, int productivity_percentage) {
        zoneId = zone_id;
        startTime = start_time;
        stopTime = stop_time;
        productivityPercentage = productivity_percentage;
    }
}
