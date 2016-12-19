package ch.rasc.sec.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by 9480 on 12/14/2016.
 */
@Data
public class IVectorDto implements Serializable {
    @NotNull
    private String sessionId;

    private String ivector;
}
