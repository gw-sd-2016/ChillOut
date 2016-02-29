package com.example.grayapps.contextaware;

import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by AGray on 2/28/16.
 */
public class CalendarEvent {

    private static EventGraph mEventGraph = new EventGraph();
    private ParseObject mParseObject;
    private ArrayList<ArrayList<String>> mEventParams;
    private String mEvenTitle;
    private String mEventLocation;
    private long mTime;
    private int mDurationMinutes;
    private String[] mPeople;
    private double mNoiseLevel;
    private double mMovementLevel;
    private double mStressLevel;

    public CalendarEvent(String title, String location, String[] people, long time, int duration)
    {
        mParseObject = new ParseObject("CalendarEvent");
        mEventParams = new ArrayList<ArrayList<String>>();
        mEvenTitle = title;
        mEventLocation = location;
        mPeople = people;
        mTime = time;
        mDurationMinutes = duration;
    }

    public void completeEvent(double stress, double noise, double movement)
    {
        mStressLevel = stress;
        mNoiseLevel = noise;
        mMovementLevel = movement;

        ArrayList<String> one = new ArrayList<String>();
        one.add(mEvenTitle);
        one.add(mEventLocation);
        one.add(String.valueOf(mTime));
        one.add(String.valueOf(mDurationMinutes));

        ArrayList<String> two = new ArrayList<String>();
        two.add(mEvenTitle);
        two.add(mEventLocation);
        two.add(String.valueOf(noise));
        two.add(String.valueOf(movement));

        ArrayList<String> three = new ArrayList<String>();
        three.add(mEvenTitle);

        ArrayList<String> four = new ArrayList<String>();
        four.add(String.valueOf(mTime));
        four.add(String.valueOf(mDurationMinutes));
        four.add(String.valueOf(noise));
        four.add(String.valueOf(movement));

        ArrayList<String> five = new ArrayList<String>();
        five.add(String.valueOf(mNoiseLevel));
        five.add(String.valueOf(mMovementLevel));
        for(int i = 0; i < mPeople.length; i++)
        {
            if(mPeople[i] != null && mPeople[i].length() > 0)
            {
                three.add(mPeople[i]);
                five.add(mPeople[i]);
            }
        }

        mEventParams.add(one);
        mEventParams.add(two);
        mEventParams.add(three);
        mEventParams.add(four);
        mEventParams.add(five);

        int stressLevel = mStressLevel > 0.5 ? 1 : -1;

        mEventGraph.addEvent(mEventParams, stressLevel);

        mParseObject.put("eventParams", mEventParams);
        mParseObject.saveInBackground();
    }


}
