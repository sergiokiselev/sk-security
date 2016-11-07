package ch.rasc.sec.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * User: NotePad.by
 * Date: 11/7/2016.
 */
@Data
public class RsaKeyDto implements Serializable {
    private String rsaKey;

    private boolean encryption;

    private boolean postCode;
}
