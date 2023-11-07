package models;

import indexation.Index;
import indexation.Parser;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Vectoriel extends IRModel{
	
	private static final long serialVersionUID = 1L;
	
	private boolean normalized=true;
	private HashMap<String,Double> norms;
	public Vectoriel(Weighter weighter){
		super(weighter);
	}
	
	public Vectoriel(Weighter weighter,boolean normalized){
		this(weighter);
		this.normalized=normalized;
	}
	@Override
	// If normalized, compute the norms of the doc from weighter
	// Serializes the model
	public void prepareModelFromIndex(String outfile){
		try{
			if(normalized){
				HashMap<String,String> docs=weighter.getIndex().getDocs();
				norms=new HashMap<String,Double>();
				//FileOutputStream fout = new FileOutputStream(outfile);
				//PrintStream out = new PrintStream(fout);
				//DecimalFormat format = new DecimalFormat();
				//format.setMaximumFractionDigits(5);
				//format.setGroupingUsed(false);
				//out.println(weighter.getClass());
				for(String id:docs.keySet()){
					HashMap<String,Double> w=weighter.getDocWeightsForDoc(id);
					double sum=0.0;
					for(Double v:w.values()){
						sum+=v*v;
					}
					sum=Math.sqrt(sum);
					norms.put(id, sum);
					//out.println(id+"="+sum); //format.format(sum));
				}
				//out.close();
			}
			prepared=true;
			serialize(outfile);
			
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public HashMap<String,Double> getScores(HashMap<String,Integer> query) throws NonPreparedModelException {
		if (!prepared)
		{
			throw new NonPreparedModelException("Vectoriel model not prepared");
		}
		HashMap<String,Double> wq=weighter.getQueryWeights(query);
		double norm=0.0;
		HashMap<String,Double> things=new HashMap<String,Double>();
		HashMap<String,Double> scores=new HashMap<String,Double>();
		HashMap<String,Double> ret=new HashMap<String,Double>();
		for(String stem:wq.keySet()){
			double v=wq.get(stem);
			norm+=v*v;
			//HashMap<String,Integer> tfs=weighter.getIndex().getTfsForStem(stem);
			HashMap<String,Double> wd=weighter.getDocWeightsForStem(stem); 
			
			for(String doc:wd.keySet()){
				double w=wd.get(doc);
				Double s=scores.get(doc);
				s=(s==null)?0:s;
				scores.put(doc, s+(w*v));
			}
		}
		//System.out.println(wq);
		//System.out.println(weighter.getWeightsForDoc("55"));
		if(normalized){
			norm=Math.sqrt(norm);
			
			for(String doc:scores.keySet()){
				double s=scores.get(doc);
				
				/*HashMap<String,Integer> tfs=weighter.getIndex().getTfsForDoc(doc);
				if(tfs.size()<10){
					s=0.0;
				}*/
				//System.out.println(doc+" "+s+" "+norms.get(doc)+" "+weighter.getWeightsForDoc(doc));
				s/=(norms.get(doc)*norm);
				//System.out.println(doc+" "+norm+" "+s);
				
				ret.put(doc, s);
			}
		}
		else{
			ret=scores;
		}
		return ret;
	}
	
	public String getName(){
		return ((normalized)?"Cosine_":"DotProduct_")+weighter.getClass().toString();
	}
	
	public static void main(String[] args){
		Index index=Index.deserialize("cisi");
		Weighter weighter=new Weighter1(index);
		Vectoriel cosine=new Vectoriel(weighter);
		//cosine.prepareModelFromIndex("cosineModelCISI_BIS1");
		cosine.prepareModelFromIndex("cosineModelCISI_Weighter1");
		cosine=new Vectoriel(weighter,false);
		cosine.prepareModelFromIndex("dotproductModelCISI_Weighter1");
		//Cosine cosine=(Cosine)Cosine.deserialize("cosineModelCACM3");
		
		
		
		/*HashMap<String,Integer> query=new HashMap<String,Integer>();
		query.put("zone", 1);
		query.put("attempt", 1);*/
		try {
			System.out.println(cosine.getSortedScores("blue screen "));
		}
		catch(NonPreparedModelException e) {
			System.out.println(e);
		}
		//System.out.println(cosine.getSortedScores("beach body "));
	}
	
}
