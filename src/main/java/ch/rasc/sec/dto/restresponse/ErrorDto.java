package ch.rasc.sec.dto.restresponse;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Error dto for rest response. Contains error code and error message.
 *
 * @author Sergey Kiselev
 */
@Data
@NoArgsConstructor
public class ErrorDto implements Serializable {
    private String code;
    private String message;

    private static final String DEFAULT_CODE = "DEFAULT";

    public ErrorDto(String message) {
        this.message = message;
        this.code = DEFAULT_CODE;
    }
}
