package ch.rasc.sec.secureserver.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name = "docGroup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
