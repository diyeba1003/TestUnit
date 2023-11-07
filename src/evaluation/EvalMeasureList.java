package evaluation;



import java.util.ArrayList;
import java.util.LinkedHashMap;

public class EvalMeasureList extends EvalMeasure{
	protected ArrayList<EvalMeasure> mes;
	public EvalMeasureList(ArrayList<EvalMeasure> mes){
		this.mes=mes;
	}
	public String getName(){
		String s=" List of Measures : ";
		for(EvalMeasure m:mes){
			s+=m.getName()+" \t ";
		}
		return(s);
	}
	public Result eval(Hyp hyp){
		Result res=new Result(hyp.getModel().toString(),"Query_"+hyp.getQuery().getId());
		for(EvalMeasure m:mes){
			System.out.println("Evaluation par "+m.getName());
			Result r=m.eval(hyp);
			System.out.println(r);
			res.add(r);
		}
		
		return(res);
	}
}