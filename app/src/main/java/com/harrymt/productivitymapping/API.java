package com.harrymt.productivitymapping;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.harrymt.productivitymapping.database.DatabaseAdapter;
import com.harrymt.productivitymapping.fragments.StatFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Handles the interfacing with the projects API.
 */
public class API {

    private static String TAG = "API-Class";

    public static JsonObjectRequest makeRequest(final Context c, final String endpoint) {
        String url = PROJECT_GLOBALS.base_url(c) + endpoint + PROJECT_GLOBALS.apiKey(c);

        return new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject response_value;
                try {
                    response_value = (JSONObject) response.get("response");

                    PROJECT_GLOBALS.TOP_APPS_BLOCKED = new ArrayList<>();
                    for (Iterator<String> iter = response_value.keys(); iter.hasNext(); ) {
                        // Get the name of the top apps blocked
                        PROJECT_GLOBALS.TOP_APPS_BLOCKED.add(iter.next());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "JSON exception with endpoint(" + endpoint + "): " + e.getMessage());
                }

            }
        }, getVolleyErrorListener(c, volley_error_type.apps));

    }

    public static JsonObjectRequest makeRequestZone(final Context c, final String endpoint, JSONObject payload, final int zoneID) {
        String url = PROJECT_GLOBALS.base_url(c) + endpoint + PROJECT_GLOBALS.apiKey(c);

        return new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String friendlyResponse = "";

                try {
                    friendlyResponse = response.get("response").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast.makeText(c, "Sync successful " + friendlyResponse, Toast.LENGTH_SHORT).show();

                // Mark that zone as sync'd
                DatabaseAdapter dbAdapter;
                dbAdapter = new DatabaseAdapter(c); // Open and prepare the database
                dbAdapter.setZoneAsSynced(zoneID);
                dbAdapter.close();
            }
        }, getVolleyErrorListener(c, volley_error_type.zone ));

    }

    enum volley_error_type {
        zone,
        apps, stat
    }

    private static Response.ErrorListener getVolleyErrorListener(final Context c, final volley_error_type type) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showVolleyError(c, type, error);
            }
        };
    }

    private static void showVolleyError(Context c, volley_error_type type, VolleyError e) {
        logError(e);
        String msg = "API error";

        switch(type) {
            case zone:
                Toast.makeText(c, "Cannot sync zones.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Cannot sync zones!");
                return;
            case stat:
                if(PROJECT_GLOBALS.IS_DEBUG) Toast.makeText(c, "Can't get stats.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Cannot get stats. Not connected to internet.");
                return;
            case apps:
                if(PROJECT_GLOBALS.IS_DEBUG) Toast.makeText(c, "Can't get popular apps.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Cannot get popular apps. Not connected to internet.");
                return;
        }


        if(e == null) {
            Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
        } else if(e.networkResponse == null) {
            Toast.makeText(c, msg + ": " + e.getMessage() , Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(c, msg + ": " + e.networkResponse.statusCode , Toast.LENGTH_SHORT).show();
        }
    }

    private static void logError(Exception e) {
        e.printStackTrace();
        Log.e(TAG, e.getMessage());
    }

    public static JsonObjectRequest makeRequestStat(final Context c, final String endpoint, final TextView tv) {
        String url = PROJECT_GLOBALS.base_url(c) + endpoint + PROJECT_GLOBALS.apiKey(c);

        return new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONObject response_value;
                try {
                    response_value = (JSONObject) response.get("response");

                    String statsString = "";
                    for (Iterator<String> iter = response_value.keys(); iter.hasNext(); ) {
                        String element = iter.next();
                        String value;
                        try {
                            value = response_value.get(element).toString();
                        } catch (JSONException e) {
                            logError(e);
                            tv.setText(R.string.stats_api_error);
                            statsString = "";
                            break; // exit for
                        }
                        statsString += element + ": " + value;

                        if(iter.hasNext()) {
                            statsString += "\n";
                        }
                    }

                    if(!statsString.equals("")) {
                        tv.setText(statsString);
                    }

                } catch (JSONException e) {
                    logError(e);
                    Log.e(TAG, "JSON exception with stats: " + e.getMessage());
                    tv.setText(R.string.stats_api_error);
                }
            }
        }, getVolleyErrorListener(c, volley_error_type.stat));


    }
}
