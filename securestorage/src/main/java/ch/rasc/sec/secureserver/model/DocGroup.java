package ch.rasc.sec.secureserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "docGroup")
@Getter
@Setter
@NoArgsConstructor
public class DocGroup extends AbstractPersistable<Long> {

    public enum AccessLevel {READWRITE,READONLY}

    @Column(nullable = false)
    private AccessLevel accessLevel;

    @ManyToOne
    @JoinColumn(name = "group_id")

    private Group group;


    @ManyToOne
    @JoinColumn(name = "doc_id")
    private Document doc;

    public DocGroup(AccessLevel aLevel, Group group, Document doc) {
        this.doc = doc;
        this.group = group;
        this.accessLevel = aLevel;


    }

}
