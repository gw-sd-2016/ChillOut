package com.example.grayapps.contextaware;

import java.util.ArrayList;

/**
 * Created by Adam Gray on 2/5/16.
 */
public class EventGraph
{
    CorrelationGraph mGraph;
    public EventGraph()
    {
        mGraph = new CorrelationGraph();
    }

    public void addEvent(ArrayList<ArrayList<String>> params, boolean stressReading)
    {
        for(int i = 0; i < params.size(); i++)
        {
            updateGraph(params.get(i), stressReading);
        }
    }

    public double predictEvent(ArrayList<ArrayList<String>> params)
    {
        double total = 0;
        int count = 0;
        for(int i = 0; i < params.size(); i++)
        {
            StringBuilder id = new StringBuilder();
            for(int j = 0; j < params.get(i).size(); j++)
            {
                id.append(params.get(i).get(j));
                id.append('_');
            }
            double val = mGraph.makePrediction(id.toString());
            if(val > 0)
            {
                total += val;
                count++;
            }
        }
        return total / count;
    }

    private void updateGraph(ArrayList<String> params, boolean stressReading)
    {
        for(int i = 0; i < (1 << params.size()) - 1; i++)
        {
            StringBuilder id = new StringBuilder();
            ArrayList<String> parents = new ArrayList<String>();
            int p = i;
            for(int j = 0; j < params.size(); j++)
            {
                if(p % 2 == 0)
                {
                    id.append(params.get(j));
                    id.append('_');
                    parents.add(params.get(j));
                }

                p /= 2;
            }
            if(mGraph.containsNode(id.toString()))
            {
                mGraph.getNode(id.toString()).update(stressReading);
            }
            else
            {
                mGraph.insert(id.toString(), new Node(parents));
            }
        }
    }
}