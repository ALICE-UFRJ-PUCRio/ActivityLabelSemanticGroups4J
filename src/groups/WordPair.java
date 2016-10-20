package groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.lti.jawjaw.db.SenseDAO;
import edu.cmu.lti.jawjaw.db.SynlinkDAO;
import edu.cmu.lti.jawjaw.db.SynsetDAO;
import edu.cmu.lti.jawjaw.pobj.Link;
import edu.cmu.lti.jawjaw.pobj.Sense;
import edu.cmu.lti.jawjaw.pobj.Synlink;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.pobj.Word;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;


//Test this new class!!!!!!!!!!!

public class WordPair{

	public Word word1;
	public Word word2;
	public Integer searchLevel;
	public String context;
	public ArrayList<Synset> commonHyperHolos;
	public Synset bestHyperHolo;
	public Double similarityW1toHyperHolo;
	public Double similarityW2toHyperHolo;
	public String action;
	
	public WordPair(Word word1, Word word2, Integer searchLevel, String context){
		
		this.commonHyperHolos = new ArrayList<Synset>();
		this.word1 = word1;
		this.word2 = word2;
		this.context = context;
		this.searchLevel = searchLevel;
		this.setcommonHyperHolos();
		this.setBestCommonHyperHolo();
		this.similarityW1toHyperHolo = this.getSimilarityCalc(this.word1, this.bestHyperHolo);
		this.similarityW2toHyperHolo = this.getSimilarityCalc(this.word2, this.bestHyperHolo);
		
	}
	
	public void setAction(String action){
		this.action= action;
	}
	
	public Double getSimilarityValue(){
		return this.similarityW1toHyperHolo+this.similarityW2toHyperHolo;
	}
	
	public void displaySimilarityValues(){
		if(this.bestHyperHolo!=null){
			System.out.println(this.word1.getLemma() +";"+this.bestHyperHolo.getName()+" "+this.similarityW1toHyperHolo);
			System.out.println(this.word2.getLemma() +";"+this.bestHyperHolo.getName()+" "+this.similarityW2toHyperHolo);
		} else {
			System.out.println(this.word1.getLemma() +";No Best Hyper Holo");
			System.out.println(this.word2.getLemma() +";No Best Hyper Holo");
		}
	}
	

	
	public void setcommonHyperHolos(){
		ArrayList<Synset> findHyperAndHolosForWord1 = this.findHyperAndHolosByLevel(this.word1);
		ArrayList<Synset> findHyperAndHolosForWord2 = this.findHyperAndHolosByLevel(this.word2);
		
		//add common hyper and holos
		for(Synset synset1HH : findHyperAndHolosForWord1){
			for(Synset synset2HH : findHyperAndHolosForWord2){
				if(synset1HH.equals(synset2HH)){
					//System.out.println(synset1HH);
					this.commonHyperHolos.add(synset1HH);
				}
			}
		}
	}
	
	public void setBestCommonHyperHolo(){
		if(this.commonHyperHolos.size()>0){
			this.bestHyperHolo = this.disambiguateWordToSet(this.commonHyperHolos, this.context);
		}
	}
	
	public ArrayList<Synset> findHyperAndHolosByLevel(Word word){
		ArrayList<Synset> resultHHtmp = new ArrayList<Synset>();
		ArrayList<Synset> resultHHtmp2 = new ArrayList<Synset>();
		Integer level = this.searchLevel;
		//this.displayWords();
			//System.out.println("findHyperAndHolosFor: "+word);
			resultHHtmp.addAll(findNextHyperAndHolosForASingleWord(word));
			while(level>1){ //iterate over discovered synsetss
				for(Synset SynsetN : resultHHtmp){
					//System.out.println(wordN);
					resultHHtmp2.addAll(findNextHyperAndHolosForASingleSynset(SynsetN));
				}
				level--;
				resultHHtmp.addAll(resultHHtmp2);
			}
			//remove duplicates
			Set<Synset> hs = new HashSet<>();
			hs.addAll(resultHHtmp);
			resultHHtmp.clear();
			resultHHtmp.addAll(hs);
			return resultHHtmp;
	}
		
	public ArrayList<Synset> findNextHyperAndHolosForASingleWord(Word word){
		ArrayList<Synset> resultHH = new ArrayList<Synset>();
		
		try{
			List<Sense> senses = SenseDAO.findSensesByWordid( word.getWordid() );
			//resultHH.add(word);
			//System.out.println("--------");
			for(Sense sense : senses){    		
				String synsetId = sense.getSynset();
				Synset synset = SynsetDAO.findSynsetBySynset( sense.getSynset() );
				resultHH.add(synset);
				//System.out.println(synset.getName());
				List<Synlink> synlinks = SynlinkDAO.findSynlinksBySynset(synsetId);
		    	//Continue on validating if the simple functions are similar to using DAO
		    	for(Synlink synlink : synlinks){
		    		if(synlink.getLink().equals(Link.hype) || synlink.getLink().equals(Link.holo) || synlink.getLink().equals(Link.hprt) || synlink.getLink().equals(Link.hmem) || synlink.getLink().equals(Link.hsub)){
		    			Synset hyperOrHolonym = SynsetDAO.findSynsetBySynset( synlink.getSynset2() );
		    			//System.out.println( hyperOrHolonym.getName()+"#"+hyperOrHolonym.getPos());
		    			//remove duplicates
		    			if(!resultHH.contains(hyperOrHolonym.getName()+"#"+hyperOrHolonym.getPos()))
		    				resultHH.add(hyperOrHolonym);
		    		}
		    	}
			}
			//System.out.println("--------");

			return resultHH;
		} catch (Exception e){
			return resultHH;
		}

	}

