package com.example.grayapps.contextaware;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.db.chart.model.Bar;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDetailsActivity extends AppCompatActivity implements EventAttendeesListFragment.OnFragmentInteractionListener {

    private static SimpleDateFormat mSimpleDateFormat;
    private static SimpleDateFormat mSimpleTimeFormat;
    double mNoise = -1;
    double mMovement = -1;
    double mStress = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        String eventId = getIntent().getStringExtra("eventId");
        int position = getIntent().getIntExtra("position", -1);
        if (getIntent().hasExtra("noise"))
        {
            mNoise = getIntent().getDoubleExtra("noise", -1);
        }
        if (getIntent().hasExtra("movement"))
        {
            mMovement = getIntent().getDoubleExtra("movement", -1);
        }
        if (getIntent().hasExtra("stress"))
        {
            mStress = getIntent().getDoubleExtra("stress", -1);
        }
        editor.putLong("lastAccessedEventId", Long.valueOf(eventId));
        editor.commit();
        setContentView(R.layout.activity_event_details);
        mSimpleDateFormat = new SimpleDateFormat("EEEE, MM/dd/yy");
        mSimpleTimeFormat = new SimpleDateFormat("h:mma");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               launchPrediction(view);
            }
        });

        /** Drawer begins*/
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        //pass your items here
                )
                .build();

       // getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        /** Drawer ends */

        /*if(position >= 0)
        {
            if(position % 4 == 0)
            toolbar.setBackgroundColor();
        }*/
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        BarFragment chart = new BarFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, chart)
                .commit();

        Uri uri = CalendarContract.Events.CONTENT_URI;
        String[] proj =
                new String[]{
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.EVENT_LOCATION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND};

        Cursor cursor = getContentResolver().query(uri, proj, CalendarContract.Events._ID + " = ?", new String[]{eventId}, null);
        if (position >= 0) {
            RelativeLayout contentLayout = (RelativeLayout) findViewById(R.id.eventContent);
            contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.eventBackground));
            Window w = getWindow();
            CardView detsCard = (CardView) findViewById(R.id.timeAndPlace);
            FrameLayout backgroundColor = (FrameLayout) findViewById(R.id.backgroundFrame);
            if (position % 3 == 0) {
             //   contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStress));
                toolbar.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorStress))));
                w.setStatusBarColor(Color.parseColor(getResources().getString(R.color.colorStressDark)));
                backgroundColor.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorStress))));
                fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorStress));
              //  contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStressBackground));
                detsCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorStressBar));
            } else if (position % 5 == 0 ) {
            //    contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorNoise));
                toolbar.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorNoise))));
                w.setStatusBarColor(Color.parseColor(getResources().getString(R.color.colorNoiseDark)));
                backgroundColor.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorNoise))));
               // contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorNoiseBackground));
                fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorNoise));
                detsCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorNoiseBar));
            } else if (position % 7 == 0 || position % 4 == 0) {
             //   contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAnxious));
                toolbar.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorAnxious))));
                w.setStatusBarColor(Color.parseColor(getResources().getString(R.color.colorAnxiousDark)));
                backgroundColor.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorAnxious))));
                fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorAnxious));
               // contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAnxiousBackground));
                detsCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorAnxiousBar));
            } else {
             //   contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorNeutral));
                toolbar.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorNeutral))));
                w.setStatusBarColor(Color.parseColor(getResources().getString(R.color.colorNeutralDark)));
                backgroundColor.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorNeutral))));
                fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorNeutral));
                //contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorNeutralBackground));
                detsCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorNeutralBar));
            }

            if(mStress == 2)
            {
                toolbar.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorStress))));
                w.setStatusBarColor(Color.parseColor(getResources().getString(R.color.colorStressDark)));
                backgroundColor.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorStress))));
                fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorStress));
               // contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorStressBackground));
                detsCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorStressBar));
            }
            else if(mStress == 1)
            {
                toolbar.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorNoise))));
                w.setStatusBarColor(Color.parseColor(getResources().getString(R.color.colorNoiseDark)));
                backgroundColor.setBackground(new ColorDrawable(Color.parseColor(getResources().getString(R.color.colorNoise))));
                fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorNoise));
               // contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorNoiseBackground));
                detsCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorNoiseBar));
            }

        }

        if (cursor.moveToFirst()) {
            TextView eventLocation = (TextView) findViewById(R.id.eventLocation);
            mTitle.setText(cursor.getString(0));
            if(cursor.getString(1) != null && cursor.getString(1).length() > 0)
            {
                eventLocation.setText(cursor.getString(1));
            }
            else
            {
                eventLocation.setVisibility(View.GONE);
                ImageView locationIcon = (ImageView) findViewById(R.id.locationIcon);
                locationIcon.setVisibility(View.GONE);
            }
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

    private void launchPrediction(View view)
    {
        int notificationId = 001;

        // Gets an instance of the NotificationManager service
        NotificationManager notifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_group_2)
                        .setContentTitle("Upcoming Stressful Event")
                        .setContentText("Job Performance Review")
                        .setColor(Color.parseColor(getResources().getString(R.color.colorStress)));

        builder.setContentIntent(resultPendingIntent);
        builder.setAutoCancel(true);

        // Builds the notification and issues it.
        notifyMgr.notify(notificationId, builder.build());


    }
}
