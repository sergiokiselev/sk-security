package ch.rasc.sec.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * User: NotePad.by
 * Date: 2/25/2016.
 */
@Data
public class VerifyDto implements Serializable {

    private String sessionId;

    private String code;

    private String exception;

}
