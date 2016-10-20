package groups;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.*;

public class RunSemanticGroups
{
	
	public static void main(String[] args) throws Exception
	{
		final int MYTHREADS = 300;
		ExecutorService executor1 = Executors.newFixedThreadPool(MYTHREADS);
		 
		//activity label list	
		ArrayList<String> processActivitiesNames = new ArrayList<String>();
		
		/*
		if(args.length<3){
			System.out.println("Three arguments needed:");
			System.out.println("1) Path to directory of the jar file");
			System.out.println("2) look up levels in WordNet tree (Integer)");
			System.out.println("3) Minimum similarity threshold value to create groups (Double)");
			System.exit(0);
		}		
				
		String directoryPath = args[0];
		Integer level = Integer.parseInt(args[1]);
		Double minimumThreshold = Double.parseDouble(args[2]);
		*/
		
		// Mock args
		
		String directoryPath = "C:/Users/pedro/Desktop";
		Integer level = 1; //look up levels in WordNet tree
		Double minimumThreshold = 0.25;
		
		
		//read input file with label's list
		Scanner s = new Scanner(new File(directoryPath+"/labels.txt"));
		while (s.hasNextLine()){
			processActivitiesNames.add(s.nextLine());
		}
		s.close();
		
		System.out.println("Start:	" + new java.util.Date());

		ArrayList<ActivityLabel> ProcessActivities = new ArrayList<ActivityLabel>();
		
		/*
		System.out.println("*************POS TAGGING***************");
		System.out.println("Start POS TAGGING:	" + new java.util.Date());
		*/
		
		MaxentTagger tagger = new MaxentTagger("models/english-bidirectional-distsim.tagger");
		//MaxentTagger tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger");
		
		for(String activity : processActivitiesNames){
			//System.out.println(activity);
			ActivityLabel act = new ActivityLabel(activity, tagger);
			ProcessActivities.add(act);			
		}
		/*
		System.out.println("End POS TAGGING:	" + new java.util.Date());
		System.out.println("");
		*/
		
		//System.out.println("*************CONTEXT GENERATION***************");
		//Uses all words from all labels as context
		String processContext = getProcessContext(ProcessActivities);
		//System.out.println(processContext);
		
		
		//System.out.println("Start Pair Generation:	" + new java.util.Date());
		//combine all activity labels in pairs
		int i,j;
		ArrayList<ActivityLabelPair> pairs = new ArrayList<ActivityLabelPair>();
		for(i=0;i<ProcessActivities.size();i++){
			for(j=0;j<ProcessActivities.size();j++){
				if(i<j){
					ActivityLabelPair pair = new ActivityLabelPair(ProcessActivities.get(i),ProcessActivities.get(j), level, processContext);
					pairs.add(pair);
				}
			}
		}
		
		for(ActivityLabelPair pair : pairs){
			pair.setAction("createWordPairs");
			executor1.execute(pair);
		}
		executor1.shutdown();
		// Wait until all threads are finish
		while (!executor1.isTerminated()) {
 		}
		
		//System.out.println("End Pair Generation:	" + new java.util.Date());
		
		
		for(ActivityLabelPair pair : pairs){
			pair.setPairSimilarityValue();
		}
		
		/*
		for(ActivityLabelPair pair : pairs){
			pair.displayAvgSimilarity();
			pair.displayWPSimilarityValues();
			System.out.println("");
		}
		*/
		
		//System.out.println("");
		//System.out.println("Start Groups Proposed:	" + new java.util.Date());
		
		ArrayList<String> activityRelationsOutput = new ArrayList<String>();
		
		for(ActivityLabelPair pair : pairs){
			//pair.displayAvgSimilarity();
			activityRelationsOutput.add(pair.activity1.getOriginalText()+";"+pair.activity2.getOriginalText()+";"+pair.avgSimilarity);
		}
		
		ActivityGroups activityCluster = new ActivityGroups(activityRelationsOutput, minimumThreshold);
		
		PrintWriter writer = new PrintWriter(directoryPath+"/output.txt", "UTF-8");
		for(String out : activityCluster.getOutput()){
			writer.println(out);
			System.out.println(out);
		}
		writer.close();
		System.out.println("End:	" + new java.util.Date());
		
	}
	
	
	public static String deDup(String s) {
	    return new LinkedHashSet<String>(Arrays.asList(s.split(" "))).toString().replaceAll("(^\\[|\\]$)", "").replace(", ", " ");
	}	
	
	public static String getProcessContext(ArrayList<ActivityLabel> ProcessActivities){
		String processContext = "";
		for(ActivityLabel activity : ProcessActivities){
			processContext += activity.getActivityContext()+" "; 			
		}
		return deDup(processContext);		
	}
	
	
	
}