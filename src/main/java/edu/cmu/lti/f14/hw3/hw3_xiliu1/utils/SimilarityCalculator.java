package edu.cmu.lti.f14.hw3.hw3_xiliu1.utils;

import java.util.Iterator;
import java.util.Map;

public class SimilarityCalculator {
	private static SimilarityCalculator instance=null;
	public static SimilarityCalculator getInstance(){
		if (instance == null){
			instance = new SimilarityCalculator();
		}
		return instance;
	}
	/**
	 * 
	 * @return cosine_similarity
	 */
	public double computeCosineSimilarity(Map<String, Integer> queryVector,
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
	public double computeJaccardSimilarity(Map<String, Integer> queryVector,
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
	public double computeDiceSimilarity(Map<String, Integer> queryVector,
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
}
