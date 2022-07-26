import java.util.*;
import java.io.*;

public class RunCorrelation {


    public static ArrayList<ArrayList<Integer>> getGroundTruth(String data_set, String delimiter, int gt_num) {

        String file_name = "Data/"+ data_set + "/graph.txt";
        HashMap<Integer, Integer> my_mapping = new HashMap<Integer, Integer>();

        try {
            BufferedReader myReader = new BufferedReader(new FileReader(file_name));

            String data = myReader.readLine();
            String[] split = data.split(" ");
            int num_pts = Integer.parseInt(split[0]);

            int cur_index = 0;
            while ((data = myReader.readLine()) != null) {

                split = data.split(delimiter);
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);

                // RELABEL
                if (!my_mapping.containsKey(x)) {
                    my_mapping.put(x, cur_index);
                    cur_index += 1;
                }
                if (!my_mapping.containsKey(y)) {
                    my_mapping.put(y, cur_index);
                    cur_index += 1;
                }

            }
            myReader.close();

            BufferedReader myReader2 = new BufferedReader(new FileReader("Data/" + data_set + "/ground_truth" + gt_num + ".txt"));

            //ArrayList<ArrayList<Integer>> my_clustering = new ArrayList<ArrayList<Integer>>();
            HashMap<Integer, ArrayList<Integer>> map_clustering = new HashMap<Integer, ArrayList<Integer>>();

            cur_index = 0;
            while ((data = myReader2.readLine()) != null) {
                // answers are ordered by question!!
                split = data.split(delimiter);
                int x = Integer.parseInt(split[0]);
                if (!map_clustering.containsKey(x)) {
                    ArrayList<Integer> cur_cluster = new ArrayList<Integer>();
                    cur_cluster.add(my_mapping.get(cur_index));
                    map_clustering.put(x, cur_cluster);
                } else {
                    map_clustering.get(x).add(my_mapping.get(cur_index));
                }

                cur_index += 1;

            }

            myReader2.close();
            ArrayList<ArrayList<Integer>> my_clustering = new ArrayList<ArrayList<Integer>>();
            for (Integer x : map_clustering.keySet()) {
                my_clustering.add(map_clustering.get(x));
            }

            return my_clustering;

          } catch (Exception e) {
            System.out.println("An error occurred while reading ground truth " + gt_num);
            // e.printStackTrace();
            return null;
          }


