package evaluation;

import models.IRModel;
import models.NonPreparedModelException;

import java.util.LinkedHashMap;
public class Hyp {
	private IRModel model;
	private Query query;
	private LinkedHashMap<String,Double> list;
	
	
	public Hyp(IRModel model, Query query) throws NonPreparedModelException {
		super();
		this.model = model;
		this.query = query;
		list=model.getRanking(query.getStems(model.getWeighter().getIndex().getTextRepresenter()));
	}
	public IRModel getModel() {
		return model;
	}
	public Query getQuery() {
		return query;
	}
	public  LinkedHashMap<String,Double> getRanking(){
		return list;
	}
	
}
