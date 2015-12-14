package com.example.grayapps.contextaware;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

public class EventDetailsActivity extends AppCompatActivity implements EventAttendeesListFragment.OnFragmentInteractionListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        long eventId = getIntent().getLongExtra("eventId", -1);
        editor.putLong("lastAccessedEventId", eventId);
        editor.commit();
        setContentView(R.layout.activity_event_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new BarGraphFragment())
                .commit();

        Uri uri = CalendarContract.Events.CONTENT_URI;
        String[] proj =
                new String[]{
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.EVENT_LOCATION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND};

        Cursor cursor = getContentResolver().query(uri, proj, CalendarContract.Events._ID + " = ?", new String[]{String.valueOf(eventId)}, null);
        if (cursor.moveToFirst()) {
            // read event data
            //TextView eventTitle = (TextView) findViewById(R.id.eventTitle);
            TextView eventLocation = (TextView) findViewById(R.id.eventLocation);
            setTitle(cursor.getString(0));
           // mTitle.setText();
            eventLocation.setText(cursor.getString(1));
        }


    }

    @Override
    public void onFragmentInteraction(String id)
    {
        Log.d("FragmentID", id);
    }

}
