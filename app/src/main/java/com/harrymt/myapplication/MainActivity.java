package com.harrymt.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

enum StudyState
{
    STUDYING, NOT_STUDYING
};

public class MainActivity extends AppCompatActivity {

    private StudyState studyState = StudyState.NOT_STUDYING;
    public Date studyStartTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * When you start a study session.
     * @param v
     */
    public void onClickBtnStudy(View v)
    {
        Date btnClickTime = Calendar.getInstance().getTime();
        final TextView txtStudyTime = (TextView) findViewById(R.id.txtStudyTime);
        final Button btnStudy = (Button) findViewById(R.id.btnStudy);

        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(btnClickTime);

        switch (studyState)
        {
            case STUDYING:

                Log.d("g53ids", "Stop study session at " + btnClickTime.toString());

                long timeSpentInStudySession = btnClickTime.getTime() - studyStartTime.getTime();

                // Tell user what time they started the study session
                txtStudyTime.setText("Total study time: " + timeSpentInStudySession / 1000 + " seconds");

                btnStudy.setText("Start study");

                studyState = StudyState.NOT_STUDYING;
                break;

            case NOT_STUDYING:
                Log.d("g53ids", "Start study session at " + btnClickTime.toString());

                studyStartTime = btnClickTime;

                // Tell user what time they started the study session
                txtStudyTime.setText("Started " + btnClickTime.toString());

                btnStudy.setText("End study");

                studyState = StudyState.STUDYING;
                break;
        default:
            break;
        }
    }
}
