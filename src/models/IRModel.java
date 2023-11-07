package models;

import indexation.Index;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.io.Serializable;
import indexation.TextRepresenter;

public abstract class IRModel implements Serializable{
	
	private static final long serialVersionUID = 1L;
	//protected Index index;
	protected Weighter weighter;
	protected boolean prepared=false;
	public IRModel(Weighter weighter){
		this.weighter=weighter;
	}
	public IRModel(Index index){
		this.weighter=new WeighterTF(index);
	}
	public boolean isPrepared(){
		return prepared;
	}
	public abstract void prepareModelFromIndex(String outfile);
	//public abstract void loadModel(String fileName);
	
	// Computes scores for documents that have at least one word in common with the query
	public abstract HashMap<String,Double> getScores(HashMap<String,Integer> query) throws NonPreparedModelException;
	
	public Weighter getWeighter(){
		return weighter;
	}
	
	// Computes sorted scores for documents that have at least one word in common with the query
	public LinkedHashMap<String,Double> getSortedScores(String query) throws NonPreparedModelException{
		TextRepresenter textRep=weighter.getIndex().getTextRepresenter();
		HashMap<String,Integer> ret=textRep.getTextRepresentation(query);
		return getSortedScores(ret);
	}
	
	// Computes sorted scores for documents that have at least one word in common with the query
	public LinkedHashMap<String,Double> getSortedScores(HashMap<String,Integer> query) throws NonPreparedModelException{
		HashMap<String,Double> scores=getScores(query);
		List<Map.Entry<String, Double>> entries =  new ArrayList<Map.Entry<String, Double>>(scores.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b){
				return b.getValue().compareTo(a.getValue());
			}
		});
		LinkedHashMap<String, Double> ret = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : entries) {
			ret.put(entry.getKey(), entry.getValue());
		}
		return ret;
	}
	
	// Computes sorted scores for all documents (sets min value for all documents that have no word in common with the query)
	public LinkedHashMap<String,Double> getRanking(HashMap<String,Integer> query) throws NonPreparedModelException {
		LinkedHashMap<String, Double> ret = getSortedScores(query);
		
		ArrayList<String> nullDocs=new ArrayList<String>();
		Index index=weighter.getIndex();
		for(String doc:index.getDocs().keySet()){
			if(!ret.containsKey(doc)){
				nullDocs.add(doc);
			}
		}
		Double minscore=null;
		for(Double score:ret.values()){
			if((minscore==null) || (minscore>score)){
				minscore=score;
			}
		}
		Collections.shuffle(nullDocs);
		for(String doc:nullDocs){
			ret.put(doc, minscore-1.0);
		}
		return ret;
	}
	public abstract String getName();
	/*public Index getIndex(){
		return index;
	}*/
	
	public void serialize(String fileName){
		FileOutputStream fos =null;
		ObjectOutputStream oos= null;
		try {
				fos = new FileOutputStream(fileName);
				oos= new ObjectOutputStream(fos);
				oos.writeObject(this); 
				oos.flush();
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
				try {
					oos.close();
					fos.close();
				}
				catch(Exception e){
					e.printStackTrace();
					throw new RuntimeException(e);
				}
		}
		
	}
	
	public static IRModel deserialize(String fileName) { 
		IRModel ret=null;
		try{
			FileInputStream fis = new FileInputStream(fileName);
			ObjectInputStream ois= new ObjectInputStream(fis);
			try {	
				ret = (IRModel) ois.readObject(); 
			} finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}
		}
		catch(ClassNotFoundException e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		catch(IOException e){
			e.printStackTrace();
			throw new RuntimeException(e);
			
		}
		return(ret);
	}
		
	
}
