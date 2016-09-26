package ch.rasc.sec.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * User: NotePad.by
 * Date: 2/21/2016.
 */
@Data
public class UserDto implements Serializable {

    @NotNull
    private String login;

    @NotNull
    private String password;

}
