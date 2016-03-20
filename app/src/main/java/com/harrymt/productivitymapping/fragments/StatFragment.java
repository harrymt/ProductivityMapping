package com.harrymt.productivitymapping.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.harrymt.productivitymapping.API;
import com.harrymt.productivitymapping.R;

public class StatFragment extends Fragment {

    public static String TAG = "StatFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_stat, container, false);

        TextView tvStats = (TextView) v.findViewById(R.id.tvStats);
        tvStats.setText(getYourStatsString());

        // TODO move this to a poller that does it every 5 seconds? until it gets the stats
        // cuz if wifi isnt on, on app load. There wont be any stats to show there
        fetchStatsFromServer(v);

        return v;
    }

    private void fetchStatsFromServer(View v) {
        final TextView tvBlockedAppsStats = (TextView) v.findViewById(R.id.tvBlockedAppsStats);
        final TextView tvKeywordsStats = (TextView) v.findViewById(R.id.tvKeywordsStats);

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(API.makeRequestStat(getContext(), "/apps/3", tvBlockedAppsStats));
        queue.add(API.makeRequestStat(getContext(), "/keywords/3", tvKeywordsStats));
    }

    private String getYourStatsString() {
        return "You have created " +
                getNumberOfZones() + " zones, with " +
                getUniqueNumberOfKeywords() + " different keywords and " +
                getUniqueNumberOfBlockingApps() + " unqiue apps."
                + "\n'Harry' is your most popular keyword and Facebook is your most blocked app";
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