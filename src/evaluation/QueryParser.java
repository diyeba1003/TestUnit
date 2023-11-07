package evaluation;

import java.io.RandomAccessFile;

public abstract class QueryParser {
	protected RandomAccessFile queriesFile;
	protected RandomAccessFile relevanceFile;
	String queriesFileName;
	String relevanceFileName;
	
	public QueryParser(String queriesFile,String relevanceFile){
		try{
			this.queriesFile=new RandomAccessFile(queriesFile,"r");
			this.relevanceFile=new RandomAccessFile(relevanceFile,"r");
			this.queriesFileName=queriesFile;
			this.relevanceFileName=relevanceFile;
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	@Override
	public void finalize(){
		try{
			this.queriesFile.close();
			this.relevanceFile.close();
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	public void reinit(){
		try{
			queriesFile.seek(0);
			relevanceFile.seek(0);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public String getQueriesName(){
		return  queriesFileName+" "+relevanceFileName;
	}
	
	abstract Query nextQuery();
	
	public static void main(String[] args){
		//QueryParser1 qp=new QueryParser1("cisi/CISI.QRY","cisi/CISI.REL"); 
		QueryParser1 qp=new QueryParser1("data/cacm/query.text","data/cacm/qrels.text"); 
		while(true){
			Query q=qp.nextQuery();
			if(q==null){
				break;
			}
			System.out.println(q);
		}
	}
}

class QueryParser1 extends QueryParser{
	public QueryParser1(String queriesFile,String relevanceFile){
		super(queriesFile,relevanceFile);
	}
	Query nextQuery(){
		try{
			Query ret=null;
			String id="";
			String text="";
			boolean readMode=false;
			while(true){
				long cur=this.queriesFile.getFilePointer();
				String line=this.queriesFile.readLine();
				if(line==null){
					break;
				}
				if(line.startsWith(".I")){
					if(id.length()>0){
						this.queriesFile.seek(cur);
						break;
					}
					else{
						id=line.substring(3);
						continue;
					}
				}
				if(line.startsWith(".W")){
					readMode=true;
					continue;
				}
				if(line.startsWith(".")){
					readMode=false;
					continue;
				}
				if(readMode){
					text+=line+" ";
				}
				
			}
			
			if(id.length()==0){
				return null;
			}
			else{
				ret=new Query(id,text);
			}
			int idQ=Integer.parseInt(id);
			while(true){
				long cur=this.relevanceFile.getFilePointer();
				String line=this.relevanceFile.readLine();
				if(line==null){
					break;
				}
				line=line.replaceAll("\t", " ");
				String[] st=line.split(" ");
				int i=0;
				while(i<st.length){
					if(st[i].length()>0){
						break;
					}
					i++;
				}
				int q=Integer.parseInt(st[i]);
				if(q>idQ){
					this.relevanceFile.seek(cur);
					break;
				}
				
				if(q==idQ){
					i++;
					while(i<st.length){
						if(st[i].length()>0){
							break;
						}
						i++;
					}
					ret.addRelevant(st[i]);
				}
			}
			return ret;
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	
}
