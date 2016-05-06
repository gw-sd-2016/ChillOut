package com.example.grayapps.contextaware;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
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
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.SampleRate;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class DataRecordingService extends Service
{

    static final public String STRESS_RESULT = "com.example.grayapps.contextaware.DataRecordingService.UI_UPDATE";
    static final public String STRESS_START = "com.example.grayapps.contextaware.DataRecordingService.START_FOREGROUND";
    static final public String STRESS_PAUSE = "com.example.grayapps.contextaware.DataRecordingService.PAUSE_FOREGROUND";
    static final public String STRESS_END = "com.example.grayapps.contextaware.DataRecordingService.END_FOREGROUND";
    static final public String STRESS_RESUME = "com.example.grayapps.contextaware.DataRecordingService.RESUME_FOREGROUND";
    static final public int START_RECORDING = 77;
    static final public int STOP_RECORDING = 99;

    private LocalBroadcastManager mBroadcaster;
    private static BandClient client = null;
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
    private static int mStates[];
    private static int[] mChanges;
    private static Handler mHandler;
    private static SoundMeter mSensor;
    private static SoundOutlierDetector mDetector;
    private static double mUps;
    private static double mDowns;
    private static int count;
    private static double mAcclMoved;
    private static double mAcclChange;
    private static double[] mMostRecent;
    private static double mNumStressBits;
    private static double mRRReadings;
    private static double[][] mRanges;
    private static long mSteps;
    private static long mStepDifference;
    private static int mAverageHR;
    private static int mHRreadingCount;
    private static int mBreaths;
    private static Context mContext;
    private static String mTimerText = "0";
    private static Timer mTimer;
    private int mStartId;
    private static NotificationCompat.Builder mNotificationBuilder;
    private static NotificationManager mNotificationManager;


    public DataRecordingService() {
    }

    class DataRecordingServiceHandler extends Handler
    {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case STOP_RECORDING:
                    stopRecording(true, "");
                    break;
                case START_RECORDING:
                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new DataRecordingServiceHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        mBroadcaster = LocalBroadcastManager.getInstance(this);
        Log.d("RecordingService", "Triggered " + mStartId);
        if (intent.getAction().equals(STRESS_START))
        {

            mStartId = startId;
            Intent pauseIntent = new Intent(this, DataRecordingService.class);
            pauseIntent.setAction(STRESS_PAUSE);
            PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

            Intent resultIntent = new Intent(this, LiveResultsActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_group_2)
                            .setContentTitle("Breathe is running")
                            .setContentText("Current stress level")
                            .setColor(Color.parseColor(getResources().getString(R.color.colorStress)))
                            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", pausePendingIntent);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            mNotificationBuilder.setContentIntent(pendingIntent);
            mNotificationBuilder.setContentIntent(resultPendingIntent);
            //.setAutoCancel(true);

            startForeground(1234, mNotificationBuilder.build());
            setup();
        } else if (intent.getAction().equals(STRESS_PAUSE))
        {
            stopRecording(false, "");

        } else if (intent.getAction().equals(STRESS_END))
        {
            Log.d("EndRecording","YUP");
            stopRecording(true, intent.getStringExtra("eventId"));
        }
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (SoundOutlierDetector.hasStarted())
        {
            SharedPreferences.Editor edit = getSharedPreferences("userData", MODE_PRIVATE).edit();
            for (int i = 0; i < SoundOutlierDetector.getLength(); i++)
            {
                edit.putFloat("max" + i, (float) SoundOutlierDetector.getMax(i));
                edit.putFloat("min" + i, (float) SoundOutlierDetector.getMin(i));
            }
            double[] lookback = SoundOutlierDetector.getLookback();
            for (int i = 0; i < SoundOutlierDetector.getRange(); i++)
            {
                edit.putFloat("lookback" + i, (float) lookback[i]);
            }
            edit.commit();
        }

        super.onDestroy();
    }

    private void setup() {
        SharedPreferences.Editor editor = getSharedPreferences("userData", MODE_PRIVATE).edit();

        mContext = getApplicationContext();
        mBreaths = 0;
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
        mMostRecent = new double[4];
        mNumStressBits = 0;
        mRRReadings = 0;
        mSteps = 0;
        mStepDifference = 0;
        mAverageHR = 0;
        mHRreadingCount = 0;

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

        mKF.configure(F, Q, H);
        mKF.setState(priorX, priorP);

        mKF2 = new KalmanFilterOperations();

        mKF2.configure(F, Q, H);
        mKF2.setState(priorX, priorP);
        new RRIntervalSubscriptionTask().execute();
        startListening();
    }


    private void startListening() {

        mDetector = new SoundOutlierDetector();
        mSensor = new SoundMeter();
        mHandler = new Handler();
        mSensor.start();
        mDetector.start();
        mStates = new int[SoundOutlierDetector.getLength() - 1];
        mChanges = new int[SoundOutlierDetector.getLength() - 1];
        SharedPreferences prefs = mContext.getSharedPreferences("userData", MODE_PRIVATE);
        if (prefs.contains("max0"))
        {
            for (int i = 0; i < SoundOutlierDetector.getLength(); i++)
            {
                double max = prefs.getFloat("max" + i, -1);
                double min = prefs.getFloat("min" + i, -1);
                if (max >= 0)
                {
                    SoundOutlierDetector.setMax(max, i);
                }
                if (min >= 0)
                {
                    SoundOutlierDetector.setMin(min, i);
                }
            }

            double[] lookback = new double[SoundOutlierDetector.getRange()];
            for (int i = 0; i < SoundOutlierDetector.getRange(); i++)
                lookback[i] = prefs.getFloat("lookback" + i, 0);

            for (int i = 0; i < SoundOutlierDetector.getRange(); i++)
            {
                if (lookback[i] >= 0)
                {
                    SoundOutlierDetector.setLocationInLookback(i, lookback[i]);
                }
            }
        }
        mUps = 0;
        mDowns = 0;
        count = 0;
        mTimer = new Timer();
        SoundTask myTimerTask = new SoundTask();

        mTimer.scheduleAtFixedRate(myTimerTask, 0, 2000);
    }

    private class SoundTask extends TimerTask
    {
        private int mTimesRun = 0;
        private int mLouder = 0;
        private int mQuieter = 0;
        private double[] mWeights = SoundOutlierDetector.getWeights();

        @Override
        public void run() {
            mTimesRun++;
            double[] val = mSensor.getAmplitudeEMA();
            double[][] ranges = mDetector.update(val[0]);
            count++;

            for (int i = 0; i < mStates.length; i++)
            {
                if (ranges[0][i] > ranges[0][i + 1] && ranges[1][i] > ranges[1][i + 1])
                {

                    if (mStates[i] < 1)
                    {
                        mStates[i] = mStates[i];
                    }
                    mChanges[i] = 4;//louder
                    mStates[i]++;
                } else if (ranges[0][i] < ranges[0][i + 1] && ranges[1][i] < ranges[1][i + 1])
                {

                    if (mStates[i] < 1)
                    {
                        mStates[i] = mStates[i];

                    }
                    mChanges[i] = 1;//quieter
                    mStates[i]++;
                } else
                {
                    if (mChanges[i] == 1 || mChanges[i] == 3)
                    {
                        if (mChanges[i] == 1)
                            mStates[i] = 0;
                        mChanges[i] = 3;//getting louder
                    } else
                    {
                        if (mChanges[i] == 4)
                            mStates[i] = 0;
                        mChanges[i] = 2;//getting quieter
                    }
                    mStates[i]++;
                }
            }

            for (int i = 0; i < mChanges.length; i++)
            {
                switch (mChanges[i])
                {
                    case 1:
                        mQuieter += 200 * mWeights[i];
                        break;
                    case 4:
                        mLouder += 200 * mWeights[i];
                        break;
                }
            }

            mMostRecent[2] = (((double) mLouder + 1.0) / (((double) mLouder + mQuieter + 1.0)));

            mRanges = ranges;
            if (SoundOutlierDetector.hasStarted())
            {
                SharedPreferences.Editor edit = getSharedPreferences("userData", MODE_PRIVATE).edit();
                for (int i = 0; i < SoundOutlierDetector.getLength(); i++)
                {
                    edit.putFloat("max" + i, (float) SoundOutlierDetector.getMax(i));
                    edit.putFloat("min" + i, (float) SoundOutlierDetector.getMin(i));
                }
                double[] lookback = SoundOutlierDetector.getLookback();
                for (int i = 0; i < SoundOutlierDetector.getRange(); i++)
                {
                    edit.putFloat("lookback" + i, (float) lookback[i]);
                }
                edit.commit();
            }
            Log.d("Readings", String.format("stress: %.5f, noise: %.5f %d, move: %.5f", mMostRecent[1], mMostRecent[2], mChanges[0], mMostRecent[3]));
        }

    }

    private BandRRIntervalEventListener mRRIntervalEventListener = new BandRRIntervalEventListener()
    {
        @Override
        public void onBandRRIntervalChanged(final BandRRIntervalEvent event) {
            if (event != null)
            {
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
                if (!mDipping && prevRR[2] > prevRR[3])
                {
                    mDipping = true;
                    mStartDip = prevRR[2];
                } else if (mDipping && prevRR[2] < prevRR[3])
                {
                    if (mStartDip / prevRR[2] >= 1.05 && mStartDip / prevRR[2] <= 2)
                    {
                        isDip = true;
                    }
                    mDipping = false;
                    mStartDip = 0;
                }

                long currentTime = System.currentTimeMillis();
                isDip &= prevRR[1] < prevRR[0];
                boolean isValid = currentTime - mLastMove > 1000;
                isValid |= mLoss < 17;
                mRRReadings++;
                if (100 * Math.abs((prevRR[3] / prevRR[2]) - 1) <= 2.0)
                {
                    mNumStressBits++;
                }
                if (isValid)
                {

                    mValids++;
                    if (isDip)
                    {
                        ++mBreaths;
                        mDips++;
                    }

                }
            }
        }
    };

    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener()
    {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null)
            {
                mAcclChange++;
                mAcclReading = 100 * (Math.pow(event.getAccelerationX() - prevXYZ[0], 2) +
                        Math.pow(event.getAccelerationY() - prevXYZ[1], 2) + Math.pow(event.getAccelerationZ() - prevXYZ[2], 2));
                prevXYZ[0] = event.getAccelerationX();
                prevXYZ[1] = event.getAccelerationY();
                prevXYZ[2] = event.getAccelerationZ();
                if (mAcclReading > 0.01)
                {

                    mLastMove = System.currentTimeMillis();
                    if (mAcclReading > 5.0)
                    {
                        mAcclMoved++;
                        Log.d("movement", String.format("move %.3f", mAcclReading));
                    }
                }
                mMostRecent[3] = (mAcclMoved + (3 * mStepDifference)) / mAcclChange;
            }
        }
    };

    private BandPedometerEventListener mStepEventListener = new BandPedometerEventListener()
    {
        @Override
        public void onBandPedometerChanged(BandPedometerEvent event) {
            if (event != null)
            {
                if (mSteps == 0)
                {
                    mSteps = event.getTotalSteps();
                } else
                {
                    if (mStepDifference < event.getTotalSteps() - mSteps)
                    {
                        mStepDifference = event.getTotalSteps() - mSteps;
                    }
                }

            }
        }
    };

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener()
    {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null)
            {
                int temp = event.getHeartRate();
                mAverageHR += temp;
                mHRreadingCount++;
                mCurrentReadings = new DenseMatrix64F(1, 1, true, Math.abs(mCalcHR));
                mKF2.predict();
                mKF2.update(mCurrentReadings, mIdentityMatrix);
                int tempHR = (int) Math.round(mKF2.getState().getData()[0]);

                double loss = Math.pow(tempHR - temp, 2);
                mLoss = (int) Math.round(loss);
                if (loss > 9)
                {
                    mCurrentReadings = new DenseMatrix64F(1, 1, true, Math.abs(temp));
                    mKF2.predict();
                    mKF2.update(mCurrentReadings, mIdentityMatrix);
                }

                final long currentTime = System.currentTimeMillis();
                final int temp2 = temp;
                Log.d("level", String.format("%.2f, %.2f, %s, %d, %d, %.2f, %.2f", mValids, mDips, mHRInterval, temp2, mStressMinutes, mValids / mDips, (mValids / mDips) * (mNumStressBits / mRRReadings)));
                // Log.d("level", String.format("%.2f, %.2f, %s, %d, %d, %.2f, %.2f", mValids, mDips, mHRInterval, temp, mStressMinutes, mValids / mDips, (mValids / mDips) / (temp / 60.0)));
                if (currentTime - mLastMinute > 59500)
                {
                    if (mValids / ((double) mAverageHR / mHRreadingCount) > 1 / 3.0)
                    {
                        mMinutes++;
                        if (mDips == 0)
                            mDips = 1;
                        mSumAverage += (mValids / mDips) * (mNumStressBits / mRRReadings);
                        if ((mValids / mDips) * (mNumStressBits / mRRReadings) > 20)
                        {
                            mStressMinutes++;
                        }
                    }

                    mLastMinute = currentTime;
                    mValids = 0;
                    mDips = 0;
                    if (mMinutes > 0)
                    {
                        mMostRecent[1] = (double) mStressMinutes / mMinutes;
                    } else
                    {
                        mMostRecent[1] = 0;
                    }
                    mNumStressBits = 0;
                    mRRReadings = 0;
                    mAverageHR = 0;
                    mHRreadingCount = 0;

                    if(mMinutes > 0)
                    mNotificationBuilder.setContentText(String.format("Current stress level: %.0f%%", (double) 100 * mStressMinutes / mMinutes));
                    else
                        mNotificationBuilder.setContentText(String.format("Current stress level: 0%%"));
                    mNotificationManager.notify(1234, mNotificationBuilder.build());
                }

                if (((mValids / mDips) * (mNumStressBits / mRRReadings)) > 0 && ((mValids / mDips) * (mNumStressBits / mRRReadings)) < 100)
                {
                    mMostRecent[0] = Math.min(((mValids / mDips) * (mNumStressBits / mRRReadings)) / 20.0, 1.0);
                } else
                {
                    mMostRecent[0] = 0;
                }
                long timerVal = (long) Math.floor((currentTime - mLastMinute) / 1000.0);
                mTimerText = String.valueOf(timerVal);
                updateUI();
            }
        }
    };

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null)
        {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0)
            {
                //   appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState())
        {
            return true;
        }

        //  appendToUI("0 breaths detected");
        return ConnectionState.CONNECTED == client.connect().await();
    }

    private class RRIntervalSubscriptionTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            try
            {
                if (getConnectedBandClient())
                {
                    int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
                    if (hardwareVersion >= 20)
                    {
                        if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED)
                        {
                            client.getSensorManager().registerRRIntervalEventListener(mRRIntervalEventListener);
                            client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                            client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS128);
                            client.getSensorManager().registerPedometerEventListener(mStepEventListener);
                        } else
                        {
                            //  appendToUI("You have not given this application consent to access heart rate data yet."
                            //      + " Please press the Heart Rate Consent button.\n");
                        }
                    } else
                    {
                        //   appendToUI("The RR Interval mSensor is not supported with your Band version. Microsoft Band 2 is required.\n");
                    }
                } else
                {
                    //appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e)
            {
                String exceptionMessage = "";
                switch (e.getErrorType())
                {
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
                //appendToUI(exceptionMessage);

            } catch (Exception e)
            {
                //appendToUI(e.getMessage());
            }
            return null;
        }
    }

    private void updateUI() {
        Intent intent = new Intent(STRESS_RESULT);
        intent.putExtra("graphUpdate", mMostRecent);
        intent.putExtra("breathUpdate", mBreaths);
        intent.putExtra("timerUpdate", mTimerText);

        mBroadcaster.sendBroadcast(intent);
    }

    private int stopRecording(boolean getResults, String eventId)
    {
        try
        {
            if (getConnectedBandClient())
            {
                client.getSensorManager().unregisterRRIntervalEventListener(mRRIntervalEventListener);
                client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
                client.getSensorManager().unregisterAccelerometerEventListener(mAccelerometerEventListener);
                client.getSensorManager().unregisterPedometerEventListener(mStepEventListener);
            }
        } catch (InterruptedException e)
        {
        } catch (BandException e)
        {
        }
        mSensor.stop();
        mTimer.cancel();
        Intent broadcastIntent = new Intent(STRESS_PAUSE);
        mBroadcaster.sendBroadcastSync(broadcastIntent);

        if (getResults)
        {
            updateEventDetails(eventId);
        }
        else
        {
            stopForeground(true);
            stopSelf();
        }
        return -1;
    }

    private void updateEventDetails(String id)
    {
        SharedPreferences savedData = mContext.getSharedPreferences("userData", Context.MODE_PRIVATE);
        String objectID = savedData.getString("parseEventMapID", "ID Not Available");
        final Gson converter = new Gson();
        if (!objectID.equals("ID Not Available"))
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("CalendarEvents");
            query.fromLocalDatastore();
            final String eventId = id;
            query.getInBackground(objectID, new GetCallback<ParseObject>()
            {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null)
                    {
                        JSONObject eventMap = object.getJSONObject("EventMapWrapper");
                        try
                        {
                            CalendarEventRecordingTrigger cEvent = converter.fromJson(eventMap.getString(eventId), CalendarEventRecordingTrigger.class);
                            cEvent.setMovementLevel(mMostRecent[3]);
                            cEvent.setNoiseLevel(mMostRecent[2]);
                            cEvent.setStressLevel(mMostRecent[1]);
                            cEvent.addToGraph();
                            eventMap.put(eventId, converter.toJson(cEvent));
                            object.put("EventMapWrapper", eventMap);
                            object.pinInBackground(new SaveCallback()
                            {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null)
                                    {
                                        Log.d("Content", String.format("Saved %.3f %.3f %.3f", mMostRecent[1], mMostRecent[2], mMostRecent[3]));

                                    } else
                                    {
                                        Log.d("Content", "Not Saved");
                                    }
                                    stopForeground(true);
                                    stopSelf();
                                }
                            });
                        } catch (org.json.JSONException j)
                        {

                        }

                    } else
                    {
                        Log.d("Event", "Not found");
                    }
                }
            });
        }
    }

    public static boolean isRunning() {
        return SoundOutlierDetector.hasStarted();
    }

}