	public ArrayList<Synset> findNextHyperAndHolosForASingleSynset(Synset synset){
		ArrayList<Synset> resultHH = new ArrayList<Synset>();
		
		try{
			List<Synlink> synlinks = SynlinkDAO.findSynlinksBySynset(synset.getSynset());
	    	//Continue on validating if the simple functions are similar to using DAO
	    	for(Synlink synlink : synlinks){
	    		if(synlink.getLink().equals(Link.hype) || synlink.getLink().equals(Link.holo) || synlink.getLink().equals(Link.hprt) || synlink.getLink().equals(Link.hmem) || synlink.getLink().equals(Link.hsub)){
	    			Synset hyperOrHolonym = SynsetDAO.findSynsetBySynset( synlink.getSynset2() );
	    			//System.out.println( hyperOrHolonym.getName()+"#"+hyperOrHolonym.getPos());
	    			//remove duplicates
	    			if(!resultHH.contains(hyperOrHolonym.getName()+"#"+hyperOrHolonym.getPos()))
	    				resultHH.add(hyperOrHolonym);
	    		}
	    	}
			return resultHH;
		} catch (Exception e){
			return resultHH;
		}
		
		
	}

	public static String getMaxSimilarity(Synset synset, String contextWord){
		String result="";
		ILexicalDatabase db = new NictWordNet();
		RelatednessCalculator lesk = new Lesk(db);

		double maxScore = 0.0;
		
		Concept ss1 = new Concept(synset.getSynset(), synset.getPos(), synset.getName(), synset.getSrc());
		
		//System.out.println(ss1.getSynset()+" - "+ss1.getName() +" - "+contextWord);
			
			List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(contextWord, ss1.getPos().toString());

			for (Concept ss2: synsets2) {
				if(ss2.getPos().equals(ss1.getPos())){
					//System.out.println(ss2);
					Relatedness relatedness = lesk.calcRelatednessOfSynset(ss1, ss2);
			        double score = relatedness.getScore();
			        if (score > maxScore) { 
			        	maxScore = score;
			        }
		    	}
			  //  System.out.println(ss1.getName()+ " "+ss2.getName()+":"+maxScore);
			}
		
			
		result = ss1.getSynset()+";"+maxScore;
		
		return result;
	}

	public Synset disambiguateWordToSet(ArrayList<Synset> synsets, String context){
		
		String[] contextWords = context.split(" ");
		
		ArrayList<String> maxValuesForEachSynset = new ArrayList<String>();
		
		for(Synset synset : synsets){
			for(String contextWord : contextWords){
				String result = getMaxSimilarity(synset, contextWord);
				maxValuesForEachSynset.add(result);
			}
		}	
		
		HashMap<String, Double> hm = new HashMap<String, Double>();
		
		for(String synset : maxValuesForEachSynset){
			
			String[] decomp = synset.split(";");
			String syn = decomp[0];
			Double maxSim = Double.parseDouble(decomp[1]);
			
			if(hm.containsKey(syn)){
				hm.put(syn, hm.get(syn)+maxSim);
			} else {
				hm.put(syn, maxSim);
			}			
		}
		
		//System.out.println("----------------");
		
		double maxScore = -1D;
		String maxScoreSynset = null;
		
		Set<Map.Entry<String, Double>> set = hm.entrySet();
		for (Map.Entry<String, Double> me : set) {
		     // System.out.print(me.getKey() + ": ");
		     // System.out.println(me.getValue());
		      double score = me.getValue();
		        if (score > maxScore) { 
		                 maxScore = score;
		                 maxScoreSynset = me.getKey();
		        }
		}
		
		Synset bestSynset = SynsetDAO.findSynsetBySynset(maxScoreSynset);
		
		return bestSynset;		
	}

	public Double getSimilarityCalc( Word word, Synset hyperHolo ) {		
		
		double maxScore = 0.0;
		
		if(hyperHolo != null){
			ILexicalDatabase db = new NictWordNet();
			RelatednessCalculator lin = new Lin(db);
	
			List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word.getLemma(), word.getPos().name());
	
			for(Concept ss1: synsets1){
		        Relatedness relatedness = lin.calcRelatednessOfSynset(ss1, new Concept(hyperHolo.getSynset(),hyperHolo.getPos()));
		        double score = relatedness.getScore();
		        if (score > maxScore) { 
		                 maxScore = score;
		        }
			}
		}
		
		return maxScore;
	}
	
}
