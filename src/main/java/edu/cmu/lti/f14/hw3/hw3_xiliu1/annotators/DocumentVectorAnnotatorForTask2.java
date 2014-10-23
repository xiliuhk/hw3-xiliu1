package edu.cmu.lti.f14.hw3.hw3_xiliu1.annotators;

import java.util.*;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_xiliu1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.utils.OpenNLPTokenization;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.utils.StanfordLemmatizer;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.utils.StopWordRemover;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.utils.Utils;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.utils.SimilarityCalculator;


public class DocumentVectorAnnotatorForTask2 extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}
	
	
	
	/**
   * A basic white-space tokenizer, it deliberately does not split on punctuation!
   *
	 * @param doc input text
	 * @return    a list of tokens.
	 */
	
	List<String> tokenize0(String doc) {
	  List<String> res = new ArrayList<String>();
	  
	  for (String s: doc.split("\\s+"))
	    res.add(s);
	  return res;
	}

	/**
	 * 
	 * @param jcas
	 * @param doc
	 * Step 1. Read the document content
	 * Step 2. Tokenize the content with tokenize0()
	 * Step 3. Count the tokens and add it to the vector of tokens
	 * Step 4. Add the vector of tokens to document in CAS
	 */
	/**
	 * Also I did an experiment on the word STEMMING with StanfordLemmatizer
	 * this experiment aims to remove -s, -es,-d, -ed, -'s 
	 * to deal with words such as guesses, potatoes, named, David's
	 * */
	
	private void createTermFreqVector(JCas jcas, Document doc) {
		//TO DO: construct a vector of tokens and update the tokenList in CAS
	    //TO DO: use tokenize0 from above 
		String docText = doc.getText();
		
		
		/*
		//Baseline: Tokenize the sentence
		List<String> tokens = this.tokenize0(docText);
				
		// Experiment 1. Remove stop-words and punctuation
		StopWordRemover stopWordRemover = StopWordRemover.getInstance();
		String stopRemovedText = stopWordRemover.removeStopWords(docText);
		//List<String> tokens = this.tokenize0(stopRemovedText);
		*/
		/*
		 //Experiment 2. Remove punctuations and convert to lowercases
		docText.replaceAll("\\p{Punct}+", "").toLowerCase();
		List<String> tokens = this.tokenize0(docText);
		*/	
		/**/
		//Experiment 3. StanfordLemmatizer
		String stemmedText = StanfordLemmatizer.stemText(docText);
			//List<String> tokens = this.tokenize0(stemmedText);

		//Experiment 4. OpenNLPtokenizer
		OpenNLPTokenization OpenNLPtokenizer= OpenNLPTokenization.getInstance();
		List<String> tokens = OpenNLPtokenizer.tokenize(stemmedText);
		//List<String> tokens = OpenNLPtokenizer.tokenize(docText);
		//List<String> tokens = OpenNLPtokenizer.tokenize(stopRemovedText);
		
		//hash the counts by token text
		HashMap<String, Integer> tokenMap = new HashMap<String, Integer>();
		for (String token: tokens){
			if (tokenMap.containsKey(token)){
				tokenMap.put(token, tokenMap.get(token)+1);
			}else{
				tokenMap.put(token, 1);	
			}
		}
		// iterate the hashmap and create tokens, then add the token to the token vector
		Iterator<String> tokenMapIter = tokenMap.keySet().iterator();
		List<Token> tokenList = new ArrayList<Token>();
		while (tokenMapIter.hasNext()){
			String text = tokenMapIter.next();
			Integer frequency = tokenMap.get(text);
			Token tempToken = new Token(jcas);
			tempToken.setText(text);
			tempToken.setFrequency((int)frequency);
			tokenList.add(tempToken);
		}
		//add token vectors to document in CAS
		doc.setTokenList(Utils.fromCollectionToFSList(jcas, tokenList));

	}

}
