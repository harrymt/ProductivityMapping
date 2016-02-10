package com.harrymt.productivitymapping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by harrymt on 06/02/16.
 */
public class StatFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_stat, container, false);

        ArrayList<String> stats = getStats();
        String fullStatsText = "Stats\n\n";
        for (String stat : stats) {
            fullStatsText += stat + "\n";
        }
        TextView tvStats = (TextView) v.findViewById(R.id.tvStats);
        tvStats.setText(fullStatsText);

        return v; // inflater.inflate(R.layout.fragment_stat, container, false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

    }

    private ArrayList<String> getStats() {
        ArrayList<String> stats = new ArrayList<>();

        String numberOfZones = "Number of Zones: " + getNumberOfZones();
        stats.add(numberOfZones);

        String numberOfDifferentAppsTracked = "Number of unique apps tracked: " + getNumberOfDifferentAppsTracked();
        stats.add(numberOfDifferentAppsTracked);

        return stats;
    }


    private String getNumberOfDifferentAppsTracked() {
        return "99";
    }

    private String getNumberOfZones() {
        return "21";
    }
}
