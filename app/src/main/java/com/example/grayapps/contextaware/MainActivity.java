package com.example.grayapps.contextaware;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private SoundMeter sensor;
    private SoundOutlierDetector detector;
    private int count;
    private double ups;
    private double downs;
    private int states[];
    private int[] changes;
    private ParseObject profile;
    private int frequency = 2 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "nJPOm5SDvGW96lZm5PbZuzlmOyvyJN0hfnSoSojT", "d1jQXqyGIxj0Xc1dOyoVCXAylgtgym7DLhWWI5y8");
        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        profile = ParseObject.createWithoutData("UserProfile", "myprofile");
        profile.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    profile = object;
                } else {

                }
            }
        });
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Saved", "Instance was saved");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (SoundOutlierDetector.hasStarted()) {
            for (int i = 0; i < SoundOutlierDetector.getLength(); i++) {
                profile.put("max" + i, SoundOutlierDetector.getMax(i));
                profile.put("min" + i, SoundOutlierDetector.getMin(i));
                if (i < SoundOutlierDetector.getLength() - 1)
                    profile.put("state" + i, states[i]);
            }
            double[] lookback = SoundOutlierDetector.getLookback();
            for (int i = 0; i < SoundOutlierDetector.getRange(); i++) {
                profile.put("lookback" + i, lookback[i]);
            }

            profile.put("location", SoundOutlierDetector.getLocation());
            profile.pinInBackground();
            Log.d("ObjectID", "ID: " + profile.getObjectId());
        }
        Log.d("Stopped", "App was stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Destroyed", "App was destroyed");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startListening(View view) {
        if(SoundOutlierDetector.hasStarted())
        {
            Context context = getApplicationContext();
            CharSequence text = "Already Listening!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        if (profile != null)
            Log.d("ObjectID", "ID: " + profile.getObjectId());

        detector = new SoundOutlierDetector();
        sensor = new SoundMeter();
        handler = new Handler();
        sensor.start();
        detector.start();
        states = new int[SoundOutlierDetector.getLength() - 1];
        changes = new int[SoundOutlierDetector.getLength() - 1];
        if (profile.has("max0")) {
            for (int i = 0; i < SoundOutlierDetector.getLength(); i++) {
                double max = profile.getDouble("max" + i);
                double min = profile.getDouble("max" + i);
                //Log.d("Max", i + " -> " + max);
                if (max >= 0) {
                    SoundOutlierDetector.setMax(max, i);
                }
                if (min >= 0) {
                    SoundOutlierDetector.setMin(min, i);
                }
            }
            for (int i = 0; i < states.length; i++) {
                int state = profile.getInt("state" + i);
                states[i] = state;
            }
            double[] lookback = new double[SoundOutlierDetector.getRange()];
            for (int i = 0; i < SoundOutlierDetector.getRange(); i++)
                lookback[i] = profile.getDouble("lookback" + i);
            int location = profile.getInt("location");


            for (int i = 0; i < SoundOutlierDetector.getRange(); i++) {
                if (lookback[i] >= 0) {
                    SoundOutlierDetector.setLocationInLookback(i, lookback[i]);
                }
            }

            if (location >= 0) {
                SoundOutlierDetector.setLocation(location);
            }
        }
        ups = 0;
        downs = 0;
        count = 0;
        Timer myTimer = new Timer();
        SoundTask myTimerTask = new SoundTask();

        myTimer.scheduleAtFixedRate(myTimerTask, 0, frequency * 1000);
    }

    public void setFrequency(View view)
    {
      //  EditText freq = (EditText) findViewById(R.id.frequency);
        //frequency = Integer.valueOf(freq.getText().toString());
    }

    public void updateTextView(int text_id, String toThis) {
        TextView val = (TextView) findViewById(text_id);
        val.setText(toThis);

        return;
    }

    public void launchCalendar(View view)
    {
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }

    private class SoundTask extends TimerTask {
        @Override
        public void run() {
            double readings = 5;
            double total = 0;
            final double[] val = sensor.getAmplitudeEMA();
            Log.d("FirstValue", "" + val[0]);
            final double[][] ranges = detector.update(val[0]);
            count++;

            for (int i = 0; i < states.length; i++) {
                if (ranges[0][i] > ranges[0][i + 1] && ranges[1][i] > ranges[1][i + 1]) {

                    if (states[i] < 1) {
                        states[i] = states[i];
                       // Log.d("Changes", "LOUDER " + i);
                    } //else
                        changes[i] = 4;//louder
                        states[i]++;
                } else if (ranges[0][i] < ranges[0][i + 1] && ranges[1][i] < ranges[1][i + 1]) {

                    if (states[i] < 1) {
                        states[i] = states[i];

                       // Log.d("Changes", "QUIETER " + i);
                    }// else
                        changes[i] = 1;//quieter
                        states[i]++;
                } else
                {
                    if(changes[i] == 1 || changes[i] == 3)
                    {
                        if(changes[i] == 1)
                            states[i] = 0;
                        changes[i] = 3;//getting louder
                    }
                    else
                    {
                        if(changes[i] == 4)
                            states[i] = 0;
                        changes[i] = 2;//getting quieter
                    }
                    states[i]++;
                }
            }

            Log.d("States1", states[0] + " " + states[1] + " " + states[2] + " " + states[3]);
            Log.d("Min1", "" + 100 * ranges[0][0]);
            Log.d("Changes", "" + changes[0]);
            Log.d("Max1", "" + 100 * ranges[1][0]);
            Log.d("Min2", "" + 100 * ranges[0][1]);
            Log.d("Max2", "" + 100 * ranges[1][1]);
            Log.d("Min3", "" + 100 * ranges[0][2]);
            Log.d("Max3", "" + 100 * ranges[1][2]);
            Log.d("Min4", "" + 100 * ranges[0][3]);
            Log.d("Max4", "" + 100 * ranges[1][3]);
            Log.d("Min5", "" + 100 * ranges[0][4]);
            Log.d("Max5", "" + 100 * ranges[1][4]);

            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                 /* updateTextView(R.id.avgMax, "1 -> Min: " + (int) Math.round(100 * ranges[0][0]) + " Max: " + (int) Math.round(100 * ranges[1][0]));
                                  updateTextView(R.id.avgMin, "2 -> Min: " + (int) Math.round(100 * ranges[0][1]) + " Max: " + (int) Math.round(100 * ranges[1][1]));
                                  updateTextView(R.id.percentUp, "3 -> Min: " + (int) Math.round(100 * ranges[0][2]) + " Max: " + (int) Math.round(100 * ranges[1][2]));
                                  updateTextView(R.id.percentDown, "4 -> Min: " + (int) Math.round(100 * ranges[0][3]) + " Max: " + (int) Math.round(100 * ranges[1][3]));
                                  updateTextView(R.id.soundValue, String.valueOf((int) Math.round(1000 * val[0])));
                                  TextView upOne = (TextView) findViewById(R.id.upOne);
                                  TextView upTwo = (TextView) findViewById(R.id.upTwo);
                                  TextView upThree = (TextView) findViewById(R.id.upThree);
                                  TextView upFour = (TextView) findViewById(R.id.upFour);

                                  if (changes[0] < 0)
                                      upOne.setTextColor(Color.RED);
                                  else
                                      upOne.setTextColor(Color.BLUE);
                                  if (changes[1] < 0)
                                      upTwo.setTextColor(Color.RED);
                                  else
                                      upTwo.setTextColor(Color.BLUE);
                                  if (changes[2] < 0)
                                      upThree.setTextColor(Color.RED);
                                  else
                                      upThree.setTextColor(Color.BLUE);
                                  if (changes[3] < 0)
                                      upFour.setTextColor(Color.RED);
                                  else
                                      upFour.setTextColor(Color.BLUE);

                                  updateTextView(R.id.upOne, String.valueOf(states[0]));
                                  updateTextView(R.id.upTwo, String.valueOf(states[1]));
                                  updateTextView(R.id.upThree, String.valueOf(states[2]));
                                  updateTextView(R.id.upFour, String.valueOf(states[3]));*/

                                  switch(changes[0])
                                  {
                                      case 1:
                                          updateTextView(R.id.soundValue, "It's been quieter for " + (2 * states[0]) + " seconds");
                                          break;
                                      case 2:
                                          updateTextView(R.id.soundValue, "Change detected: quieter");
                                          break;
                                      case 3:
                                          updateTextView(R.id.soundValue, "Changed detected: louder");
                                          break;
                                      case 4:
                                          updateTextView(R.id.soundValue, "It's been louder for " + (2 * states[0]) + " seconds");
                                          break;

                                  }

                              }
                          }

            );
        }

    }

}

