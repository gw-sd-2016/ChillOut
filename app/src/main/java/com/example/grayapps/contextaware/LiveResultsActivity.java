package com.example.grayapps.contextaware;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;

public class LiveResultsActivity extends AppCompatActivity {

    public static BandClient client = null;
    private TextView txtStatus;
    public static BarGraphFragment mBarChart;
    private BroadcastReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences userData = getSharedPreferences("userData", MODE_PRIVATE);
        SharedPreferences.Editor editor = userData.edit();

        long eventId = getIntent().getLongExtra("eventId", -1);
        int position = getIntent().getIntExtra("position", -1);
        editor.putLong("lastAccessedEventId", eventId);
        editor.commit();
        setContentView(R.layout.activity_live_results);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle.setText("Live Results");
        mBarChart = new BarGraphFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mBarChart)
                .commit();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equals(DataRecordingService.STRESS_RESULT))
                {
                mBarChart.setValues(intent.getDoubleArrayExtra("graphUpdate"));
                TextView timer = (TextView) findViewById(R.id.timer);
                timer.setText(String.valueOf(intent.getStringExtra("timerUpdate")));
                int newBreaths = intent.getIntExtra("breathUpdate", 0);
                appendToUI(newBreaths + " breaths detected");
                }
                else if(intent.getAction().equals(DataRecordingService.STRESS_PAUSE))
                {
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(mReceiver);
                }
            }

        };

    }

 @Override
 protected void onStart(){
     super.onStart();
     LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(DataRecordingService.STRESS_RESULT));
     LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(DataRecordingService.STRESS_PAUSE));
 }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                final WeakReference<Activity> reference = new WeakReference<Activity>(this);
                new HeartRateConsentTask().execute(reference);
                return true;

            default:
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Stopped", "App was stopped");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(final WeakReference<Activity>... params) {
            try {
                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            final Activity actvty = params[0].get();
                            @Override
                            public void userAccepted(boolean consentGiven) {

                                Intent intent = new Intent(actvty, DataRecordingService.class);
                                intent.setAction(DataRecordingService.STRESS_START);
                                startService(intent);
                                LocalBroadcastManager.getInstance(params[0].get()).registerReceiver(mReceiver, new IntentFilter(DataRecordingService.STRESS_RESULT));
                                LocalBroadcastManager.getInstance(params[0].get()).registerReceiver(mReceiver, new IntentFilter(DataRecordingService.STRESS_PAUSE));

                            }

                        });
                    }
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage = "";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }

    private void appendToUI(final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (txtStatus != null && string != null) {
                    txtStatus.setText(string);
                }
            }
        });
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("0 breaths detected");
        return ConnectionState.CONNECTED == client.connect().await();
    }

}
