package ch.rasc.sec.repository;

import ch.rasc.sec.model.Grants;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrantsRepository extends JpaRepository<Grants, Long> {
}
