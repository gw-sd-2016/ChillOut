package com.example.grayapps.contextaware;

import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.ChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;

import java.util.ArrayList;


public class BarGraphFragment extends Fragment {


    /** First chart */
    private BarChartView mChartOne;
    private ImageButton mPlayOne;
    private boolean mUpdateOne;
    private final String[] mLabelsOne= {"Stress", "Noise", "Movement"};
    private final float [][] mValuesOne = {{9.5f, 7.5f, 5.5f}};


    /** Second chart
    private HorizontalBarChartView mChartTwo;
    private ImageButton mPlayTwo;
    private boolean mUpdateTwo;
    private final String[] mLabelsTwo= {"Mon", "Tue", "Wed", "Thu", "Fri"};
    private final float [] mValuesTwo = {23f, 34f, 55f, 71f, 98f};
    private TextView mTextViewTwo;
    private TextView mTextViewMetricTwo; */


    /** Third chart
    private BarChartView mChartThree;
    private ImageButton mPlayThree;
    private boolean mUpdateThree;
    private final String[] mLabelsThree= {"", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", ""};
    private final float[] mValuesThree = {2.5f, 3.7f, 4f, 8f, 4.5f, 4f, 5f, 7f, 10f, 14f,
            12f, 6f, 7f, 8f, 9f, 8f, 9f, 8f, 7f, 6f,
            5f, 4f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 11f,
            12f, 14, 13f, 10f ,9f, 8f, 7f, 5f, 4f, 6f};*/


    public BarGraphFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.bargraph, container, false);

        // Init first chart
        mUpdateOne = true;
        mChartOne = (BarChartView) layout.findViewById(R.id.barchart1);

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
    private void updateChart(final int tag, final ChartView chart, ImageButton btn){

       // dismissPlay(btn);

        switch(tag){
            case 0:
                updateOne(chart); break;
            default:
        }
    }


    /**
     * Dismiss a CardView chart
     * @param tag   Tag specifying which chart should be dismissed
     * @param chart   Chart view
     * @param btn    Play button

    private void dismissChart(final int tag, final ChartView chart){

        dismissPlay(btn);

        Runnable action =  new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        showPlay(btn);
                        showChart(tag, chart, btn);
                    }
                }, 500);
            }
        };

        switch(tag){
            case 0:
                dismissOne(chart, action); break;
            default:
        }
    }*/


    /**
     * Show CardView play button
     * @param btn    Play button

    private void showPlay(ImageButton btn){
        btn.setEnabled(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            btn.animate().alpha(1).scaleX(1).scaleY(1);
        else
            btn.setVisibility(View.VISIBLE);
    } */


    /**
     * Dismiss CardView play button
     * @param btn    Play button

    private void dismissPlay(ImageButton btn){
        btn.setEnabled(false);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            btn.animate().alpha(0).scaleX(0).scaleY(0);
        else
            btn.setVisibility(View.INVISIBLE);
    }*/



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

        Tooltip tooltip = new Tooltip(getActivity(), R.layout.barchart_one_tooltip);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            tooltip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1));
            tooltip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0));
        }
        barChart.setTooltips(tooltip);

        BarSet barSet = new BarSet(mLabelsOne, mValuesOne[0]);
        barSet.setColor(Color.parseColor("#90A4AE"));
        barChart.addData(barSet);
        barChart.setBarSpacing(Tools.fromDpToPx(35));


        barChart.setBorderSpacing(5)
                .setAxisBorderValues(0, 10, 2)
                .setYAxis(false)
                .setXLabels(XController.LabelPosition.OUTSIDE)
                .setYLabels(YController.LabelPosition.NONE)
                .setLabelsColor(Color.parseColor("#90A4AE"))
                .setAxisColor(Color.parseColor("#117E93"));

        int[] order = {1, 0, 2};//, 0, 4};
        final Runnable auxAction = action;
        Runnable chartOneAction = new Runnable() {
            @Override
            public void run() {
                showTooltipOne();
                auxAction.run();
            }
        };
        barChart.show(new Animation()
                .setOverlap(.5f, order)
                .setEndAction(chartOneAction))
        //.show()
        ;
    }

    public void updateOne(ChartView chart){

        dismissTooltipOne();
        float [][]newValues = {{8.5f, 6.5f, 4.5f, 3.5f, 9f}, {5.5f, 3.0f, 3.0f, 2.5f, 7.5f}};
        chart.updateValues(0, newValues[0]);
        chart.updateValues(1, newValues[1]);
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
                tooltip.prepare(areas.get(i).get(j), mValuesOne[i][j]);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    tooltip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1));
                    tooltip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0));
                }
                mChartOne.showTooltip(tooltip, true);
            }
        }

    }


    private void dismissTooltipOne(){
        mChartOne.dismissAllTooltips();
    }


}