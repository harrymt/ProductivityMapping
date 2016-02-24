package com.harrymt.productivitymapping;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by harrymt on 15/02/16
 */
public class Zone implements Parcelable {
    double lat;
    double lng;
    double radiusInMeters;

    String name;
    int autoStartStop; // 0: false, 1: true
    String[] blockingApps = new String[] {};
    String[] keywords = new String[] {};

    public Zone(double lt, double lg, double r) {
        lat = lt;
        lng = lg;
        radiusInMeters = r;
        name = "default zone";
        autoStartStop = 0;
        blockingApps = new String[] {};
        keywords = new String[] {};
    }

    public Zone(double lt, double lg, double r, String nm, int auto, String[] appsToBlock, String[] words) {
        lat = lt;
        lng = lg;
        radiusInMeters = r;
        name = nm;
        autoStartStop = auto;
        blockingApps = appsToBlock;
        keywords = words;
    }

    public Zone(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
        radiusInMeters = in.readDouble();

        name = in.readString();
        autoStartStop = in.readInt();
        in.readStringArray(blockingApps);
        in.readStringArray(keywords);
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
        out.writeDouble(lat);
        out.writeDouble(lng);
        out.writeDouble(radiusInMeters);
        out.writeString(name);
        out.writeInt(autoStartStop);
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
}