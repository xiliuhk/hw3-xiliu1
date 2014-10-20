package edu.cmu.lti.f14.hw3.hw3_xiliu1.utils;

import java.util.HashMap;

public class RankedDoc implements Comparable<RankedDoc> {
	private int qid;
	private int relevance;
	private String content;
	private HashMap<String, Integer> tokenList;
	private double score;
	public RankedDoc(int id, int rel, String text){
		this.qid = 	id;
		this.relevance = rel;
		this.content = text;
		this.tokenList = new HashMap<String, Integer>();
		this.score = 0.0;
	}
	public void setScore(double score){
		this.score = score;
	}
	public void setTokenList (HashMap<String, Integer> tokenList){
		this.tokenList = tokenList;
	}
	public double getScore(){
		return this.score;
	}
	public int getQid(){
		return this.qid;
	}
	public String getText(){
		return this.content;
	}
	public HashMap<String, Integer> getTokenList(){
		return this.tokenList;
	}
	public int getRelevance(){
		return this.relevance;
	}
	@Override
	public int compareTo(RankedDoc o){
		if (this.score > o.score){
			return -1;
		}else if (this.score < o.score){
			return 1;
		}else{
			return 0;
		}
	}
}
