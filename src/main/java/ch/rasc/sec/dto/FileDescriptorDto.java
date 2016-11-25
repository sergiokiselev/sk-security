package ch.rasc.sec.dto;

import ch.rasc.sec.model.User;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;

/**
 * User: NotePad.by
 * Date: 11/24/2016.
 */
@Data
public class FileDescriptorDto implements Serializable {

    private String name;

    private String link;

    private String googleId;

    private int size;

    private Date created;

    private Date lastModified;

    private long ownerId;

}
