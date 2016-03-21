package com.harrymt.productivitymapping.coredata;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.utility.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Describes a zone object.
 */
public class Zone implements Parcelable {

    // Zone properties
    public int zoneID;
    public int hasSynced; // if zone has been synced with server
    public double lat;
    public double lng;
    public float radiusInMeters;
    public String name;
    public int autoStartStop; // 0: false, 1: true
    public String[] blockingApps = new String[] {};
    public String[] keywords = new String[] {};

    /**
     * Constructor.
     *
     * @param latLng Center point of zone.
     */
    public Zone(LatLng latLng) {
        this(latLng.latitude, latLng.longitude);
    }

    /**
     * Constructor.
     *
     * @param lt Longitude of center.
     * @param lg Latitude of center.
     */
    public Zone(double lt, double lg) {
        this(lt, lg, 5.0f);
    }

    /**
     * Constructor.
     *
     * @param lt Longitude of center.
     * @param lg Latitude of center.
     * @param r Radius.
     */
    public Zone(double lt, double lg, float r) {
        this(-1, lt, lg, r, "default zone", 0, 0, new String[] {}, new String[] {});
    }

    /**
     * Constructor.
     *
     * @param id Zone id.
     * @param lt Longitude of center.
     * @param lg Latitude of center.
     * @param r Radius of zone.
     * @param nm Name of zone.
     * @param auto Auto start.
     * @param synced Has Synched.
     * @param appsToBlock List of apps to block.
     * @param words List of keywords to let through.
     */
    public Zone(int id, double lt, double lg, float r, String nm, int auto, int synced, String[] appsToBlock, String[] words) {
        zoneID = id;
        lat = lt;
        lng = lg;
        radiusInMeters = r;
        name = nm;
        autoStartStop = auto;
        hasSynced = synced;
        blockingApps = appsToBlock;
        keywords = words;
    }

    /**
     * Constructor.
     *
     * @param in Parcel.
     */
    public Zone(Parcel in) {
        zoneID = in.readInt();

        lat = in.readDouble();
        lng = in.readDouble();
        radiusInMeters = in.readFloat();

        name = in.readString();
        autoStartStop = in.readInt();
        hasSynced = in.readInt();

        blockingApps = in.createStringArray();
        keywords = in.createStringArray();
    }

    /**
     * Unused.
     * @return contents.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write zone data to parcel.
     *
     * @param out Parcel to write to.
     * @param flags Flags.
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(zoneID);
        out.writeDouble(lat);
        out.writeDouble(lng);
        out.writeFloat(radiusInMeters);
        out.writeString(name);
        out.writeInt(autoStartStop);
        out.writeInt(hasSynced);
        out.writeStringArray(blockingApps);
        out.writeStringArray(keywords);
    }

    /**
     * Convert keywords to string.
     *
     * @return String without delimiters.
     */
    public String keywordsAsStr() {
        return Util.arrayToString(keywords);
    }

    /**
     * Convert blocking apps to string.
     *
     * @return String without delimiters.
     */
    public String blockingAppsAsStr() {
        return Util.arrayToString(blockingApps);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods

    /**
     * Regenerate the zone parcel object.
     */
    public static final Parcelable.Creator<Zone> CREATOR = new Parcelable.Creator<Zone>() {

        /**
         * Make the zone from the parcel.
         *
         * @param in Parcel.
         * @return Zone object.
         */
        @Override
        public Zone createFromParcel(Parcel in) {
            return new Zone(in);
        }

        /**
         * Make a new zone array from a parcel.
         *
         * @param size Size of array.
         * @return Zone array of give size.
         */
        @Override
        public Zone[] newArray(int size) {
            return new Zone[size];
        }
    };

    /**
     * Convert the zone object to a JSON representation.
     *
     * @param c Context of zone.
     * @return Zone object in JSON format.
     * @throws JSONException If we cannot serialize the zone to JSON.
     */
    public JSONObject getJSONObject(Context c) throws JSONException {
        JSONObject zoneInJSON = new JSONObject();
        zoneInJSON.put("user_id", PROJECT_GLOBALS.getUniqueDeviceId(c));
        zoneInJSON.put("id", this.zoneID);
        zoneInJSON.put("name", this.name);
        zoneInJSON.put("lat", this.lat);
        zoneInJSON.put("lng", this.lng);
        zoneInJSON.put("radius", this.radiusInMeters);
        zoneInJSON.put("blockingApps", new JSONArray(new ArrayList<>(Arrays.asList(this.blockingApps))));
        zoneInJSON.put("keywords", new JSONArray(new ArrayList<>(Arrays.asList(this.keywords))));

        return zoneInJSON;
    }
}