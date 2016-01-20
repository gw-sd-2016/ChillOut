package com.example.grayapps.contextaware;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.SampleRate;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class LiveResultsActivity extends AppCompatActivity {

    private BandClient client = null;
    private ParseObject profile;
    private TextView txtStatus;
    public static BarGraphFragment mBarChart;
    private static DenseMatrix64F mIdentityMatrix;
    private static KalmanFilter mKF;
    private static DenseMatrix64F mCurrentReadings;
    private static KalmanFilter mKF2;
    private static double[] prevXYZ;
    private static double[] prevRR;
    private static double mAcclReading;
    private static int mLoss;
    private static long mLastMove;
    private static long mLastMinute;
    private static int mStressMinutes;
    private static boolean mDipping;
    private static double mStartDip;
    private static double mSumAverage;
    private static double mCalcHR;
    private static int mMinutes;
    private static double mDips;
    private static double mValids;
    private static String mHRInterval = "";
    private int mStates[];
    private int[] mChanges;
    private Handler mHandler;
    private SoundMeter mSensor;
    private SoundOutlierDetector mDetector;
    private double mUps;
    private double mDowns;
    private int count;
    private double mAcclMoved;
    private double mAcclChange;
    private double[] mMostRecent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
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

        mMinutes = 0;
        mCalcHR = 0;
        mStressMinutes = 0;
        mAcclReading = 0;
        mLoss = 0;
        mLastMove = 0;
        mLastMinute = 0;
        mSumAverage = 0;
        mStartDip = 0;
        mValids = 0;
        mDips = 0;
        mDipping = false;
        prevXYZ = new double[3];
        prevRR = new double[4];
        mAcclMoved = 0;
        mAcclChange = 0;
        mMostRecent = new double[3];

        long eventId = getIntent().getLongExtra("eventId", -1);
        int position = getIntent().getIntExtra("position", -1);
        editor.putLong("lastAccessedEventId", eventId);
        editor.commit();
        setContentView(R.layout.activity_live_results);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mBarChart = new BarGraphFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mBarChart)
                .commit();

        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        Button btnConsent = (Button) findViewById(R.id.btnConsent);
        btnConsent.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                new HeartRateConsentTask().execute(reference);
            }
        });
    }

    private BandRRIntervalEventListener mRRIntervalEventListener = new BandRRIntervalEventListener() {
        @Override
        public void onBandRRIntervalChanged(final BandRRIntervalEvent event) {
            if (event != null) {
                double temp = event.getInterval();

                mCurrentReadings = new DenseMatrix64F(1, 1, true, Math.abs(temp));

                mKF.predict();
                mKF.update(mCurrentReadings, mIdentityMatrix);
                temp = mKF.getState().getData()[0];
                String val = String.format("%.3f", 200 * temp);
                mHRInterval = val;
                mCalcHR = (60.0 / temp);
                prevRR[0] = prevRR[1];
                prevRR[1] = prevRR[2];
                prevRR[2] = prevRR[3];
                prevRR[3] = 200 * temp;

                boolean isDip = false;
                if (!mDipping && prevRR[2] > prevRR[3]) {
                    mDipping = true;
                    mStartDip = prevRR[2];
                    Log.d("FunDip", "Detected");
                } else if (mDipping && prevRR[2] < prevRR[3]) {
                    if (mStartDip / prevRR[2] >= 1.05 && mStartDip / prevRR[2] <= 2) {
                        isDip = true;
                        Log.d("FunDip", String.format("Completed: %.2f", mStartDip / prevRR[2]));
                    } else {
                        Log.d("FunDip", String.format("Incomplete: %.2f", mStartDip / prevRR[2]));
                    }
                    mDipping = false;
                    mStartDip = 0;
                }

                long currentTime = System.currentTimeMillis();
                isDip &= prevRR[1] < prevRR[0];
                boolean isValid = currentTime - mLastMove > 1000;
                isValid |= mLoss < 17;

                if (isValid) {
                    mValids++;
                    if (isDip) {
                        mDips++;
                    }
                }
            }
        }
    };

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null) {
                mAcclChange++;
                mAcclReading = 100 * (Math.pow(event.getAccelerationX() - prevXYZ[0], 2) +
                        Math.pow(event.getAccelerationY() - prevXYZ[1], 2) + Math.pow(event.getAccelerationZ() - prevXYZ[2], 2));
                prevXYZ[0] = event.getAccelerationX();
                prevXYZ[1] = event.getAccelerationY();
                prevXYZ[2] = event.getAccelerationZ();
                if (mAcclReading > 0.01) {
                    mLastMove = System.currentTimeMillis();
                    mAcclMoved++;
                }
                mMostRecent[2] = mAcclMoved / mAcclChange;
            }
        }
    };

    /**/ private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
                int temp = event.getHeartRate();

                mCurrentReadings = new DenseMatrix64F(1, 1, true, Math.abs(mCalcHR));
                mKF2.predict();
                mKF2.update(mCurrentReadings, mIdentityMatrix);
                int tempHR = (int) Math.round(mKF2.getState().getData()[0]);

                double loss = Math.pow(tempHR - temp, 2);
                mLoss = (int) Math.round(loss);
                if (loss > 9) {
                    mCurrentReadings = new DenseMatrix64F(1, 1, true, Math.abs(temp));
                    mKF2.predict();
                    mKF2.update(mCurrentReadings, mIdentityMatrix);
                }

                long currentTime = System.currentTimeMillis();
                Log.d("level", String.format("%.2f, %.2f, %s, %d, %d, %.2f, %.2f", mValids, mDips, mHRInterval, temp, mStressMinutes, mValids / mDips, (mValids / mDips) / (temp / 60.0)));
                if (currentTime - mLastMinute >= 60000) {
                    if (mValids > 20) {
                        mMinutes++;
                        if (mDips == 0)
                            mDips = 1;
                        mSumAverage += mValids / mDips;
                    }
                    if (mValids / mDips > 20 && mValids > 20) {
                        if ((mDips == 0 && System.currentTimeMillis() - mLastMove > 15000) || mDips > 0)
                            mStressMinutes++;

                    }
                    mLastMinute = currentTime;
                    mValids = 0;
                    mDips = 0;
                    if(mMinutes > 0){
                    mMostRecent[0] = (double) mStressMinutes / mMinutes;
                    }
                    else {
                        mMostRecent[0] = 0;
                    }
                }

            }

        }
    };

    private class RRIntervalSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
                    if (hardwareVersion >= 20) {
                        if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                            client.getSensorManager().registerRRIntervalEventListener(mRRIntervalEventListener);
                            client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                            client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS128);
                        } else {
                            appendToUI("You have not given this application consent to access heart rate data yet."
                                    + " Please press the Heart Rate Consent button.\n");
                        }
                    } else {
                        appendToUI("The RR Interval mSensor is not supported with your Band version. Microsoft Band 2 is required.\n");
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

    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {
            try {
                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {

                                mKF = new KalmanFilterOperations();
                                mIdentityMatrix = CommonOps.identity(1);

                                DenseMatrix64F priorX = new DenseMatrix64F(2, 1, true, 1, 0);
                                DenseMatrix64F priorP = CommonOps.identity(2);

                                double[] f = {1, 0, 0, 1};
                                DenseMatrix64F F = new DenseMatrix64F(2, 2, true, f);
                                double[] t = {1 / 4.0, 1 / 4.0, 1 / 4.0, 1 / 4.0};
                                DenseMatrix64F Q = new DenseMatrix64F(2, 2, true, t);
                                DenseMatrix64F H = new DenseMatrix64F(1, 2);

                                H.set(0, 0, 1);

                              //  double[] t2 = {1 / 8.0, 3 / 8.0, 3 / 8.0, 1 / 8.0};
                              //  DenseMatrix64F Q2 = new DenseMatrix64F(2, 2, true, t2);
                                mKF.configure(F, Q, H);
                                mKF.setState(priorX, priorP);

                                mKF2 = new KalmanFilterOperations();

                                mKF2.configure(F, Q, H);
                                mKF2.setState(priorX, priorP);
                                new RRIntervalSubscriptionTask().execute();
                                startListening();
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

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }

    public void startListening() {
        if (SoundOutlierDetector.hasStarted()) {
            Context context = getApplicationContext();
            CharSequence text = "Already Listening!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        if (profile != null)
            Log.d("ObjectID", "ID: " + profile.getObjectId());

        mDetector = new SoundOutlierDetector();
        mSensor = new SoundMeter();
        mHandler = new Handler();
        mSensor.start();
        mDetector.start();
        mStates = new int[SoundOutlierDetector.getLength() - 1];
        mChanges = new int[SoundOutlierDetector.getLength() - 1];
        if (profile.has("max0")) {
            for (int i = 0; i < SoundOutlierDetector.getLength(); i++) {
                double max = profile.getDouble("max" + i);
                double min = profile.getDouble("max" + i);
                if (max >= 0) {
                    SoundOutlierDetector.setMax(max, i);
                }
                if (min >= 0) {
                    SoundOutlierDetector.setMin(min, i);
                }
            }
            for (int i = 0; i < mStates.length; i++) {
                int state = profile.getInt("state" + i);
                mStates[i] = state;
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
        mUps = 0;
        mDowns = 0;
        count = 0;
        Timer myTimer = new Timer();
        SoundTask myTimerTask = new SoundTask();

        myTimer.scheduleAtFixedRate(myTimerTask, 0, 2000);
    }

    private class SoundTask extends TimerTask {
        private int mTimesRun = 0;
        private int mLouder = 0;
        private int mQuieter = 0;

        @Override
        public void run() {
            mTimesRun++;
            double readings = 5;
            double total = 0;
            final double[] val = mSensor.getAmplitudeEMA();
            // Log.d("FirstValue", "" + val[0]);
            final double[][] ranges = mDetector.update(val[0]);
            count++;

            for (int i = 0; i < mStates.length; i++) {
                if (ranges[0][i] > ranges[0][i + 1] && ranges[1][i] > ranges[1][i + 1]) {

                    if (mStates[i] < 1) {
                        mStates[i] = mStates[i];
                    }
                    mChanges[i] = 4;//louder
                    mStates[i]++;
                } else if (ranges[0][i] < ranges[0][i + 1] && ranges[1][i] < ranges[1][i + 1]) {

                    if (mStates[i] < 1) {
                        mStates[i] = mStates[i];

                    }
                    mChanges[i] = 1;//quieter
                    mStates[i]++;
                } else {
                    if (mChanges[i] == 1 || mChanges[i] == 3) {
                        if (mChanges[i] == 1)
                            mStates[i] = 0;
                        mChanges[i] = 3;//getting louder
                    } else {
                        if (mChanges[i] == 4)
                            mStates[i] = 0;
                        mChanges[i] = 2;//getting quieter
                    }
                    mStates[i]++;
                }
            }

            switch (mChanges[0]) {
                case 1:
                    mQuieter++;
                    break;
                case 4:
                    mLouder++;
                    break;
            }

            if(mLouder > mQuieter)
            {
                mMostRecent[1] =  0.5 + (((double) mLouder + 1.0) / ( 2.0 * ((double) mLouder + mQuieter + 1.0)));
            }
            else
            {
                mMostRecent[1] =  0.5 - (((double) mQuieter + 1.0) / ( 2.0 * ((double) mLouder + mQuieter + 1.0)));
            }

            Log.d("Readings",String.format("stress: %.5f, noise: %.5f %d, move: %.5f", mMostRecent[0], mMostRecent[1], mChanges[0], mMostRecent[2]));
            final double[] update = mMostRecent;
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {

                                  mBarChart.setValues(update);

                              }
                          }

            );
        }

    }

}
