package com.example.grayapps.contextaware;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adam Gray on 2/7/16.
 */
public class CorrelationGraph
{
    private HashMap<Integer, Node> mNodes;
    public CorrelationGraph()
    {
        mNodes = new HashMap<Integer, Node>(111);
    }

    public boolean insert(String id, Node n)
    {
        if(id == null || n == null || mNodes.containsKey(Node.getKey(id)))
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
        //  System.out.println("Size: " + mNodes.size());
        if(id == null)
        {
            return -1;
        }
        Node n = mNodes.get(Node.getKey(id));

        if(n == null)
            return 0;

        if(n.getDenominator() >= 11 && n.getParents().length < 3)
        {
            // System.out.println("It's null");
            return n.getFraction();
        }
        int[] parentIDs = n.getParents();
        Node[] parents = new Node[parentIDs.length];
        double[] values = new double[parentIDs.length];
        double total = 0;
        for(int i = 0; i < parentIDs.length; i++)
        {
            parents[i] = mNodes.get(parentIDs[i]);

            if(parents[i] == null)
                return -1;

            double loss = getLoss(n, parents[i]);

            values[i] = Math.exp(-loss) * n.getDenominator();
            total += values[i];


            // System.out.format("%s <== %.5f | %d/%d%n", Node.keyString(parentIDs[i]), parents[i].getFraction(), parents[i].getNumerator(), parents[i].getDenominator());
        }


        double prediction = 0;
        for(int i = 0; i < values.length; i++)
        {
           /**/ double pastPrediction = makePrediction(Node.keyString(parentIDs[i])) / 2.0;
            if(pastPrediction > 0)
                prediction += (pastPrediction + (1 * parents[i].getFraction()/ 2.0)) * (values[i] / total);
            else
                prediction += parents[i].getFraction() * (values[i] / total);
        }
        // System.out.format("%s => %.5f : %.5f%n" , id, prediction, n.getFraction());


        if(n.getDenominator() < 20)
            return (prediction + n.getFraction()) / 2.0;
        else
            return n.getFraction();
    }

    private double getLoss(Node n, Node ancestor)
    {
        return Math.pow((n.getFraction() - ancestor.getFraction()), 2);
    }

    public void print(ArrayList<String[]> factors)
    {
        for(int i = 0; i < factors.size(); i++)
        {
            for(int j = 0; j < factors.get(i).length; j++)
            {
                if (mNodes.containsKey(Node.getKey(factors.get(i)[j] + '_')))
                    System.out.format("%s %d/%d = %.3f | ", factors.get(i)[j], getNode(factors.get(i)[j] + '_').getNumerator(), getNode(factors.get(i)[j] + '_').getDenominator(), getNode(factors.get(i)[j] + '_').getFraction());
                if(j > 0 && j % 4 == 0)
                    System.out.println();
            }System.out.println();
        }
    }

    public int graphSize()
    {
        return mNodes.size();
    }
}
