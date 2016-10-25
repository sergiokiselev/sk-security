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

    public VerifyDto(String exception) {
        this.exception = exception;
    }

    private String sessionId;

    private String code;

    private String exception;

}
