package indexation;

import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import models.IRModel;
import core.*;
public class Index implements Serializable {

	
	private static final long serialVersionUID = 1L;
	private String name;
	private transient RandomAccessFile index;
	private transient RandomAccessFile inverted;
	private HashMap<String,String> docFrom;
	private HashMap<String,String> docs;
	private HashMap<String,String> stems;
	private Parser parser;
	private TextRepresenter textRepresenter;
	
	
	public Index(String name,Parser parser,TextRepresenter textRepresenter){
		this.name=name;
		this.parser=parser;
		this.textRepresenter=textRepresenter;
	}
	
	public String getName(){
		return name;
	}
	
	public TextRepresenter getTextRepresenter(){
		return textRepresenter;
	}	
	private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
		this.name = (String) ois.readObject();
		this.docFrom = (HashMap<String,String>) ois.readObject();
		this.docs = (HashMap<String,String>) ois.readObject();
		this.stems = (HashMap<String,String>) ois.readObject();
		this.parser = (Parser) ois.readObject();
		this.textRepresenter=(TextRepresenter)ois.readObject();
		index=new RandomAccessFile(name+"_index","r");
		inverted=new RandomAccessFile(name+"_inverted","r");
	}
	
	private void writeObject(final ObjectOutputStream oos) throws IOException {
		oos.writeObject(this.name);
		oos.writeObject(this.docFrom);
		oos.writeObject(this.docs);
		oos.writeObject(this.stems);
		oos.writeObject(this.parser);
		oos.writeObject(this.textRepresenter);
	}
	
	public void index(String fileName){
		try{
			
			File fi=new File(name+"_index");
			File fi2=new File(name+"_inverted");
			if (fi.exists()){
				fi.delete();
				fi2.delete();
			}
			
			parser.init(fileName);
			Document doc;
			HashMap<String,Integer> stemsLengths=new LinkedHashMap<String,Integer>();
			stems=new LinkedHashMap<String,String>();
			docs=new LinkedHashMap<String,String>();
			docFrom=new LinkedHashMap<String,String>();
			int nb=0;
			
			index=new RandomAccessFile(name+"_index","rw");
			long cur=0;
			System.out.println("Compute positions");
			while((doc=parser.nextDocument())!=null){
				String id=doc.getId();
				docFrom.put(id+"", doc.get("from"));
				String text=doc.getText();
				HashMap<String,Integer> h=textRepresenter.getTextRepresentation(text);
				StringBuilder stDoc=new StringBuilder();
				for(String s:h.keySet()){
					Integer n=stemsLengths.get(s);
					n=(n==null)?0:n;
					int tf=h.get(s);
					String st="("+id+"="+tf+");";
					
					stemsLengths.put(s,n+st.getBytes("UTF-8").length);
					stDoc.append("("+s+"="+tf+");");
				}
				byte[] b=stDoc.toString().getBytes("UTF-8");
				int l=b.length;
				String std=id+":";
				byte[] b2=std.getBytes("UTF-8");
				docs.put(id+"", cur+b2.length+":"+l);
				index.seek(cur);
				index.write(b2);
				index.write(b);
				cur+=b2.length+l;
				nb++;
				System.out.println(nb+" traites");
			}
			index.close();
			HashMap<String,Long> offSets=new LinkedHashMap<String,Long>();
			cur=0;
			inverted=new RandomAccessFile(name+"_inverted","rw");
			for(String s:stemsLengths.keySet()){
					
					String st=s+":";
					byte[] b=st.getBytes("UTF-8");
					inverted.seek(cur);
					inverted.write(b);
					long l=stemsLengths.get(s);	;
					stems.put(s,cur+b.length+":"+l);
					offSets.put(s,cur+b.length);
					cur+=b.length+l;
			}
			
			System.out.println("Create index");
			nb=0;
			parser.init(fileName);
			while((doc=parser.nextDocument())!=null){
				String id=doc.getId();
				String text=doc.getText();
				HashMap<String,Integer> h=textRepresenter.getTextRepresentation(text);
				for(String s:h.keySet()){
					Long n=offSets.get(s);
					String st="("+id+"="+h.get(s)+");";
					byte[] b=st.getBytes("UTF-8");
					inverted.seek(n);
					inverted.write(b);
					offSets.put(s,n+b.length);
				}
				nb++;
				System.out.println(nb+" traites");
			}
			inverted.close();
			
			index=new RandomAccessFile(name+"_index","r");
			inverted=new RandomAccessFile(name+"_inverted","r");
			serialize(name);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}

	}
	
	
	public void serialize(String fileName){
		FileOutputStream fos =null;
		ObjectOutputStream oos= null;
		try {
				fos = new FileOutputStream(fileName);
				oos= new ObjectOutputStream(fos);
				oos.writeObject(this); 
				oos.flush();
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
				try {
					oos.close();
					fos.close();
				}
				catch(Exception e){
					e.printStackTrace();
					throw new RuntimeException(e);
				}
		}
		
	}
	
	public static Index deserialize(String fileName) { 
		Index ret=null;
		try{
			FileInputStream fis = new FileInputStream(fileName);
			ObjectInputStream ois= new ObjectInputStream(fis);
			try {	
				ret = (Index) ois.readObject(); 
			} finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}
		}
		catch(ClassNotFoundException e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		catch(IOException e){
			e.printStackTrace();
			throw new RuntimeException(e);
			
		}
		return(ret);
	}
	
	
	@Override
	protected void finalize(){
		try{
			if(index!=null){
				index.close();
			}
			if(inverted!=null){
				inverted.close();
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	
	
	
	public HashMap<String,Integer> getTfsForDoc(String doc){
		try{
			/*if(index==null){
				load();
			}*/
			HashMap<String,Integer> tfs=new HashMap<String,Integer>();
			String o=docs.get(doc);
			if(o==null){
				return tfs;
			}
			String st[]=o.split(":"); 
			if(st.length!=2){
	    		throw new RuntimeException("Pb with doc "+doc+":"+o);
	    	}
			int off=Integer.valueOf(st[0]);
			int len=Integer.valueOf(st[1]);
			byte[] b=new byte[len];
			index.seek(off);
			index.read(b);
			String str = new String(b, "UTF-8");
			if(str.length()==0){
				return tfs;
			}
			//System.out.println(str);
			st=str.split(";");
			for(String s:st){
				s=s.substring(1, s.length()-1);
				String ss[]=s.split("=");
				if(ss.length!=2){
					throw new RuntimeException("Format pb on "+s);
				}
				tfs.put(ss[0], Integer.valueOf(ss[1]));
			}
			return tfs;
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public HashMap<String,Integer> getTfsForStem(String stem){
		try{
			
			
			HashMap<String,Integer> tfs=new HashMap<String,Integer>();
			String o=stems.get(stem);
			if(o==null){
				return tfs;
			}
			String st[]=o.split(":"); 
			if(st.length!=2){
	    		throw new RuntimeException("Pb with stem "+stem+":"+o);
	    	}
			int off=Integer.valueOf(st[0]);
			int len=Integer.valueOf(st[1]);
			byte[] b=new byte[len];
			inverted.seek(off);
			inverted.read(b);
			String str = new String(b, "UTF-8");
			st=str.split(";");
			//System.out.println(off+" "+len+" "+str);
			for(String s:st){
				s=s.substring(1, s.length()-1);
				String ss[]=s.split("=");
				if(ss.length!=2){
					throw new RuntimeException("Format pb on "+s);
				}
				tfs.put(ss[0], Integer.valueOf(ss[1]));
			}
			return tfs;
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	
	public String getStrDoc(String id) throws FileNotFoundException,IOException{
		/*if(docFrom==null){
			load();
		}*/
		if(!docFrom.containsKey(id)){
			return null;
		}
		String from=docFrom.get(id);
		String st[]=from.split(";");
		if(st.length!=3){
			throw new RuntimeException("Format pb on docFrom "+from);
		}
		RandomAccessFile f=new RandomAccessFile(st[0],"r");
		Integer off=Integer.valueOf(st[1]);
		Integer len=Integer.valueOf(st[2]);
		byte[] b=new byte[len];
		f.seek(off);
		f.read(b);
		String str = new String(b, "UTF-8");
		f.close();
		return str;
	}
	public Document getDoc(String id) throws FileNotFoundException,IOException, InvalidFormatDocumentException{
		return parser.getDocument(getStrDoc(id));	
	}
	
	public HashMap<String,String> getDocs(){
		return docs;
	}
	public HashMap<String,String> getStems(){
		return stems;
	}
	
	public static void main(String[] args){
		try{
			Index index;
			index=new Index("index/cisi",new ParserCISI(),new Stemmer());
			index.index("data/cisi/cisi.txt");
			
			System.out.println(index.getDoc("55").getText());
			System.out.println(index.getTfsForDoc("55"));
			System.out.println(index.getTfsForStem("attempt"));
		}
		catch(Exception e){
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
}
