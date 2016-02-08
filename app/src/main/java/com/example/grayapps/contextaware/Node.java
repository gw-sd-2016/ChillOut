package com.example.grayapps.contextaware;
import java.util.ArrayList;

/**
 * Created by Adam Gray on 2/5/16.
 */
public class Node
{
    int mFraction;
    String[] mParents;

    public Node(ArrayList<String> parentIDs)
    {
        mFraction = (1 << 16) | 2; // first 16 bits are 1, next 16 bits are 2
        if (parentIDs != null && parentIDs.size() > 0)
        {
            mParents = new String[parentIDs.size()];
            for (int i = 0; i < parentIDs.size(); i++)
            {
                StringBuilder parent = new StringBuilder();
                for(int j = 0; j < parentIDs.size(); j++)
                {
                    if(j != i)
                    {
                        parent.append(parentIDs.get(j));
                        parent.append('_');
                    }
                }
                mParents[i] = parent.toString();
            }
        }
    }

    public void update(boolean stressReading)
    {
        int denom = getDenominator();
        setDenominator(++denom);

        if(stressReading)
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

    public String[] getParents()
    {
        return mParents;
    }
}
