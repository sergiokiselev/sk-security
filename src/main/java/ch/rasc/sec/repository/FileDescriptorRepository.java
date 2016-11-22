package ch.rasc.sec.repository;

import ch.rasc.sec.model.FileDescriptor;
import ch.rasc.sec.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileDescriptorRepository extends JpaRepository<FileDescriptor, Long> {

    List<FileDescriptor> getByOwnerId(User owner);

    FileDescriptor getByGoogleId(String googleId);

}
