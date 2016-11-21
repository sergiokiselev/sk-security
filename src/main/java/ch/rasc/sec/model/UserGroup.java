package ch.rasc.sec.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "user_groups")
@Getter
@Setter
@NoArgsConstructor
public class UserGroup extends AbstractPersistable<Long> {

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "userGroup")
    private Set<Grants> grants;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_group", joinColumns = @JoinColumn(name = "user_group_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<User> users;

    public UserGroup(String name) {
        this.name = name;
    }
}
