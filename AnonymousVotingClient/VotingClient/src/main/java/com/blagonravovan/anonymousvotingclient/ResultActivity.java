package com.blagonravovan.anonymousvotingclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    private static final String ARG_RESULTS = "arg_results";

    private List<String> mResults;

    public static Intent newIntent(Context context, ArrayList<String> results){
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putStringArrayListExtra(ARG_RESULTS, results);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);

        mResults = getIntent().getStringArrayListExtra(ARG_RESULTS);
        setUI();
    }

    private void setUI(){
        TextView[] ratingTextViews = new TextView[]{
                (TextView) findViewById(R.id.rating1),
                (TextView) findViewById(R.id.rating2),
                (TextView) findViewById(R.id.rating3),
                (TextView) findViewById(R.id.rating4),
                (TextView) findViewById(R.id.rating5)
        };

        for (int i = 0; i < ratingTextViews.length; ++i) {
            ratingTextViews[i].setText(mResults.get(i));
        }
    }
}
