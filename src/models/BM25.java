package models;

import java.util.HashMap;

import indexation.Index;

public class BM25 extends IRModel {

	private static final long serialVersionUID = 1L;
	
	private double b;
	private double k1;
	private HashMap<String,Double> pidf;
	private double lmoy;
	private HashMap<String,Double> docLengths;
	//private HashMap<String,Double> stemLengths
	public BM25(Index index,double k1,double b){
		super(index);
		this.k1=k1;
		this.b=b;
		
	}
	
	
	
	@Override
	public void prepareModelFromIndex(String outfile) {
		
		docLengths=new HashMap<String,Double>();
		this.pidf=new HashMap<String,Double>();
		lmoy=0;
		int nb=0;
		for(String doc:weighter.getIndex().getDocs().keySet()){
			HashMap<String,Double> tfs=weighter.getDocWeightsForDoc(doc);
			double sum=0;
			for(Double v:tfs.values()){
				sum+=v;
			}
			lmoy+=sum;
			docLengths.put(doc, sum);
			nb++;
		}
		if(nb>0){
			lmoy/=nb;
		}
		
		for(String stem:weighter.getIndex().getStems().keySet()){
			HashMap<String,Double> tfs=weighter.getDocWeightsForStem(stem);
			int n=tfs.size();
			double p=Math.log((nb-n+0.5)/(n+0.5));
			if(p<0){
				p=0;
			}
			pidf.put(stem,p);
		}
		prepared=true;
		serialize(outfile);
		
	}

	@Override
	public HashMap<String, Double> getScores(HashMap<String, Integer> query) throws NonPreparedModelException {
		if (!prepared)
		{
			throw new NonPreparedModelException("BM25 model not prepared");
		}
		HashMap<String,Double> wq=weighter.getQueryWeights(query);
		HashMap<String,Double> scores=new HashMap<String,Double>();
		for(String stem:wq.keySet()){
			Double p=pidf.get(stem);
			if(p==null){
				continue;
			}
			HashMap<String,Integer> tfs=weighter.getIndex().getTfsForStem(stem);
			for(String doc:tfs.keySet()){
				Integer tf=tfs.get(doc);
				Double s=scores.get(doc);
				s=(s==null)?0.0:s;
				double v=p*(((k1+1.0)*tf)/(k1*((1-b)+b*docLengths.get(doc)/lmoy)+tf));
				scores.put(doc, s+v);
				
			}
		}
		return scores;
	}

	public void setB(double b){
		this.b=b;
	}
	public void setK1(double k1){
		this.k1=k1;
	}
	
	@Override
	public String getName() {
		return "BM25_k1="+k1+"_b="+b;
	}

	public static void main(String[] args){
		Index index=Index.deserialize("cisi");
		//System.out.println(index);
		//System.out.println(index.getTextRepresenter());
		BM25 mod=new BM25(index,1.5,0.75);
		mod.prepareModelFromIndex("BM25_CISI");
		try{
			System.out.println(mod.getSortedScores("beach body "));
		}
		catch(NonPreparedModelException e) {
			System.out.println(e);
		}
	}
	
}
