package edu.cmu.lti.f14.hw3.hw3_xiliu1.casconsumers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.BufferedWriter;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_xiliu1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.utils.SimilarityCalculator;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.utils.Utils;
import edu.cmu.lti.f14.hw3.hw3_xiliu1.utils.RankedDoc;


public class RetrievalEvaluatorForTask2 extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	//public ArrayList<Integer> relList;
	/**map for query tokens**/
	private HashMap<Integer, HashMap<String, Integer>> queryMap;
	/**map for relevant documents tokens**/
	private HashMap<Integer, ArrayList<RankedDoc>> documentMap;
	
	private BufferedWriter reportWriter;
	
	private SimilarityCalculator similarityCal = SimilarityCalculator.getInstance();
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();
		queryMap = new HashMap<Integer, HashMap<String, Integer>>();
		documentMap = new HashMap<Integer, ArrayList<RankedDoc>>();
		//relList = new ArrayList<Integer>();
		File output = new File("report.txt");
		try{
		      reportWriter = new BufferedWriter(new FileWriter(output));
		 } catch (Exception e) {
		      e.printStackTrace();
		    }
	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	
	
	//private HashMap<>
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator<Annotation> it = jcas.getAnnotationIndex(Document.type).iterator();
		HashMap<String, Integer> queryVector = new HashMap<String, Integer>();
		HashMap<String, Integer> documentVector = new HashMap<String, Integer>();		
		if (it.hasNext()) {
			Document doc = (Document) it.next();
			//Make sure that your previous annotators have populated this in CAS
			//FSList fsTokenList = doc.getTokenList();
			//ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);
			//System.out.println(doc.getQueryID()+"	"+doc.getRelevanceValue());
			if(doc.getRelevanceValue()==99){
				qIdList.add(doc.getQueryID());
				queryVector = constructVector(doc);
				this.queryMap.put(doc.getQueryID(), queryVector);
				System.out.println("Question: "+ doc.getText());
	
			}else{
				ArrayList<RankedDoc> documentList;
				documentVector = constructVector(doc);
				RankedDoc temp = new RankedDoc(doc.getQueryID(), doc.getRelevanceValue(), doc.getText());
				temp.setTokenList(documentVector);
				if (this.documentMap.containsKey(doc.getQueryID())){
					documentList = documentMap.get(doc.getQueryID());					
				}else{
					documentList = new ArrayList<RankedDoc>(); 
				}
				documentList.add(temp);
				System.out.println("Answers: "+ doc.getText());
				this.documentMap.put(doc.getQueryID(), documentList);
			}
			
			//qIdList.add(doc.getQueryID());
			//relList.add(doc.getRelevanceValue());
		}

	}
	public HashMap<String, Integer> constructVector(Document doc){
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		FSList fsTokenList = doc.getTokenList();
		ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class); 
		for (Token token: tokenList){
			String text = token.getText();
			//System.out.println(text);
			Integer freq;
			if (!result.containsKey(text)){
				freq = token.getFrequency();
			}else{
				freq =  result.get(text)+ token.getFrequency();
			}
			result.put(text, freq);
		}
		return result;
	}
	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 
	 * 2.Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {
		super.collectionProcessComplete(arg0);
		HashMap<String, Integer> queryVector;
		ArrayList<RankedDoc> docList;
		ArrayList<Double> rrList = new ArrayList<Double>();
		String reportContent;
		for (Integer qid : qIdList){
			queryVector = queryMap.get(qid);
			docList = documentMap.get(qid);
			// TODO :: compute the cosine similarity measure
			for (RankedDoc curDoc : docList){
				HashMap<String, Integer> curTokenList = curDoc.getTokenList();
				//double curConsine = similarityCal.computeCosineSimilarity(queryVector, curTokenList);
				
				// Experiment 5. compute jaccard similarity
				double curConsine = similarityCal.computeJaccardSimilarity(queryVector, curTokenList);
				/*	*/
				/*
				//Experiment 6. compute dice similarity
				double curConsine = similarityCal.computeDiceSimilarity(queryVector, curTokenList);
				*/
				curDoc.setScore(curConsine);
			}
			// TODO :: compute the rank of retrieved sentences
			Collections.sort(docList);
			//System.out.println("Quest-Token: "+ queryMap.get(qid).toString());
			System.out.println("Picked Answer: "+ docList.get(0).getText());
			for (int i=0; i<docList.size(); i++){
				RankedDoc curDoc = docList.get(i);
				if (curDoc.getRelevance()==1){
					rrList.add((double)1/(i+1));
					reportContent = String.format("coisne=%.4f\trank=%d\tqid=%d\trel=%d\t%s%n",
			                  curDoc.getScore(), (i+1), curDoc.getQid(), curDoc.getRelevance(), curDoc.getText());
					reportWriter.write(reportContent);
				}
			}
		}
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr(rrList);
		reportContent =  String.format("MRR=%.4f", metric_mrr);
		reportWriter.write(reportContent);
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		
		// TODO :: compute cosine similarity between two sentences
		double cosine_similarity=0.0;
		double numer = 0.0;
		double a,b;
		double a_square = 0.0;
		double b_square = 0.0;
		
		Iterator<String> keyIter = queryVector.keySet().iterator();
		keyIter = queryVector.keySet().iterator();
		while (keyIter.hasNext()){
			String token = keyIter.next();
			a = queryVector.get(token);
			if (docVector.containsKey(token)){
				b = docVector.get(token);
				numer = numer+a*b;
			}else{
				numer +=0;
			}
			a_square += a*a;
		}
		keyIter = docVector.keySet().iterator();
		while (keyIter.hasNext()){
			String token = keyIter.next();
			b = docVector.get(token);
			b_square += b*b;
		}
		cosine_similarity =  numer / (Math.sqrt(a_square)*Math.sqrt(b_square));
		return cosine_similarity;
	}
	/**
	 * 
	 * @return jaccard_similarity
	 */
	private double computeJaccardSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		
		// TODO :: compute cosine similarity between two sentences
		double jaccard_similarity=0.0;
		int inter_count = 0;
		int union_count = 0;
		
		Iterator<String> keyIter = queryVector.keySet().iterator();
		keyIter = queryVector.keySet().iterator();
		while (keyIter.hasNext()){
			String token = keyIter.next();
			if (docVector.containsKey(token)){
				inter_count += Math.min(docVector.get(token),queryVector.get(token) );
				union_count += Math.max(docVector.get(token),queryVector.get(token) );
			}else{
				union_count += queryVector.get(token);
			}
		}
		keyIter = docVector.keySet().iterator();
		while (keyIter.hasNext()){
			String token = keyIter.next();
			if (!docVector.containsKey(token)){
				union_count += docVector.get(token);
			}
		}
		jaccard_similarity =  (double) inter_count/ (double) union_count;
		return jaccard_similarity;
	}
	/**
	 * 
	 * @return dice_similarity
	 */
	private double computeDiceSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		
		// TODO :: compute cosine similarity between two sentences
		double dice_similarity=0.0;
		int inter_count = 0;
		int a_count = 0;
		int b_count = 0;
		Iterator<String> keyIter = queryVector.keySet().iterator();
		keyIter = queryVector.keySet().iterator();
		while (keyIter.hasNext()){
			String token = keyIter.next();
			if (docVector.containsKey(token)){
				inter_count += Math.min(docVector.get(token),queryVector.get(token) );
			}
			a_count += queryVector.get(token);
		}
		keyIter = docVector.keySet().iterator();
		while (keyIter.hasNext()){
			String token = keyIter.next();
			b_count += docVector.get(token);
		}
		dice_similarity =  (double) 2*inter_count/(double) (a_count+b_count);
		return dice_similarity;
	}
	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr(ArrayList<Double> rrList) {
		double metric_mrr=0.0;
		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		for (Double rr: rrList){
			metric_mrr += rr;
		}
		return metric_mrr / rrList.size();
	}
	@Override
	  public void destroy() {
	    try {
	      reportWriter.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	  }
}
