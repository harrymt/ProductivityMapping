package com.harrymt.productivitymapping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by harrymt on 06/02/16.
 */
public class StatFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_stat, container, false);

        TextView tvStats = (TextView) v.findViewById(R.id.tvStats);
        TextView tvEveryonesStats = (TextView) v.findViewById(R.id.tvEveryonesStats);
        tvStats.setText(getYourStatsString());
        tvEveryonesStats.setText(getEveryonesStatsString());

        return v;
    }

    private String getEveryonesStatsString() {
        return "The most popular apps blocked are " + getDataMostPopularAppBlocked() + " and " + getDataSecondMostPopularAppBlocked() + ".\n" +
                "'Family' is the most used keyword, used by 107 people, across 10 zones";
    }

    private String getYourStatsString() {
        return "You have created " +
                getNumberOfZones() + " zones, with " +
                getUniqueNumberOfKeywords() + " different keywords and " +
                getUniqueNumberOfBlockingApps() + " unqiue apps."
                + "\n'Harry' is your most popular keyword and Facebook is your most blocked app";
    }


    private String getDataMostPopularAppBlocked() {
        return "Facebook";
    }
    private String getDataSecondMostPopularAppBlocked() {
        return "WhatsApp";
    }
    private String getDataMostPopularKeyword() {
        return "help";
    }

    private String getUniqueNumberOfKeywords() {
        return "12";
    }
    private String getUniqueNumberOfBlockingApps() {
        return "35";
    }
    private String getNumberOfZones() {
        return "5";
    }
}
