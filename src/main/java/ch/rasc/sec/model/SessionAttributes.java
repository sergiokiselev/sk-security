package ch.rasc.sec.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.SecretKey;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * User: NotePad.by
 * Date: 10/25/2016.
 */
@Data
@NoArgsConstructor
public class SessionAttributes implements Serializable {

    public SessionAttributes(String sessionId) {
        this.sessionId = sessionId;
    }

    @NotNull
    private SecretKey aesKey;

    @NotNull
    private byte[] ivector;

    private boolean correctPassword;

    private String code;

    private byte[] totpSecret;

    private boolean encryption;

    private boolean postCode;

    private String sessionId;

    private long userId;
}
