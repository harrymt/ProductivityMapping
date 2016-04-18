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
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.utility.Util;

/**
 * Fragment that displays statistical information about the current user
 * and other users from the server.
 */
public class StatFragment extends Fragment {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "StatFragment";

    // Text view to display all the stat information.
    TextView tvStats;

    /**
     * When fragment is created, setups the stat information and gets the stats
     * from the server.
     *
     * @param inflater used to inflate the fragment to a layout.
     * @param container parent container.
     * @param savedInstanceState saved state.
     * @return The view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_stat, container, false);

        tvStats = (TextView) v.findViewById(R.id.tvStats);
        tvStats.setText(getYourStatsString());

        fetchStatsFromServer(v);

        return v;
    }

    /**
     * Get the stats from the server, so we can display them to the user.
     *
     * @param v view of fragment that we can display the stats in.
     */
    private void fetchStatsFromServer(View v) {
        final TextView tvBlockedAppsStats = (TextView) v.findViewById(R.id.tvBlockedAppsStats);
        final TextView tvKeywordsStats = (TextView) v.findViewById(R.id.tvKeywordsStats);

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(API.makeRequestStat(getContext(), "/apps/3", tvBlockedAppsStats));
        queue.add(API.makeRequestStat(getContext(), "/keywords/3", tvKeywordsStats));
    }

    /**
     * Create and build the whole personal stats string, from database
     * info.
     * @return the stats string.
     */
    private String getYourStatsString() {
        String statsString;

        DatabaseAdapter dbAdapter = new DatabaseAdapter(getContext()); // Prepare the database
        if(dbAdapter.hasASessionEverStartedYet()) {

            Util.StatsTuple most_popular_keyword = Util.getMostPopularSetFromList(dbAdapter.getAllKeywords());
            Util.StatsTuple most_popular_blocked_app = Util.getMostPopularSetFromList(dbAdapter.getAllBlockingApps());

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

    /**
     * Refresh the data source in this fragment.
     */
    public void refresh() {
        tvStats.setText(getYourStatsString());
    }
}