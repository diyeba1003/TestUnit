package indexation;

import java.util.HashMap;

import core.Document;

/**
 * 
 * Format of input files :
 * .I <id>
 * .T 
 * <Title>
 * .A <Author>
 * .K
 * <Keywords>
 * .W
 * <Text>
 * .X
 * <Links> 
 *
 */
class ParserCISI extends Parser{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public ParserCISI(){
		super(".I");
	}
	
	
	// Returns the Document object corresponding to the string as argument
	// If the string is well formatted for the CISI format, 
	// the returned document contains its id, a text that concatenates title, authors, keywords and text fields
	// separated fields are given in the other variable of the document 
	Document getDocument(String str) throws InvalidFormatDocumentException {
		//System.out.println("xx:"+str+"</end>");
		
		HashMap<String,String> other=new HashMap<String,String>();
		String st[]=str.split("\n");
		boolean modeT=false;
		boolean modeA=false;
		boolean modeK=false;
		boolean modeW=false;
		boolean modeX=false;
		String info="";
		String id="";
		String text="";
		String author="";
		String keyWords="";
		String links="";
		String title="";
		for(String s:st){
			if(s.startsWith(".I")){
				id=s.substring(3);
				continue;
			}
			if(s.startsWith(".")){
				if(modeW){
					text=info;
					info="";
					modeW=false;
				}
				if(modeA){
					author=info;
					info="";
					modeA=false;
				}
				if(modeK){
					keyWords=info;
					info="";
					modeK=false;
				}
				if(modeT){
					title=info;
					info="";
					modeT=false;
				}
				if(modeX){
					other.put("links", links);
					info="";
					modeX=false;
				}
			}
			
			if(s.startsWith(".W")){
				modeW=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".A")){
				modeA=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".K")){
				modeK=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".T")){
				modeT=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".X")){
				modeX=true;
				continue;
			}
			if(modeX){
				String l[]=s.split("\t");
				if(!l[0].equals(id)){
					links+=l[0]+";";
				}
				continue;
			}
			if((modeK) || (modeW) || (modeA) || (modeT)){
				info+=" "+s;
			}
		}
	
		if(modeW){
			text=info;
			info="";
			modeW=false;
		}
		if(modeA){
			author=info;
			info="";
			modeA=false;
		}
		if(modeK){
			keyWords=info;
			info="";
			modeK=false;
		}
		if(modeX){
			other.put("links", links);
			info="";
			modeX=false;
		}
		if(modeT){
			title=info;
			info="";
			modeT=false;
		}
		other.put("title", title);
		other.put("text", text);
		other.put("author", author);
		other.put("keywords", keyWords);
		
		if (id==""){
				throw new InvalidFormatDocumentException("no id");
		}
		
		Document doc=new Document(id,title+" \n "+author+" \n "+keyWords+" \n "+text,other);
		return doc;
	}
	
	public static void main(String[] args){
		ParserCISI parser=new ParserCISI();
		parser.init("data/cisi/cisi.txt");
		try {
			Document doc=parser.nextDocument();
			
		} catch (NonInitializedParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}