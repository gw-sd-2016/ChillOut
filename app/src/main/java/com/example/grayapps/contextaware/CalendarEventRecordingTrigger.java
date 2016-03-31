package com.example.grayapps.contextaware;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by AGray on 3/30/16.
 */
public class CalendarEventRecordingTrigger
{
    private long mStartTime;
    private long mEndTime;
    private String mEventId;
    private double mNoiseLevel;
    private int mStessLevel;
    private double mMovementLevel;
    //private Parcel mStartIntent;
    //private Parcel mStopIntent;
    public CalendarEventRecordingTrigger(Context context, long startTime, long endTime, String eventId)
    {
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

    public int getStressLevel()
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

    public void setStressLevel(int stress)
    {
        mStessLevel = stress;
    }
}
