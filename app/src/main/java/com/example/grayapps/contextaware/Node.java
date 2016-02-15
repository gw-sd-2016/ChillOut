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

    public Node(String id, ArrayList<String> parentIDs)
    {
        mFraction = (2 << 16) | 4; // first 16 bits are 1, next 16 bits are 2
        // mFraction = 1;
        if(!mNodeIDs.containsKey(id))
        {
            mNodeIDs.put(id, mNewNodeID++);
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
                if(!mStringIDs.containsKey(getKey(parent.toString())))
                {
                    mStringIDs.put(mNewNodeID, parent.toString());
                }

                if (!mNodeIDs.containsKey(parent.toString()))
                {
                    mParents[i] = mNewNodeID;
                    mNodeIDs.put(parent.toString(), mNewNodeID++);
                    mStringIDs.put(mNewNodeID - 1, parent.toString());
                }
                else
                {
                    mParents[i] = mNodeIDs.get(parent.toString());
                }

            }
        }
    }

    public void update(boolean stressReading)
    {
        int denom = getDenominator();
        int cap = 10;
        // if(denom < cap)

        setDenominator(++denom);

        if (stressReading)
        {
            int num = getNumerator();
            setNumerator(++num);
        }

      /*  else
        {
            int val = (int) Math.round((cap/2) * getFraction());
            setNumerator(val);
            setDenominator(cap/2);
        }*/
    }

    private void setDenominator(int denominator)
    {
        mFraction &= ((1 << 16) - 1) << 16;
        mFraction |= denominator;
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
        return (double) getNumerator() / getDenominator();
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

    public static String keyString(int key)
    {
        return mStringIDs.get(key);
    }
}
