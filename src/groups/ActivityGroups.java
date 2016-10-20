package groups;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.*;

public class ActivityGroups {

	public ArrayList<String> activityGroupsOutput;
	
	public ActivityGroups(ArrayList<String> activityRelationsOutput, Double minimumThreshold) {
		
		DecimalFormat fmt = new DecimalFormat("#0.0");
				
		int i=0;
		
		BigDecimal minThreshold = new BigDecimal(0.0d);
			
		activityGroupsOutput = new ArrayList<String>();
		
		for(i=0;i<100;i++){
			
			minThreshold = minThreshold.add(BigDecimal.valueOf(minimumThreshold));
						
			SimpleWeightedGraph<String, DefaultWeightedEdge> graph = createGraphfromSemanticGrouper(activityRelationsOutput,minThreshold.doubleValue());
			
			//System.out.println("Graph: "+graph);
			//System.out.println("-------------");
			//System.out.println("Limit: "+minThreshold);
			//System.out.println("-------------");
					
			BronKerboschCliqueFinder<String, DefaultWeightedEdge> cf = new BronKerboschCliqueFinder<String, DefaultWeightedEdge>(graph);   
			
			Set<String> selectedGroup = null;
			
			//System.out.println("Selected groups:");
			
			while(graph.edgeSet().size()>0){
				
				Collection<Set<String>> cliques = cf.getAllMaximalCliques();
				HashMap<Set<String>, Double> cliqueValue = new HashMap<Set<String>, Double>();
		        
				for (Set<String> clique : cliques)   
		        {           	
		        	//System.out.print("Clique: "+clique+ " ");
		        	//System.out.print(CliqueWeightCalc(clique,graph));
		        	//System.out.println();
		        	cliqueValue.put(clique, CliqueWeightCalc(clique,graph));
		        }
		        
		        //Search for the group with the highest value in the graph 
		        ArrayList<Set<String>>selectedGroups = new ArrayList<Set<String>>();
		        Double MaxValue = 0.0;		        
		        for(Set<String> key: cliqueValue.keySet()){
		        	if(cliqueValue.get(key) > MaxValue){
		        		MaxValue = cliqueValue.get(key);
		        		selectedGroup = key;
		        	} 
			    }		        	        
				
		        //System.out.println(fmt.format(minThreshold)+";"+selectedGroup+";"+MaxValue);
				activityGroupsOutput.add(fmt.format(minThreshold).toString()+";"+selectedGroup.toString()+";"+MaxValue.toString());
				
				//store group in selection
		        selectedGroups.add(selectedGroup);
		        //remove group from original graph
		        for(String sVertex : selectedGroup){
					graph.removeVertex(sVertex);
				}
			}
		}
		
	}
	
	public ArrayList<String> getOutput() {
        return activityGroupsOutput;
    }
	
	private static Double CliqueWeightCalc(Set<String> clique, SimpleWeightedGraph<String, DefaultWeightedEdge> graph){
		Double cliqueWeight=0.0;
		int i=0,j=0;
		for(i=0;i<clique.size();i++){
			for(j=0;j<clique.size();j++){
				if(i<j){
					cliqueWeight += graph.getEdgeWeight(graph.getEdge(clique.toArray()[i].toString(),clique.toArray()[j].toString()));
				}
			}			
		}		
		return cliqueWeight;
	}
	
	private static SimpleWeightedGraph<String, DefaultWeightedEdge> createGraphfromSemanticGrouper(ArrayList<String> saidaBuscaRelacoes, double minThreshold){
		
		SimpleWeightedGraph<String, DefaultWeightedEdge> graph =
	            new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		for(String parRaw : saidaBuscaRelacoes){
			String[] parSplit = parRaw.split(";");
			double edgeWeight = Double.parseDouble(parSplit[2]);
			if(edgeWeight>=minThreshold){
				graph.addVertex(parSplit[0]);
			    graph.addVertex(parSplit[1]);
			    DefaultWeightedEdge e = graph.addEdge(parSplit[0], parSplit[1]);
			    graph.setEdgeWeight(e, edgeWeight);	
			}
		}
	
	     return graph;		
	}
}
	




