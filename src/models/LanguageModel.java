package models;

import indexation.Index;

import java.util.HashMap;

public class LanguageModel extends IRModel {
	//HashMap<String,Double> lambdas;
	HashMap<String,Integer> docLengths;
	HashMap<String,Integer> stemLengths;
	double tLength;
	double lambda;
	public LanguageModel(Index index,double lambda){
		super(index);
		this.lambda=lambda;
		
	}
	/*public LanguageModel(Weighter weighter,HashMap<String,Double> lambdas){
		super(weighter);
		this.lambdas=lambdas;
		lengths=new HashMap<String,Integer>();
	}*/
	
	public void setLambda(double l){
		lambda=l;
	}
	
	@Override
	public String getName() {
		
		return "LanguageModel_"+lambda;
	}

	@Override
	public HashMap<String, Double> getScores(HashMap<String, Integer> query) throws NonPreparedModelException {
		if (!prepared)
		{
			throw new NonPreparedModelException("Language model not prepared");
		}
		HashMap<String,Double> wq=weighter.getQueryWeights(query);
		HashMap<String,Double> scores=new HashMap<String,Double>();
		double nullS=0.0;
		for(String stem:wq.keySet()){
			Integer sl=stemLengths.get(stem);
			if(sl==null){
				continue;
			}
			
			double pt=(sl*1.0)/tLength;
			
			HashMap<String,Integer> tfs=weighter.getIndex().getTfsForStem(stem);
			for(String doc:scores.keySet()){
				if(tfs.containsKey(doc)){
					continue;
				}
				Double s=scores.get(doc);
				s+=Math.log((1.0-lambda)*pt);
				scores.put(doc, s);
				
			}
			for(String doc:tfs.keySet()){
				Integer tf=tfs.get(doc);
				Double s=scores.get(doc);
				s=(s==null)?nullS:s;
				//System.out.println(doc+" "+s+" "+nullS+" "+pt+" "+lambda+" "+tf);
				scores.put(doc, s+Math.log(((1.0-lambda)*pt) + (lambda*((tf*1.0)/docLengths.get(doc)))));
				//System.out.println(scores.get(doc));
				
			}
			nullS+=Math.log((1.0-lambda)*pt);
			
		}
		return scores;
	}

	@Override
	public void prepareModelFromIndex(String outfile) {
		docLengths=new HashMap<String,Integer>();
		stemLengths=new HashMap<String,Integer>();
		tLength=0;
		for(String doc:weighter.getIndex().getDocs().keySet()){
			HashMap<String,Integer> tfs=weighter.getIndex().getTfsForDoc(doc);
			int sum=0;
			for(Integer v:tfs.values()){
				sum+=v;
			}
			tLength+=sum;
			docLengths.put(doc, sum);
		}
		for(String stem:weighter.getIndex().getStems().keySet()){
			HashMap<String,Integer> tfs=weighter.getIndex().getTfsForStem(stem);
			int sum=0;
			for(Integer v:tfs.values()){
				sum+=v;
			}
			stemLengths.put(stem, sum);
		}
		
		prepared=true;
		serialize(outfile);
		
	}

	
	public static void main(String[] args){
		Index index=Index.deserialize("cisi");
		LanguageModel model=new LanguageModel(index,0.02);
		model.prepareModelFromIndex("LanguageModelCISI");
		
		//Cosine cosine=(Cosine)Cosine.deserialize("cosineModelCACM3");
		
		
		
		/*HashMap<String,Integer> query=new HashMap<String,Integer>();
		query.put("zone", 1);
		query.put("attempt", 1);*/
		//System.out.println(model.getSortedScores("blue screen "));
		try {
			System.out.println(model.getSortedScores("beach body "));
		}
		catch(NonPreparedModelException e) {
			System.out.println(e);
		}
	}
}
