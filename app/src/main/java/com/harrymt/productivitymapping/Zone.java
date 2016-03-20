package com.harrymt.productivitymapping;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by harrymt on 15/02/16
 */
public class Zone implements Parcelable {
    static String uniqueDelimiter = "_%@%_";
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
     * @param array
     * @return String delimited by unique delimiter.
     */
    public static String arrayToString(String[] array) {
        if (array == null || array.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        int i;

        for(i = 0; i < array.length - 1; i++) {
            sb.append(array[i]);
            sb.append(Zone.uniqueDelimiter);
        }
        sb.append(array[i]);
        return sb.toString();
    }

    public String keywordsAsStr() {
        return arrayToString(keywords);
    }

    public String blockingAppsAsStr() {
        return arrayToString(blockingApps);
    }

    public Zone(LatLng latLng) {
        this(latLng.latitude, latLng.longitude, 5.0f);
    }

    public Zone(double lt, double lg) {
        this(lt, lg, 5.0f);
    }

    public Zone(double lt, double lg, float r) {
        this(-1, lt, lg, r, "default zone", 0, 0, new String[] {}, new String[] {});
    }

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

    public Zone(Parcel in) {
        zoneID = in.readInt();

        lat = in.readDouble();
        lng = in.readDouble();
        radiusInMeters = in.readFloat();

        name = in.readString();
        autoStartStop = in.readInt();
        hasSynced= in.readInt();

        blockingApps = in.createStringArray();
        keywords = in.createStringArray();
    }

    /* everything below here is for implementing Parcelable */

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
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

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Zone> CREATOR = new Parcelable.Creator<Zone>() {
        public Zone createFromParcel(Parcel in) {
            return new Zone(in);
        }

        public Zone[] newArray(int size) {
            return new Zone[size];
        }
    };

    public JSONObject getJSONObject(Context c) throws JSONException {
        JSONObject p = new JSONObject();
        p.put("user_id", PROJECT_GLOBALS.getUniqueDeviceId(c));
        p.put("id", this.zoneID);
        p.put("name", this.name);
        p.put("lat", this.lat);
        p.put("lng", this.lng);
        p.put("radius", this.radiusInMeters);
        p.put("blockingApps", new JSONArray(new ArrayList<>(Arrays.asList(this.blockingApps))));
        p.put("keywords", new JSONArray(new ArrayList<>(Arrays.asList(this.keywords))));

        return p;
    }
}