package ch.rasc.sec.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "file_descriptors")
@Getter
@Setter
@NoArgsConstructor
public class FileDescriptor extends AbstractPersistable<Long> {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private String name;

    @Column
    private String link;

    @Column
    private Integer size;

    @Column
    private Date created;

    @Column
    private Date lastModified;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "fileDescriptor")
    private Set<Grants> grants;

    public FileDescriptor(String name, String link) {
        this.name = name;
        this.link = link;
    }
}