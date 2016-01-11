package com.example.grayapps.contextaware;

import android.media.MediaRecorder;
import android.util.Log;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.io.IOException;

/**
 * Created by AGray on 10/14/15.
 */
public class SoundMeter {

    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA;
    private KalmanFilter mKF;
    private DenseMatrix64F mCurrentReadings;
    private DenseMatrix64F mIdentityMatrix;
    private static double mRecentlyRead;

    public SoundMeter(){
        mEMA = 0.0002;
    }

    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();

            Log.d("ESTIMATED MOVING",  "" + mEMA);

            mKF = new KalmanFilterOperations();
            mIdentityMatrix = CommonOps.identity(1);

            DenseMatrix64F priorX = new DenseMatrix64F(2, 1, true, getAmplitude(), 0);
            DenseMatrix64F priorP = CommonOps.identity(2);

            double[] f = {1, 0, 0, 1};
            DenseMatrix64F F = new DenseMatrix64F(2, 2, true, f);
            double[] t = {1/4.0, 1/4.0, 1/4.0, 1/4.0};
            DenseMatrix64F Q = new DenseMatrix64F(2, 2, true, t);
            DenseMatrix64F H = new DenseMatrix64F(1, 2);
            for (int i = 0; i < 1; i++)
            {
                H.set(i, i, 1.0);
            }

            mKF.configure(F, Q, H);
            mKF.setState(priorX, priorP);

        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
        {
            mRecentlyRead = mRecorder.getMaxAmplitude();
            return (10 * mRecentlyRead/51805.5336);
        }
        else
            return 0;

    }

    public double[] getAmplitudeEMA() {
        double amp = getAmplitude();
        Log.d("AMP", "" + amp);
        mEMA = (EMA_FILTER * amp) + ((1.0 - EMA_FILTER) * mEMA);
        Log.d("EMA", "" + mEMA);
        //Log.d("DB", "" + db);
        mCurrentReadings = new DenseMatrix64F(1, 1, true, mEMA);

        mKF.predict();
        mKF.update(mCurrentReadings, mIdentityMatrix);

        return mKF.getState().getData();
    }

    public static int getRecentlyRead()
    {
        return (int) Math.round(mRecentlyRead);
    }
}



