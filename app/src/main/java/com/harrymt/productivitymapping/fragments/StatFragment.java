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
import com.harrymt.productivitymapping.database.DatabaseAdapter;

public class StatFragment extends Fragment {

    public static String TAG = "StatFragment";

    TextView tvStats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_stat, container, false);

        tvStats = (TextView) v.findViewById(R.id.tvStats);
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
        String statsString;

        DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext()); // Prepare the database
        if(dbAdapter.hasASessionEverStartedYet()) {

            DatabaseAdapter.StatsTuple most_popular_keyword = dbAdapter.getMostPopularSetFromMap(dbAdapter.getAllKeywords());
            DatabaseAdapter.StatsTuple most_popular_blocked_app = dbAdapter.getMostPopularSetFromMap(dbAdapter.getAllBlockingApps());

            String popular_keywords_str = "";
            if(most_popular_keyword != null) {
                popular_keywords_str = "'" + most_popular_keyword.word + "' is your most popular keyword with "
                        + most_popular_keyword.occurrences + " occurrence(s)";
            }

            String popular_app_str = "";
            if(most_popular_blocked_app != null) {
                popular_app_str = "'" + most_popular_blocked_app.word + "' is your most blocked app with "
                        + most_popular_blocked_app.occurrences + " occurrence(s).";
            }

            if(!popular_app_str.equals("") && !popular_keywords_str.equals("")) {
                popular_app_str = ", and " + popular_app_str;
            }

            statsString = "You have created " +
                    dbAdapter.getNumberOfZones() + " zones with " +
                    dbAdapter.getUniqueNumberOfKeywords() + " different keywords and " +
                    dbAdapter.getUniqueNumberOfBlockingApps() + " unique apps."
                    + "\n" + popular_keywords_str + popular_app_str;

        } else {
            statsString = "Start a study session to see personal stats.";
        }
        dbAdapter.close();

        return statsString;
    }

    public void refresh() {
        tvStats.setText(getYourStatsString());
    }
}