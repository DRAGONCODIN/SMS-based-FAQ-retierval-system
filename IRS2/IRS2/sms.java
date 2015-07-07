

public class sms {

	private String id;

	private String text;
	
	private String matches;
	
	private String domain;
	
	public sms(){
		
	}
	
	public sms(String id, String text,String matches,String domain) {
		this.id = id;
		this.text = text;
		this.matches = matches;
		this.domain = domain;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String name) {
		this.text = name;
	}

	public String getMatches() {
		return matches;
	}
	
	public void setMatches(String matches) {
		this.matches=matches;
	}

		
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("sms Details - ");
		sb.append("Id:" + getId());
		sb.append(", ");
		sb.append("Text:" + getText());
		sb.append(", ");
		sb.append("Matches:" + getMatches());
		sb.append(", ");
		return sb.toString();
	}
}
