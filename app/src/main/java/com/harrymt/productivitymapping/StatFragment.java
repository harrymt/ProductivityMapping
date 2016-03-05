package com.harrymt.productivitymapping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by harrymt on 06/02/16.
 */
public class StatFragment extends Fragment {

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

        String base_url = "http://horizab1.miniserver.com/~harry/server/ProductivityMapping-Server/api/v1";
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(makeJSONrequest(base_url + "/apps/3", tvBlockedAppsStats));
        queue.add(makeJSONrequest(base_url + "/keywords/3", tvKeywordsStats));
    }

    private JsonObjectRequest makeJSONrequest(String url, final TextView tv) {

        return new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String statsString = "";
                for (Iterator<String> iter = response.keys(); iter.hasNext(); ) {
                    String element = iter.next();
                    String value = "";
                    try {
                        value = response.get(element).toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        value = "error";
                    }
                    statsString += element + ": " + value + "\n";
                }
                tv.setText(statsString);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                tv.setText("Can't get stats, please connect to the internet and try again later.");
            }
        });
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