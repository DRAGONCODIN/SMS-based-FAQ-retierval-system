import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.document.Field;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.CharStream;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.PhoneticFilterFactory;
import org.apache.solr.analysis.StandardFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.apache.solr.analysis.StopFilterFactory;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tangentum.phonetix.Metaphone;

public class parser extends QueryParser{

	//No generics
	List mySms;
	static List myFaq;
	faq[] faqList = new faq[1000];
	org.w3c.dom.Document dom;
	org.w3c.dom.Document dom2;
	static final int MAX_FILES = 1;
	static final int MAX_SMS = 30;
	static final int MAX_RANKS = 100000;
	static final int TOTAL_DOMAINS = 20;
	String chkInt[] = { "0","1","2","3","4","5","6","7","8","9"};
	static int stringLength =0;
	static String domainList[] = new String[TOTAL_DOMAINS];
	int domainListLength =0;
	/*
	@AnalyzerDef(name="phonetic",
			tokenizer =
			@TokenizerDef(factory = StandardTokenizerFactory.class ),
			filters = {
			@TokenFilterDef(factory = StandardFilterFactory.class),
			@TokenFilterDef(factory = StopFilterFactory.class,
			params = @Parameter(name="words", value="stopwords.txt") ),
			@TokenFilterDef(factory = PhoneticFilterFactory.class,
			params = {
			@Parameter(name="encoder", value="DoubleMetaphone"),
			@Parameter(name="inject", value="false")
			} )
			})
	
	*/
	
	/*
	String[] files = new String[MAX_FILES];
	files[0]="faq_agriculture.xml";
	files[1]="faq_bank.xml";
	files[2]="faq_career.xml";
	files[3]="faq_gk.xml";
	files[4]="faq_health.xml";
	files[5]="faq_insurance.xml";
	files[6]="faq_irctc.xml";
	files[7]="faq_loan.xml";
	files[8]="faq_sports.xmll";
	files[9]="faq_telecom.xml";
	files[10]="faq_tourism.xml";
*/	
	String[] files = {"eng.xml","faq_agriculture.xml","faq_career.xml","faq_health.xml","faq_irctc.xml","faq_sports.xml","faq_telecom.xml","faq_insurance.xml","faq_gk.xml","faq_loan.xml","faq_bank.xml","faq_tourism.xml"};
	String[] files1 = {"eng.xml","faq_agriculture.txt","faq_bank.txt","faq_career.txt","faq_gk.txt","faq_health.txt","faq_insurance.txt","faq_irctc.txt","faq_loan.txt","faq_sports.txt","faq_telecom.txt","faq_tourism.txt"};
	rank[] Ranks = new rank [MAX_RANKS];              // declares an array of integers
	rank[] topRank = new rank [MAX_RANKS];
    //initialising Ranks array with hits = 0 and blank id
	
	
	
	//anArray = new int[10];
	public parser(CharStream arg0){
		super(arg0);
		//create a list to hold the employee objects
		mySms = new ArrayList();
		myFaq = new ArrayList();
		for (int i = 0 ; i< MAX_RANKS; i++){
			 Ranks[i] = new rank();
			 Ranks[i].setHits(0);
			 Ranks[i].setId("");
			 Ranks[i].setQuestion("");
			 Ranks[i].setDomain("");
		}
		for (int i = 0 ; i< MAX_RANKS; i++){
			 topRank[i] = new rank();
			 topRank[i].setHits(0);
			 topRank[i].setId("");
			 topRank[i].setQuestion("");
			 topRank[i].setDomain("");
		}
		
	}

