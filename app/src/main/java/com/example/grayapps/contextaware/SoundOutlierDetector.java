package com.example.grayapps.contextaware;

/**
 * Created by AGray on 10/14/15.
 */
public class SoundOutlierDetector
{
    private static int mRange;
    private static double[] mLookback;
    private static double[] mAvgMax;
    private static double[] mAvgMin;
    private static int mLocation;
    private static double[] mWeight = {1/2.0, 1/12.0, 1/36.0, 1/180.0, 1/1260.0};
    private static boolean mStarted = false;

    public void start()
    {
        mStarted = true;
        mRange = 5;
        mLocation = 0;
        mAvgMax = new double[mWeight.length];
        mAvgMin = new double[mWeight.length];
        for(int i = 0; i < mWeight.length ; i++)
        {
            mAvgMax[i] = 0;
            mAvgMin[i] = 0;
        }
        mLookback = new double[mRange];
    }

    public double[][] update(double val)
    {
        int countMult = 0;
        int count = 1;
        for(int i = 1; i < mRange; i++)
        {
            if(val > mLookback[(mLocation + i) % mRange])
            {
                count++;
            }
            if(val > 2*mLookback[(mLocation + i) % mRange])
            {
                countMult++;
            }
        }

        if(count == mRange)
        {
            if(countMult == mRange - 1)
            {
                mLookback[mLocation] = val;
                mLocation = (mLocation + 1) % mRange;
                double[][] range = new double[2][mRange];
                range[0] = mAvgMin;
                range[1] = mAvgMax;
                return range;
            }

            for(int i = 0; i < mWeight.length; i++)
            {
                mAvgMax[i] = ((1-mWeight[i])*mAvgMax[i]) + (mWeight[i] * val);
                if(mAvgMax[i] < mAvgMin[i])
                {
                    mAvgMin[i] = mAvgMax[i];
                }
            }
        }
        else if (count == 1)
        {
            for(int i = 0; i < mWeight.length; i++)
            {
                mAvgMin[i] = ((1-mWeight[i])*mAvgMin[i]) + (mWeight[i] * val);
                if(mAvgMin[i] > mAvgMax[i])
                {
                    mAvgMax[i] = mAvgMin[i];
                }
            }
        }


        mLookback[mLocation] = val;
        mLocation = (mLocation + 1) % mRange;
        double[][] range = new double[2][mWeight.length];
        range[0] = mAvgMin;
        range[1] = mAvgMax;
        return range;
    }

    public static void setMax(double max, int i)
    {
        mAvgMax[i] = max;
    }

    public static double getMax(int i)
    {
        return  mAvgMax[i];
    }

    public static void setMin(double min, int i)
    {
        mAvgMin[i] = min;
    }

    public static double getMin(int i)
    {
        return  mAvgMin[i];
    }

    public static void setLocationInLookback(int i, double val)
    {
        mLookback[i] = val;
    }

    public static double[] getLookback()
    {
        return mLookback;
    }

    public static void setLocation(int loc)
    {
        mLocation = loc;
    }

    public static int getLocation()
    {
        return mLocation;
    }

    public static boolean hasStarted()
    {
        return mStarted;
    }

    public static int getRange()
    {
        return mRange;
    }

    public static int getLength()
    {
       return mWeight.length;
    }

    public static double[] getWeights(){ return mWeight;}
}
