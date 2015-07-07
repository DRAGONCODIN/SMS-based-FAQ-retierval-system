import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
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
import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import com.tangentum.phonetix.DoubleMetaphone;
import com.tangentum.phonetix.Metaphone;
import java.io.PrintWriter;
import java.io.FileWriter;
public class parser extends QueryParser{

	//No generics
	List mySms;
	static List myFaq;
	static faq[] faqList = new faq[10000];
	static sms[] smsList = new sms[10000];
	org.w3c.dom.Document dom;
	org.w3c.dom.Document dom2;
	static final int MAX_FILES = 6;
	static final int MAX_SMS = 30;
	static final int MAX_RANKS = 100000;
	static final int TOTAL_DOMAINS = 30;
	String chkInt[] = { "0","1","2","3","4","5","6","7","8","9"};
	static int stringLength =0;
	static String domainList[] = new String[TOTAL_DOMAINS];
	static int domainsListed =0;
	int domainListLength =0;
	
	String[] files = {"1.txt","2.txt","3.txt","4.txt","5.txt","eng.xml","faq_sports.xml","faq_telecom.xml","faq_insurance.xml","faq_gk.xml","faq_loan.xml","faq_bank.xml","faq_tourism.xml"};
	String[] files1 = {"1.txt","2.txt","3.txt","4.txt","5.txt","eng.xml","faq_agriculture.txt","faq_bank.txt","faq_career.txt","faq_gk.txt","faq_health.txt","faq_insurance.txt","faq_irctc.txt","faq_loan.txt","faq_sports.txt","faq_telecom.txt","faq_tourism.txt"};
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

