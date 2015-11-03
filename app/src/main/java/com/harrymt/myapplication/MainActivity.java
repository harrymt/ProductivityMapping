package com.harrymt.myapplication;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum StudyState
{
    STUDYING, NOT_STUDYING
};

public class MainActivity extends AppCompatActivity {

    private StudyState studyState = StudyState.NOT_STUDYING;
    public Calendar studyStartTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the permissions activity / ask the user for permissions
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    /**
     * When you start a study session.
     * @param v
     */
    public void onClickBtnStudy(View v)
    {
        Calendar btnClickTime = Calendar.getInstance();
        final TextView txtStudyTime = (TextView) findViewById(R.id.txtStudyTime);
        final Button btnStudy = (Button) findViewById(R.id.btnStudy);

        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(btnClickTime);

        switch (studyState)
        {
            case STUDYING:

                Log.d("g53ids", "Stop study session at " + btnClickTime.toString());

                long timeSpentInStudySession = btnClickTime.getTime().getTime() - studyStartTime.getTime().getTime();

                // Tell user what time they started the study session
                txtStudyTime.setText("Total study time: " + timeSpentInStudySession / 1000 + " seconds");

                Calendar beginCal = Calendar.getInstance();
                beginCal.set(Calendar.DATE, 1);
                beginCal.set(Calendar.MONTH, 0);
                beginCal.set(Calendar.YEAR, 2012);

                Calendar endCal = Calendar.getInstance();
                endCal.set(Calendar.DATE, 1);
                endCal.set(Calendar.MONTH, 0);
                endCal.set(Calendar.YEAR, 2016);


                List<AppStats> appUsage = getAppUsage(beginCal, endCal); // getAppUsage(studyStartTime, btnClickTime);

                if(appUsage != null) {
                    displayAppUsage(appUsage);
                } else {
                    // Display toast message to say enable permissions!

                    Context context = getApplicationContext();
                    CharSequence text = "Please enable permissions!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast.makeText(context, text, duration).show();
                }

                btnStudy.setText("Start study");

                studyState = StudyState.NOT_STUDYING;
                break;

            case NOT_STUDYING:
                Log.d("g53ids", "Start study session at " + btnClickTime.toString());

                studyStartTime = btnClickTime;

                // Tell user what time they started the study session
                txtStudyTime.setText("Started " + btnClickTime.getTime().toString());

                btnStudy.setText("End study");

                studyState = StudyState.STUDYING;
                break;
        default:
            break;
        }
    }

    public void displayAppUsage(List<AppStats> appUsage)
    {

        // http://androidexample.com/Create_A_Simple_Listview_-_Android_Example/index.php?view=article_discription&aid=65&aaid=90
        // Get ListView object from xml
        listAppUsageView = (ListView) findViewById(R.id.listAppUsage);

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        for (AppStats app: appUsage) {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("name", app.appName + " - " + app.packageName);
            datum.put("time", "" + app.usageInSeconds + " seconds");
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {"name", "time"},
                new int[] {android.R.id.text1, android.R.id.text2}
        );

        // Assign adapter to ListView
        listAppUsageView.setAdapter(adapter);
    }

    public List<AppStats> getAppUsage(Calendar beginTime, Calendar endTime)
    {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE); // Need context object // Context.USAGE_STATS_SERVICE

        final List<UsageStats> queryUsageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, beginTime.getTimeInMillis(), endTime.getTimeInMillis());

        boolean granted = queryUsageStats != null && queryUsageStats != Collections.EMPTY_LIST;

        Log.d("g53ids", "Permissions granted? " + String.valueOf(granted));

        List<AppStats> packageValues = new ArrayList<AppStats>();

        if (granted) {
            for (UsageStats stats : queryUsageStats) {
                AppStats app = new AppStats();
                app.packageName = stats.getPackageName();
                app.appName = getAppName(stats.getPackageName());
                app.usageInSeconds = stats.getTotalTimeInForeground() / 1000;

                packageValues.add(app);
            }
        } else {
            Log.d("g53ids", "Please enable permissions!");
            return null;
        }

        Collections.sort(packageValues, new AppStats.CompareUsage());

        return packageValues;
    }

    String getAppName(String packageName) {

        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }


    public static class AppStats {
        String appName;
        String packageName;
        long usageInSeconds;

        // Comparator
        public static class CompareUsage implements Comparator<AppStats> {
            @Override
            public int compare(AppStats arg0, AppStats arg1) {
                return (int) (arg1.usageInSeconds - arg0.usageInSeconds);
            }
        }
    }

    ListView listAppUsageView;
}
