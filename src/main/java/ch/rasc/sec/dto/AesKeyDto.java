package ch.rasc.sec.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AesKeyDto {

    @NotNull
    private String sessionId;

    @NotNull
    private String aesKey;

    @NotNull
    private String ivector;

}
