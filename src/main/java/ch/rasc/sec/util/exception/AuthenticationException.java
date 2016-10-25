package ch.rasc.sec.util.exception;

/**
 * Author: s.kiselyov
 * Date: 25.10.2016
 */
public class AuthenticationException extends Exception {

    /**
     * Exception constructor  with message (no stack trace).
     *
     * @param reason exception's detailed message
     */
    public AuthenticationException(String reason) {
        super(reason);
    }

}
