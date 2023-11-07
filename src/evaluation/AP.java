package evaluation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AP extends EvalMeasure {

	@Override
	public String getName() {
		return "AP";
	}

	@Override
	public Result eval(Hyp hyp) {
		Result res=new Result(this.getName(),"Query_"+hyp.getQuery().getId());
		LinkedHashMap<String,Double> list=hyp.getRanking();
		ArrayList<String> ranking=new ArrayList<String>(list.keySet());
		HashMap<String,Double> rels=hyp.getQuery().getRelevants();
		double ap=0.0;
		int nrels=0;
		for(int i=0;i<ranking.size();i++){
			String id=ranking.get(i);
			if(rels.containsKey(id)){
				nrels++;
				ap+=(nrels*1.0)/(i+1.0);
			}
		}
		if(nrels>0){
			ap/=nrels;
		}
		res.addScore("AP", ap);
	
		return res;
	}

}
