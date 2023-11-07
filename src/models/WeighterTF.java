package models;

import java.util.HashMap;

import indexation.Index;


/**
 * 
 * For doc weights : tf
 * For query weights : tf
 *
 */
class WeighterTF extends Weighter{
	
	private static final long serialVersionUID = 1L;
	public WeighterTF(Index index){
		super(index);
	}
	public HashMap<String,Double> getDocWeightsForDoc(String idDoc){
		HashMap<String,Integer> tfs=getIndex().getTfsForDoc(idDoc);
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String stem:tfs.keySet()){
			w.put(stem,tfs.get(stem)*1.0);
		}
		return w;
	}
	public HashMap<String,Double> getDocWeightsForStem(String stem){
		HashMap<String,Integer> tfs=getIndex().getTfsForStem(stem);
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String doc:tfs.keySet()){
			w.put(doc,tfs.get(doc)*1.0);
		}
		return w;
	}
	public double getWeightForDocStem(String stem,int tf,HashMap<String,Double> things){
		return tf*1.0;
	}
	public HashMap<String,Double> getQueryWeights(HashMap<String,Integer> query){
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String stem:query.keySet()){
			w.put(stem, query.get(stem)*1.0);
		}
		return w;
	}
}