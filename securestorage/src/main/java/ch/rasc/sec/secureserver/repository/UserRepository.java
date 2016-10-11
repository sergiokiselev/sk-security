package ch.rasc.sec.secureserver.repository;
import ch.rasc.sec.secureserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
