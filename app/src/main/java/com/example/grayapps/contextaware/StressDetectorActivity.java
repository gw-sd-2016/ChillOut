package com.example.grayapps.contextaware;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.lang.ref.WeakReference;


public class StressDetectorActivity extends AppCompatActivity {

    private BandClient client = null;
    private Button btnStart, btnConsent;
    private TextView txtStatus;
    private static double[] mLastRead;
    private static int mPos;
    private static DenseMatrix64F mIdentityMatrix;
    private static KalmanFilter mKF;
    private static DenseMatrix64F mCurrentReadings;
    private static DenseMatrix64F mIdentityMatrix2;
    private static KalmanFilter mKF2;
    private static DenseMatrix64F mCurrentReadings2;
    private static com.example.grayapps.contextaware.StressOutlierDetector mStressDetector;
    private static double mMean;
    private static double mPrevReading;
    private static double[][] mReadings;
    private static int mPrevCount;
    private static int mNumtoRead;
    private static int mNumPos;
    private static double mSum;
    private static String mTemp = "";
    private static String mGSR = "";
    private static String mRRInt = "";
    private static String mHRInterval = "";
    private static int mLineNum;
    private static int mPrevLine;
    private static boolean mWasTrue;
    private static double mCalcHR;
    private static boolean mIsStressed;
    private static long mStressedTime;

    private BandRRIntervalEventListener mRRIntervalEventListener = new BandRRIntervalEventListener() {
        @Override
        public void onBandRRIntervalChanged(final BandRRIntervalEvent event) {
            if (event != null) {
                double temp = event.getInterval();

                
                String val = String.format("%.3f", 10 * event.getInterval());
                mHRInterval = val;
               // mRRInt = val;
               // mSum += event.getInterval();
               // mNumPos++;

                //if (mNumPos == mNumtoRead) {
                   // double temp = event.getInterval();// mSum / mNumtoRead;
                 //   mNumPos = 0;
                 //   mSum = 0;

                    mCurrentReadings = new DenseMatrix64F(1, 1, true, Math.abs(temp));
                    mPrevReading = temp;
                    mKF.predict();
                    mKF.update(mCurrentReadings, mIdentityMatrix);
                 //   mMean *= mPrevCount;
                 //   mMean -= mLastRead[mPos];
                   temp = mKF.getState().getData()[0];
                  mCalcHR = (60.0/temp);
                 //   mMean += mLastRead[mPos];
                 //   mMean /= mPrevCount;

                   // Log.d("MEAN",String.format("%.3f", mMean));

                 //   double std = 0;

                 //   for (int i = 0; i < mPrevCount; i++)
                 //       std += Math.pow(mLastRead[i] - mMean, 2);

                //    std /= (mPrevCount - 1);

                    //Log.d("STD1", String.format("%.3f",std));
                 //   std = Math.pow(std, 0.5);

                //mCurrentReadings2 = new DenseMatrix64F(1, 1, true, Math.abs(std));
                //mKF2.predict();
                //mKF2.update(mCurrentReadings2, mIdentityMatrix);

               // std = mKF2.getState().getData()[0];

               // Log.d("point2", String.format("%.3f",mKF2.getState().getData()[1]));
                  //  Log.d("STD2", String.format("%.3f",std));
                  //  val = String.format("%.3f", std * 1000);
                 //   mReadings = mStressDetector.update(2000 * std);
                 //   mRRInt = String.format("%.3f", 2000 * std);
                 //   Log.d("RRInt", String.format("%.3f", mLastRead[mPos]));
                   // Log.d("Min1", "" + 100 * mReadings[0][0]);
                    //  Log.d("Changes", "" + changes[0]);
                  //  Log.d("Max1", "" + 100 * mReadings[1][0]);
                  //  Log.d("Min2", "" + 100 * mReadings[0][1]);
                  //  Log.d("Max2", "" + 100 * mReadings[1][1]);
                  //  Log.d("Min3", "" + 100 * mReadings[0][2]);
                  //  Log.d("Max3", "" + 100 * mReadings[1][2]);
                  //  Log.d("Min4", "" + 100 * mReadings[0][3]);
                  //  Log.d("Max4", "" + 100 * mReadings[1][3]);
                  //  Log.d("Min5", "" + 100 * mReadings[0][4]);
                 // Log.d("Max5", "" + 100 * mReadings[1][4]);
                    //final String writeToScreen = val;
                  /*  runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          BarFragment.setValues(mReadings);
                                          //txtStatus.setText(writeToScreen);
                                      }
                                  }

                    );*/


                   // mPos += 1;
                   // mPos %= mPrevCount;
               // }
            }
        }
    };

   /**/ private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
               // Log.d("HeartRate",String.format("Heart Rate = %d beats per minute"
               //         + "Quality = %s", event.getHeartRate(), event.getQuality()));
                int temp = event.getHeartRate();
                Log.d("Readings", String.format("%d,%s,%s,%s,%s", temp, mRRInt, mHRInterval, mGSR, mTemp));


