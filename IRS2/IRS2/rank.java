
public class rank implements Comparable{
	private float hits;
	private int edited ;
	//public int edits[];
	private String id;
	
	private String Question;
	
	private String domain;
	
	public rank (){
		hits=0;
		id = "";
		
	}
	public rank(String id, String Question,String Domain,float hits){
		this.id=id;
		this.Question=Question;
		this.domain=Domain;
		this.hits=hits;
		
		edited =  0;
	}
	
	public int compareTo(Object obj1)
	{
	rank tmp = (rank)obj1;
	if(this.hits < tmp.hits)
	{
	/* instance lt received */
	return 1; // this is if u want to sort in dec order, otherwise change to -1
	}
	else if(this.hits > tmp.hits)
	{
	/* instance gt received */
	return -1; // this is if u want to sort in dec order, otherwise change to 1
	}
	/* instance == received */
	return 0;
	}
	
	public String getId(){
		return id;
		
	}
	
	public float getHits(){
		return hits;
	}
	
	public String getQuestion(){
		
		return Question;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setHits(float hits){
		this.hits = hits;
		
			edited =0;
	}
	
	public void setQuestion(String Question){
		this.Question = Question;
	}
	
	public void incHits(float increment){
		hits = hits + increment;
		//edits[edited] = wordNo;
		edited = edited+1;
		
	}
	public int getEdited()
	{
		return edited;
	}
	

}
