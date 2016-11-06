package ch.rasc.sec.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * This dto is used to transfer aes encrypted
 * user login and password to server fo login process.
 *
 * @author Sergey Kiselev
 */
@Data
public class UserDto implements Serializable {

    @NotNull
    private String login;

    @NotNull
    private String password;

    @NotNull
    private String sessionId;

}
