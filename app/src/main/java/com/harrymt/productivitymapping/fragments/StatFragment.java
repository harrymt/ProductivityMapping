package com.harrymt.productivitymapping.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.harrymt.productivitymapping.API;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;
import com.harrymt.productivitymapping.coredata.BlockedApps;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.listviews.BlockedAppsArrayAdapter;
import com.harrymt.productivitymapping.utility.Util;

/**
 * Fragment that displays statistical information about the current user
 * and other users from the server.
 */
public class StatFragment extends Fragment {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "StatFragment";

    // List of most blocked apps
    ListView blockedAppsList;

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
        final TextView tvKeywordsStats = (TextView) v.findViewById(R.id.tvKeywordsStats);
        final TextView tvBlockedAppsStats = (TextView) v.findViewById(R.id.tvBlockedAppsStats);

        RequestQueue queue = Volley.newRequestQueue(getContext());
        blockedAppsList = (ListView) v.findViewById(R.id.lvPopularApps);
        queue.add(API.makeRequestStatTable(getContext(), "/apps/3", tvBlockedAppsStats, blockedAppsList));
        queue.add(API.makeRequestStatString(getContext(), "/keywords/3", tvKeywordsStats));
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
                popular_keywords_str = "'" + most_popular_keyword.word + "' is used "
                        + most_popular_keyword.occurrences + " times";
            }

            String popular_app_str = "";

            if(most_popular_blocked_app != null) {

                BlockedApps app = Util.getAppDetails(this.getContext(), most_popular_blocked_app.word);
                if(app != null) {
                    most_popular_blocked_app.word = app.name;
                }

                popular_app_str = "'" + most_popular_blocked_app.word + "' is your most blocked app, used "
                        + most_popular_blocked_app.occurrences + " times.";
            }

            if(!popular_app_str.equals("") && !popular_keywords_str.equals("")) {
                popular_app_str = ", and " + popular_app_str;
            }

            statsString = "Created " +
                    dbAdapter.getNumberOfZones() + " zones, " +
                    dbAdapter.getUniqueNumberOfKeywords() + " unique keywords and " +
                    dbAdapter.getUniqueNumberOfBlockingApps() + " apps. "
                    + popular_keywords_str + popular_app_str;

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

        fetchStatsFromServer(getView());
    }
}