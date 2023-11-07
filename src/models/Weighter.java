package models;

import indexation.Index;

import java.io.IOException;
import java.util.HashMap;
import java.io.Serializable;

public abstract class Weighter implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private transient Index index=null;
	protected String indexName;
	public Weighter(Index index){
		this.index=index;
		this.indexName=index.getName();
	}
	public abstract HashMap<String,Double> getDocWeightsForDoc(String idDoc);
	public abstract HashMap<String,Double> getDocWeightsForStem(String stem);
	
	//public abstract double getWeightForDocStem(String stem,int tf,HashMap<String,Double> things);
	public abstract HashMap<String,Double> getQueryWeights(HashMap<String,Integer> query);
	public static Weighter getWeighter1(Index index){
		return new Weighter1(index);
	}
	public static Weighter getWeighter2(Index index){
		return new Weighter2(index);
	}
	public Index getIndex(){
		if(index==null){
			System.out.println("load Index");
			index=Index.deserialize(indexName);
			
		}
		return index;
	}
}



/**
 * 
 * For doc weights : 1+log(tf)
 * For query weights : idf => log((N+1)/(nbdocs containing the stem))
 *
 */
class Weighter1 extends Weighter{
	
	private static final long serialVersionUID = 1L;
	public Weighter1(Index index){
		super(index);
	}
	public HashMap<String,Double> getDocWeightsForDoc(String idDoc){
		HashMap<String,Integer> tfs=getIndex().getTfsForDoc(idDoc);
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String stem:tfs.keySet()){
			w.put(stem, 1+Math.log(tfs.get(stem)));
		}
		return w;
	}
	public HashMap<String,Double> getDocWeightsForStem(String stem){
		HashMap<String,Integer> tfs=getIndex().getTfsForStem(stem);
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String doc:tfs.keySet()){
			w.put(doc, 1+Math.log(tfs.get(doc)));
		}
		return w;
	}
	public double getWeightForDocStem(String stem,int tf,HashMap<String,Double> things){
		return 1.0+Math.log(tf);
	}
	public HashMap<String,Double> getQueryWeights(HashMap<String,Integer> query){
		HashMap<String,Double> w=new HashMap<String,Double>();
		int nb=getIndex().getDocs().size();
		//System.out.println(nb);
		for(String stem:query.keySet()){
			HashMap<String,Integer> tfs=getIndex().getTfsForStem(stem);
			//System.out.println(stem+" => "+(1.0+nb)+" "+tfs.size());
			if(tfs.size()>0){
				w.put(stem, Math.log((1.0+nb)/tfs.size()));
			}
		}
		return w;
	}
	
}


/**
 * 
 * For doc weights : tf
 * For query weights : idf => log((N+1)/(nbdocs containing the stem))
 *
 */
class Weighter2 extends Weighter{
	
	private static final long serialVersionUID = 1L;
	public Weighter2(Index index){
		super(index);
	}
	public HashMap<String,Double> getDocWeightsForDoc(String idDoc){
		HashMap<String,Integer> tfs=getIndex().getTfsForDoc(idDoc);
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String stem:tfs.keySet()){
			w.put(stem, tfs.get(stem)*1.0);
		}
		return w;
	}
	public HashMap<String,Double> getDocWeightsForStem(String stem){
		HashMap<String,Integer> tfs=getIndex().getTfsForStem(stem);
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String doc:tfs.keySet()){
			w.put(doc, tfs.get(doc)*1.0);
		}
		return w;
	}
	public double getWeightForDocStem(String stem,int tf,HashMap<String,Double> things){
		return tf;
	}
	public HashMap<String,Double> getQueryWeights(HashMap<String,Integer> query){
		HashMap<String,Double> w=new HashMap<String,Double>();
		int nb=getIndex().getDocs().size();
		for(String stem:query.keySet()){
			HashMap<String,Integer> tfs=getIndex().getTfsForStem(stem);
			if(tfs.size()>0){
				w.put(stem, Math.log((1.0+nb)/tfs.size()));
			}
		}
		return w;
	}
}


/**
 * 
 * For doc weights : (1+log(tf)) * idf
 * For query weights : idf 
 * with idf = log((N+1)/(nbdocs containing the stem))
 *
 */
class Weighter3 extends Weighter{
	
	private static final long serialVersionUID = 1L;
	private HashMap<String,Double> idfs;
	public Weighter3(Index index){
		super(index);
		idfs=new HashMap<String,Double>();
	}
	public HashMap<String,Double> getDocWeightsForDoc(String idDoc){
		HashMap<String,Integer> tfs=getIndex().getTfsForDoc(idDoc);
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String stem:tfs.keySet()){
			w.put(stem, (1.0+Math.log(tfs.get(stem)*1.0))*getIdf(stem));
		}
		return w;
	}
	public HashMap<String,Double> getDocWeightsForStem(String stem){
		HashMap<String,Integer> tfs=getIndex().getTfsForStem(stem);
		HashMap<String,Double> w=new HashMap<String,Double>();
		double idf=getIdf(stem);
		for(String doc:tfs.keySet()){
			w.put(doc, (1.0+Math.log(tfs.get(doc)*1.0))*idf);
		}
		return w;
	}
	private double getIdf(String stem){
		Double idf=idfs.get(stem);
		if(idf==null){
			int nb=getIndex().getDocs().size();
			HashMap<String,Integer> tfs=getIndex().getTfsForStem(stem);
			if(tfs.size()>0){
				idf=Math.log((1.0+nb)/tfs.size());
			}
			else{idf=0.0;}
			idfs.put(stem, idf);
		}
		return idf;
	}
	public double getWeightForDocStem(String stem,int tf,HashMap<String,Double> things){
		return  (1.0+Math.log(tf*1.0))*getIdf(stem);
	}
	public HashMap<String,Double> getQueryWeights(HashMap<String,Integer> query){
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String stem:query.keySet()){
			
			w.put(stem, getIdf(stem));
		}
		return w;
	}
}

/**
 * 
 * For doc weights : (1+log(tf))
 * For query weights : 1 
 *
 */
class Weighter4 extends Weighter{
	
	private static final long serialVersionUID = 1L;
	public Weighter4(Index index){
		super(index);
		
	}
	public HashMap<String,Double> getDocWeightsForDoc(String idDoc){
		HashMap<String,Integer> tfs=getIndex().getTfsForDoc(idDoc);
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String stem:tfs.keySet()){
			w.put(stem, (1.0+Math.log(tfs.get(stem)*1.0)));
		}
		return w;
	}
	public HashMap<String,Double> getDocWeightsForStem(String stem){
		HashMap<String,Integer> tfs=getIndex().getTfsForStem(stem);
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String doc:tfs.keySet()){
			w.put(doc, 1+Math.log(tfs.get(doc)));
		}
		return w;
	}
	
	public double getWeightForDocStem(String stem,int tf,HashMap<String,Double> things){
		return  (1.0+Math.log(tf*1.0));
	}
	public HashMap<String,Double> getQueryWeights(HashMap<String,Integer> query){
		HashMap<String,Double> w=new HashMap<String,Double>();
		for(String stem:query.keySet()){
			w.put(stem, 1.0);
		}
		return w;
	}
}


