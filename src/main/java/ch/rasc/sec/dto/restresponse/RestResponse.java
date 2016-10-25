package ch.rasc.sec.dto.restresponse;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Common rest response class for all rest services.
 * @param <T> type of data inside the response
 *
 * @author Sergey Kiselev
 */
@Data
@NoArgsConstructor
public class RestResponse<T> implements Serializable {

    private T data;
    private ErrorDto errorDto;

    public RestResponse(T data) {
        this.data = data;
        this.errorDto = new ErrorDto();
    }

    public RestResponse(T data, ErrorDto errorDto) {
        this.data = data;
        this.errorDto = errorDto;
    }

    public RestResponse(ErrorDto errorDto) {
        this.errorDto = errorDto;
    }
}
