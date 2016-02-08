package com.example.grayapps.contextaware;
import java.util.HashMap;

/**
 * Created by Adam Gray on 2/7/16.
 */
public class CorrelationGraph
{
    HashMap<String, Node> mNodes;
    public CorrelationGraph()
    {
        mNodes = new HashMap<String, Node>(111);
    }

    public boolean insert(String id, Node n)
    {
        if(id == null || n == null || mNodes.containsKey(id))
        {
            return false;
        }

        mNodes.put(id, n);
        return true;
    }

    public Node getNode(String id)
    {
        return mNodes.get(id);
    }

    public boolean containsNode(String id)
    {
        return mNodes.containsKey(id);
    }

    public double makePrediction(String id)
    {
        if(id == null)
        {
            return -1;
        }
        Node n = mNodes.get(id);

        if(n ==  null)
        {
            System.out.println("It's null");
            return -1;
        }
        String[] parentIDs = n.getParents();
        Node[] parents = new Node[parentIDs.length];
        double[] values = new double[parentIDs.length];
        double total = 0;
        for(int i = 0; i < parentIDs.length; i++)
        {
            parents[i] = mNodes.get(parentIDs[i]);

            if(parents[i] == null)
                return -1;

            double loss = getLoss(n, parents[i]);

            values[i] = Math.exp(-loss);
            total += values[i];
        }

        double prediction = 0;
        for(int i = 0; i < values.length; i++)
        {
            prediction += parents[i].getFraction() * (values[i] / total);
        }
        System.out.println(id + " => " + prediction);
        return prediction;
    }

    private double getLoss(Node n, Node ancestor)
    {
        return Math.pow((n.getFraction() - ancestor.getFraction()), 2);
    }
}
