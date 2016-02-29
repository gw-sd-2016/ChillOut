package com.example.grayapps.contextaware;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by AGray on 2/28/16.
 */
public class EventSimulator
{
    public static String[] events = {"Meeting", "Study", "Haircut", "Phone Call", "Test", "Watch TV", "Play Game", "Chores", "Errands", "Laundry", "Exercise"};
    public static String[] locations = {"Home", "Work", "Store 1", "Store 2", "Store 3", "Store 4", "Gym"};
    public static String[] tOFd = {"Morning", "Afternoon", "Evening", "Night"};
    public static String[] dOFw = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    public static String[] noise = {"High", "Low"};
    public static String[] move = {"Light", "Heavy"};
    public static String[][] people = {{"Mom", "Dad", "Brother", "Sister"}, {"Michael", "Jim", "Pam", "Dwight"}, {"Ross", "Rachel", "Chandler", "Monica"}, {"Leslie", "Tom", "Ron", "April"}};

    public static void main(String args[])
    {
        double[] totals = new double[4];
        double runs = 100;
        for (int z = 0; z < runs; z++)
        {
            EventGraph testGraph = new EventGraph();
            EventGraph fullGraph = new EventGraph();
            Random rand = new Random();
            double[] count = new double[7];
            int testNum = 500;
            for (int i = 0; i < testNum; i++)
            {
                ArrayList<ArrayList<String>> params = new ArrayList<ArrayList<String>>();
                ArrayList<ArrayList<String>> testing = new ArrayList<ArrayList<String>>(1);

                String title = events[rand.nextInt(10)];
                String loc = locations[rand.nextInt(6)];
                String timeOFday = tOFd[rand.nextInt(4)];
                String dayOFweek = dOFw[rand.nextInt(7)];
                String noiseLevel = noise[rand.nextInt(2)];
                String moveLevel = move[rand.nextInt(2)];
                String[] peeps = new String[4];

                if (rand.nextInt(10) > 7)
                {
                    peeps[0] = people[rand.nextInt(4)][0];
                    peeps[1] = people[rand.nextInt(4)][1];
                    peeps[2] = people[rand.nextInt(4)][2];
                    peeps[3] = people[rand.nextInt(4)][3];
                }
                else
                {
                    peeps = people[rand.nextInt(4)];
                }
                ArrayList<String> test = new ArrayList<String>(10);

                ArrayList<String> one = new ArrayList<String>(4);
                one.add(title);
                one.add(loc);
                one.add(timeOFday);
                one.add(dayOFweek);

                test.add(title);
                test.add(loc);
                test.add(timeOFday);
                test.add(dayOFweek);
                test.add(noiseLevel);
                test.add(moveLevel);
                test.add(peeps[0]);
                test.add(peeps[1]);
                test.add(peeps[2]);
                test.add(peeps[3]);

                testing.add(test);

                ArrayList<String> two = new ArrayList<String>(4);
                two.add(title);
                two.add(loc);
                two.add(noiseLevel);
                two.add(moveLevel);

                ArrayList<String> three = new ArrayList<String>();
                three.add(timeOFday);
                three.add(dayOFweek);
                three.add(noiseLevel);
                three.add(moveLevel);

                ArrayList<String> four = new ArrayList<String>();
                four.add(title);
                for (int j = 0; j < peeps.length; j++)
                {
                    four.add(peeps[j]);
                }

                ArrayList<String> five = new ArrayList<String>();
                five.add(noiseLevel);
                five.add(moveLevel);
                ArrayList<String> six = new ArrayList<String>();
                for (int j = 0; j < peeps.length; j++)
                {
                    five.add(peeps[j]);
                    six.add(peeps[j]);
                }

                ArrayList<String> seven = new ArrayList<String>();
                seven.add(loc);
                for (int j = 0; j < peeps.length; j++)
                {
                    seven.add(peeps[j]);
                }
                int random = 3;
                int stress = -1;
                boolean sLevel = rand.nextInt(10) < random;
                int remove;


                params.add(one);
                params.add(two);
                params.add(three);


                int numPeople = 4;
                while (rand.nextInt(5) > 1 && numPeople > 0)
                {
                    remove = 1 + rand.nextInt(numPeople--);

                    four.remove(remove);
                    five.remove(remove + 1);
                    six.remove(remove - 1);
                    seven.remove(remove);
                }
                params.add(four);
                params.add(five);
                params.add(seven);

                if (print(testing) == 1)
                    sLevel = rand.nextInt(10) > random - 1;

                if (sLevel)
                {
                    stress = 1;
                }

                if (i < testNum / 5)
                {
                    testGraph.addEvent(params, stress);
                }
                else
                {
                    ArrayList<String[]> factors = new ArrayList<String[]>();
                    factors.add(events);
                    factors.add(locations);
                    factors.add(tOFd);
                    factors.add(dOFw);
                    factors.add(noise);
                    factors.add(move);
                    factors.add(people[0]);
                    factors.add(people[1]);
                    factors.add(people[2]);
                    factors.add(people[3]);
                    params.remove(1);
                    params.remove(1);
                    params.remove(2);
                    double predicted[] = testGraph.predictEvent(params);
                    double prediction = 0;

                    double sum = 0;
                    for (int j = 0; j < predicted.length; j++)
                    {
                        sum += predicted[j];
                    }

                    prediction = sum / predicted.length;
                    double range = 0.0;
                    int printParams = print(testing);
                    if (sLevel)
                    {
                        count[6]++;
                        if (prediction > 0.5 + range)
                            count[5]++;
                    }
                    if (prediction > 0.5 + range)
                    {
                        count[1]++;
                        if (printParams == 1)
                        {
                            count[0]++;
                        }
                        if (sLevel)
                        {
                            count[2]++;
                        }
                    }
                    else
                    {
                        if (printParams == 0)
                        {
                            count[3]++;
                        }
                        count[4]++;
                    }
                    testGraph.addEvent(params, stress);

                    if (i == testNum - 1)
                    {
                        ArrayList<String[]> factors2 = new ArrayList<String[]>();
                        for (int q = 0; q < params.size(); q++)
                        {
                            String[] arr = new String[params.get(q).size()];
                            factors2.add(params.get(q).toArray(arr));
                        }
                    }
                }

            }
            totals[0] += count[5] / count[6];
            totals[1] += count[2] / count[1];
            totals[2] += count[0] / count[1];
            totals[3] += count[3] / count[4];
        }
        System.out.format("%.5f %.5f %.5f %.5f%n", totals[0] / runs, totals[1] / runs, totals[2] / runs, totals[3] / runs);
    }

    public static int print(ArrayList<ArrayList<String>> params)
    {
        int val = 0;
        for (int i = 0; i < params.size(); i++)
        {
            if ((params.get(i).contains("Monday") && params.get(i).contains("Morning")) || (params.get(i).contains("Mom") && params.get(i).contains("Dad")) || (params.get(i).contains("Ron") && !params.get(i).contains("Leslie")) || (params.get(i).contains("Work") && (params.get(i).contains("Jim") || params.get(i).contains("Dwight"))) || (params.get(i).contains("Test") && params.get(i).contains("Store 1")) || params.get(i).contains("Chores"))
                val = 1;
        }
        return val;
    }
}
