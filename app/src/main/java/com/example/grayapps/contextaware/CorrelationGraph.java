package com.example.grayapps.contextaware;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adam Gray on 2/7/16.
 */
public class CorrelationGraph
{
    private HashMap<Integer, Node> mNodes;
    private HashMap<Integer, Double> mPredictedVals;

    public CorrelationGraph()
    {
        mNodes = new HashMap<Integer, Node>(111);
    }

    public boolean insert(String id, Node n)
    {
        if (id == null || n == null || mNodes.containsKey(Node.getKey(id)))
        {
            return false;
        }

        mNodes.put(Node.getKey(id), n);
        return true;
    }


    public Node getNode(String id)
    {
        return mNodes.get(Node.getKey(id));
    }

    public boolean containsNode(String id)
    {
        return mNodes.containsKey(Node.getKey(id));
    }

    public double makePrediction(String id, boolean avgOrDiff)
    {
        /*
            avgOrDiff = true uses avg
            avgOrDiff = false uses diff
        */

        assert id != null : "Node ID does not exist. " + id;
        Node n = mNodes.get(Node.getKey(id));
        assert n != null : id;

        avgOrDiff = n.getDenominator() <= Node.minDenom();

        if (!n.hasParents())
        {
            // System.out.println(n.getFraction() + " " + id + " " + avgOrDiff + " " + n.getDenominator());
            return n.getFraction();
        }

        //        if(n.getDenominator() <= Node.minDenom())
        //        {
        int[] parentIDs = n.getParents();
        Node[] parents = new Node[parentIDs.length];
        double[] values = new double[parentIDs.length];
        double[] factors = new double[parentIDs.length];
        double total = 0;
        double prediction = 0;
        for (int i = 0; i < parentIDs.length; i++)
        {
            parents[i] = mNodes.get(parentIDs[i]);
            assert parents[i] != null : parentIDs.length;
            if (parents[i].getDenominator() <= Node.minDenom())
            {
                if (!mPredictedVals.containsKey(parentIDs[i]))
                {
                    factors[i] = makePrediction(Node.keyString(parentIDs[i]), true);
                    mPredictedVals.put(parentIDs[i], factors[i]);
                }
                else
                {
                    factors[i] = mPredictedVals.get(parentIDs[i]);
                }
            }
            else
            {
                factors[i] = parents[i].getFraction();
            }
            if(!avgOrDiff)
            {
                double loss = getLoss2(n.getFraction(), factors[i]);

                values[i] = 1 / Math.exp(Math.tan((Math.PI/2.0)*loss)/parents[i].getImpact());
                // System.out.println(values[i]);
                total += values[i];
            }
            else
            {
                values[i] = parents[i].getImpact();
            }
        }
        //  System.out.println();
        if(avgOrDiff)
        {
            total = factors.length;
        }

        for (int i = 0; i < values.length; i++)
        {
            parents[i].updateImpact(values[i] / total);
            prediction += (factors[i] * (values[i] / total));
            //  System.out.println((factors[i]));

        }

        System.out.println(prediction + " "  + id + " diff? " + avgOrDiff + " " + n.getDenominator());
        return prediction;
        //        }
        //        else
        //            return n.getFraction();
    }

    private double getLoss(Node n, Node ancestor)
    {
        return Math.pow((n.getFraction() - ancestor.getFraction()), 2.0);// / (double) ancestor.getDenominator();
    }

    private double getLoss2(double nFraction, double pFraction)
    {
        return nFraction - pFraction;//Math.pow(Math.abs(nFraction - pFraction), 2.0);
    }

    public void print(ArrayList<String[]> factors)
    {
        for (int i = 0; i < factors.size(); i++)
        {
            for (int j = 0; j < factors.get(i).length; j++)
            {
                if (mNodes.containsKey(Node.getKey(factors.get(i)[j] + '_')))
                    System.out.format("%s %d/%d = %.3f | ", factors.get(i)[j], getNode(factors.get(i)[j] + '_').getNumerator(), getNode(factors.get(i)[j] + '_').getDenominator(), getNode(factors.get(i)[j] + '_').getFraction() * getNode(factors.get(i)[j] + '_').getImpact() );
                if (j > 0 && j % 4 == 0)
                    System.out.println();
            }
            System.out.println();
        }
    }

    public int graphSize()
    {
        return mNodes.size();
    }

    public double predict(String id)
    {
        mPredictedVals = new HashMap<Integer, Double>();
        double prediction = makePrediction(id, false);
        mPredictedVals = null;
        System.out.println();
        return prediction;
    }

    public void changeNodeNumerator(String id)
    {
        Node n = mNodes.get(Node.getKey(id));
        double prediction = predict(id);
        int startingNumerator = (int) Math.round(10 * prediction);
        if (startingNumerator != 5)
        {
            System.out.println(startingNumerator);
            n.updateStartingNumerator(startingNumerator);
        }
    }

}
