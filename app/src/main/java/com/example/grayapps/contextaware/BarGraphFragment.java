package com.example.grayapps.contextaware;

import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.BarSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.db.chart.view.ChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;

import java.util.ArrayList;

/*
 *  Copyright 2015 Diogo Bernardino
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

public class BarGraphFragment extends Fragment {


    /** First chart */
    private static BarChartView mChartOne;
    private ImageButton mPlayOne;
    private boolean mUpdateOne;
    private final String[] mLabelsOne= {"sMetric", "Stress", "Noise", "Move"};
    private float [][] mValuesOne = {{91.0f, 50.0f, 75.0f, 55.0f}};
    private static float[] mNewValues = new float[4];
    private static float mMax;
    private int mPos;
    private String mStressColor = "#5e4072";

    public BarGraphFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity().getIntent().hasExtra("position"))
        {
       /* mPos = getActivity().getIntent().getExtras().getInt("position",-1);
        if(mPos % 4 == 0)//stressed
        {
            mValuesOne[0][0] = 8.0f;
            mStressColor = "#db0c42";
        }
        else if(mPos % 3 == 0)//relaxed
        {
            mValuesOne[0][0] = 2.0f;
            mStressColor = "#21BEDB";
        }
        else if(mPos % 7 == 0)//relaxed
        {
            mValuesOne[0][0] = 6.5f;
            mStressColor = "#3ac298";
        }*/
        this.setHasOptionsMenu(true);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.bargraph, container, false);

        // Init first chart
        mUpdateOne = true;
        mChartOne = (BarChartView) layout.findViewById(R.id.barchart1);

        CardView barCard = (CardView) layout.findViewById(R.id.cardContent);
        barCard.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.colorPrimaryDark)));
        showChart(0, mChartOne);
        //showChart(1, mChartTwo, mPlayTwo);
        //showChart(2, mChartThree, mPlayThree);
        return layout;
    }


    /**
     * Show a CardView chart
     * @param tag   Tag specifying which chart should be dismissed
     * @param chart   Chart view
     * @param btn    Play button
     */
    private void showChart(final int tag, final ChartView chart){
       // dismissPlay(btn);
        Runnable action =  new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //showPlay(btn);
                    }
                }, 500);
            }
        };

        switch(tag) {
            case 0:
                produceOne(chart, action); break;
            default:
        }
    }


    /**
     * Update the values of a CardView chart
     * @param tag   Tag specifying which chart should be dismissed
     * @param chart   Chart view
     * @param btn    Play button
     */
    private static void updateChart(final int tag, final ChartView chart, ImageButton btn){

       // dismissPlay(btn);

        switch(tag){
            case 0:
                updateOne(chart); break;
            default:
        }
    }



    /**
     *
     * Chart 1
     *
     */

    public void produceOne(ChartView chart, Runnable action){
        BarChartView barChart = (BarChartView) chart;

        barChart.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect rect) {
                System.out.println("OnClick "+rect.left);
            }
        });

       // Tooltip tooltip = new Tooltip(getActivity(), R.layout.barchart_one_tooltip);
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
         //   tooltip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1));
         //   tooltip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0));
       // }
       // barChart.setTooltips(tooltip);
        //mValuesOne[0][1] = Float.valueOf(String.valueOf(10 * Math.random()));
        //mValuesOne[0][2] = Float.valueOf(String.valueOf(10 * Math.random()));
        BarSet barSet = new BarSet(mLabelsOne, mValuesOne[0]);
        barSet.setColor(Color.parseColor(mStressColor));
        barChart.addData(barSet);
        barChart.setBarSpacing(Tools.fromDpToPx(35));

        barSet.getEntry(0).setColor(Color.parseColor(getResources().getString(R.color.colorNeutral)));
        barSet.getEntry(1).setColor(Color.parseColor(getResources().getString(R.color.colorStress)));
        barSet.getEntry(2).setColor(Color.parseColor(getResources().getString(R.color.colorNoise)));
        barSet.getEntry(3).setColor(Color.parseColor(getResources().getString(R.color.colorAnxious)));

        Paint gridColor = new Paint();
        gridColor.setColor(Color.parseColor(getResources().getString(R.color.textAccent)));
        barChart.setBorderSpacing(5)
                .setAxisBorderValues(0, 100, 20)
                .setYAxis(true)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setGrid(ChartView.GridType.HORIZONTAL, 10, 1, gridColor)
                .setLabelsColor(Color.parseColor(getResources().getString(R.color.textPrimary)))
                .setFontSize(24)
                .setAxisColor(Color.parseColor(getResources().getString(R.color.textAccent)));

        int[] order = {3, 1, 0, 2};//, 0, 4};
        final Runnable auxAction = action;
        Runnable chartOneAction = new Runnable() {
            @Override
            public void run() {
                //showTooltipOne();
                auxAction.run();
            }
        };
        barChart.show(new Animation()
                .setOverlap(.5f, order)
                .setEndAction(chartOneAction))
        //.show()
        ;
    }

    public static void updateOne(ChartView chart){

       //dismissTooltipOne();
        ///float[] newValues = //{{8.5f, 6.5f, 4.5f, 3.5f, 9f}, {5.5f, 3.0f, 3.0f, 2.5f, 7.5f}};
        chart.updateValues(0, mNewValues);
        //chart.updateValues(1, newValues[1]);
        chart.notifyDataUpdate();
    }

    public void dismissOne(ChartView chart, Runnable action){

        dismissTooltipOne();
        int[] order = {0, 4, 1, 3, 2};
        chart.dismiss(new Animation()
                .setOverlap(.5f, order)
                .setEndAction(action));
    }


    private void showTooltipOne(){

        ArrayList<ArrayList<Rect>> areas = new ArrayList<>();
        areas.add(mChartOne.getEntriesArea(0));
//        areas.add(mChartOne.getEntriesArea(1));

        for(int i = 0; i < areas.size(); i++) {
            for (int j = 0; j < areas.get(i).size(); j++) {

                Tooltip tooltip = new Tooltip(getActivity(), R.layout.barchart_one_tooltip, R.id.value);
                tooltip.prepare(areas.get(i).get(j), mNewValues[i]);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    tooltip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1));
                    tooltip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0));
                }
                mChartOne.showTooltip(tooltip, true);
            }
        }

    }
    public static void setValues(double[] values)
    {
        mMax = 0;
        for(int i = 0; i < values.length; i++)
        {
            mNewValues[i] = Float.valueOf(String.valueOf(Math.min(Double.valueOf(String.valueOf(100 * values[i])), 100.0)));
        }
        updateChart(0, mChartOne, null);
    }

    private static void dismissTooltipOne(){
        mChartOne.dismissAllTooltips();
    }


}