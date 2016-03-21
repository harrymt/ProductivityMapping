package com.harrymt.productivitymapping.coredata;

/**
 * Data object that describes a study session.
 */
public class Session {

    public int zoneId;
    public int startTime;
    public int stopTime;

    /**
     * Constructor.
     */
    public Session() { }

    /**
     * Constructor.
     *
     * @param zone_id ID of zone.
     * @param start_time Start time of session.
     * @param stop_time Stop time of session.
     */
    public Session(int zone_id, int start_time, int stop_time) {
        zoneId = zone_id;
        startTime = start_time;
        stopTime = stop_time;
    }
}