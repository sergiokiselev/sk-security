package ch.rasc.sec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * User: NotePad.by
 * Date: 2/25/2016.
 */
@Data
@NoArgsConstructor
public class VerifyDto implements Serializable {

    private String sessionId;

    private String code;

    private String secret;

    public String toString() {
        return sessionId + " " + code + " " + secret;
    }
}
