package ch.rasc.sec.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * This dto is used to transfer client totp token to server.
 *
 * @author Sergey Kiselev
 */
@Data
public class TokenDto implements Serializable {

    private String sessionId;

    private long token;

}
