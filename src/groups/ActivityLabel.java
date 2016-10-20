package groups;

import java.util.*;

import edu.cmu.lti.jawjaw.db.WordDAO;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Word;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class ActivityLabel {

	//public String rawText;
    public String[] words;
    public String context;
    public String originalName;
    public ArrayList<Word> wordsJAWJAW;
    
    //public ActivityLabel(String activity,String rawActivity) {
    public ActivityLabel(String activity, MaxentTagger tagger) {	
    	originalName = activity;
    	
    	wordsJAWJAW = new ArrayList<Word>();
    	
    	String activityLemma = "";
    	String activitySWR = "";
    	
    	Lemmatizer nlp = new Lemmatizer();
		List<String> lemmas = nlp.lemmatize(activity);
		for(String lemma : lemmas){
			activityLemma = activityLemma.trim() +" "+ lemma; 
		}
		
    	stopWordRemoval swr = new stopWordRemoval();
		activitySWR = swr.removeStopWords(activityLemma);
		
		String tagged = tagger.tagString(activitySWR);
		
		
		tagged = tagged.replace("_","#");
		//simplified tagsets from (https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html)
		
		tagged = tagged.replace("_","#");
		tagged = tagged.replace("#VBD","#v");
		tagged = tagged.replace("#VBG","#v");
		tagged = tagged.replace("#VBN","#v");
		tagged = tagged.replace("#VBP","#v");
		tagged = tagged.replace("#VB","#v");
		
		tagged = tagged.replace("#NNPS","#n");
		tagged = tagged.replace("#NN","#n");
		tagged = tagged.replace("#NNS","#n");
		tagged = tagged.replace("#NNP","#n");
		
		//must remove all non verbs and nouns
		String taggedTokens[] = tagged.split(" ");
		String taggedNounsAndVerbs = "";
		for(String taggedToken : taggedTokens){
			if(taggedToken.split("#")[1].equals("n") || taggedToken.split("#")[1].equals("v")){
				taggedNounsAndVerbs = taggedNounsAndVerbs +" "+taggedToken;
			}
		}
		
		words = taggedNounsAndVerbs.trim().split(" ");
		
		for(String word : words){
			if(!word.isEmpty()){
				List<Word> wordsFound = WordDAO.findWordsByLemmaAndPos(word.split("#")[0], POS.valueOf(word.split("#")[1]));
	    		if(wordsFound.size()>0) {//Exclude words not found in Wordnet
	    			wordsJAWJAW.add(wordsFound.get(0));
	    		}
			}
    	}
    	
    	//Remove tags and format context String
    	context = taggedNounsAndVerbs;
		context = context.replace("#v","");
    	context = context.replace("#n","");
 
    }
	
    
    public ArrayList<Word> getWordsJAWJAW(){
    	return this.wordsJAWJAW;
    }
    
    public String getOriginalText() {
        return originalName;
    }
     
    public String getActivityContext() {
        return context;
    }
   
    
}
