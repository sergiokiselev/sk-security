package ch.rasc.sec.secureserver.repository;

import ch.rasc.sec.secureserver.model.DocGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocGroupRepository  extends JpaRepository<DocGroup, Long> {
}