                  mCurrentReadings = new DenseMatrix64F(1, 1, true, Math.abs(mCalcHR));
                 //  mPrevReading = mCalcHR;
                   mKF2.predict();
                   mKF2.update(mCurrentReadings, mIdentityMatrix);
                 //  mMean *= mPrevCount;
                 //   mMean -= mLastRead[mPos];
                 //  mLastRead[mPos] = mKF.getState().getData()[0];
                int tempHR = (int) Math.round(mKF2.getState().getData()[0]);

                double loss = Math.pow(tempHR - temp, 2);
                if(loss > 100)
                    loss = 100;
                else if (loss > 64)
                {
                    tempHR = (int)Math.round((tempHR + temp)/2.0);
                }

                mSum -= mLastRead[mPos];
                mLastRead[mPos] = loss;
                mSum += loss;

                double acrcy = mSum / Math.pow(100,2);
                Log.d("Accuracy", String.format("%.3f %d %d %.3f", acrcy, tempHR, temp, loss));
                if(acrcy < 1/3.0)
                {
                    if(!mIsStressed)
                    {
                        mIsStressed = true;
                        mStressedTime = System.currentTimeMillis();
                    }
                    String time = DateUtils.formatElapsedTime((System.currentTimeMillis() - mStressedTime)/1000);
                    appendToUI("Stressed for " + time);
                    Log.d("StressStatus", "Stressed for " + time);
                }
                else
                {
                    mIsStressed = false;
                    appendToUI("Not Stressed");
                    Log.d("StressStatus", "Not Stressed");
                }

                mPos++;
                mPos %= mPrevCount;

                mLineNum++;
            }

        }
    };

    private BandSkinTemperatureEventListener mSkinTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(final BandSkinTemperatureEvent event) {
            if (event != null) {
                String val = String.format("%.3f", (event.getTemperature() * (9.0/5.0)) + 32);
                mTemp = val;
                //Log.d("Temperature", val);
            }
        }
    };
    private BandGsrEventListener mGsrEventListener = new BandGsrEventListener() {
        @Override
        public void onBandGsrChanged(final BandGsrEvent event) {
            if (event != null) {
                String val = String.format("%.3f", event.getResistance()/100.0);
               // appendToUI(String.format("Resistance = %s kOhms\n", val));
                mGSR = val;
               // Log.d("GSR", val);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stress_detector);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new BarFragment())
                .commit();
        mStressDetector = new com.example.grayapps.contextaware.StressOutlierDetector();
        mStressDetector.start();
        mMean = 0;
        mPos = 0;
        mWasTrue = true;
        mNumPos = 0;
        mPrevReading = 0;
        mPrevCount = 100;
        mNumtoRead = 1;
        mSum = 0;
        mLineNum = 1;
        mPrevLine = 0;
        mCalcHR = 0;
        mIsStressed = false;
        mLastRead = new double[mPrevCount];
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        btnStart = (Button) findViewById(R.id.listening);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtStatus.setText("");
                new RRIntervalSubscriptionTask().execute();
            }
        });

        final WeakReference<Activity> reference = new WeakReference<Activity>(this);

        btnConsent = (Button) findViewById(R.id.btnConsent);
        btnConsent.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                new HeartRateConsentTask().execute(reference);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtStatus.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
      /*  if (client != null) {
            try {
             //   client.getSensorManager().unregisterRRIntervalEventListener(mRRIntervalEventListener);
               // client.getSensorManager().unregisterGsrEventListener(mGsrEventListener);
               // client.getSensorManager().unregisterSkinTemperatureEventListener(mSkinTemperatureEventListener);
            } catch (BandIOException e) {
                appendToUI(e.getMessage());
            }
        }*/
    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }

    private class RRIntervalSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
                    if (hardwareVersion >= 20) {
                        if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                            client.getSensorManager().registerRRIntervalEventListener(mRRIntervalEventListener);
                            client.getSensorManager().registerGsrEventListener(mGsrEventListener);
                            client.getSensorManager().registerSkinTemperatureEventListener(mSkinTemperatureEventListener);
                            client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                        } else {
                            appendToUI("You have not given this application consent to access heart rate data yet."
                                    + " Please press the Heart Rate Consent button.\n");
                        }
                    } else {
                        appendToUI("The RR Interval sensor is not supported with your Band version. Microsoft Band 2 is required.\n");
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
                                for (int i = 0; i < 1; i++) {
                                    H.set(i, i, 1.0);
                                }

                                double[] t2 = {1 / 8.0, 3 / 8.0, 3 / 8.0, 1 / 8.0};
                                DenseMatrix64F Q2 = new DenseMatrix64F(2, 2, true, t2);
                                mKF.configure(F, Q, H);
                                mKF.setState(priorX, priorP);

                                mKF2 = new KalmanFilterOperations();

                                mKF2.configure(F, Q, H);
                                mKF2.setState(priorX, priorP);
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
                txtStatus.setText(string);
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

    public void newCard(View view)
    {
        Log.d("NEWCARD", String.format("%d to %d was %b",mPrevLine, mLineNum, mWasTrue));
        mPrevLine = mLineNum;
    }
    public void wasTrue(View view)
    {
        mWasTrue = true;
    }

    public void wasFalse(View view)
    {
        mWasTrue = false;
    }

}