package com.example.grayapps.contextaware;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Adam Gray on 2/5/16.
 */
public class EventGraph
{
    private CorrelationGraph mGraph;
    private HashSet<String> mUpdatedNodes;

    public EventGraph()
    {
        mGraph = new CorrelationGraph();
    }

    public void addEvent(ArrayList<ArrayList<String>> params, int stressReading)
    {
        mUpdatedNodes = new HashSet<String>();
        for (int i = 0; i < params.size(); i++)
        {
            updateGraph(params.get(i), stressReading);
        }
    }

    public double[] predictEvent(ArrayList<ArrayList<String>> params)
    {
        double total[] = new double[params.size()];
        int count = 0;
        for (int i = 0; i < params.size(); i++)
        {
            StringBuilder id = new StringBuilder();
            for (int j = 0; j < params.get(i).size(); j++)
            {
                id.append(params.get(i).get(j));
                id.append('_');
            }
            if (!mGraph.containsNode(id.toString()))
            {
                updateGraph(params.get(i), 0);
                assert mGraph.containsNode(id.toString());
                //System.out.println(id.toString());
            }

            double val = mGraph.predict(id.toString());
            if (val > 0)
            {
                total[i] = val;
                count++;
            }
        }
        return total;
    }

    private void updateGraph(ArrayList<String> params, int stressReading)
    {

        for (int i = 0; i < (1 << params.size()); i++)
        {
            StringBuilder id = new StringBuilder();
            ArrayList<String> parents = new ArrayList<String>();
            int p = i;
            for (int j = 0; j < params.size(); j++)
            {
                if (p % 2 == 1)
                {
                    id.append(params.get(j));
                    id.append('_');
                    parents.add(params.get(j));

                }

                p /= 2;
            }
            if (mGraph.containsNode(id.toString()) && !mUpdatedNodes.contains(id.toString()))
            {
                Node n = mGraph.getNode(id.toString());
                n.update(stressReading);
                mUpdatedNodes.add(id.toString());
            }
            else if (!mGraph.containsNode(id.toString()))
            {

                if (id.toString() != null && id.toString().length() > 0)
                {
                    //   System.out.println(id.toString());
                    mGraph.insert(id.toString(), new Node(id.toString(), parents));
                    mUpdatedNodes.add(id.toString());
                    //mGraph.changeNodeNumerator(id.toString());
                }
            }
        }
    }

    public int size()
    {
        return mGraph.graphSize();
    }

    public void print(ArrayList<String[]> factors)
    {
        mGraph.print(factors);
    }

}