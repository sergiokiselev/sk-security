package ch.rasc.sec.secureserver.repository;

import ch.rasc.sec.secureserver.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocRepository extends JpaRepository<Document, Long> {
}
