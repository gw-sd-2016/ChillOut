package com.example.grayapps.contextaware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.util.Log;

import com.google.gson.Gson;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CalendarEventChangeReceiver extends BroadcastReceiver
{

    private Cursor mCursor;
    private Context mContext;
    private JSONObject mEventMapJsonObject;
    private ParseObject mParseWrapper;
    private EventChecker mChecker;

    public CalendarEventChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if (intent.getAction().equals("android.intent.action.PROVIDER_CHANGED"))
        {
            mContext = context;
            getEventsFromCalendar();
            mChecker = new EventChecker();
        }
    }

    public void getEventsFromCalendar() {
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String[] projection = new String[]{
                CalendarContract.Events._ID,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };
        String query = CalendarContract.Events.ACCOUNT_NAME + " = ? AND " + CalendarContract.Events.DTSTART + ">" + System.currentTimeMillis();
        mCursor = mContext.getContentResolver().query(uri, projection, query, new String[]{"ajgray123@gmail.com"}, CalendarContract.Events.DTSTART + " ASC");
        getEventsFromParse();
    }

    public void getEventsFromParse() {
        Log.d("eventsFromParse", "Reached");
        SharedPreferences savedData = mContext.getSharedPreferences("userData", Context.MODE_PRIVATE);
        String objectID = savedData.getString("parseEventMapID", "ID Not Available");
        Log.d("ObjectID", objectID);
        final Gson converter = new Gson();
        if (!objectID.equals("ID Not Available"))
        {
            Log.d("IDAvailable", "Reached");
            ParseQuery<ParseObject> query = ParseQuery.getQuery("CalendarEvents");
            query.fromLocalDatastore();
            query.getInBackground(objectID, new GetCallback<ParseObject>()
            {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null)
                    {
                        Log.d("GetsObject", "Reached");
                        mParseWrapper = object;
                        mEventMapJsonObject = mParseWrapper.getJSONObject("EventMapWrapper");
                        new EventChecker().execute();

                    } else
                    {
                        Log.d("GetsObject", "Doesn't exist.");

                    }
                }
            });
        } else
        {
            mEventMapJsonObject = new JSONObject();
            new EventChecker().execute();
        }
    }

    private class EventChecker extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void... params) {
            findChanges();
            return null;
        }

        public void findChanges() {
            Log.d("findChanges", "Reached");
            Gson converter = new Gson();
            if (mEventMapJsonObject != null)
            {
                Log.d("Level1", "Reached");
                if (mCursor != null)
                {
                    Log.d("Level2", "Reached");
                    while (mCursor.moveToNext())
                    {
                        String eventId = mCursor.getString(0);
                        long start = mCursor.getLong(1);
                        long stop = mCursor.getLong(2);
                        if (mEventMapJsonObject.has(eventId))
                        {
                            CalendarEventRecordingTrigger cEvent = null;
                            try
                            {
                                cEvent = converter.fromJson(mEventMapJsonObject.getString(eventId), CalendarEventRecordingTrigger.class);
                                if (start != cEvent.getStartTime() || stop != cEvent.getEndTime())
                                {
                                    cEvent.cancelAlarms(mContext);
                                    Log.d("Event Status", "Cancelled");
                                    try
                                    {
                                        mEventMapJsonObject.put(eventId, converter.toJson(new CalendarEventRecordingTrigger(mContext, start, stop, eventId), CalendarEventRecordingTrigger.class));
                                    } catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            try
                            {
                                mEventMapJsonObject.put(eventId, converter.toJson(new CalendarEventRecordingTrigger(mContext, start, stop, eventId), CalendarEventRecordingTrigger.class));
                            } catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                Log.d("Level3", "Reached");
                if (mParseWrapper != null)
                {
                    Log.d("ParseWrapper", "Not Null");
                    mParseWrapper.put("EventMapWrapper", mEventMapJsonObject);
                    mParseWrapper.pinInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e) {
                            if (e == null)
                            {
                                Log.d("ParseWrapper", "Saved");
                            } else
                            {
                                Log.d("ParseExcepetion1", e.getMessage());
                            }
                        }
                    });
                } else
                {
                    Log.d("ParseWrapper", "Is Null");
                    final ParseObject parseWrapper = new ParseObject("CalendarEvents");
                    parseWrapper.put("EventMapWrapper", mEventMapJsonObject);
                    parseWrapper.setObjectId("uniqueID");
                    parseWrapper.pinInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e) {
                            if (e == null)
                            {
                                SharedPreferences.Editor editor = mContext.getSharedPreferences("userData", Context.MODE_PRIVATE).edit();
                                editor.putString("parseEventMapID", "uniqueID");
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
            mCursor.close();
        }
    }
}
