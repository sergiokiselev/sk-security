package ch.rasc.sec.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * This dto is used to transfer client totp token to server.
 *
 * @author Sergey Kiselev
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto implements Serializable {

    private String sessionId;

    private long token;

}
