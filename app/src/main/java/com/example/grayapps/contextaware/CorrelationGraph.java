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

    public double makePrediction(String id)
    {
        //   System.out.println(id);
        assert id != null : "Node ID does not exist. " + id;
        Node n = mNodes.get(Node.getKey(id));
        assert n != null : id;

        if(!n.hasParents())
        {
            return n.getFraction();
        }

        int[] parentIDs = n.getParents();
        Node[] parents =  new Node[parentIDs.length];
        double[] values = new double[parentIDs.length];
        double[] factors = new double[parentIDs.length];
        double total = 0;
        double prediction = 0;
        for(int i = 0; i < parentIDs.length; i++)
        {
            parents[i] = mNodes.get(parentIDs[i]);
            assert parents[i] != null : parentIDs.length;
            if(parents[i].getDenominator() <= Node.minDenom())
            {
                if(!mPredictedVals.containsKey(parentIDs[i]))
                {
                    factors[i] = makePrediction(Node.keyString(parentIDs[i]));
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
            double loss = getLoss(n.getFraction(), factors[i], parents[i].getDenominator());
            values[i] = Math.exp(-loss);

            total += values[i];
        }

        for(int i = 0; i < values.length; i++)
        {
            prediction += (factors[i] * (values[i] / total));
        }

        return prediction;
    }


    private double getLoss(double nFraction, double pFraction, int pDenom)
    {
        return Math.pow((nFraction - pFraction), 2.0) / (double) pDenom;
    }

    public int graphSize()
    {
        return mNodes.size();
    }

    public double predict(String id)
    {
        mPredictedVals = new HashMap<Integer, Double>();
        double prediction = makePrediction(id);
        mPredictedVals = null;
        return prediction;
    }

}
