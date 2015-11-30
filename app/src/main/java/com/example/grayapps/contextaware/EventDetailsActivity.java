package com.example.grayapps.contextaware;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

public class EventDetailsActivity extends AppCompatActivity implements EventAttendeesListFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        long eventId = getIntent().getLongExtra("eventId", -1);
        editor.putLong("lastAccessedEventId", eventId);
        editor.commit();
        setContentView(R.layout.activity_event_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String[] proj =
                new String[]{
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.EVENT_LOCATION};

        Cursor cursor = getContentResolver().query(uri, proj, CalendarContract.Events._ID + " = ?", new String[]{String.valueOf(eventId)}, null);
        if (cursor.moveToFirst()) {
            // read event data
            TextView eventTitle = (TextView) findViewById(R.id.eventTitle);
            TextView eventLocation = (TextView) findViewById(R.id.eventLocation);

            eventTitle.setText(cursor.getString(0));
            eventLocation.setText(cursor.getString(1));
        }

    }

    @Override
    public void onFragmentInteraction(String id)
    {
        Log.d("FragmentID", id);
    }

}
