package evaluation;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
public class PrecisionRecall extends EvalMeasure {

	private int nbPoints=10;
	
	@Override
	public String getName() {
		return "PrecisionRecall";
	}

	@Override
	public Result eval(Hyp hyp) {
		DecimalFormat format = new DecimalFormat();
		format.setMaximumFractionDigits(3);
		format.setGroupingUsed(false);
		Result res=new Result(this.getName(),"Query_"+hyp.getQuery().getId());
		LinkedHashMap<String,Double> list=hyp.getRanking();
		ArrayList<String> ranking=new ArrayList<String>(list.keySet());
		HashMap<String,Double> rels=hyp.getQuery().getRelevants();
		double recall=0.0;
		double stepRecall=1.0/nbPoints;
		double nextRecall=1.0/nbPoints;
		int nrels=0;
		double prec=0.0;
		for(int i=0;i<ranking.size();i++){
			String id=ranking.get(i);
			if(rels.containsKey(id)){
				nrels++;
				recall=(nrels*1.0)/rels.size();
				while(recall>=nextRecall){
					prec=(nrels*1.0)/(i+1.0);
					int n=nrels;
					for(int j=i+1;j<ranking.size();j++){
						id=ranking.get(j);
						if(rels.containsKey(id)){
							n++;
							double p=(n*1.0)/(j+1.0);
							if(p>prec){
								prec=p;
							}
						}
					}
					res.addScore("PrecAtRec_"+format.format(nextRecall), prec);
					nextRecall+=stepRecall;
				}
			}
		}
		return res;
	}

}
