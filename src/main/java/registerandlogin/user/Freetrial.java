package registerandlogin.user;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "freetrial")
public class Freetrial {
    private @Id @GeneratedValue long id;
    private @NotBlank String username;
    private @NotBlank String email;
    private @NotBlank String password;
    
    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "account_closed")
    private boolean accountClosed;
    
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
  
	public LocalDateTime getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}
	public boolean isAccountClosed() {
		return accountClosed;
	}
	public void setAccountClosed(boolean accountClosed) {
		this.accountClosed = accountClosed;
	}

	
	
}

