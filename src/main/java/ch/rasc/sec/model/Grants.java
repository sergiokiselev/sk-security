package ch.rasc.sec.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name = "grants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Grants extends AbstractPersistable<Long> {

    public enum AccessLevel {READWRITE, READONLY}

    @Column(nullable = false)
    private AccessLevel accessLevel;

    @ManyToOne
    @JoinColumn(name = "user_group_id")
    private UserGroup userGroup;

    @ManyToOne
    @JoinColumn(name = "file_descriptor_id")
    private FileDescriptor fileDescriptor;
}
