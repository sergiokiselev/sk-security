package ch.rasc.sec.secureserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "docs")
@Getter
@Setter
@NoArgsConstructor
public class Document extends AbstractPersistable<Long> {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private String name;

    @Column
    private String link;

    @Column
    private Integer size;

    @Column
    private  Date created;

    @Column
    private Date lastModified;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User ownerId;

    @OneToMany(mappedBy = "doc")
    private Set<DocGroup> docGroups;

    public Document(String name, String link) {
        this.name = name;
        this.link = link;
    }
}