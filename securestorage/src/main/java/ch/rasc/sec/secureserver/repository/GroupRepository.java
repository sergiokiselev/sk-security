package ch.rasc.sec.secureserver.repository;

import ch.rasc.sec.secureserver.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
