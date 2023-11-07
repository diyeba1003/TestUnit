package evaluation;
import java.util.HashMap;
import indexation.TextRepresenter;
public class Query {
	private String id;
	private String text;
	private HashMap<String,Double> relevants;
	
	public Query(String id,String text){
		this.id=id;
		this.text=text;
		this.relevants=new HashMap<String, Double>();
	}
	
	public HashMap<String,Integer> getStems(TextRepresenter textRepresenter){
		HashMap<String,Integer> ret=textRepresenter.getTextRepresentation(text);
		return ret;
	}
	
	public String getId() {
		return id;
	}
	public String getText() {
		return text;
	}
	public HashMap<String, Double> getRelevants() {
		return relevants;
	}
	public void addRelevant(String idDoc, Double score){
		relevants.put(idDoc, score);
	}
	
	public void addRelevant(String idDoc){
		addRelevant(idDoc,1.0);
	}
	public String toString(){
		String s="id = "+id+"\n"+text+"\n"+"Relevants="+relevants;
		return s;
	}
	
}
