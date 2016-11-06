package ch.rasc.sec.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * This dto is used to transfer aes encrypted totp secret to client.
 *
 * @author Sergey Kiselev
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotpSecretDto implements Serializable {

    private String secret;

    private String error;

    private String sessionId;
}
