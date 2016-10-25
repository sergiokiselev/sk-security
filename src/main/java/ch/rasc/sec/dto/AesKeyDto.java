package ch.rasc.sec.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class AesKeyDto implements Serializable {

    @NotNull
    private String sessionId;

    @NotNull
    private String aesKey;

    @NotNull
    private String ivector;

}
