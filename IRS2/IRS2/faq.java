
import java.io.*;

public class faq implements Serializable, Comparable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String question;
	
	private String answer;
	
	private String domain;
	
	public faq(){
		
	}
	
	public faq(String id, String question,String answer,String domain) {
		this.id = id;
		this.question = question;
		this.answer=answer;
		this.domain=domain;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String name) {
		this.question = name;
	}
	
	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String name) {
		this.answer = name;
	}
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}


		
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getId());
		sb.append(",");
		sb.append(getQuestion());
		sb.append(",");
		sb.append(getDomain());
		sb.append("\n\n");
				
		return sb.toString();
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
