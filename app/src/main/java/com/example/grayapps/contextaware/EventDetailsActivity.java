package com.example.grayapps.contextaware;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDetailsActivity extends AppCompatActivity implements EventAttendeesListFragment.OnFragmentInteractionListener {

    private static SimpleDateFormat mSimpleDateFormat;
    private static SimpleDateFormat mSimpleTimeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        long eventId = getIntent().getLongExtra("eventId", -1);
        int position = getIntent().getIntExtra("position", -1);
        editor.putLong("lastAccessedEventId", eventId);
        editor.commit();
        setContentView(R.layout.activity_event_details);
        mSimpleDateFormat = new SimpleDateFormat("EEEE, MM/dd/yy");
        mSimpleTimeFormat = new SimpleDateFormat("h:mma");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        /*if(position >= 0)
        {
            if(position % 4 == 0)
            toolbar.setBackgroundColor();
        }*/
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new BarFragment())
                .commit();

        Uri uri = CalendarContract.Events.CONTENT_URI;
        String[] proj =
                new String[]{
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.EVENT_LOCATION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND};

        Cursor cursor = getContentResolver().query(uri, proj, CalendarContract.Events._ID + " = ?", new String[]{String.valueOf(eventId)}, null);
        if (position >= 0) {
            RelativeLayout contentLayout = (RelativeLayout) findViewById(R.id.eventContent);
            if (position % 4 == 0) {
                contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStress));
            } else if (position % 3 == 0) {
                contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorNoise));
            } else if (position % 7 == 0) {
                contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAnxious));
            } else {
                contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorNeutral));
            }

        }

        if (cursor.moveToFirst()) {
            TextView eventLocation = (TextView) findViewById(R.id.eventLocation);
            setTitle(cursor.getString(0));
            eventLocation.setText(cursor.getString(1));
            TextView eventDate = (TextView) findViewById(R.id.eventDate);
            String timeAsString = mSimpleDateFormat.format(new Date(cursor.getLong(2)));
            eventDate.setText(timeAsString);
            TextView eventTime = (TextView) findViewById(R.id.eventTime);
            String fullTime = mSimpleTimeFormat.format(new Date(cursor.getLong(2))) + " - ";
            fullTime += mSimpleTimeFormat.format(new Date(cursor.getLong(3)));
            eventTime.setText(fullTime);
        }


    }

    @Override
    public void onFragmentInteraction(String id) {
        Log.d("FragmentID", id);
    }

}
