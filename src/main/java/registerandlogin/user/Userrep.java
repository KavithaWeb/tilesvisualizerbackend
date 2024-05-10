package registerandlogin.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Userrep extends JpaRepository<Freetrial, Long> {
	 List<Freetrial> findByCreationDateBeforeAndAccountClosedFalse(LocalDateTime date);

	void deleteByAccountClosedTrue();

	Freetrial findByEmail(String email);

}
