package ch.rasc.sec.secureserver.repository;

        import ch.rasc.sec.secureserver.model.Document;

        import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User: NotePad.by
 * Date: 2/21/2016.
 */
public interface DocRepository extends JpaRepository<Document, Long> {



}
