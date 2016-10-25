package ch.rasc.sec.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.crypto.SecretKey;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * User: NotePad.by
 * Date: 10/25/2016.
 */
@Data
@AllArgsConstructor
public class SessionAttributes implements Serializable {

    public SessionAttributes(SecretKey aesKey, byte[] ivector) {
        this.aesKey = aesKey;
        this.ivector = ivector;
    }

    @NotNull
    private SecretKey aesKey;

    @NotNull
    private byte[] ivector;

    private boolean correctPassword;

    private String code;
}
