package ch.rasc.sec.repository;

import ch.rasc.sec.model.FileDescriptor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDescriptorRepository extends JpaRepository<FileDescriptor, Long> {
}
