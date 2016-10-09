package ch.rasc.sec.repository;

import ch.rasc.sec.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User: NotePad.by
 * Date: 2/21/2016.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByAuthSessionId(String sessionId);

}
