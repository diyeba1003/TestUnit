package evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;





import models.*;
import indexation.Index;
import indexation.Parser;
public class EvalIRModel {
	private EvalMeasureList evMes;
	private IRModel model;
	private ResultFile rf;
	private String name;
	private QueryParser qp;
	
	public EvalIRModel(IRModel model, EvalMeasureList evMes, String name, String output){
		this.evMes=evMes;
		this.model=model;
		this.name=name;
		rf=new ResultFile(output);
	}
	
	
	public Result go(QueryParser qp){
		try{
			qp.reinit();
			Query q;
			ArrayList<Result> results=new ArrayList<Result>();
			
			while((q=qp.nextQuery())!=null){
				if(q.getRelevants().size()>0){
					Hyp hyp=new Hyp(model,q);
					Result result=evMes.eval(hyp);
					rf.append(result);
					results.add(result);
				}
			}
			Result stats=Result.getStats(results);
			stats.setDonnee(name);
			
			return(stats);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	
	
	public static void eval(ArrayList<IRModel> models,EvalMeasureList evMes,QueryParser qp,String outputRep){
		File rep=new File(outputRep);
		if (rep.exists()){
			try{
				Reader reader = new InputStreamReader(System.in);
				BufferedReader input = new BufferedReader(reader);
				System.out.print("Warning : "+rep+" already exists, overwrite ? (Y/N)");
				String ok = input.readLine();
				if ((ok.compareTo("Y")!=0) && (ok.compareTo("y")!=0)){return;}
			}
			catch(IOException e){
				e.printStackTrace();
				return;
			}
		}
		rep.mkdirs();
		
		ResultFile rf=new ResultFile(outputRep+"/stats.txt");
		int i=1;
		String titre="EvalPropagationModel \n\n Modeles : \n";
		for(IRModel model:models){
			titre+="\t model_"+i+": \t "+model.getName()+"\n";
			i++;
		}
		titre+="\n "+evMes.getName()+"\n";
		titre+=qp.getQueriesName();
		
		i=1;
		for (IRModel model:models){
			String modch="model_"+i;
			if(!model.isPrepared()){
				//throw new RuntimeException("Model "+model+" not prepared");
				model.prepareModelFromIndex(modch);
			}
			
			EvalIRModel expe=new EvalIRModel(model,evMes,modch,outputRep+"/"+modch+".txt");
			
			Result res=expe.go(qp);
			if(i==1){
				res.setExperiment(titre);
			}
			try{
				rf.append(res);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			i++;
		}
	}
	public static void main(String[] args){
		ArrayList<IRModel> models=new ArrayList<IRModel>();
		//Index index=new Index("test",Parser.getParserCISI());
		//Weighter weighter=Weighter.getWeighter2(index);
		IRModel model;
		
		model=Vectoriel.deserialize("dotproductModelCISI_Weighter1");
		models.add(model);
		model=Vectoriel.deserialize("cosineModelCISI_Weighter1");
		models.add(model);
		
		model=LanguageModel.deserialize("LanguageModelCISI");
		models.add(model);
		model=LanguageModel.deserialize("BM25_CISI");
		models.add(model);
		model=LanguageModel.deserialize("BM25_CISI");
		BM25 mod=(BM25)model;
		mod.setB(0.8);
		models.add(model);
		model=LanguageModel.deserialize("BM25_CISI");
		mod=(BM25)model;
		mod.setB(0.85);
		//mod.setK1(1.4);
		models.add(model);
		model=LanguageModel.deserialize("BM25_CISI");
		mod=(BM25)model;
		mod.setB(0.9);
		//mod.setK1(1.4);
		models.add(model);
		model=LanguageModel.deserialize("BM25_CISI");
		mod=(BM25)model;
		mod.setB(0.95);
		//mod.setK1(1.5);
		models.add(model);
		
		QueryParser qp=new QueryParser1("data/cisi/CISI.QRY","data/cisi/CISI.REL"); 
		//QueryParser qp=new QueryParser1("data/cacm/cacm.qry","data/cacm/cacm.rel"); 
		//QueryParser qp=new QueryParser1("data/cran/cran.qry","data/cran/cranqrel"); 
		ArrayList<EvalMeasure> ev=new ArrayList<EvalMeasure>();
		ev.add(new PrecisionRecall());
		ev.add(new AP());
		EvalMeasureList evMes=new EvalMeasureList(ev);
		eval(models,evMes,qp,"Results/CISI2");
	}
}