	public void runParser() throws Exception {
		
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
			dom = db.parse("eng-mono-masked.xml");

		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}

	
private void parseDocument()throws Exception{
		
		int counter=0;
		int successful_attempts =0;
		int totalNone =0;
		int totalNonNone =0;
		int correct = 0;
		int i =0;   // counter for total no of sms queries
		float avgSmsLen =0;
		float avgMaxHits =0;
		float maxHitsQueries =0;
		int domainCorrect =0;
		float maxScoreChk   = 0;
		PrintWriter pw = null;
		
		//For LUCENE
		//StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
		//SnowballAnalyzer analyzer = new SnowballAnalyzer("English", StopAnalyzer.ENGLISH_STOP_WORDS);
		
		Directory index = new RAMDirectory();
		//IndexWriter w = new IndexWriter(index, analyzer, true,IndexWriter.MaxFieldLength.UNLIMITED);
		//Document document = new Document();
		//Lucene declarations end here
		
		//PHONETIX keyword generator ( m1 )
		
		Metaphone m1 = new Metaphone();
		
		//PHONETIX 2.0 declaration ends here
		
		
		int testRun = 0 ;
		int totalOneDomain = 0 ;
		while (testRun < 1){
			IndexWriter w = new IndexWriter(index, analyzer, true,IndexWriter.MaxFieldLength.UNLIMITED);
			
			totalOneDomain = 0;
			totalNonNone = 0 ;
			correct =0;
		
		//Creating Index by parsing XML files.------------------------------------------------------
		for (int j=MAX_FILES -1; j>=0;j--){
			if (j==5){
				
				runParser2(j);
				//continue;
			}
			else {
				continue;
				//readFaq(files[j]);
			}
			faqList = (faq[])myFaq.toArray(faqList);
			writeFaq("wfaq.txt",myFaq.size());
			//Iterator it = myFaq.iterator();
			int count = 0;
			
			int chkStopWord =0;
			//System.out.println(domainsListed);
    		while(count < myFaq.size()) {
    			if (!faqList[count].getDomain().contains(domainList[testRun])){
    				//count++;
    				//continue;
    			}
    			totalOneDomain ++;
    			//System.out.println(totalOneDomain + " " + domainList[testRun]);
    			Document document = new Document();
    			document.add(new Field("id",faqList[count].getId(),Field.Store.YES,
    		 			Field.Index.NO));
    			document.add(new Field("domain",faqList[count].getDomain(),Field.Store.YES,
    		 			Field.Index.NO));
    		    
    			//System.out.println(count);
    			int fields =1;
    			String s1 =  " "; 
    			String s2 = " ";
    			while (fields<2){
    			if (!faqList[count].getDomain().equals("ENG_CAREER")){
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
    			}
    		    //s1 = s1.concat(s2);
    			faqList[count].setQuestion(s1);
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
    		//writeFaq("19012012.txt",myFaq.size());
    	}
		w.close();
		//System.out.println(w.docCount());
		//System.out.println(totalOneDomain);
		//System.exit(0);
		// INDEX CREATED----------------------------------------------------------------------------		
		//writeFaq("19012012.txt",myFaq.size());
		//get the root elememt
		Element docEle = dom.getDocumentElement();
		int count2=0;
		//get a nodelist of <sms> elements
		NodeList nl = docEle.getElementsByTagName("SMS");
		try {

		     

	        pw = new PrintWriter(new FileWriter("ANS1.txt",true));
	        System.out.println(nl.getLength());
		if(nl != null && nl.getLength() > 0) {
			for(i = 0 ; i < nl.getLength();i++) {
				
				
				String smsShortened = " ";
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
				mySms.add(s);
				smsText = s.getText();
				
				if (!s.getDomain().contains(domainList[testRun])){
				//if (!s.getDomain().contains("NONE")){
		    				
					//continue;
    			}
				else
					totalNonNone++;
				//domainCorrect ++;
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
						int  l ;
						

		    			
						
		    			for (l =0; l<stringLength;l++){
		    				queryLength = stringLength;
		    				dontProcess =0;
		    				chkStopWord =0;
		    				
		    				/***********************************************************************
		    				 	FOLLOWING LINE OF CODE STEMS THE WORDS IN SMS
		    				 ***********************************************************************/
		    				
		    				//tokens[l] = stemming(tokens[l]);
		    				//System.out.print(tokens[l] + " ");
		    				smsShortened = smsShortened.concat(m1.generateKey(tokens[l].toLowerCase()));
		    				smsShortened = smsShortened.concat(" ");
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
		    			    
		    			    int hitsPerPage = 1000;
		    			    
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
		    			      if (maxScoreChk < hits[hitCount].score)
		    			    	  maxScoreChk = hits[hitCount].score;
		    			      
		    			      int n;
		    			      for (n =0;n<totalRanks;n++){
		    			    	  if (d.get("id").equals(Ranks[n].getId()))
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
		    			      else if (Ranks[n].getId().equals(d.get("id"))) {
		    			    	  
		    			    	  
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
		    					if (Ranks[p].getHits() == maxHits){
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
		    			//boostByDomain(totalRanks);
		    			Arrays.sort(topRank);
		    			Arrays.sort(Ranks);
		    			String none = "NONE";
		    			/*
		    			if (((float)maxHits) < ((float)queryLength*1.15)){
		    				topRank[0].setId(none);
		    				topRank[0].setDomain(none);
		    				topRank[1].setId(none);
		    				topRank[1].setDomain(none);
		    				//maxHitsQueries ++;
		    				//avgMaxHits += maxHits;
		    				//System.out.println("changed " + topRank[0].getId() + " " + topRank[1].getId());
		    			}
		    			if (topRank[0].getId().equals("")){
		    				topRank[0].setDomain(none);
		    				topRank[0].setId("NONE");
		    				
		    				
		    			}
		    			if (topRank[1].getId().equals("")){
		    				topRank[1].setId(topRank[0].getId());
		    				topRank[1].setDomain(topRank[0].getDomain());
		    			}
		    			
		    			else {
		    				//boostByDomain(totalTopHits);
		    			}
		    			//System.out.print(" matches = "+ s.getMatches() +" "  );
		    			int tempCorrect =0;
		    			//Arrays.sort(topRank);
		    			avgSmsLen += queryLength;
		    			if (s.getMatches().equals(none)){
		    				maxHitsQueries ++;
		    				//avgMaxHits += maxHits;
		    			}
		    			System.out.println(topRank[0].getId() + " " +topRank[1].getId());
		    			if (s.getMatches().contains(topRank[0].getId()) || ((s.getMatches().contains(topRank[1].getId()))&& (topRank[1].getHits() <=(float) topRank[0].getHits()) )){
		    				//if (maxHits >= (queryLength/2)) {
		    				tempCorrect =1;	
		    				correct++;
		    				if (!s.getMatches().equals(none)){
		    				//correct++;
		    				
		    				}
		    				//}
		    			}
		    			if (s.getMatches().equals(none)){
		    			if ((s.getDomain().contains(topRank[0].getDomain())) || (s.getDomain().contains(topRank[1].getDomain()))){
		    				domainCorrect ++;
		    			}}
		    			//System.out.println("");	
		    			
		    			//System.out.println(" corect = " + correct + " " + topRank[1].getId() );
		    			*/
		    			int answers =5;
		    			//System.out.println("maxHits = " + Ranks[0].getHits() + " stringLength = " + stringLength );
		    			
		    			//System.out.println();
		    			/*
		    			try {
		    				Thread.sleep(1000);
		    			}
		    			catch (Exception e) {
		    				System.out.println(e);
						}
    			      */
		    			
		    			if (((float)maxHits) < ((float)stringLength*1.15)){
		    				Ranks[0].setId(none);
		    				Ranks[0].setDomain(none);
		    				Ranks[1].setId(none);
		    				Ranks[1].setDomain(none);
		    				Ranks[2].setId(none);
		    				Ranks[2].setDomain(none);
		    				Ranks[3].setId(none);
		    				Ranks[3].setDomain(none);
		    				Ranks[4].setId(none);
		    				Ranks[4].setDomain(none);
		    				answers = 1;
		    				//maxHitsQueries ++;
		    				//avgMaxHits += maxHits;
		    				//System.out.println("changed " + topRank[0].getId() + " " + topRank[1].getId());
		    			}
		    			
		    			/*if (Ranks[0].getId().equals("")){
		    				Ranks[0].setDomain(none);
		    				Ranks[0].setId("NONE");
		    				Ranks[1].setId(topRank[0].getId());
		    				Ranks[1].setDomain(topRank[0].getDomain());
		    				Ranks[2].setId(topRank[0].getId());
		    				Ranks[2].setDomain(topRank[0].getDomain());
		    				Ranks[3].setId(topRank[0].getId());
		    				Ranks[3].setDomain(topRank[0].getDomain());
		    				Ranks[4].setId(topRank[0].getId());
		    				Ranks[4].setDomain(topRank[0].getDomain());
		    				
		    			}*/
		    			//Ranks[1].getHits() < ((float)queryLength*1.15)
		    			else if (Ranks[1].getHits() < ((float)queryLength*1.15)){
		    				Ranks[1].setId(Ranks[0].getId());
		    				Ranks[1].setDomain(Ranks[0].getDomain());
		    				Ranks[2].setId(Ranks[0].getId());
		    				Ranks[2].setDomain(Ranks[0].getDomain());
		    				Ranks[3].setId(Ranks[0].getId());
		    				Ranks[3].setDomain(Ranks[0].getDomain());
		    				Ranks[4].setId(Ranks[0].getId());
		    				Ranks[4].setDomain(Ranks[0].getDomain());
		    				answers=1;
		    			}
		    			else if (Ranks[2].getHits() < ((float)queryLength*1.15)){
		    				
		    				Ranks[2].setDomain(Ranks[0].getDomain());
		    				Ranks[3].setId(Ranks[0].getId());
		    				Ranks[3].setDomain(Ranks[0].getDomain());
		    				Ranks[4].setId(Ranks[0].getId());
		    				Ranks[4].setDomain(Ranks[0].getDomain());
		    				answers=2;
		    			}
		    			else if (Ranks[3].getHits() < ((float)queryLength*1.15)){
		    				Ranks[3].setId(Ranks[0].getId());
		    				Ranks[3].setDomain(Ranks[0].getDomain());
		    				Ranks[4].setId(Ranks[0].getId());
		    				Ranks[4].setDomain(Ranks[0].getDomain());
		    				answers =3;
		    			}
		    			else if (Ranks[4].getHits() < ((float)queryLength*1.15)){
		    				Ranks[4].setId(Ranks[0].getId());
		    				Ranks[4].setDomain(Ranks[0].getDomain());
		    				answers=4;
		    			}
		    			
		    			else {
		    				//boostByDomain(totalTopHits);
		    			}
		    			//System.out.print(" matches = "+ s.getMatches() +" "  );
		    			//int tempCorrect =0;
		    			//Arrays.sort(topRank);
		    			
		    			if (s.getMatches().equals(none)){
		    				avgSmsLen += maxHits/queryLength;
		    				maxHitsQueries += maxHits;
		    				totalNone ++;
		    				//avgMaxHits += maxHits;
		    			}
		    			else {
		    				//totalNonNone ++;
		    				//System.out.println(Ranks[0].getHits() + " " + Ranks[1].getHits()/Ranks[0].getHits());
		    			}
		    			
//may have to uncomment this		    			//System.out.println(s.getId());
		    			
		    			//System.out.println(Ranks[0].getId());
		    			if ((s.getMatches().contains(Ranks[0].getId()) && answers>=1) || (s.getMatches().contains(Ranks[1].getId()) && answers >=2) || (s.getMatches().contains(Ranks[2].getId()) && answers >=3) || (s.getMatches().contains(Ranks[3].getId()) && answers >=4) || (s.getMatches().contains(Ranks[4].getId()) && answers >=5) ){
		    				//if (maxHits >= (queryLength/2)) {
		    				correct ++;	
		    			}
		    				/*
		    				 if (answers == 1)
		    				{
		    					if (!Ranks[0].getId().contains(none)){
		    						pw.println(s.getId()+","+Ranks[0].getId()+","+Ranks[0].getHits()/maxHits);
		    					}
		    					else{
		    						pw.println(s.getId()+","+Ranks[0].getId());
		    					}
		    					counter ++;
		    				}
		    				else if (answers == 2){
		    					pw.println(s.getId()+"," + Ranks[0].getId() + "," + Ranks[0].getHits()/maxHits +"," + Ranks[1].getId() + "," + Ranks[1].getHits()/maxHits);
		    					counter++;
		    				}
		    				else if (answers == 3){
		    					pw.println(s.getId()+"," + Ranks[0].getId() + "," + Ranks[0].getHits()/maxHits +"," + Ranks[1].getId() + "," + Ranks[1].getHits()/maxHits +"," + Ranks[2].getId() + "," + Ranks[2].getHits()/maxHits);
		    					counter++;
		    				}
		    				else if (answers == 4){
		    					pw.println(s.getId()+"," + Ranks[0].getId() + "," + Ranks[0].getHits()/maxHits +"," + Ranks[1].getId() + "," + Ranks[1].getHits()/maxHits +"," + Ranks[2].getId() + "," + Ranks[2].getHits()/maxHits +"," + Ranks[3].getId() + "," + Ranks[3].getHits()/maxHits);
		    					counter++;
		    				}
		    				else if (answers == 5){
		    					pw.println(s.getId()+"," + Ranks[0].getId() + "," + Ranks[0].getHits()/maxHits +"," + Ranks[1].getId() + "," + Ranks[1].getHits()/maxHits +"," + Ranks[2].getId() + "," + Ranks[2].getHits()/maxHits +"," + Ranks[3].getId() + "," + Ranks[3].getHits()/maxHits +"," + Ranks[4].getId() + "," + Ranks[4].getHits()/maxHits);
		    					counter++;
		    				}*/
		    				
		    				if (answers == 1)
		    				{
		    					if (!Ranks[0].getId().contains(none)){
		    						pw.println(s.getId()+","+Ranks[0].getId());
		    					}
		    					else{
		    						pw.println(s.getId()+","+Ranks[0].getId());
		    					}
		    					counter ++;
		    				}
		    				else if (answers == 2){
		    					pw.println(s.getId()+"," + Ranks[0].getId() + ","  + Ranks[1].getId());
		    					counter++;
		    				}
		    				else if (answers == 3){
		    					pw.println(s.getId()+"," + Ranks[0].getId() + ","  + Ranks[1].getId() + "," + Ranks[2].getId() );
		    					counter++;
		    				}
		    				else if (answers == 4){
		    					pw.println(s.getId()+"," + Ranks[0].getId() + "," + Ranks[1].getId() + "," + Ranks[2].getId() + "," + Ranks[3].getId());
		    					counter++;
		    				}
		    				else if (answers == 5){
		    					pw.println(s.getId()+"," + Ranks[0].getId() + "," + Ranks[1].getId() + "," + Ranks[2].getId() + "," + Ranks[3].getId() + "," + Ranks[4].getId());
		    					counter++;
		    				}
		    				
		    				//correct++;
		    				if (!s.getMatches().equals(none)){
		    				//correct++;
		    				
		    				//}
		    				//}
		    			}
		    			if (s.getMatches().equals(none)){
		    			if ((s.getDomain().contains(Ranks[0].getDomain())) || (s.getDomain().contains(Ranks[1].getDomain()))){
		    				domainCorrect ++;
		    			}}
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
		    	//s.setText(smsShortened);
				//mySms.add(s);
				count2++;
			}
		}
		 pw.flush();
		 
			
	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	    finally {
	      
	      //Close the PrintWriter
	      if (pw != null)
	        pw.close();
	      
	    }
		smsList = (sms[])mySms.toArray(smsList);
		System.out.println(domainList[testRun] + " total Faq = " + totalOneDomain + " total sms = " +totalNonNone + " correct = "+ correct + " maxScoreChk = " + maxScoreChk);
		testRun++;
		}
		writeSms("msms.txt",mySms.size());
		//System.out.println(successful_attempts);
		//System.out.println("Total = " + i  +"total non none = " + totalNonNone + "correct = " + correct);
		//System.out.println("avg sms length = " + (double)avgSmsLen/i);
		//System.out.println("avg Hits = " + (double)avgSmsLen + "total = " + avgSmsLen);
		//System.out.println("domains correct  = " + domainCorrect);
		//if (counter == i)
		//	System.out.println("yes!!!");
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
			//All the faqs are being parsed here form a file.
			myFaq.clear();
			//get the root elememt
			Element docEle = dom2.getDocumentElement();
			int k = 1;
			
			
			//static rank ;
			
			//get a nodelist of <sms> elements
			NodeList nl = docEle.getElementsByTagName("FAQ");
			System.out.println(nl.getLength());
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
					if(tokens[l].toLowerCase().equals(str)){
						chkStopWords = 1;
						break;
					}
				}
				in.close();
			} catch (IOException e) {
			}
			if (chkStopWords == 0){
				tokens1[i++]=tokens[l].toLowerCase();
			}
		}
		stringLength = i;
		return tokens1;
	}

	private sms getsms(Element smsS1) throws Exception {
		//for each <sms> element get text or int values of 
		//name ,id, age and name
		String id = getTextValue(smsS1,"SMS_QUERY_ID");
		String text = getTextValue(smsS1,"SMS_TEXT");
		/*Translate.setHttpReferrer("nothing");
		String translatedText = Translate.execute(text,Language.HINDI,Language.ENGLISH);
	    text=translatedText;*/
		//String matches = getTextValue(smsS1,"HINDI");
		String matches = getTextValue(smsS1,"ENGLISH");;
		//matches = matches.concat(matches2);
		String domain = calcDomain(matches);
		int i,end;/*
		for (i =0;i<domainsListed;i++){
			if (domainList[i].equals(domain)){
				break;
			}
		}
		if (i == domainsListed){
			domainList[domainsListed]=domain;
			domainsListed++;
		}*/
		//Create a new sms with the value read from the xml nodes
		for ( i = 4 ; i < matches.length() ; i++)
		{
			if (matches.charAt(i) == '_')
				break;
		}
		end = i ;
		domain  = matches.substring(4,end);
		if (matches.equals("NONE"))
			domain = "NONE";
		sms s = new sms(id,text,matches,domain);
		
		return s;
	}
	
	private faq getfaq(Element faqF1) {
		
		//for each <sms> element get text or int values of 
		//name ,id, age and name
		String id = getTextValue(faqF1,"FAQID");
		int i  = 0 ;
		int start = 5;
		int end;
		for ( i = 4 ; i < id.length() ; i++)
		{
			if (id.charAt(i) == '_')
				break;
		}
		end = i ;
		
		
		
		String ques = getTextValue(faqF1,"QUESTION");
		String ans = getTextValue(faqF1,"ANSWER");
		String domain = getTextValue(faqF1,"DOMAIN");
		domain = id.substring(4,end);
		
		//String domain = " ";
		/*String domain = calcDomain(id);
		int i;
		*/
		for (i =0;i<domainsListed;i++){
			if (domainList[i].equals(domain)){
				break;
			}
		}
		if (i == domainsListed){
			domainList[domainsListed]=domain;
			domainsListed++;
		}
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
		
		int i,j;
		String domain;
		if (id.contains("NONE")){
			domain ="NONE";
		}
		else {
			for (i=0;id.charAt(i)!='_' && i<id.length()-1;i++);
		
		
		
			for (j=i+1;id.charAt(j)!='_';j++);
			domain =id.substring(i+1,j);
		
		}
		return domain;
	}
	
	public static void writeSms (String filename,int totalSms){
		PrintWriter pw = null;
		 int totalWrote =0;
	    try {

	     

	        pw = new PrintWriter(new FileWriter(filename));
	        //this is equal to:
	        //pw = new PrintWriter(new FileWriter(filename, false));

	      
	     
	      for (int i = 0; i < totalSms; i++) {
	    	  int j;
	    	  //if (smsList[i].getDomain().toLowerCase().contains("none")){
	    	//	  continue;
	    	 // }
	    	  for (j=0;j<domainsListed;j++){
	    		  if (domainList[j].contains(smsList[i].getDomain())){
	    			  System.out.println("found");
	    			  break;
	    		  }
	    	  }
	    	  j++;
	    	  if ( smsList[i].getMatches().contains("CAREER") || smsList[i].getMatches().contains("NONE")){
	    	  //pw.println("\""+smsList[i].getId()+"\""+","+"\"" +smsList[i].getText()+"\"");
	    	  pw.println("\""+smsList[i].getId()+"\""+","+"\"" +smsList[i].getMatches()+"\"");
	    	  //pw.println(smsList[i].getId()+","+smsList[i].getMatches());
	    	  
	    	  
	    	  //pw.println("\""+smsList[i].getId()+"\""+","+"\"" +smsList[i].getText()+"\"");
	    	  //pw.println("\""+smsList[i].getId()+"\""+","+"\"" +smsList[i].getText()+"\""+","+j);
	    	  //pw.println(smsList[i].getText());
	    	  
	    	  
	    	  totalWrote ++;
	    	  
	    	  }
	    	  //pw.println(j);
	    	 // pw.println();
	      }
	      pw.flush();

	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	    finally {
	      
	      //Close the PrintWriter
	      if (pw != null)
	        pw.close();
	      
	    }
	    System.out.println("total wrote" + totalWrote);
	    
	    try {

		     

	        pw = new PrintWriter(new FileWriter("domains1.txt"));
	        //this is equal to:
	        //pw = new PrintWriter(new FileWriter(filename, false));

	      
	      
	     
	    	  for (int j=0;j<domainsListed;j++){
	    		  
	    	  
	    	  System.out.println(j);
	    	  //pw.println("\""+faqList[i].getId()+"\""+","+"\"" +faqList[i].getQuestion()+"\""+","+j);
	    	  pw.println("'"+domainList[j]+"'"+",");
	      }
	      pw.flush();

	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	    finally {
	      
	      //Close the PrintWriter
	      if (pw != null)
	        pw.close();
	      
	    }
    }
	
	
	public static void writeFaq(String filename,int totalFaqs) {
        
        /*ObjectOutputStream outputStream = null;
        
        try {
            
            //Construct the LineNumberReader object
            outputStream = new ObjectOutputStream(new FileOutputStream(filename));
            
            int count =0;
    		//Iterator it = myFaq.iterator();
    		while(count <totalFaqs) {
    			outputStream.writeObject(faqList[count]);
    			count ++;
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
        }*/
		PrintWriter pw = null;
		
	    try {

	    	int count =0;

	        //pw = new PrintWriter(new FileWriter(filename));
	        //this is equal to:
	        pw = new PrintWriter(new FileWriter(filename, true));

	      
	        System.out.println(totalFaqs);
	      for (int i = 0; i < totalFaqs; i++) {
	    	  int j;
	    	  for (j=0;j<domainsListed;j++){
	    		  if (domainList[j].equals(faqList[i].getDomain())){
	    			  break;
	    		  }
	    	  }
	    	  j++;
	    	  //System.out.println(faqList[i].getDomain());
	    	  
	    	  if (faqList[i].getDomain().contains("CAREER") || faqList[i].getDomain().contains("SPORTS"))
	    		  
	    		  
	    	  {//pw.println("\""+faqList[i].getId()+"\""+","+"\"" +faqList[i].getQuestion()+"\""+","+j);
	    	  pw.println("\""+faqList[i].getId()+"\""+","+"\"" +faqList[i].getQuestion()+"\"");
	    	  //pw.println(faqList[i].getQuestion());
	    	  //pw.println(j);
	    	  //pw.println();
	    	  count++;
	    	  }
	    	  
	    	  //pw.println(faqList[i].getQuestion()+","+faqList[i].getDomain());
	    	  
	      }
	      pw.flush();
	      System.out.println("I = "+ count);
	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	    finally {
	      
	      //Close the PrintWriter
	      if (pw != null)
	        pw.close();
	      
	    }
	    
	    try {

		     

	        pw = new PrintWriter(new FileWriter("domains.txt"));
	        //this is equal to:
	        //pw = new PrintWriter(new FileWriter(filename, false));

	      
	      
	     
	    	  for (int j=0;j<domainsListed;j++){
	    		  
	    	  
	    	  
	    	  //pw.println("\""+faqList[i].getId()+"\""+","+"\"" +faqList[i].getQuestion()+"\""+","+j);
	    	  pw.println("'"+domainList[j]+"'"+",");
	      }
	      pw.flush();

	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	    finally {
	      
	      //Close the PrintWriter
	      if (pw != null)
	        pw.close();
	      
	    }
    }
	
	
	//reads a text file and gets  the rank of every faq
		
	public static void main(String[] args) throws Exception {
		
		
		//create an instance
		parser dpe = new parser(null);
		
		dpe.runParser();
		
		
		
	}

}