        // return null;
    }



    public static void main(String args[]){

        // ----- PARAMETERS -----
        String data_set = args[0];
        String delimiter = "\\s"; 
        int ROUNDS = 10;
        // ----------------------

        ArrayList<ArrayList<Integer>> prob_matrix = Helper.read_large_network_relabel("Data/"+ data_set + "/graph.txt", delimiter);
        ArrayList<ArrayList<Integer>> ground_truth1 = getGroundTruth(data_set, delimiter, 1);
        ArrayList<ArrayList<Integer>> ground_truth2 = getGroundTruth(data_set, delimiter, 2);
	ArrayList<ArrayList<Integer>> ground_truth3 = getGroundTruth(data_set, delimiter, 3);
	ArrayList<ArrayList<Integer>> ground_truth4 = getGroundTruth(data_set, delimiter, 4);
	ArrayList<ArrayList<Integer>> ground_truth5 = getGroundTruth(data_set, delimiter, 5);

        long pivot_disagreement1 = 0;
        long hybrid_disagreement1 = 0;
        long pivot_disagreement2 = 0;
        long hybrid_disagreement2 = 0;
        long pivot_disagreement3 = 0;
        long hybrid_disagreement3 = 0;
        long pivot_disagreement4 = 0;
        long hybrid_disagreement4 = 0;
        long pivot_disagreement5 = 0;
        long hybrid_disagreement5 = 0;

        double pKwikTimeTotal = 0;
        double hybridTimeTotal = 0;
	double hybrid2TimeTotal = 0;
        double localTimeTotal = 0;
        //double randTimeTotal = 0;

        double[] pKwikTimes = new double[ROUNDS];
        double[] hybridTimes = new double[ROUNDS];
        double[] hybrid2Times = new double[ROUNDS];
        double[] localTimes = new double[ROUNDS];
  
        long pKwikNumClusters = 0;
        long hybridNumClusters = 0;
	long hybrid2NumClusters = 0;
        long localNumClusters = 0;
        //long randNumClusters = 0;

        long largestPKwikCluster = 0;
        long largestHybridCluster = 0;
	long largestHybrid2Cluster = 0;
        long largestLocalCluster = 0;
        //long largestRandCluster = 0;

	long pKwikScores = 0;
	long hybridScores = 0;
	long hybrid2Scores = 0;
	long localScores = 0;
	//long randScores = 0;

        long[] pKwikScoresList = new long[ROUNDS];
        long[] hybridScoresList = new long[ROUNDS];
        long[] hybrid2ScoresList = new long[ROUNDS];
        long[] localScoresList = new long[ROUNDS];

        long pCost = 0;
        long hCost = 0;
	long h2Cost = 0;
        long lCost = 0;
        long rCost = 0;

        double pPrecision = 0;
        double hPrecision = 0;
        double h2Precision = 0;
        double lPrecision = 0;

        double[] pPrecisionList = new double[ROUNDS];
        double[] hPrecisionList = new double[ROUNDS];
        double[] h2PrecisionList = new double[ROUNDS];
        double[] lPrecisionList = new double[ROUNDS];

        double pRecall = 0;
        double hRecall = 0;
        double h2Recall = 0;
        double lRecall = 0;

        double[] pRecallList = new double[ROUNDS];
        double[] hRecallList = new double[ROUNDS];
        double[] h2RecallList = new double[ROUNDS];
        double[] lRecallList = new double[ROUNDS];

        System.out.println("Num Nodes: " + prob_matrix.size());
        System.out.println("Start");
        for (int j = 0; j < ROUNDS; j++) {

        long pKwikStart = System.currentTimeMillis();
        ArrayList<ArrayList<Integer>> result = PKwik.pKwikClustering(prob_matrix);
        long pKwikTime = System.currentTimeMillis() - pKwikStart;
        pKwikTimeTotal += (pKwikTime / 1000.0);
        pKwikTimes[j] = pKwikTime / 1000.0;
        // System.out.println("pKwik finished in: " + pKwikTime / 1000.0 + " s");

        long hybridStart = System.currentTimeMillis();
        ArrayList<ArrayList<Integer>> fix = Hybrid.large_graph_fix_clusters_local(result, prob_matrix, true);
        long hybridTime = System.currentTimeMillis() - hybridStart;
        hybridTimeTotal += (hybridTime / 1000.0);
        hybridTimes[j] = hybridTime / 1000.0;
        // System.out.println("Hybrid finished in: " + hybridTime / 1000.0 + " s");

        long hybrid2Start = System.currentTimeMillis();
        ArrayList<ArrayList<Integer>> fix2 = Hybrid.large_graph_fix_clusters_local(result, prob_matrix, false);
        long hybrid2Time = System.currentTimeMillis() - hybrid2Start;
        hybrid2TimeTotal += (hybrid2Time / 1000.0);
        hybrid2Times[j] = hybrid2Time / 1000.0;

        /*
        long randStart = System.currentTimeMillis();
        ArrayList<ArrayList<Integer>> rand_result = DNode.random_node_network(prob_matrix);
        long randTime = System.currentTimeMillis() - randStart;
        randTimeTotal += (randTime / 1000.0);
        */

        /*
        ArrayList<ArrayList<Integer>> result_copy = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < result.size(); i++) {
            ArrayList<Integer> new_cluster = new ArrayList<Integer>();
            new_cluster.addAll(result.get(i));
            result_copy.add(new_cluster);
        }*/

        long localStart = System.currentTimeMillis();
        ArrayList<ArrayList<Integer>> fix3 = DNode.local_search_network(result, prob_matrix, false);
        long localTime = System.currentTimeMillis() - localStart;
        localTimeTotal += (localTime / 1000.0);
        localTimes[j] = localTime / 1000.0;
	
        if (ground_truth1 != null) {
            pCost = Consensus.quickEditDist(result, ground_truth1);
            pivot_disagreement1 += pCost;
            hCost = Consensus.quickEditDist(fix, ground_truth1);
            hybrid_disagreement1 += hCost;
        }

	if (ground_truth2 != null) {
            pCost = Consensus.quickEditDist(result, ground_truth2);
            pivot_disagreement2 += pCost;
            hCost = Consensus.quickEditDist(fix, ground_truth2);
            hybrid_disagreement2 += hCost;
        }

        if (ground_truth3 != null) {
            pCost = Consensus.quickEditDist(result, ground_truth3);
            pivot_disagreement3 += pCost;
            hCost = Consensus.quickEditDist(fix, ground_truth3);
            hybrid_disagreement3 += hCost;
        }

        if (ground_truth4 != null) {
            pCost = Consensus.quickEditDist(result, ground_truth4);
            pivot_disagreement4 += pCost;
            hCost = Consensus.quickEditDist(fix, ground_truth4);
            hybrid_disagreement4 += hCost;
        }

        if (ground_truth5 != null) {
            pCost = Consensus.quickEditDist(result, ground_truth5);
            pivot_disagreement5 += pCost;
            hCost = Consensus.quickEditDist(fix, ground_truth5);
            hybrid_disagreement5 += hCost;
        }
        


        // System.out.println();
        pKwikNumClusters += result.size();
        // System.out.println("Num pKwik Clusters: " + result.size());
        hybridNumClusters += fix.size();
	hybrid2NumClusters += fix2.size();
        // System.out.println("Num Hybrid Clusters: " + fix.size());
        // System.out.println();
        localNumClusters += fix3.size();
        // randNumClusters += rand_result.size();

        int max_pkwik = 0;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).size() > max_pkwik)
                max_pkwik = result.get(i).size();
        }
        largestPKwikCluster += max_pkwik;
        // System.out.println("Largest pKwik cluster: " + max_pkwik);
        
        int max_hybrid = 0;
        for (int i = 0; i < fix.size(); i++) {
            if (fix.get(i).size() > max_hybrid)
                max_hybrid = fix.get(i).size();
        }
        largestHybridCluster += max_hybrid;
        // System.out.println("Largest Hybrid cluster: " + max_hybrid);
        
        int max_hybrid2 = 0;
        for (int i = 0; i < fix2.size(); i++) {
            if (fix2.get(i).size() > max_hybrid2)
                max_hybrid2 = fix2.get(i).size();
        }
        largestHybrid2Cluster += max_hybrid2;


        int max_local = 0;
        for (int i = 0; i < fix3.size(); i++) {
            if (fix3.get(i).size() > max_local)
                max_local = fix3.get(i).size();
        }
        largestLocalCluster += max_local;

        /*
        int max_rand = 0;
        for (int i = 0; i < rand_result.size(); i++) {
            if (rand_result.get(i).size() > max_rand)
                max_rand = rand_result.get(i).size();
        }
        largestRandCluster += max_rand;
        */

        pKwikStart = System.currentTimeMillis();
        int p_score = Helper.quick_edit_dist(result, prob_matrix);
        pKwikTime = System.currentTimeMillis() - pKwikStart;
        // System.out.println("pKwik Score finished in: " + pKwikTime / 1000.0 + " s");        
        // int score = Helper.large_graph_prob_edit_dist(result, prob_matrix);
        pKwikScores += p_score;
        pKwikScoresList[j] = p_score;
	// System.out.println("PKwik Score: " + p_score);
	
        
        hybridStart = System.currentTimeMillis();
        // int h_score = Helper.large_graph_prob_edit_dist_hash(fix, prob_matrix);
        int h_score = Helper.quick_edit_dist(fix, prob_matrix);
        hybridTime = System.currentTimeMillis() - hybridStart;
        // System.out.println("Hybrid Score finished in: " + hybridTime / 1000.0 + " s");
        hybridScores += h_score;
        hybridScoresList[j] = h_score;
	// System.out.println("Hybrid Score: " + h_score);

        hybrid2Start = System.currentTimeMillis();
        // int h_score = Helper.large_graph_prob_edit_dist_hash(fix, prob_matrix);
        int h2_score = Helper.quick_edit_dist(fix2, prob_matrix);
        hybrid2Time = System.currentTimeMillis() - hybrid2Start;
        // System.out.println("Hybrid Score finished in: " + hybridTime / 1000.0 + " s");
        hybrid2Scores += h2_score;
        hybrid2ScoresList[j] = h2_score;

        int l_score = Helper.quick_edit_dist(fix3, prob_matrix);
        localScores += l_score;
        localScoresList[j] = l_score;

        // int r_score = Helper.quick_edit_dist(rand_result, prob_matrix);
        // randScores += r_score;

        double[] pPrecisionRecall = Helper.get_precision_recall(result, prob_matrix);
        pPrecision += pPrecisionRecall[0];
        pPrecisionList[j] = pPrecisionRecall[0];
        pRecall += pPrecisionRecall[1];
        pRecallList[j] = pPrecisionRecall[1];
        /*double[] hPrecisionRecall = Helper.get_precision_recall(fix, prob_matrix);
        hPrecision += hPrecisionRecall[0];
        hPrecisionList[j] = hPrecisionRecall[0];
        hRecall += hPrecisionRecall[1];
        hRecallList[j] = hPrecisionRecall[1];
        double[] h2PrecisionRecall = Helper.get_precision_recall(fix2, prob_matrix);
        h2Precision += h2PrecisionRecall[0];
        h2PrecisionList[j] = h2PrecisionRecall[0];
        h2Recall += h2PrecisionRecall[1];
        h2RecallList[j] = h2PrecisionRecall[1];
        double[] lPrecisionRecall = Helper.get_precision_recall(fix3, prob_matrix);
        lPrecision += lPrecisionRecall[0];
        lPrecisionList[j] = lPrecisionRecall[0];
        lRecall += lPrecisionRecall[1];
        lRecallList[j] = lPrecisionRecall[1];
*/
        }
        System.out.println("Finish");
        System.out.println();
        System.out.println("pivot times: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(pKwikTimes[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("pivot scores: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(pKwikScoresList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("hybrid times: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(hybridTimes[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("hybrid scores: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(hybridScoresList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("hybrid2 times: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(hybrid2Times[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("hybrid2 scores: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(hybrid2ScoresList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("local times: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(localTimes[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("local scores: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(localScoresList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("Average pivot time: " + pKwikTimeTotal / ((double) ROUNDS));
        System.out.println("Average hybrid time: " + hybridTimeTotal / ((double) ROUNDS));
	System.out.println("Average hybrid2 time: " + hybrid2TimeTotal / ((double) ROUNDS));
        System.out.println("Average local time: " + localTimeTotal / ((double) ROUNDS));
        // System.out.println("Average rand time: " + randTimeTotal / ((double) ROUNDS));
        System.out.println();


	    System.out.println("Average pivot score: " + pKwikScores / ((double) ROUNDS));
	    System.out.println("Average hybrid score: " + hybridScores / ((double) ROUNDS));
        System.out.println("Percent Improvement: " + (pKwikScores - hybridScores) / ((double) pKwikScores) * 100.0);
	System.out.println("Average hybrid2 score: " + hybrid2Scores / ((double) ROUNDS));
        System.out.println("Percent Improvement: " + (pKwikScores - hybrid2Scores) / ((double) pKwikScores) * 100.0);
	    System.out.println("Average local score: " + localScores / ((double) ROUNDS));
        System.out.println("Percent Improvement: " + (pKwikScores - localScores) / ((double) pKwikScores) * 100.0); 
	//     System.out.println("Average rand score: " + randScores / ((double) ROUNDS));
        // System.out.println("Percent Improvement: " + (pKwikScores - randScores) / ((double) pKwikScores) * 100.0); 
        System.out.println();


        System.out.println("Average pivot num clusters: " + pKwikNumClusters / ((double) ROUNDS));
        System.out.println("Average hybrid num clusters: " + hybridNumClusters / ((double) ROUNDS));
	System.out.println("Average hybrid2 num clusters: " + hybrid2NumClusters / ((double) ROUNDS));
        System.out.println("Average local num clusters: " + localNumClusters / ((double) ROUNDS));
        // System.out.println("Average rand num clusters: " + randNumClusters / ((double) ROUNDS));
        System.out.println();
        System.out.println("Average pivot max cluster size: " + largestPKwikCluster / ((double) ROUNDS));
        System.out.println("Average hybrid max cluster size: " + largestHybridCluster / ((double) ROUNDS));
	System.out.println("Average hybrid2 max cluster size: " + largestHybrid2Cluster / ((double) ROUNDS));
        System.out.println("Average local max cluster size: " + largestLocalCluster / ((double) ROUNDS));
        // System.out.println("Average rand max cluster size: " + largestRandCluster / ((double) ROUNDS));
        System.out.println();

        System.out.println("pivot precision: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(pPrecisionList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("pivot recall: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(pRecallList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("hybrid precision: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(hPrecisionList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("hybrid recall: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(hRecallList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("hybrid2 precision: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(h2PrecisionList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("hybrid2 recall: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(h2RecallList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("local precision: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(lPrecisionList[i] + " ");
        System.out.println();
        System.out.println();
        System.out.println("local recall: ");
        for (int i = 0; i < ROUNDS; i++)
            System.out.print(lRecallList[i] + " ");
        System.out.println();
        System.out.println();

	System.out.println("Average pivot precision: " + pPrecision / ((double) ROUNDS));
	    System.out.println("Average hybrid precision: " + hPrecision / ((double) ROUNDS));
	System.out.println("Average hybrid2 precision: " + h2Precision / ((double) ROUNDS));
	    System.out.println("Average local precision: " + lPrecision / ((double) ROUNDS));
        System.out.println();

	    System.out.println("Average pivot recall: " + pRecall / ((double) ROUNDS));
	    System.out.println("Average hybrid recall: " + hRecall / ((double) ROUNDS));
	    System.out.println("Average hybrid2 recall: " + h2Recall / ((double) ROUNDS));
	    System.out.println("Average local recall: " + lRecall / ((double) ROUNDS));
        // System.out.println();

        /*
        if (ground_truth1 != null) {
            System.out.println("Average pivot Disagreement 1: " + pivot_disagreement1 / ((double) ROUNDS));
            System.out.println("Average hybrid Disagreement 1: " + hybrid_disagreement1 / ((double) ROUNDS));
            System.out.println("Percent Improvement: " + (pivot_disagreement1 - hybrid_disagreement1) / ((double) pivot_disagreement1) * 100.0); 
            System.out.println();
        }

        if (ground_truth2 != null) {
            System.out.println("Average pivot Disagreement 2: " + pivot_disagreement2 / ((double) ROUNDS));
            System.out.println("Average hybrid Disagreement 2: " + hybrid_disagreement2 / ((double) ROUNDS));
            System.out.println();
        }

        if (ground_truth3 != null) {
            System.out.println("Average pivot Disagreement 3: " + pivot_disagreement3 / ((double) ROUNDS));
            System.out.println("Average hybrid Disagreement 3: " + hybrid_disagreement3 / ((double) ROUNDS));
            System.out.println();
        }

        if (ground_truth4 != null) {
            System.out.println("Average pivot Disagreement 4: " + pivot_disagreement4 / ((double) ROUNDS));
            System.out.println("Average hybrid Disagreement 4: " + hybrid_disagreement4 / ((double) ROUNDS));
            System.out.println();
        }

        if (ground_truth5 != null) {
            System.out.println("Average pivot Disagreement 5: " + pivot_disagreement5 / ((double) ROUNDS));
            System.out.println("Average hybrid Disagreement 5: " + hybrid_disagreement5 / ((double) ROUNDS));
        }
        */
	
       
    }

    
}