	public void runParser() throws IOException, ParseException {
		
		//parse the xml file and get the dom object
		parseXmlFile();
		
		//get each employee element and create a Employee object
		parseDocument();
		
		//Iterate through the list and print the data
		//printData();
		
	}
	
	
	private void parseXmlFile(){
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			//parse using builder to get DOM representation of the XML file
			//dom = db.parse("SMS_QUERIES.xml");
			dom = db.parse("eng-mono.xml");

		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}

	
	private void parseDocument()throws IOException, ParseException{
		
		int successful_attempts =0;
		float minMaxHits =1000;
		float maxMaxHits =0;
		int correct = 0;
		int i =0;   // counter for total no of sms queries
		int avgSmsLen =0;
		float avgMaxHits =0;
		int maxHitsQueries =0;
		int domainCorrect =0;
		//For LUCENE
		//StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
		//SnowballAnalyzer analyzer = new SnowballAnalyzer("English", StopAnalyzer.ENGLISH_STOP_WORDS);
		
		Directory index = new RAMDirectory();
		IndexWriter w = new IndexWriter(index, analyzer, true,IndexWriter.MaxFieldLength.UNLIMITED);
		//Document document = new Document();
		//Lucene declarations end here
		
		//PHONETIX keyword generator ( m1 )
		
		Metaphone m1 = new Metaphone();
		
		//PHONETIX 2.0 declaration ends here
		
		
		
		
		
		//Creating Index by parsing XML files.------------------------------------------------------
		for (int j=MAX_FILES -1; j>=0;j--){
			runParser2(j);
			faqList = (faq[])myFaq.toArray(faqList);
			//Iterator it = myFaq.iterator();
			int count = 0;
			
			int chkStopWord =0;
    		while(count < myFaq.size()) {
    			Document document = new Document();
    			document.add(new Field("id",faqList[count].getId(),Field.Store.YES,
    		 			Field.Index.NO));
    			document.add(new Field("domain",faqList[count].getDomain(),Field.Store.YES,
    		 			Field.Index.NO));
    		    
    			//System.out.println(count);
    			int fields =1;
    			String s1 =  " "; 
    			String s2 = " ";
    			String domainName = "BANK";
    			//while (fields<2){
    			if (!faqList[count].getDomain().contains(domainName)){
    				//count++;
    				//continue;
    			}
    			/*********************************************************
    			 		Generating PHONETIC equivalents for FAQs
    			 *********************************************************/
    			String tokensFaq[] = new String[2000];
    			
    		/***********************************************************************************
					EXTRACTING WORDS FROM THE FAQ QUES also removes STOP-WORDS
			************************************************************************************/
    			if (fields == 1){
    				tokensFaq = extractTokens(faqList[count].getQuestion(),tokensFaq);
    			}
    			else{
    				System.out.println("WAT IS THIS????");
    				try {
	    				Thread.sleep(1);
	    			}
	    			catch (Exception e) {
	    				System.out.println(e);
					}
			      
    				tokensFaq = extractTokens(faqList[count].getAnswer(),tokensFaq);
    			}
    			String s[] = new String[2000];
    			
    		    int z,z1;
    		    z1=0;
    		    for (z=0; z<stringLength;z++){
    		    	chkStopWord = 0;
    		    /***********************************************************************
    		    		 FOLLOWING LINE OF CODE STEMS THE WORDS IN FAQ
	 			***********************************************************************/
    		    	//System.out.println(tokensFaq[z] + z +" " + tokensFaq.length);
    		    	//tokensFaq[z] = stemming(tokensFaq[z]);
    		    	s[z*2] = m1.generateKey(tokensFaq[z]);
    		    	s[z*2 +1 ] = " ";
    		    	
    		    	  		    		
    		    }
    		    if (fields ==1 ){
    		    	for (int y =0;y<(stringLength*2);y++){
    		    		s1 = s1.concat(s[y]);
    		    	}
    		    }
    		    else {
    		    	for (int y =0;y<(stringLength*2);y++){
        		    	s2 = s2.concat(s[y]);
    		    }
    		    	
    		    }
    		    fields++;
    			//}
    		    //s1 = s1.concat(s2);
    		    document.add(new Field("question",s1,Field.Store.YES,
        				Field.Index.ANALYZED));
    		    
    		    //System.out.println(s1);
    		    /*************************************************************
    		     			PHONETIC equivalents generated ...
    		     *************************************************************/
    			/*
    			Document document = new Document();
    			document.add(new Field("id",faqList[count].getId(),Field.Store.YES,
    		 			Field.Index.NO));
    			document.add(new Field("domain",faqList[count].getDomain(),Field.Store.YES,
    		 			Field.Index.NO));
    		    /*
    			document.add(new Field("question",faqList[count].getQuestion(),Field.Store.YES,
    		 			Field.Index.ANALYZED));
    		    */
    			
    			
    			
    			
    		    //document.setBoost((float)-faqList[count].getQuestion().length());
    		    w.addDocument(document);
    		    count++;
    		}
    		w.optimize();
    	}
		w.close();
		// INDEX CREATED----------------------------------------------------------------------------		
		
		//get the root elememt
		Element docEle = dom.getDocumentElement();
		int count2=0;
		//get a nodelist of <sms> elements
		NodeList nl = docEle.getElementsByTagName("SMS");
		if(nl != null && nl.getLength() > 0) {
			for(i = 0 ; i < nl.getLength();i++) {
				
				for (int it = 0 ; it< MAX_RANKS; it++){
					 topRank[it] = new rank();
					 topRank[it].setHits(-10000);
					 topRank[it].setId("");
					 topRank[it].setQuestion("");
					 topRank[it].setDomain("");
				}
				rank rankLocal = new rank();
				String[] tokens = new String [100];
				String smsText ;
				int totalRanks =0;
				//get the sms element
				Element sl = (Element)nl.item(i);
				
				//get the sms object
				sms s = getsms(sl);
				smsText = s.getText();
				if (!s.getDomain().contains("BANK")){
    				//continue;
    			}
				domainCorrect ++;
				/***********************************************************************************
				 					EXTRACTING WORDS FROM THE SMS TEXT
				************************************************************************************/
				
				tokens = extractTokens(s.getText(),tokens);
				
				/*************************************************************************************
				 		THE ABOVE LINE OF CODE ALSO REMOVES ANY STOP-WORD IF THERE
				 *************************************************************************************/
				
				
				
				int queryLength = tokens.length;
				for (int m = 0 ; m< MAX_RANKS; m++){
					 Ranks[m].setHits(0);
					 Ranks[m].setId("");
					 Ranks[m].setQuestion("");
				}
				
				/*******************************************************************************
				                              TRASH CODE 
				 ********************************************************************************/
				//search in FAQS
				/*
				Ranks[i*2].setHits(0);
    			Ranks[i*2].setId("");
    			Ranks[i*2 + 1].setHits(0);
    			Ranks[i*2 + 1].setId("");
    			*/
				/*
				int j =0;
				for(;j<MAX_FILES;j++){
					//readFaq(files1[j],s,j);
					
					runParser2(j);
					
					//System.out.println(tokens[1]);
					
					
					
					//System.out.println(myFaq.size());
					//try {
					//	  Thread.sleep(50);
					//	}
					//	catch (Exception e) {}
					faqList = (faq[])myFaq.toArray(faqList);
					Iterator it = myFaq.iterator();
					int count = 0;
					
		    		while(count < myFaq.size()) {
		    			rankLocal.setId(faqList[count].getId());
		    			
		    			rankLocal.setHits(0);
		    			//System.out.println(Ranks[j*2].getId() + " " +Ranks[j*2].getHits() );
		    			//System.out.println("RankIndex= " + Ranks[j*2].getHits() + " rankLocal = " + rankLocal.getHits());
		    			*/
				
				/*******************************************************************************
				 								TRASH CODE ENDS HERE
                 ********************************************************************************/
						int chkStopWord =0;
						int dontProcess =0;
		    			for (int l =0; l<stringLength;l++){
		    				queryLength = stringLength;
		    				dontProcess =0;
		    				chkStopWord =0;
		    				
		    				/***********************************************************************
		    				 	FOLLOWING LINE OF CODE STEMS THE WORDS IN SMS
		    				 ***********************************************************************/
		    				
		    				//tokens[l] = stemming(tokens[l]);
		    				//System.out.print(tokens[l] + " ");		    				
		    				String querystr= new String(m1.generateKey(tokens[l].toLowerCase()));
		    				//System.out.println(querystr);
		    				//byte byte1[] = tokens[l].getBytes();
		    		    	/*			
		    				try {
		    					BufferedReader in = new BufferedReader(new FileReader("stopWords.txt"));
		    					String str;
		    					while ((str = in.readLine()) != null) {
		    						if(tokens[l].equals(str)){
		    							chkStopWord =1 ;
		    							queryLength --;
		    							break;
		    						}
		    					}
		    					in.close();
		    				} catch (IOException e) {
		    				}
		    				if (chkStopWord == 1){
		    					continue;
		    				}
		    				*/
		    				/*******************************************************************************
		                      
		                      FOLLOWING CODE WHEN NOT IN GREEN COLOUR REMOVES SMS queries containg nos.   
		                 
		                 ********************************************************************************/
		    			
		    				
		 		/*
		    			
		    				
		    				for (int numChk =0;numChk < 9;numChk++){
		    					if (tokens[l].contains(chkInt[numChk])){
		    							dontProcess = 1;
		    							break;
		    						}
		    				}
		    				if (dontProcess == 1){
		    					continue;
		    				}
		    			*/
		    	/*******************************************************************************
                Query removing code ends here, if the above code is green, queries with nos will be present 
                  ********************************************************************************/			
		    				Query q;
		    			    q = new FuzzyQuery(new Term("question",querystr),0.99f,0);
		    			    
		    			    int hitsPerPage = 300;
		    			    
		    			    IndexSearcher searcher = new IndexSearcher(index, true);
		    			    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		    			    searcher.search(q, collector);
		    			    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    			    //System.out.println("Found " + hits.length + " hits.");
		    			    for(int hitCount=0;hitCount<hits.length;++hitCount) {
		    			      int docId = hits[hitCount].doc;
		    			      Document d = searcher.doc(docId);
		    			      
		    // remove the comments from the follwing code if u want to see hts for each token of sms string
		    			     
		    			      /*
		    			      if ( i== 10){
		    			    	  System.out.println(tokens[l] +" "+ d.get("question"));
		    			      
				    			try {
				    				Thread.sleep(1000);
				    			}
				    			catch (Exception e) {
				    				System.out.println(e);
								}
		    			      }
		    			      */
		    			      
		    			      
		    			      int n;
		    			      for (n =0;n<totalRanks;n++){
		    			    	  if (d.get("id").contains(Ranks[n].getId()))
		    			    		  break;
		    			    	  //System.out.println((hitCount + 1) + ". " + d.get("content") + " " + d.get("content2"));
		    			    
		    			      }
		    			      if (n==totalRanks){   // its a new faq
		    			    	  Ranks[n].setId(d.get("id"));
		    			    	  Ranks[n].setQuestion(d.get("question"));
		    			    	  Ranks[n].setDomain(d.get("domain"));
		    			    	  Ranks[n].setHits(hits[hitCount].score);
		    			    	  totalRanks++;
		    			      }
		    			      else if (Ranks[n].getId().contains(d.get("id"))) {
		    			    	  Ranks[n].incHits(hits[hitCount].score);
		    			      }
		    			    }
		    			    searcher.close();
		    			}
		    			//Arrays.sort(Ranks);
		    			float maxHits = Ranks[0].getHits();
		    			int q;
		    			
		    			for (q=1;q<totalRanks;q++){
		    				if (Ranks[q].getHits() > maxHits){
		    					maxHits = Ranks[q].getHits();
		    				}
		    			}
		    			int totalTopHits =0;
		    			
	/** IF RESSULTS WITH 0 HITS are to be ignored, uncomment following lines*/
		    			
		    			/*
		    			if (maxHits == 0) {
		    				System.out.println(s.getId() +" No hits found " + totalRanks);
		    			}
		    			
		    			else {
		    			*/	
		    				
		    				
		    				for (int p =0; p<totalRanks;p++)
		    					if (Ranks[p].getHits() >= maxHits/2){
		    						totalTopHits++;
		    						topRank[totalTopHits].setId(Ranks[p].getId());
		    						topRank[totalTopHits].setQuestion(Ranks[p].getQuestion());
		    						topRank[totalTopHits].setDomain(Ranks[p].getDomain());
		    						topRank[totalTopHits].setHits(Ranks[p].getHits());
		    						//System.out.println("sms query " + s.getId() + " maxHits= " + maxHits + " $" + Ranks[p].getId() + "$");
		    					}
		    				successful_attempts ++;
		    			//}
		    			
		    			if (maxHits >= 0){
		    			/********************************************
		    			* following code multiplies Ranks with the FAQ LENGTH
		    				 
		    			for (int p=0;p<totalTopHits;p++){
		    				int effHit = 1;

		    				//effHit = topRank[p].getHits()*(-1)*(topRank[p].getQuestion().length());
		    				//topRank[p].setHits(effHit);
		    			}
		    			**********************************************/
		    			//boostByDomain(totalTopHits);
		    			Arrays.sort(topRank);
		    			String none = "NONE";
		    			/*
		    			if (((float)maxHits) < ((float)queryLength*1.2)){
		    				topRank[0].setId(none);
		    				topRank[0].setDomain(none);
		    				topRank[1].setId(none);
		    				topRank[1].setDomain(none);
		    				//maxHitsQueries ++;
		    				//avgMaxHits += maxHits;
		    				System.out.println("changed " + topRank[0].getId() + " " + topRank[1].getId());
		    			}
		    			
		    				
		    			if (topRank[0].getId().equals("")){
		    				topRank[0].setDomain(none);
		    				topRank[0].setId("NONE");
		    				topRank[1].setId("NONE");
		    				topRank[1].setDomain(none);
		    				
		    			}
		    			if (topRank[1].getId().equals("")){
		    				topRank[1].setId(topRank[0].getId());
		    				topRank[1].setDomain(topRank[0].getDomain());
		    			}
		    			
		    			else {
		    				//boostByDomain(totalTopHits);
		    			}*/
		    			System.out.print(" matches = "+ s.getMatches() +" "  );
		    			int tempCorrect =0;
		    			//Arrays.sort(topRank);
		    			avgSmsLen += queryLength;
		    			if (s.getMatches().equals(none)){
		    				//maxHitsQueries ++;
		    				//avgMaxHits += maxHits;
		    				if (maxHits>maxMaxHits){
		    					maxMaxHits = maxHits;
		    				}
		    			}
		    			else{
		    				maxHitsQueries ++;
		    				avgMaxHits += maxHits;
		    				if (maxHits<minMaxHits){
		    					minMaxHits = maxHits;
		    				}
		    			}
		    			System.out.print(" maxHits= " + maxHits + " " + topRank[0].getId());
		    			if (s.getMatches().contains(topRank[0].getId()) || ((s.getMatches().contains(topRank[1].getId()))&& (topRank[1].getHits() <=(float) topRank[0].getHits()) )){
		    				//if (maxHits >= (queryLength/2)) {
		    				tempCorrect =1;	
		    				correct++;
		    				if (!s.getMatches().equals(none)){
		    				//correct++;
		    				
		    				}
		    				//}
		    			}
		    			//if (s.getMatches().equals(none)){
		    			if ((s.getDomain().contains(topRank[0].getDomain())) || (s.getDomain().contains(topRank[1].getDomain()))){
		    				//domainCorrect ++;
		    			}//}
		    			//System.out.println("");	
		    			System.out.println(" corect = " + correct + " " + topRank[1].getId() );
		    			
		    			}
		    			
		    			/*
		    			try {
					Thread.sleep(500);
				}
				catch (Exception e) {
						System.out.println(e);
						}
				
				
				
				//runParser2("faq_irctc.xml",s.getText());
				
				
				//add it to list
				try {
					  Thread.sleep(500);
					}
					catch (Exception e) {}*/
				mySms.add(s);
				count2++;
			}
		}
		System.out.println(successful_attempts);
		System.out.println("Total = " + i + " correct = " + correct);
		System.out.println("avg sms length = " + (double)avgSmsLen/i);
		System.out.println("avg Hits = " + (double)avgMaxHits/maxHitsQueries + "total = " + maxHitsQueries);
		System.out.println("domains correct  = " + domainCorrect);
		System.out.println("min hit  = " + minMaxHits +"max hit = " + maxMaxHits );
	}
	public void runParser2(int fileNo) {
		
			//parse the xml file and get the dom object
			parseXmlFile2(fileNo);
		
			//get each employee element and create a Employee object
			parseDocument2(fileNo);
		
			//Iterate through the list and print the data
			//printData();
			/*try {
				  Thread.sleep(50000);
				}
				catch (Exception e) {}
		*/
	}
	
	
	private void parseXmlFile2(int fileNo){
			//get the factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
			try {
			
				//Using factory get an instance of document builder
				DocumentBuilder db = dbf.newDocumentBuilder();
			
				//parse using builder to get DOM representation of the XML file
				dom2 = db.parse(files[fileNo]);
			

			}catch(ParserConfigurationException pce) {
				pce.printStackTrace();
			}catch(SAXException se) {
				se.printStackTrace();
			}catch(IOException ioe) {
				ioe.printStackTrace();
			}
	}

	
	private void parseDocument2(int fileNo){
			
			myFaq.clear();
			//get the root elememt
			Element docEle = dom2.getDocumentElement();
			int k = 1;
			
			
			//static rank ;
			
			//get a nodelist of <sms> elements
			NodeList nl = docEle.getElementsByTagName("FAQ");
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i < nl.getLength();i++) {
				
					//get the faq element
					Element sl = (Element)nl.item(i);
				
					//get the faq object
					faq f = getfaq(sl);
					
					/*
					extractTokens(f.getQuestion(),tokens);
					
					for (int l = 0; k < tokens.length ; k ++){
						
					}
					*/
					
					
					
		/*			if ((f.getQuestion()).contains(ques) && k==1){
						System.out.println("    "  +  f.getId()  +  "   ");
						k++;
					}
		*/				
				
					//add it to list
					myFaq.add(f);
				}
				
				
		//		writeFaq(files1[fileNo]);
			
			
			}
	}

	static String[] extractTokens (String a, String [] tokens){
		
		int chkStopWords =0;
		int i=0;
		tokens = a.split(" ");
		String tokens1[] = new String [2000];
		//System.out.println(tokens.length);
		for (int l =0; l<tokens.length;l++){
			chkStopWords =0;
			//System.out.println(querystr);
			try {
				BufferedReader in = new BufferedReader(new FileReader("stopWords.txt"));
				String str;
				while ((str = in.readLine()) != null) {
					if(tokens[l].equals(str)){
						chkStopWords = 1;
						break;
					}
				}
				in.close();
			} catch (IOException e) {
			}
			if (chkStopWords == 0){
				tokens1[i++]=tokens[l];
			}
		}
		stringLength = i;
		return tokens1;
	}

	/**
	 * I take an sms element and read the values in, create
	 * an sms object and return it
	 * @param empEl
	 * @return
	 */
	private sms getsms(Element smsS1) {
		
		//for each <sms> element get text or int values of 
		//name ,id, age and name
		String id = getTextValue(smsS1,"SMS_QUERY_ID");
		String text = getTextValue(smsS1,"SMS_TEXT");
		
		String matches = getTextValue(smsS1,"ENGLISH");
		String domain = calcDomain(matches);
		//Create a new sms with the value read from the xml nodes
		sms s = new sms(id,text,matches,domain);
		
		return s;
	}
	
	private faq getfaq(Element faqF1) {
		
		//for each <sms> element get text or int values of 
		//name ,id, age and name
		String id = getTextValue(faqF1,"FAQID");
		String ques = getTextValue(faqF1,"QUESTION");
		String ans = getTextValue(faqF1,"ANSWER");
		String domain = getTextValue(faqF1,"DOMAIN");
		
		//Create a new sms with the value read from the xml nodes
		faq f = new faq(id,ques,ans,domain);
		
		return f;
	}


	/**
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content 
	 * i.e for <sms><name>John</name></sms> xml snippet if
	 * the Element points to sms node and tagName is name I will return John  
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element sl = (Element)nl.item(0);
			textVal = sl.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	private String calcDomain(String id){
		if (id.contains("CAREER")){
			return "ENG_CAREER";
		}
		if (id.contains("AGRICULTURE")){
			return "AGRICULTURE";
		}
		if (id.contains("GK")){
			return "GK";
		}
		if (id.contains("HEALTH")){
			return "ENG_HEALTH";
		}
		if (id.contains("INSURANCE")){
			return "ENG_INSURANCE";
		}
		if (id.contains("INDIAN_RAILWAYS")){
			return "INDIAN_RAILWAYS";
		}
		if (id.contains("SPORTS")){
			return "ENG_SPORTS";
		}
		if (id.contains("TELECOMMUNICATION"))
			return "TELECOMMUNICATION";
		if (id.contains("TOURISM"))
			return "ENG_TOURISM";
		if (id.contains("BANK"))
			return "ENG_BANK";
		if (id.contains("LOAN"))
			return "ENG_LOAN";
		if (id.contains("INTERNAL_DEVICES"))
			return "INTERNAL DEVICES";
		if (id.contains("PERSONALITY_DEVELOPMENT"))
			return "PERSONALITY DEVELOPMENT";
		if (id.contains("RECIPES"))
			return "RECIPES";
		if (id.contains("VISA"))
			return "VISA";
		if (id.contains("WEB")){
			return "WEB";
		}
		if (id.contains("NONE"))
			return "NONE";
		return id;
	}
	private void boostByDomain(int totalTopHits){
		
		int n=0;
		int i=0;
		int j=0;
		int domainHits[] = new int[TOTAL_DOMAINS];
		while (i<totalTopHits){
			j=0;
			while (j<n){
				if (domainList[j].equals(topRank[i].getDomain())){
					domainHits[j]++;
					break;
				}
				j++;
			}
			if (j==n){
				domainList[n++] = topRank[i].getDomain();
				domainHits[j] = 1;
			}
			i++;
		}
		i=0;
		while (i<totalTopHits){
			j=0;
			while (j<n){
				if (domainList[j].equals(topRank[i].getDomain())){
					topRank[i].setHits(topRank[i].getHits() * domainHits[j]);
					break;
				}
				j++;
			}
			i++;
		}
			
		
	}
	
	
	/**
	 * Calls getTextValue and returns a int value
	 * @param ele
	 * @param tagName
	 * @return
	 */
	////////////////////////private int getIntValue(Element ele, String tagName) {
		//in production application you would catch the exception
	///////////////////////	return Integer.parseInt(getTextValue(ele,tagName));
	//////////////////////}
	
	
	
	public static void writeFaq(String filename) {
        
        ObjectOutputStream outputStream = null;
        
        try {
            
            //Construct the LineNumberReader object
            outputStream = new ObjectOutputStream(new FileOutputStream(filename));
            

    		Iterator it = myFaq.iterator();
    		while(it.hasNext()) {
    			outputStream.writeObject(it.next());
    			}
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the ObjectOutputStream
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
	
	
	//reads a text file and gets  the rank of every faq
	/*
	public static void readFaq(String filename, sms sms1, int fileNo) {
    
		ObjectInputStream inputStream = null;
		String tokens [] = new String[100];
		rank rankLocal = new rank();
		try {
        
			File file = new File(filename);
			/*RandomAccessFile randomAccessFile = new RandomAccessFile(filename,"r");
			randomAccessFile.seek(0);
			System.out.println(randomAccessFile.getFilePointer());
			randomAccessFile.close() ;
			
			//File file = new File(filename);
			RandomAccessFile randomAccessFile1 = new RandomAccessFile(filename,"r");
			//randomAccessFile.seek(0);
			System.out.println(randomAccessFile1.getFilePointer());
			randomAccessFile.close() ;
			*/
			//Construct the ObjectInputStream object
			/*
			inputStream = new ObjectInputStream(new FileInputStream(file));
        
			faq faq1= null;
			/*if (inputStream.available() == 0){
				System.out.println("llllll");
			}*/
				/*
			while ((faq1 = (faq) inputStream.readObject()) != null) {
            
				if (faq1 instanceof faq) {
            
                //System.out.println(((faq)faq1).toString());
                
                extractTokens(sms1.getText(),tokens);
				rankLocal.setId(sms1.getId());
				for (int l = 0; l < tokens.length ; l ++){
					if (sms1.getText().contains(tokens[l]))
						rankLocal.incHits();
				}
				
				if (rankLocal.getHits() > Ranks[fileNo*2].getHits()){
					Ranks[fileNo*2 + 1] = Ranks[fileNo*2];
					Ranks[fileNo*2] = rankLocal;
				}
				
				else if (rankLocal.getHits() > Ranks[fileNo*2 + 1].getHits()){
					Ranks[fileNo*2 + 1] = rankLocal;
				}
                
                
                //System.out.println(rank1.getHits() + rank1.getId());
            }
            
        }
        
     
		} catch (EOFException ex) { //This exception will be caught when EOF is reached
        System.out.println("End of file reached.");
		} catch (ClassNotFoundException ex) {
        ex.printStackTrace();
		} catch (FileNotFoundException ex) {
        ex.printStackTrace();
		} catch (IOException ex) {
        ex.printStackTrace();
		} finally {
			//Close the ObjectInputStream
			try {
				if (inputStream != null) {
                inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	    /**
	     * @param args the command line arguments
	     */
	    
	
	
	
	
	
	
	
	
	
	/**
	 * Iterate through the list and print the 
	 * content to console
	 */
	private void printData(){
		
		System.out.println("No of smss '" + myFaq.size() + "'.");
		
		Iterator it = myFaq.iterator();
		while(it.hasNext()) {
			System.out.println(it.next().toString());
		}/*
		System.out.println ("Query Results");
		for (int i = 0; i < MAX_FILES*2; i++)
		{
			System.out.println(Ranks[i].getId() + Ranks[i].getHits());
		}*/
		
		
	}

	public String stemming (String sms)
	{
		sms = sms.concat(" ");
		String tokens = new String();
		Stemmer s = new Stemmer();
		char[] w = new char[501];
		//int tokensCtr = 0;
		for (int i = 0; i < 1; i++)
		     /* try*/
		      {
		    	  //String input = a.nextLine();
		    	  //FileInputStream in = new FileInputStream("test.txt"/*args[i]*/);
		         //System.out.println(i);
		    	  int z=0;
		    	  byte temp [] = new byte[100];
		    	  temp = sms.getBytes();
		    	  
		         try
		         { while(z<temp.length)

		           {  
		        	 //System.out.println("working");
		        	 
		        	 //int ch = in.read();
		        	  int ch = temp[z++];
		        	  
		              if (Character.isLetter((char) ch))
		              {
		                 int j = 0;
		                 while(z<temp.length)
		                 {  
		                	 ch = Character.toLowerCase((char) ch);
		                    w[j] = (char) ch;
		                    if (j < 500) j++;
		                    //ch = in.read();
		                    ch = temp[z++];
		                    if (!Character.isLetter((char) ch))
		                    {
		                       /* to test add(char ch) */
		                       for (int c = 0; c < j; c++) s.add(w[c]);

		                       /* or, to test add(char[] w, int j) */
		                       /* s.add(w, j); */

		                       s.stem();
		                       {  String u;

		                          /* and now, to test toString() : */
		                          u = s.toString();

		                          /* to test getResultBuffer(), getResultLength() : */
		                          /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */
		                          if (true){
		                        	  tokens = u;
		                        	  //System.out.print(u);
		                          }
		                       }
		                       break;
		                    }
		                 }
		              }
		              if (ch < 0) break;
		              //System.out.print((char)ch);
		           }
		         }finally{}
		         
		        /* catch (IOException e)
		         {  System.out.println("error reading " + args[i]);
		            break;
		         }*/
		      }
		     return tokens;
	}
	
	
	public static void main(String[] args) throws IOException, ParseException {
		
		
		//create an instance
		parser dpe = new parser(null);
		
		//call run example
		/*for(int j=0;j<MAX_FILES;j++){
			dpe.runParser2(j);
		}
		*/
		
		dpe.runParser();
		
		
		
	}

}
