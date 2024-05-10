package registerandlogin.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "enquiryusers")
public class Enquiryuser {
    private @Id @GeneratedValue long id;
    private @NotBlank String enqname;
    private @NotBlank String enqemail;
    private @NotBlank String enqsubject;
    private @NotBlank String enqmessage;
    
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getEnqname() {
		return enqname;
	}
	public void setEnqname(String enqname) {
		this.enqname = enqname;
	}
	public String getEnqemail() {
		return enqemail;
	}
	public void setEnqemail(String enqemail) {
		this.enqemail = enqemail;
	}
	public String getEnqsubject() {
		return enqsubject;
	}
	public void setEnqsubject(String enqsubject) {
		this.enqsubject = enqsubject;
	}
	public String getEnqmessage() {
		return enqmessage;
	}
	public void setEnqmessage(String enqmessage) {
		this.enqmessage = enqmessage;
	}



}