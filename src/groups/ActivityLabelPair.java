package groups;

import java.util.ArrayList;
import edu.cmu.lti.jawjaw.pobj.Word;

public class ActivityLabelPair implements Runnable{
	
	public ActivityLabel activity1;
	public ActivityLabel activity2;
	public ArrayList<WordPair> wordPairs;
	public Integer searchLevel;	
	
	public ArrayList<ArrayList<String>> commonHiperHolos;
	public ArrayList<ArrayList<String>> baseWordsCommonHiperHolos;
	public ArrayList<ArrayList<String>> baseWordsSimilarityCalc;
	public ArrayList<String> bestCommonHiperHolos;
	public Double avgSimilarity;
	public String action;
	
	public String context;
	public String directoryPath;
	
	public ActivityLabelPair(ActivityLabel act1,ActivityLabel act2, Integer searchLevel, String context) {
		// TODO Auto-generated constructor stub
				
		this.activity1 = act1;
		this.activity2 = act2;
		this.wordPairs = new ArrayList<WordPair>();
		this.searchLevel = searchLevel;
		this.context = context;
		
		this.commonHiperHolos = new ArrayList<ArrayList<String>>();
		this.baseWordsCommonHiperHolos = new ArrayList<ArrayList<String>>();
		this.baseWordsSimilarityCalc = new ArrayList<ArrayList<String>>();
		this.bestCommonHiperHolos = new ArrayList<String>();
	}
			
	public void setAction(String action){
		this.action= action;
	}
		
	public void setDisambiguationParameters(String context, String directoryPath){
		this.context = context;
		this.directoryPath = directoryPath;
	}
	
	@Override
	public void run(){
		if(this.action.equals("createWordPairs")){
			for(Word word1 : this.activity1.getWordsJAWJAW()){
				for(Word word2 : this.activity2.getWordsJAWJAW()){
					if(word1.getPos().equals(word2.getPos())){
						this.wordPairs.add(new WordPair(word1,word2, this.searchLevel, this.context));
					}
				}	
			}
		}
    }

	public void setPairSimilarityValue(){
		Integer d = this.wordPairs.size();
		Double sumSimilarityValues = 0.0;
		for(WordPair wp: this.wordPairs){
			sumSimilarityValues += wp.getSimilarityValue();
		}
		if(d>0){
			this.avgSimilarity = sumSimilarityValues / (d*2);
		} else {
			this.avgSimilarity = 0.0;
		}
	}
	
	public void displayWPSimilarityValues(){
		for(WordPair wp : this.wordPairs){
			wp.displaySimilarityValues();
		}
	}
	
	public void displayAvgSimilarity(){
		System.out.println(this.activity1.originalName+";"+this.activity2.originalName+";"+this.avgSimilarity);
	}
	
	
	public void displayAtividades(){
		System.out.println("["+this.activity1.getOriginalText()+"];["+this.activity2.getOriginalText()+"]");		
	}
	
	public void displayCommonHiperHolos(){
		System.out.println(this.commonHiperHolos);		
	}
	
	
}
