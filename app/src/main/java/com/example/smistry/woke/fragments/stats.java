package com.example.smistry.woke.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.smistry.woke.R;
import com.example.smistry.woke.bottomNav;
import com.example.smistry.woke.models.Day;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;


public class stats extends Fragment {

    HorizontalBarChart sleepChart;
    TextView tvStatsTitle;

    public stats() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sleepChart = (HorizontalBarChart) view.findViewById(R.id.sleepChart);
        tvStatsTitle = (TextView) view.findViewById(R.id.tvStatsTitle);

        //creating empty description
        Description desc= new Description();
        desc.setTextSize(600);
        desc.setText("");
        sleepChart.setDescription(desc);

        //getting user's name to set title
        String name= PreferenceManager.getDefaultSharedPreferences(getContext()).getString("name", "Your");
        if (!name.equals("Your")){
            name+="'s";
        }
        setData(7, 12);
        tvStatsTitle.setText(name + " Sleep Progress");

        // Hide grid lines
        sleepChart.getAxisLeft().setEnabled(false);
        sleepChart.getAxisRight().setEnabled(true);

        // Hide graph description
        sleepChart.getXAxis().setDrawLabels(true);
        sleepChart.getDescription().setEnabled(true);

        // Hide graph legend
        sleepChart.getLegend().setEnabled(true);
        sleepChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);

        //sleep bar graph animation
        sleepChart.getAxisLeft().setAxisMinimum(0);
        sleepChart.animateXY(1000, 1000);
    }

    private void setData(int count, int range) {
        ArrayList<BarEntry> sleepVals = new ArrayList<>();
        float barWidth = 15f;
        float spaceForBar = 20f;
        ArrayList<Day> days = ((bottomNav) getContext()).getDays();

        //calculating sleep time for each day by using sleep time of night before and wake up time of current day
        for (int i = days.size() - 1; i > 0; i--) {
            Day day = days.get(i - 1);
            Day next = days.get(i);
            int nextHours = next.getWakeUp().getHours();
            int nextMin = next.getWakeUp().getMinutes();
            int dayHours = day.getSleep().getHours();
            int dayMins = day.getSleep().getMinutes();
            float sleeptime = 1440 - Math.abs((nextHours - dayHours) * 60 + (nextMin - dayMins));
            sleepVals.add(new BarEntry(i * spaceForBar, new float [] {sleeptime, 480-sleeptime}));
        }

        //special case for calculating last day in array
        Day day = days.get(days.size() - 1);
        Day next = days.get(0);
        int nextHours = next.getWakeUp().getHours();
        int nextMin = next.getWakeUp().getMinutes();
        int dayHours = day.getSleep().getHours();
        int dayMins = day.getSleep().getMinutes();
        float sleeptime = 1440 - Math.abs((nextHours - dayHours) * 60 + (nextMin - dayMins));
        sleepVals.add(new BarEntry((0) * spaceForBar, new float [] {sleeptime, 480-sleeptime} ));

        String labelLong="Actual,Expected";
        String[] labels= labelLong.split(",");

        //setting data for Bar Graph
        BarDataSet dataset = new BarDataSet(sleepVals, "Sleep Progress");
        dataset.setColors(new int[] {Color.rgb(247,187,93), Color.rgb(195,89,28)});
        dataset.setStackLabels(new String[]{
                "Time Sleeping", "Recommended Sleep"
        });
        BarData data = new BarData(dataset);
        dataset.setDrawValues(true);
        dataset.setStackLabels(labels);

        data.setBarWidth(barWidth);
        sleepChart.setFitBars(true);
        sleepChart.setData(data);
        data.setValueTextSize(13f);
        sleepChart.invalidate();

    }
}
