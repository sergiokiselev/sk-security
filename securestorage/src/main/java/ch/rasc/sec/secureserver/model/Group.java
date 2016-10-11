package ch.rasc.sec.secureserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
public class Group extends AbstractPersistable<Long> {

    @Column(nullable = false)
    private String name;



    @OneToMany(mappedBy = "group")
    private Set<DocGroup> docGroups;

    public Group(String name) {
        this.name = name;


    }
}
