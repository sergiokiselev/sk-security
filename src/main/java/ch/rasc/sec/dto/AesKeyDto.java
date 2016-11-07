package ch.rasc.sec.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * This dto is used to transfer rsa encrypted
 * aes key data such as key and init vector
 *
 * @author Sergey Kiselev
 */
@Data
public class AesKeyDto implements Serializable {

    @NotNull
    private String sessionId;

    private String aesKey;

    private String ivector;

}
