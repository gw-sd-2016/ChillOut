package com.example.grayapps.contextaware;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adam Gray on 2/5/16.
 */
public class Node
{
    private int mFraction;
    private int[] mParents;
    private static HashMap<String, Integer> mNodeIDs = new HashMap<String, Integer>();
    private static HashMap<Integer, String> mStringIDs = new HashMap<Integer, String>();
    private static int mNewNodeID = 0;
    private static int mMinDenom = 2;
    private double mImpact;

    public Node(String id, ArrayList<String> parentIDs)
    {
        mFraction = (1 << 16) | mMinDenom; // first 16 bits are 1, next 16 bits are 2
        // mFraction = 1;
        if (!mNodeIDs.containsKey(id))
        {
            assert getKey(id) == Integer.MAX_VALUE;
            mNodeIDs.put(id, mNewNodeID);
            mStringIDs.put(mNewNodeID, id);
            mNewNodeID++;
        }
        if (parentIDs != null && parentIDs.size() > 0)
        {
            mParents = new int[parentIDs.size()];
            for (int i = 0; i < parentIDs.size(); i++)
            {
                StringBuilder parent = new StringBuilder();
                for (int j = 0; j < parentIDs.size(); j++)
                {
                    if (j != i)
                    {
                        parent.append(parentIDs.get(j));
                        parent.append('_');
                    }
                }
                if (getKey(parent.toString()) == Integer.MAX_VALUE)
                {
                    mStringIDs.put(mNewNodeID, parent.toString());
                    mParents[i] = mNewNodeID;
                    mNodeIDs.put(parent.toString(), mNewNodeID);
                    mNewNodeID++;
                }
                else
                {
                    mParents[i] = mNodeIDs.get(parent.toString());
                }
            }
        }
        else
        {
            mParents = new int[0];
        }

        mImpact = 1;
    }

    public void update(int stressReading)
    {
        if(stressReading == 0)
            return;

        int denom = getDenominator();

        setDenominator(++denom);

        if (stressReading > 0)
        {
            int num = getNumerator();
            setNumerator(++num);
        }
    }

    private void setDenominator(int denominator)
    {
        mFraction &= ((1 << 16) - 1) << 16;
        mFraction |= denominator;
    }

    public void updateStartingNumerator(int n)
    {
        setNumerator(n);
    }

    public int getDenominator()
    {
        return mFraction & ((1 << 16) - 1);
    }

    private void setNumerator(int numerator)
    {
        mFraction &= ((1 << 16) - 1);
        mFraction |= numerator << 16;
    }

    public int getNumerator()
    {
        return mFraction >> 16;
    }

    public double getFraction()
    {
        return (double)getNumerator() / getDenominator();
    }

    public int[] getParents()
    {
        return mParents;
    }

    public static int getKey(String idString)
    {
        if (mNodeIDs.containsKey(idString))
        {
            return mNodeIDs.get(idString);
        }

        return Integer.MAX_VALUE;
    }

    public boolean hasParents()
    {
        return mParents != null && mParents.length > 1;
    }

    public static String keyString(int key)
    {
        return mStringIDs.get(key);
    }

    public static int minDenom()
    {
        return mMinDenom;
    }

    public void updateImpact(double weight)
    {
        if(mImpact > 0)
        {
            int size = mParents.length;
            double fraction = mImpact / (size + 1);
            mImpact -= fraction;
            mImpact += weight;
            //System.out.println(fraction - weight);
        }
    }

    public double getImpact()
    {
        return mImpact;
    }
}
