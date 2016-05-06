package com.example.grayapps.contextaware;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by AGray on 3/30/16.
 */
public class CalendarEventRecordingTrigger
{
    private static EventGraph mEventGraph;
    private long mStartTime;
    private long mEndTime;
    private String mEventId;
    private double mNoiseLevel;
    private double mStessLevel;
    private double mMovementLevel;
    private String[] mAttendees;
    private String mEventLocation;
    private String mEventTitle;

    //private Parcel mStartIntent;
    //private Parcel mStopIntent;
    public CalendarEventRecordingTrigger(final Context context, long startTime, long endTime, String eventId)
    {
        if (mEventGraph == null)
        {
            SharedPreferences savedData = context.getSharedPreferences("userData", Context.MODE_PRIVATE);
            String objectID = savedData.getString("parseEventGraphID", "ID Not Available");
            final Gson converter = new Gson();
            if (!objectID.equals("ID Not Available"))
            {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("EventGraph");
                query.fromLocalDatastore();
                try
                {
                    ParseObject object = query.get(objectID);
                    //JSONObject eventGraph = object.getJSONObject("EventGraphWrapper");

                    mEventGraph = converter.fromJson(object.getString("EventGraphWrapper"), EventGraph.class);

                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
            } else
            {
                mEventGraph = new EventGraph();
                final ParseObject parseWrapper = new ParseObject("EventGraph");
                parseWrapper.put("EventGraphWrapper", converter.toJson(mEventGraph, EventGraph.class));
                parseWrapper.setObjectId("uniqueGraphID");
                parseWrapper.pinInBackground(new SaveCallback()
                {
                    @Override
                    public void done(ParseException e) {
                        if (e == null)
                        {
                            SharedPreferences.Editor editor = context.getSharedPreferences("userData", Context.MODE_PRIVATE).edit();
                            editor.putString("parseEventGraphID", "uniqueGraphID");
                            editor.commit();

                            Log.d("ObjectID", "" + parseWrapper.getObjectId());
                        } else
                        {
                            Log.d("ParseExcepetion2", e.getMessage());
                        }
                    }
                });
            }
        }
        mStartTime = startTime;
        mEndTime = endTime;
        mEventId = eventId;
        scheduleAlarms(context);
    }

    public long getStartTime()
    {
        return mStartTime;
    }

    public long getEndTime()
    {
        return mEndTime;
    }

    private void scheduleAlarms(Context context)
    {

        Intent starter = new Intent(context, DataRecordingService.class);
        starter.setAction(DataRecordingService.STRESS_START);

        PendingIntent startIntent = PendingIntent.getService(context, 0, starter, 0);

        Intent stopper = new Intent(context, DataRecordingService.class);
        stopper.setAction(DataRecordingService.STRESS_END);
        stopper.putExtra("eventId", mEventId);
        PendingIntent stopIntent = PendingIntent.getService(context, 0, stopper, 0);

        AlarmManager scheduler = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        scheduler.setExact(AlarmManager.RTC_WAKEUP, mStartTime, startIntent);
        scheduler.setExact(AlarmManager.RTC_WAKEUP, mEndTime, stopIntent);

        Log.d("Alarm", "Scheduled");
    }

    public void cancelAlarms(Context context) {

        Intent starter = new Intent(context, DataRecordingService.class);
        starter.setAction(DataRecordingService.STRESS_START);

        PendingIntent startIntent = PendingIntent.getService(context, 0, starter, 0);

        Intent stopper = new Intent(context, DataRecordingService.class);
        stopper.setAction(DataRecordingService.STRESS_END);

        PendingIntent stopIntent = PendingIntent.getService(context, 0, stopper, 0);

        AlarmManager scheduler = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        startIntent.cancel();
        stopIntent.cancel();
        scheduler.cancel(startIntent);
        scheduler.cancel(stopIntent);
    }

    public String getEventId()
    {
        return mEventId;
    }

    public double getNoiseLevel()
    {
        return mNoiseLevel;
    }

    public double getMovementLevel()
    {
        return mMovementLevel;
    }

    public double getStressLevel()
    {
        return mStessLevel;
    }

    public void setNoiseLevel(double noise)
    {
        mNoiseLevel = noise;
    }

    public void setMovementLevel(double movement)
    {
        mMovementLevel = movement;
    }

    public void setStressLevel(double stress)
    {
        mStessLevel = stress;
    }

    public void addAttendees(String[] attendeeNames) {
        mAttendees = attendeeNames;
    }

    public void setLocation(String mEventLocation) {
        mEventLocation = mEventLocation;
    }

    public void setTitle(String mEventTitle) {
        mEventTitle = mEventTitle;
    }

    public void addToGraph()
    {
        ArrayList<ArrayList<String>> eventParams = new ArrayList<ArrayList<String>>();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("ccc");
        String dayOFweek = dateFormatter.format(mStartTime);
        dateFormatter = new SimpleDateFormat("HH");
        int hour = Integer.valueOf(dateFormatter.format(mStartTime));
        String timeOFday;
        if (5 <= hour && hour < 11)
            timeOFday = "Morning";
        else if (11 <= hour && hour < 17)
            timeOFday = "Afternoon";
        else if (17 <= hour && hour < 23)
            timeOFday = "Evening";
        else
            timeOFday = "Night";

        String noiseLevel = mNoiseLevel > 0.5 ? "High" : "Low";
        String moveLevel = mMovementLevel > 0.5 ? "High" : "Low";

        ArrayList<String> one = new ArrayList<String>(4);
        one.add(mEventTitle);
        one.add(mEventLocation);
        one.add(timeOFday);
        one.add(dayOFweek);


        ArrayList<String> two = new ArrayList<String>(4);
        two.add(mEventTitle);
        two.add(mEventLocation);
        two.add(noiseLevel);
        two.add(moveLevel);

        ArrayList<String> three = new ArrayList<String>();
        three.add(timeOFday);
        three.add(dayOFweek);
        three.add(noiseLevel);
        three.add(moveLevel);

        ArrayList<String> four = new ArrayList<String>();
        four.add(mEventTitle);
        if (mAttendees != null)
        {
            for (int j = 0; j < mAttendees.length; j++)
            {
                four.add(mAttendees[j]);
            }

            ArrayList<String> five = new ArrayList<String>();
            five.add(noiseLevel);
            five.add(moveLevel);
            ArrayList<String> six = new ArrayList<String>();
            for (int j = 0; j < mAttendees.length; j++)
            {
                five.add(mAttendees[j]);
                six.add(mAttendees[j]);
            }

            ArrayList<String> seven = new ArrayList<String>();
            seven.add(mEventLocation);
            for (int j = 0; j < mAttendees.length; j++)
            {
                seven.add(mAttendees[j]);
            }
            eventParams.add(five);
            eventParams.add(six);
            eventParams.add(seven);
        }

        eventParams.add(one);
        eventParams.add(two);
        eventParams.add(three);
        eventParams.add(four);


        int stress = mStessLevel > 0.5 ? 1 : -1;
        mEventGraph.addEvent(eventParams, stress);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("EventGraph");
        query.fromLocalDatastore();
        final Gson converter = new Gson();
        try
        {
            ParseObject object = query.get("uniqueGraphID");
            object.put("EventGraphWrapper", converter.toJson(mEventGraph, EventGraph.class));
            object.pinInBackground();
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

}
