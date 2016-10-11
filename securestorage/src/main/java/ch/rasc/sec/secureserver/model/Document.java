package ch.rasc.sec.secureserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
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

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "doc_group", joinColumns = @JoinColumn(name = "doc_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"))
    private Set<Group> groups;

    public Document(String name, String link) {
        this.name = name;
        this.link = link;
        //this.authorities = authorities;
    }


}